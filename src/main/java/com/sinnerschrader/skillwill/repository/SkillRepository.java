package com.sinnerschrader.skillwill.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SuggestionSkill;

/**
 * Repository for skills Collection: knownSkill
 */
public interface SkillRepository extends MongoRepository<Skill, String> {
	
	@Aggregation(pipeline = {
	        "{ $unwind: '$suggestions' }",
	        "{ $group: { _id: '$suggestions.name', count: { $sum: '$suggestions.count' } } }",
	        "{ $project: { name: '$_id', count: '$count', _id: 0 } }"
	})
	Set<SuggestionSkill> findAllAggregatedSuggestionSkills();
	
	@Aggregation({
	    "{ $match: { name: { $regex: ?0, $options: 'i' } } }",
	    "{ $unwind: '$subSkillNames' }",
	    "{ $project: { _id: 0, subSkillName: '$subSkillNames' } }"
	})
	Set<String> findAllSubSkillNamesByNameIgnoreCase(String skillName);
	
	
	boolean existsByNameIgnoreCase(String name);

	List<Skill> findByNameIn(Collection<String> names);
	
	Integer countByNameIn(Collection<String> names);

	@Query("{ 'suggestions.name' : '?0' }")
	List<Skill> findBySuggestion(String suggestion);

	@Query("{ 'subSkillNames' : '?0' }")
	List<Skill> findBySubskillName(String subskillName);

	Optional<Skill> findByNameStem(String name);
	
	List<Skill> findByNameStemLike(String name);

	@Query("{ 'hidden' : false }")
	List<Skill> findAllExcludeHidden();
	
	@Query(value = "{ 'suggestions.name' : '?0' }", delete = true)
	void removeSkillInSuggestion(String skillName);

	void deleteByNameIgnoreCase(String name);

	Set<SuggestionSkill> findAllSuggestionSkillByNameIgnoreCase(String newName);

	Optional<Skill> findByNameIgnoreCase(String name);
		
}
