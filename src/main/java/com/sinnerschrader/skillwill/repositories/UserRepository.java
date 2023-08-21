package com.sinnerschrader.skillwill.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;

/**
 * MongoRepository for Persons
 * Collection: person
 *
 * @author torree
 */
public interface UserRepository extends MongoRepository<UserDetailsImpl, String> {

  UserDetailsImpl findByIdIgnoreCase(String id);

  @Query("{ 'skills._id' : '?0' }")
  List<UserDetailsImpl> findBySkill(String skillName);

  @Query("{ 'skills._id' : { $all : ?0 } }")
  List<UserDetailsImpl> findBySkills(List<String> skillNames);

  @Query("{ 'ldapDetails.mail' : '?0' }")
  UserDetailsImpl findByMail(String mail);

}
