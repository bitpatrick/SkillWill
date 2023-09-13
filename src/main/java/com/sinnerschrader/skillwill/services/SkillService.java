package com.sinnerschrader.skillwill.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.SkillAutocompleteComparator;
import com.sinnerschrader.skillwill.domain.skills.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.skills.SkillUtils;
import com.sinnerschrader.skillwill.domain.skills.SuggestionSkill;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.repositories.SkillRepository;
import com.sinnerschrader.skillwill.repositories.UserRepository;

/**
 * Services handling skills management (create, rename, suggest, delete, ...)
 */
@Service
public class SkillService {

	private static final Logger logger = LoggerFactory.getLogger(SkillService.class);

	@Autowired
	private SkillRepository skillRepository;

	@Autowired
	private UserRepository UserRepository;

	private List<Skill> getAllSkills(boolean excludeHidden) {
		
		return excludeHidden ? skillRepository.findAllExcludeHidden() : skillRepository.findAll();
	}

	public List<Skill> findSkill(String search, boolean excludeHidden, int count) {
	    
		
		List<Skill> found = new ArrayList<Skill>(count);

	    // empty search => find all
	    if (search == null || search.isBlank()) {
	    	
	      found = getAllSkills(excludeHidden);
	      
	    } else {
	    	
	      found = skillRepository.findByNameStemLike(SkillUtils.toStem(search))
	    		  .stream()
	    		  .filter(skill -> !excludeHidden || !skill.isHidden())
	    		  .sorted(new SkillAutocompleteComparator(search)).collect(Collectors.toList());
	    }

	    // if count is set, limit number of results...
	    if (count > 0) {
	      
	    	found = found.stream()
	    		  .limit(count)
	    		  .collect(Collectors.toList());
	    }

	    return found;
	  }

	public Skill getSkillByName(String name) throws SkillNotFoundException {

		return skillRepository
				.findByNameIgnoreCase(name)
				.orElseThrow(() -> new SkillNotFoundException("Skill not found:" + name));
	}

	public SkillSearchResult searchSkillsByNames(List<String> names, boolean excludeHidden)
			throws SkillNotFoundException {

		HashMap<String, Skill> mapped = new HashMap<String, Skill>();
		HashSet<String> unmapped = new HashSet<String>();

		for (String name : names) {

			Skill found = skillRepository.findByNameStem(SkillUtils.toStem(name));

			if (found != null && (!excludeHidden || !found.isHidden())) {
				mapped.put(name, found);
			} else {
				unmapped.add(name);
			}
		}

		return new SkillSearchResult(mapped, unmapped);
	}

	private List<SuggestionSkill> aggregateSuggestions(Collection<Skill> skills) {

		List<SuggestionSkill> unaggregated = skills.stream()
				.flatMap(s -> s.getSuggestions().stream())
				.collect(Collectors.toList());

		List<SuggestionSkill> aggregated = new ArrayList<>();

		// business logic
		for (SuggestionSkill suggestionSkill : unaggregated) {

			Optional<SuggestionSkill> suggestionSkillOpt = aggregated.stream()
					.filter(a -> a.getName().equals(suggestionSkill.getName())).findAny();

			if (suggestionSkillOpt.isPresent()) {

				suggestionSkillOpt.get().incrementCount(suggestionSkill.getCount());

			} else {

				aggregated.add(suggestionSkill);
			}
		}
		return aggregated;
	}
	
	/**
	 * Metodo che verifica quante volte la suggestion skill è presente nella lista passata come parametro 
	 * 
	 * @param unaggregated
	 * @return List<SuggestionSkill>
	 */
	private List<SuggestionSkill> aggregateSuggestionsPlus(List<SuggestionSkill> unaggregated) {

		List<SuggestionSkill> aggregated = new ArrayList<>();

		// cicla sulle suggestions non aggregate
		for (SuggestionSkill suggestionSkill : unaggregated) {

			// verifica se nelle suggestions aggregate è presente la suggestion non aggregata corrente
			Optional<SuggestionSkill> suggestionSkillOpt = aggregated.stream()
					.filter(a -> a.getName().equals(suggestionSkill.getName()))
					.findAny();

			// verifico la presenza della suggetion skill non aggregata all'interno della lista
			if (suggestionSkillOpt.isPresent()) {

				// incremento il count della suggestion aggregata
				suggestionSkillOpt.get().incrementCount(suggestionSkill.getCount());

			} else {
				
				// aggiungo la suggestion skill corrente perché non presente nella lista delle suggestion aggregate
				aggregated.add(suggestionSkill);
			}
		}
		
		return aggregated;
	}

