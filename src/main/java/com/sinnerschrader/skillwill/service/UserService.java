package com.sinnerschrader.skillwill.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.FitnessScore;
import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.user.Role;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.domain.user.UserLdapDetails;
import com.sinnerschrader.skillwill.domain.user.UserSimilarityUtils;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserLdapDetailsDto;
import com.sinnerschrader.skillwill.exception.EmptyArgumentException;
import com.sinnerschrader.skillwill.exception.IllegalLevelConfigurationException;
import com.sinnerschrader.skillwill.exception.SkillNotFoundException;
import com.sinnerschrader.skillwill.exception.UserAlreadyExistException;
import com.sinnerschrader.skillwill.exception.UserIdException;
import com.sinnerschrader.skillwill.exception.UserNotFoundException;
import com.sinnerschrader.skillwill.repository.SkillRepository;
import com.sinnerschrader.skillwill.repository.UserRepository;

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

	/**
	 * repository mongodb, solo per username, skills and subskills
	 */
	@Autowired
	private UserRepository userRepository;

	/**
	 * service parlante con LDap server che contiene i dati sensibili egli users
	 */
	@Autowired
	private LdapService ldapService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private FitnessScoreProperties fitnessScoreProperties;

	public List<User> getUsers(SkillSearchResult skillSearch, String company, String location) throws IllegalArgumentException {

		List<User> candidates;

		if (skillSearch.isInputEmpty()) {
			candidates = userRepository.findAll();
		} else {
			List<String> skillNames = skillSearch.mappedSkills().stream().map(Skill::getName).collect(Collectors.toList());
			candidates = userRepository.findBySkills(skillNames).stream()
					.peek(p -> p.setFitnessScore(skillSearch.mappedSkills(), fitnessScoreProperties))
					.sorted(Comparator.comparingDouble(User::getFitnessScoreValue).reversed())
					.collect(Collectors.toList());
		}

		// sync needed to search for location and company

		/*
		 * che fossero o luna o l'altra valorizzate dobbiamo evitare i nullPointer
		 */
		if (location != null && !location.isBlank() || company != null && !company.isBlank()) {
			candidates = ldapService.syncUsers(candidates, false);
			candidates = filterByCompany(candidates, company);
			candidates = filterByLocation(candidates, location);
		}

		logger.debug("Successfully found {} users for search [{}]", candidates.size(),
				skillSearch.mappedSkills().stream().map(Skill::getName).collect(Collectors.joining(", ")));

		return candidates;
	}

	private List<User> filterByLocation(List<User> unfiltered, String location) {
		if (StringUtils.isEmpty(location)) {
			return unfiltered;
		}
		return unfiltered.stream().filter(user -> user.getLdapDetails().getLocation().equals(location))
				.collect(Collectors.toList());
	}

	private List<User> filterByCompany(List<User> unfiltered, String company) {
		if (StringUtils.isEmpty(company)) {
			return unfiltered;
		}
		return unfiltered.stream().filter(user -> user.getLdapDetails().getCompany().equals(company))
				.collect(Collectors.toList());
	}

	public UserDto getUserDetails(String username) throws UserNotFoundException {

		User userFromRepo = userRepository.findById(username).orElseThrow(() -> {
			throw new UserNotFoundException("user not found");
		});

//		if (userFromRepo.getLdapDetails() == null) {
//			ldapService.syncUser(userFromRepo);
//		}

		UserDto userDto = userFromRepo.toUserDto();

		logger.debug("Successfully found user {}", username);
		return userDto;
	}

	public void updateSkills(String username, String skillName, int skillLevel, int willLevel, boolean mentor) throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
			logger.debug("Failed to modify skills: username or skillName empty");
			throw new EmptyArgumentException("arguments must not be empty or null");
		}

		var user = userRepository.findById(username);
		if (user == null) {
			logger.debug("Failed to add/modify {}'s skills: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		Optional<Skill> skill = skillRepository.findByNameIgnoreCase(skillName);
		if (skill == null || skill.isEmpty() || skill.get().isHidden() ) {
			logger.debug("Failed to add/modify {}'s skill {}: skill not found or hidden", username, skillName);
			throw new SkillNotFoundException("skill not found/hidden");
		}

		if (!isValidLevelConfiguration(skillLevel, willLevel)) {
			logger.debug("Failed to add/modify {}'s skill {}: illegal levels {}/{}", username, skillName, skillLevel,
					willLevel);
			throw new IllegalLevelConfigurationException("Invalid Skill-/WillLevel Configuration");
		}

		user.get().updateSkill(skillName, skillLevel, willLevel, skillService.isHidden(skillName), mentor);
		userRepository.save(user.get());

		logger.info("Successfully updated {}'s skill {}", username, skillName);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void removeSkill(String username, String skillName) throws UserNotFoundException, SkillNotFoundException, EmptyArgumentException {

		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(skillName)) {
			logger.debug("Failed to modify skills: username or skillName empty");
			throw new EmptyArgumentException("arguments must not be empty or null");
		}

		var user = userRepository.findById(username);
		if (user == null) {
			logger.debug("Failed to remove {}'s skills: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		if (skillRepository.findByNameIgnoreCase(skillName) == null) {
			logger.debug("Failed to remove {}'s skill {}: skill not found", username, skillName);
			throw new SkillNotFoundException("skill not found");
		}
		Skill skillToRemove = skillRepository.findByNameStem(skillName).orElseThrow(() -> new SkillNotFoundException("Skill not found: " + skillName));
		user.get().removeSkill(skillToRemove);
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

	public List<User> getSimilar(String username, Integer count) throws UserNotFoundException {
		var toSearch = userRepository.findAll();
		var user = toSearch.stream().filter(p -> p.getUsername().equals(username)).findAny();

		if (!user.isPresent()) {
			logger.debug("Failed to get users similar to {}: user not found", username);
			throw new UserNotFoundException("user not found");
		}

		return ldapService.syncUsers(UserSimilarityUtils.findSimilar(user.get(), toSearch, count), false);
	}

	public Role getRole(String username) {
		var user = userRepository.findById(username);
		if (user == null) {
			throw new UserNotFoundException("user not found");
		}

		return user.get().getLdapDetails().getRole();
	}

	public void updateUserDetails(UserDto userDto) throws UserNotFoundException, UserIdException {

    Objects.requireNonNull(userDto);
    String username = userDto.username();
    Objects.requireNonNull(username);

		// retrieve user from repository
		User userFromRepo = this.userRepository.findById(username).orElseThrow(() -> {
			throw new UserNotFoundException(username);
		});

    userFromRepo.update(userDto);

		this.userRepository.save(userFromRepo);

	}

	public void create(UserDto userDto) {

    Objects.requireNonNull(userDto);
    Objects.requireNonNull(userDto.username());
    Objects.requireNonNull(userDto.password());

		String username = userDto.username();

		Optional<User> userFromRepo = this.userRepository.findById(username);

    userFromRepo.ifPresent(user -> {
      throw new UserAlreadyExistException(username);
    });

		User newUser = User.createUser(userDto);
		this.userRepository.save(newUser);
	}

	public void updateUserLdapDetails(String username, UserLdapDetailsDto userLdap) {

		UserLdapDetails ldapDetails = UserLdapDetails.fromDto(userLdap);

		// recuperare userDetailsImpl relativo a questo ldapDetails

		Optional<User> userDetailsImplOpt = userRepository.findById(username);

		User userDetailsImpl = userDetailsImplOpt.orElseThrow(() -> new UserNotFoundException(username));
		userDetailsImpl.setLdapDetails(ldapDetails);

		userRepository.save(userDetailsImpl);

	}
	
	public void updateFitnessScore(String username, FitnessScoreDto fitnessScore) {
		
		Objects.requireNonNull(username);
		
		FitnessScore fitScore=FitnessScore.fromDto(fitnessScore);
		
		Optional<User> userDetailsImplOpt = userRepository.findById(username);

		User userDetailsImpl = userDetailsImplOpt.orElseThrow(() -> new UserNotFoundException(username));
		userDetailsImpl.setFitnessScore(fitScore);

		userRepository.save(userDetailsImpl);
		
	}

	public void deleteUser(String username) {

    Objects.requireNonNull(username); // strettamente accoppiato ovvero fe ( react ) è strettamente legato al controller
    // questa app non è una vera restful api

		userRepository.deleteById(username);
	}

	public UserDto getUser(String username) {
		
		User userFromRepo = userRepository.findById(username).orElseThrow(() -> {
			throw new UserNotFoundException("user not found");
		});

//		if (userFromRepo.getLdapDetails() == null) {
//			ldapService.syncUser(userFromRepo);
//		}

		UserDto userDto = userFromRepo.toUserDto();
		
		return userDto;
	}

	

}
