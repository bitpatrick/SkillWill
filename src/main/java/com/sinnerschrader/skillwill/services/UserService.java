package com.sinnerschrader.skillwill.services;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.user.Role;
import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
import com.sinnerschrader.skillwill.domain.user.UserSimilarityUtils;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.IllegalLevelConfigurationException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.exceptions.UserNotFoundException;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.repositories.UserRepository;

/**
 * Service handling user management
 *
 * @author torree
 */
@Service
@EnableRetry
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  
  @Value("${maxLevelValue}")
  private int maxLevelValue;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LdapService ldapService;

  @Autowired
  private SkillService skillService;

  @Autowired
  private SkillRepository skillRepository;

  @Autowired
  private FitnessScoreProperties fitnessScoreProperties;

  public List<UserDetailsImpl> getUsers(SkillSearchResult skillSearch, String company, String location)
      throws IllegalArgumentException {

    List<UserDetailsImpl> candidates;

    if (skillSearch.isInputEmpty()) {
      candidates = userRepository.findAll();
    } else {
      var skillNames = skillSearch.mappedSkills().stream().map(Skill::getName).collect(Collectors.toList());
      candidates = userRepository.findBySkills(skillNames).stream()
          .peek(p -> p.setFitnessScore(skillSearch.mappedSkills(), fitnessScoreProperties))
          .sorted(Comparator.comparingDouble(UserDetailsImpl::getFitnessScoreValue).reversed())
          .collect(Collectors.toList());
    }

    // sync needed to search for location and company
    if (!StringUtils.isEmpty(location) || !StringUtils.isEmpty(company)) {
      candidates = ldapService.syncUsers(candidates, false);
      candidates = filterByCompany(candidates, company);
      candidates = filterByLocation(candidates, location);
    }

    logger.debug("Successfully found {} users for search [{}]", candidates.size(),
        skillSearch.mappedSkills().stream().map(Skill::getName).collect(Collectors.joining(", ")));

    return candidates;
  }

  private List<UserDetailsImpl> filterByLocation(List<UserDetailsImpl> unfiltered, String location) {
    if (StringUtils.isEmpty(location)) {
      return unfiltered;
    }
    return unfiltered.stream()
        .filter(user -> user.getLdapDetails().getLocation().equals(location))
        .collect(Collectors.toList());
  }

  private List<UserDetailsImpl> filterByCompany(List<UserDetailsImpl> unfiltered, String company) {
    if (StringUtils.isEmpty(company)) {
      return unfiltered;
    }
    return unfiltered.stream()
      .filter(user -> user.getLdapDetails().getCompany().equals(company))
      .collect(Collectors.toList());
  }

  public UserDetailsImpl getUser(String username) {
    var user = userRepository.findByUsernameIgnoreCase(username).get();

    if (user == null) {
      logger.debug("Failed to find user {}: not found", username);
      throw new UserNotFoundException("user not found");
    }

    if (user.getLdapDetails() == null) {
      ldapService.syncUser(user);
    }

    logger.debug("Successfully found user {}", username);
    return user;
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void updateSkills(String username, String skillName, int skillLevel, int willLevel, boolean mentor)
      throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
      logger.debug("Failed to modify skills: username or skillName empty");
      throw new EmptyArgumentException("arguments must not be empty or null");
    }

    var user = userRepository.findByUsernameIgnoreCase(username);
    if (user == null) {
      logger.debug("Failed to add/modify {}'s skills: user not found", username);
      throw new UserNotFoundException("user not found");
    }

//    var skill = skillRepository.findByName(skillName);
//    if (skill == null || skill.isHidden()) {
//      logger.debug("Failed to add/modify {}'s skill {}: skill not found or hidden", username, skillName);
//      throw new SkillNotFoundException("skill not found/hidden");
//    }
    
    if (!isValidLevelConfiguration(skillLevel, willLevel)) {
      logger.debug("Failed to add/modify {}'s skill {}: illegal levels {}/{}", username, skillName,
          skillLevel, willLevel);
      throw new IllegalLevelConfigurationException("Invalid Skill-/WillLevel Configuration");
    }

    user.get().addUpdateSkill(skillName, skillLevel, willLevel, skillService.isHidden(skillName), mentor);
    userRepository.save(user.get());

    logger.info("Successfully updated {}'s skill {}", username, skillName);
  }

  @Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
  public void removeSkills(String username, String skillName)
      throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
      logger.debug("Failed to modify skills: username or skillName empty");
      throw new EmptyArgumentException("arguments must not be empty or null");
    }

    var user = userRepository.findByUsernameIgnoreCase(username);
    if (user == null) {
      logger.debug("Failed to remove {}'s skills: user not found", username);
      throw new UserNotFoundException("user not found");
    }

    if (skillRepository.findByNameIgnoreCase(skillName) == null) {
      logger.debug("Failed to remove {}'s skill {}: skill not found", username, skillName);
      throw new SkillNotFoundException("skill not found");
    }

    user.get().removeSkill(skillName);
    userRepository.save(user.get());
  }

  private boolean isValidLevelConfiguration(int skillLevel, int willLevel) {
    // Both levels must be between 0 and maxLevel
    // at least one level must be 1 or above (see [SKILLWILL-30])
    final boolean isValidSkillLevel = 0 <= skillLevel && skillLevel <= maxLevelValue;
    final boolean isValidWillLevel = 0 <= willLevel && willLevel <= maxLevelValue;
    final boolean isOneGreaterZero = skillLevel > 0 || willLevel > 0;
    return isValidSkillLevel && isValidWillLevel && isOneGreaterZero;
  }

  public List<UserDetailsImpl> getSimilar(String username, Integer count) throws UserNotFoundException {
    var toSearch = userRepository.findAll();
    var user = toSearch.stream().filter(p -> p.getUsername().equals(username)).findAny();

    if (!user.isPresent()) {
      logger.debug("Failed to get users similar to {}: user not found", username);
      throw new UserNotFoundException("user not found");
    }

    return ldapService.syncUsers(
      UserSimilarityUtils.findSimilar(user.get(), toSearch, count),
      false
    );
  }

  public Role getRole(String userId) {
    var user = userRepository.findByUsernameIgnoreCase(userId);
    if (user == null) {
      throw new UserNotFoundException("user not found");
    }

    return user.get().getLdapDetails().getRole();
  }

}