	public List<Skill> getSuggestionSkills(List<String> references, int count) throws SkillNotFoundException {

		// check not null conditions
		Objects.requireNonNull(references, "references can not be null");
		
		// check if count is bigger than zero
		Optional.of(count).filter(c -> c > 0).orElseThrow(() -> new IllegalArgumentException("count must be a positive integer"));

		List<SuggestionSkill> suggestions;
		
		if (references.isEmpty()) {
			
//			List<SuggestionSkill> suggestionsFromRepo = skillRepository.findAllSuggestions();
			
//			List<Skill> skills = skillRepository.findAllByHidden(false);
//			suggestions = aggregateSuggestions(skills);
			
//			suggestions = aggregateSuggestionsPlus(suggestionsFromRepo);
			
			suggestions = skillRepository.findAllAggregatedSuggestionSkills();
			
		} else {

			// retrive skills by names
			Set<Skill> sanitizedReferenceskills = searchSkillsByNames(references, true).mappedSkills();

			// get list of skill'strings
			List<String> sanitizedReferenceNames = sanitizedReferenceskills.stream().map(Skill::getName)
					.collect(Collectors.toList());

			suggestions = aggregateSuggestions(sanitizedReferenceskills).stream()
					.filter(s -> !sanitizedReferenceNames.contains(s.getName())).collect(Collectors.toList());
		}

		return suggestions.stream().sorted(Comparator.comparingInt(SuggestionSkill::getCount).reversed()).limit(count)
				.flatMap(s -> skillRepository.findBySuggestion(s.getName()).stream()).filter(s -> !s.isHidden())
				.collect(Collectors.toList());
	}

