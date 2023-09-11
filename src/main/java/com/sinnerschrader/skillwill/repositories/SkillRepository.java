package com.sinnerschrader.skillwill.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.domain.skills.Skill;

/**
 * Repository for skills Collection: knownSkill
 *
 * @author torree
 */
public interface SkillRepository extends MongoRepository<Skill, String> {

	Optional<Skill> findByNameIgnoreCase(String name);

	List<Skill> findByNameIn(Collection<String> names);

	@Query("{ 'suggestions.name' : '?0' }")
	List<Skill> findBySuggestion(String suggestion);

	@Query("{ 'subSkillNames' : '?0' }")
	List<Skill> findBySubskillName(String subskillName);

	Skill findByNameStem(String name);
	
	@Aggregation(pipeline = {
	"{ $match: { 'nameStem': { $regex: ?0, $options: 'i' }, 'hidden': ?1 } }"
	})
	List<Skill> findByNameStemLike(String name, boolean hidden);

	@Aggregation(pipeline = {
			"{ $match: { 'hidden': ?0 } }",
		    "{ $limit: ?1 }"
	})
	List<Skill> findAllLimited(boolean hidden, int limit);

	@Aggregation(pipeline = {
			"{ $match: { 'hidden': ?0 } }",
		    "{ $limit: ?1 }"
	})
	List<Skill> findAll(boolean hidden, int limit);

	@Aggregation(pipeline = {
		    "{ $match: { 'nameStem': { $regex: ?0, $options: 'i' }, 'hidden': ?1 } }",
		    "{ $limit: ?2 }"
	})
	List<Skill> findByNameStemLike(String search, boolean hidden, int limit);

	@Aggregation(pipeline = {
			"{ $match: { 'hidden': ?0 } }"
	})
	List<Skill> findAll(boolean hidden);
		
	@Query("{ 'hidden' : false }")
	List<Skill> findAllExcludeHidden();
		
	List<Skill> findAllByHidden(boolean hidden);
	
	Page<Skill> findAll(Pageable pageable);

}
