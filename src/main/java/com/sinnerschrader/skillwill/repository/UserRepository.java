package com.sinnerschrader.skillwill.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.domain.user.User;

/**
 * MongoRepository for Persons
 * Collection: person
 */
public interface UserRepository extends MongoRepository<User, String> {

  @Query("{ 'skills._id' : '?0' }")
  List<User> findBySkill(String skillName);
  
  @Query("{ '$and': [ { 'skills._id': ?0 }, { 'skills._id': { $ne: ?1 } } ] }")
  List<User> findUsersNeedMigrateSkills(String from, String to);
  
  @Query("{ 'skills._id' : { $all : ?0 } }")
  List<User> findBySkills(List<String> skillNames);

  @Query("{ 'ldapDetails.mail' : '?0' }")
  User findByMail(String mail);
  
//  @Aggregation(pipeline = {
//		    "{ $match: { 'skills._id': { $regex: ?0, $options: 'i' } } }",
//		    "{ $count: 'usersWithExactSkill' }"
//		})
//  Integer countUsersWithSkill(String skillName);

  @Aggregation(pipeline = {
		    "{ $match: { '$and': [ { 'skills._id': ?0 }, { 'skills._id': { $ne: ?1 } } ] } }",
		    "{ $count: 'usersWithSkillAndWithoutAnotherSkill' }"
		})
  Integer countUsersWithSkillAndWithoutMigrateSkill(String from, String migrateTo);

  @Aggregation(pipeline = {
		    "{ $match: { '$and': [ { 'skills._id': ?0 }, { 'skills._id': ?1  } ] } }",
		    "{ $count: 'usersWith2Skill' }"
		})
  Integer countUsersWith2Skill(String from, String migrateTo);
  
  @Aggregation(pipeline = {
		    "{ $match: { 'skills._id': ?0 } }",
		    "{ $count: 'usersWithSkill' }"
		})
  Integer countBySkillId(String skillName);
  
//  Optional<UserDetailsImpl>  findByUsername(String username);
//  Optional<UserDetailsImpl> findByIdIgnoreCase(String username);

}