	public void registerSkillSearch(Collection<Skill> searchedSkills) throws IllegalArgumentException {

		if (searchedSkills.size() < 2) {
			logger.debug("Searched for less than two skills, cannot update mutual suggestions");
			return;
		}

		for (Skill skill : searchedSkills) {
			var others = searchedSkills.stream().filter(x -> !x.equals(skill)).collect(Collectors.toList());
			for (Skill updateable : others) {
				skill.incrementSuggestion(updateable.getName());
			}
		}

		try {
			skillRepository.saveAll(searchedSkills);
		} catch (OptimisticLockingFailureException e) {
			logger.error("Failed to register search for {} - optimistic locking error; will ignore search",
					searchedSkills);
		}

		logger.info("Successfully registered search for {}", searchedSkills);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void createSkill(String name, String description, boolean isHidden, Set<String> subSkills)
			throws EmptyArgumentException, DuplicateSkillException {

		name = SkillUtils.sanitizeName(name);
		subSkills = subSkills.stream().map(SkillUtils::sanitizeName).filter(n -> !StringUtils.isEmpty(n))
				.collect(Collectors.toSet());

		if (StringUtils.isEmpty(name)) {

			throw new EmptyArgumentException("name is empty");
		}

		if (skillRepository.findByNameIgnoreCase(name) != null) {

			logger.debug("Failed to create skill {}: already exists", name);
			throw new DuplicateSkillException("skill already existing");
		}

		// check if subSkills are known
		if (!isValidSubSkills(subSkills)) {
			logger.debug("Failed to set subskills on skill {}: subskill not found", name);
			throw new SkillNotFoundException("cannot set subskill: not found");
		}

		try {

			skillRepository.insert(new Skill(name, description, new ArrayList<>(), isHidden, subSkills));
			logger.info("Successfully created skill {}", name);

		} catch (DuplicateKeyException e) {

			logger.debug("Failed to create skill {}: already exists");
			throw new DuplicateSkillException("skill already existing");
		}

	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void updateSkill(String name, String newName, String description, Boolean hidden, Set<String> subSkills)
			throws IllegalArgumentException, DuplicateSkillException {

		String nameSanitized = SkillUtils.sanitizeName(name);
		String newNameSanitized = SkillUtils.sanitizeName(newName);
		subSkills = subSkills.stream().map(SkillUtils::sanitizeName).filter(n -> !StringUtils.isEmpty(n))
				.collect(Collectors.toSet());
		Skill oldSkill;
		Skill newSkill;

		if (StringUtils.isEmpty(nameSanitized)) {
			throw new SkillNotFoundException("skill not found");
		}

		oldSkill = skillRepository.findByNameIgnoreCase(nameSanitized).orElseThrow(() -> {
			logger.info("Failed to update {}: skill not found", nameSanitized);
			throw new SkillNotFoundException("skill not found");
		});

		if (skillRepository.findByNameIgnoreCase(newNameSanitized).isPresent()) {
			logger.info("Failed to update skill {}: new name {} already exists", nameSanitized, newNameSanitized);
			throw new DuplicateSkillException("skillname already exists");
		}

		if (!isValidSubSkills(subSkills)) {
			logger.info("Failed to update skill {}: one or more subskills not found");
			throw new SkillNotFoundException("one new subskill cannot be found");
		}

	// @formatter:off
    newSkill = new Skill(
      StringUtils.isEmpty(newNameSanitized) ? oldSkill.getName() : newNameSanitized,
      description == null ? oldSkill.getDescription() : description,
      oldSkill.getSuggestions(),
      hidden == null ? oldSkill.isHidden() : hidden,
      CollectionUtils.isEmpty(subSkills) ? oldSkill.getSubSkillNames() : subSkills
    );
    // @formatter:on

		if (newSkill.equals(oldSkill)) {
			logger.info("Failed to update skill {}: new values contain no changes");
			return;
		}

		skillRepository.delete(oldSkill);
		skillRepository.insert(newSkill);

		if (!StringUtils.isEmpty(newNameSanitized)) {
			updateInSubskills(oldSkill, newSkill);
			updateInSuggestions(oldSkill, newSkill);
			updateInPersons(oldSkill, newSkill);
		} else if (hidden != null) {
			updateInPersons(oldSkill, newSkill);
		}
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	private void updateInSuggestions(Skill oldSkill, Skill newSkill) {
		var containingSkills = skillRepository.findBySuggestion(oldSkill.getName());
		containingSkills.forEach(s -> s.renameSuggestion(oldSkill.getName(), newSkill.getName()));
		skillRepository.saveAll(containingSkills);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	private void updateInSubskills(Skill oldSkill, Skill newSkill) {
		var containingSkills = skillRepository.findBySubskillName(oldSkill.getName());
		containingSkills.forEach(s -> s.renameSubSkill(oldSkill.getName(), newSkill.getName()));
		skillRepository.saveAll(containingSkills);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	private void updateInPersons(Skill oldSkill, Skill newSkill) {
		logger.debug("updating Skill {} in users", oldSkill.getName());
		var users = UserRepository.findBySkill(oldSkill.getName());

		users.forEach(p -> {
			var oldUserSkill = p.getSkill(oldSkill.getName(), true);
			p.addUpdateSkill(newSkill.getName(), oldUserSkill.getSkillLevel(), oldUserSkill.getWillLevel(),
					newSkill.isHidden(), oldUserSkill.isMentor());
		});
		UserRepository.saveAll(users);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public void deleteSkill(String name, String migrateTo) throws IllegalArgumentException {

		Objects.requireNonNull(name);
		Objects.requireNonNull(migrateTo);

		Skill deleteSkill = skillRepository.findByNameIgnoreCase(name).orElseThrow(() -> {
			logger.debug("Failed to delete skill {}: not found", name);
			throw new SkillNotFoundException("skill not found");
		});

		Skill migrateSkill = skillRepository.findByNameIgnoreCase(migrateTo).orElseThrow(() -> {
			throw new SkillNotFoundException("skill not found");
		});

		migratePersonalSkills(deleteSkill, migrateSkill);

		// delete from persons
		for (User userDetailsImpl : UserRepository.findBySkill(name)) {
			userDetailsImpl.removeSkill(name);
			UserRepository.save(userDetailsImpl);
		}

		// delete from known skills
		skillRepository.delete(deleteSkill);

		// delete in suggestion
		for (Skill skill : skillRepository.findBySuggestion(name)) {
			skill.deleteSuggestion(name);
			skillRepository.save(skill);
		}

		logger.info("Successfully deleted skill {}", name);
	}

	private void migratePersonalSkills(Skill from, Skill to) throws IllegalArgumentException {
		if (from == null || to == null) {
			logger.info("Failed to migrate {} to {}: not found", from, to);
			throw new SkillNotFoundException("Failed to migrate personal skills");
		} else if (from.getName().equals(to.getName())) {
			logger.info("Failed to migrate {} to {}: source and target equal");
			throw new IllegalArgumentException("Source and target may not be equal");
		}

		var migrateables = UserRepository.findBySkill(from.getName()).stream()
				.filter(user -> !user.hasSkill(to.getName())).collect(Collectors.toList());

		migrateables.forEach(user -> {
			var oldSkill = user.getSkill(from.getName(), true);
			user.addUpdateSkill(to.getName(), oldSkill.getSkillLevel(), oldSkill.getWillLevel(), to.isHidden(),
					oldSkill.isMentor());
			user.removeSkill(from.getName());
		});

		UserRepository.saveAll(migrateables);
	}

	@Retryable(include = OptimisticLockingFailureException.class, maxAttempts = 10)
	public boolean isHidden(String skillName) {
		Skill skill = skillRepository.findByNameIgnoreCase(skillName).orElseThrow(() -> {
			throw new SkillNotFoundException("skill not found");
		});
		return skill.isHidden();
	}

	private boolean isValidSubSkills(Collection<String> subSkills) {
		return CollectionUtils.isEmpty(subSkills) || subSkills.size() == skillRepository.findByNameIn(subSkills).size();
	}

}