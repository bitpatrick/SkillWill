package com.sinnerschrader.skillwill.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserLdapDetailsDto;
import com.sinnerschrader.skillwill.exception.UserNotFoundException;
import com.sinnerschrader.skillwill.misc.StatusResponseEntity;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller handling /users/{foo}
 */
@Tag(name = "Users", description = "User management and search")
@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private SkillService skillService;

	@Autowired
	private SessionService sessionService;
	
	/**
	 * Create a user
	 */
	@Operation(summary = "Create User", description = "Create User")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
//	@PreAuthorize("hasRole('USER')") // solamente un admin può accedere a questo metodo, ovvero, creare un utente
	@PutMapping(value = "/user")
	@ResponseStatus(HttpStatus.CREATED)
	public void createUser(
			@Parameter(description = "Informations to create a user") @RequestBody UserDto user) {
		
		userService.create(user);
	}
	
	/**
	 * Get a user
	 */
	@Operation(summary = "Get user info", description = "Returns the user with the given id")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@GetMapping(value = "/users/{username}", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<UserDto> getUser(
			@Parameter(description = "Username of the user to retrieve") @PathVariable String username
			) {
		
		UserDto userDto = userService.getUser(username);
		return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
		
	}
	
	/**
	 * Search for users with specific skills / list all users if no search query is
	 * specified
	 */
	@Operation(summary = "Search users", description = "Search users.")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "500", description = "Failure") 
	})
	@GetMapping(value = "/users", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<String> getUsers(
			@Parameter(description = "Names of skills to search, separated by ','") @RequestParam(required = false) String skills,
			@Parameter(description = "Location to filter results by") @RequestParam(required = false) String location,
			@Parameter(description = "Company to filter results by") @RequestParam(required = false) String company
			) {
		
		/*
		 * skills deve essere diversa da null
		 */
		List<String> skillSearchNames = (skills == null || skills.isBlank()) ? new ArrayList<String>()
				: Arrays.asList(skills.split(","));
		SkillSearchResult searchResult = skillService.searchSkillsByNames(skillSearchNames, true);
		List<User> foundUsers = userService.getUsers(searchResult, company, location);

		JSONObject json = new JSONObject();
		json.put("results",
				new JSONArray(foundUsers.stream().map(User::toJSON).collect(Collectors.toList())));
		json.put("searched", searchResult == null ? new JSONArray() : searchResult.mappingJson());

		skillService.registerSkillSearch(searchResult.mappedSkills());

		return new ResponseEntity<>(json.toString(), HttpStatus.OK);
	}
	
	
	@Operation(summary = "Update User", description = "Updates an existing user's details")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@PatchMapping(value = "users/{user}", consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public void updateUser(
			@Parameter(description = "User identifier")@PathVariable("user") String username,
			@Parameter(description = "Details of the user to be updated, including all user's informations uptdated or not ")@RequestBody UserDto userDto
			 ) {

		userService.updateUserDetails(userDto,username);
	}
	
	@Operation(summary = "Update UserLdap", description = "Updates just a part of an existing user's details ")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@PatchMapping(value = "usersLdap/{user}",consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public void updateUserLdapDetails(
			@Parameter(description = "User identifier")@PathVariable("user") String username,
			@Parameter(description = "Details of the user to be updated, including all sensitive informations") @RequestBody UserLdapDetailsDto userLdap) {
		
		userService.updateUserLdapDetails(username, userLdap);
		
	}
	
	@Operation(summary = "Update Fitness Score", description = "Updates user's fitness score")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@PatchMapping(value = "usersFitScore/{user}",consumes = APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public void updateFitnessScore(
			@Parameter(description = "User identifier")@PathVariable("user") String username,
			@Parameter(description = "Details of the user to be updated, including fitness score") @RequestBody FitnessScoreDto fitnessScore) {
		
		userService.updateFitnessScore(username, fitnessScore);
		
	}
	
	@Operation(summary = "Delete User", description = "Delete an existing user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@DeleteMapping(value = "users/{user}")
	@ResponseStatus(HttpStatus.OK)
//	@PreAuthorize("hasRole('ADMIN')")
	public void deleteUser(
			@Parameter(description = "User identifier") @PathVariable("user") String username) {
		
		userService.deleteUser(username);
	}
	
	/**
	 * add and modify users's skills
	 */
	@Operation(summary = "Modify user skill", description = "Create or edit a skill of a user")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@PatchMapping("/users/{user}/skills")
	public ResponseEntity<String> updateSkills(
			@Parameter(description = "User identifier") @PathVariable String user,
			@Parameter(description = "Name of skill", required = true) @RequestParam("skill") String skill,
			@Parameter(description = "Level of skill", required = true) @RequestParam("skill_level") int skill_level,
			@Parameter(description = "Level of will", required = true) @RequestParam("will_level") int will_level,
			@Parameter(description = "Mentor flag", required = true) @RequestParam("mentor") boolean mentor,
			@Parameter(description = "Session token of the current user", required = true) @CookieValue(value = "_oauth2_proxy", required = false) String oAuthToken
			) {

//    if (!sessionService.checkToken(oAuthToken, user)) {
//      logger.debug("Failed to modify {}'s skills: not logged in", user);
//      return new StatusResponseEntity("user not logged in", HttpStatus.FORBIDDEN);
//    }

		try {
			userService.updateSkills(user, skill, skill_level, will_level, mentor);
			return new StatusResponseEntity("success", HttpStatus.OK);
		} catch (UserNotFoundException e) {
			return new StatusResponseEntity("user not found", HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			return new StatusResponseEntity("invalid request", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * delete user's skill
	 */
	@Operation(summary = "Remove user skill", description = "Remove a skill from a user")
	@ApiResponses(value = { 
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@DeleteMapping(path = "/users/{user}/skills")
	public ResponseEntity<String> removeSkill(
			@Parameter(description = "User identifier") @PathVariable String user,
			@Parameter(description = "Name of skill", required = true) @RequestParam("skill") String skill,
			@Parameter(description = "Session token of the current user", required = true) @CookieValue("_oauth2_proxy") String oAuthToken
			) {

		if (!sessionService.checkToken(oAuthToken, user)) {
			logger.debug("Failed to modify {}'s skills: not logged in", user);
			return new StatusResponseEntity("user not logged in", HttpStatus.FORBIDDEN);
		}

		try {
			userService.removeSkills(user, skill);
			logger.info("Successfully deleted {}'s skill {}", user, skill);
			return new StatusResponseEntity("success", HttpStatus.OK);
		} catch (UserNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			return new StatusResponseEntity("invalid request", HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Get users with similar skill sets
	 */
	@Operation(summary = "Get similar users", description = "Get users with similar skill sets")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), 
	})
	@GetMapping(path = "/users/{user}/similar")
	public ResponseEntity<String> getSimilar(
			@Parameter(description = "User identifier") @PathVariable String user,
			@Parameter(description = "Number of users to find (max)", required = false, example = "10") @RequestParam(value = "count", defaultValue = "10") Integer count
			) {

		List<User> similar;

		try {
			similar = userService.getSimilar(user, count);
		} catch (UserNotFoundException e) {
			logger.debug("Failed to get users similar to {}: user not found", user);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		} catch (IllegalArgumentException e) {
			logger.debug("Failed to get users similar to {}: illegal parameter", user);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}

		JSONArray arr = new JSONArray(similar.stream().map(User::toJSON).collect(Collectors.toList()));
		logger.debug("Successfully found {} users similar to {}", arr.length(), user);
		return new ResponseEntity<>(arr.toString(), HttpStatus.OK);
	}
	

}
