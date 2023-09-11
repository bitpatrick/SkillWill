package com.sinnerschrader.skillwill.repositories;

import java.util.List;
import java.util.Optional;

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

  @Query("{ 'skills._id' : { $all : ?0 } }")
  List<User> findBySkills(List<String> skillNames);

  @Query("{ 'ldapDetails.mail' : '?0' }")
  User findByMail(String mail);
  
  
 // Optional<UserDetailsImpl>  findByUsername(String username);
//  Optional<UserDetailsImpl> findByIdIgnoreCase(String username);

}
