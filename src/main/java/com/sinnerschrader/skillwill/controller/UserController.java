package com.sinnerschrader.skillwill.controller;

import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller handling /users/{foo}
 */
@Tag(name = "Users", description = "User management")
@RestController
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private SkillService skillService;

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
  @PostMapping(value = "/user")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('ADMIN')")
  public void createUser(
    @Parameter(description = "Informations to create a user") @RequestBody UserDto userDto
  ) {
    userService.create(userDto);
  }

  /**
   * Get user info
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
    logger.info("request successful recovery user details: {}", userDto);
    return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
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
			@Parameter(description = "User identifier") @PathVariable("user") String username,
			@Parameter(description = "Details of the user to be updated, including all user's informations uptdated or not ") @RequestBody UserDto userDto
			 ) {

		userService.updateUserDetails(userDto);
    logger.info("request successful user update: {}", userDto);
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
    logger.info("request successful update fitness score: {} of {}", fitnessScore, username);
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
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping("/users/{user}/skills")
	public void updateSkills(
			@Parameter(description = "User identifier") @PathVariable String user,
			@Parameter(description = "Name of skill", required = true) @RequestParam("skill") String skill,
			@Parameter(description = "Level of skill", required = true) @RequestParam("skill_level") int skill_level,
			@Parameter(description = "Level of will", required = true) @RequestParam("will_level") int will_level,
			@Parameter(description = "Mentor flag", required = true) @RequestParam("mentor") boolean mentor
			) {

    userService.updateSkills(user, skill, skill_level, will_level, mentor);
    logger.info("request successful update skill: {} of {}", skill, user);
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
	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping(path = "/users/{user}/skills")
	public void removeSkill(
			@Parameter(description = "User identifier") @PathVariable String user,
			@Parameter(description = "Name of skill", required = true) @RequestParam("skill") String skill
			) {

		userService.removeSkill(user, skill);
		logger.info("Successfully deleted {}'s skill {}", user, skill);
	}

  /*
   * Delete a user
   */
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
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteUser(
    @Parameter(description = "User identifier") @PathVariable("user") String username
  ) {
    userService.deleteUser(username);
  }

}
