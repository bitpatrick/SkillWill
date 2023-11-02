package com.sinnerschrader.skillwill.controller;

import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exception.UserNotFoundException;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Users", description = "User search")
@RestController
public class SearchController {

  private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

  @Autowired
  private UserService userService;

  @Autowired
  private SkillService skillService;

  @Autowired
  private SessionService sessionService;

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

    List<String> skillSearchNames = (skills == null || skills.isBlank()) ? new ArrayList<String>() : Arrays.asList(skills.replaceAll("\\s+", "").split(","));
    SkillSearchResult searchResult = skillService.searchSkillsByNames(skillSearchNames, true);
    List<User> foundUsers = userService.getUsers(searchResult, company, location);

    JSONObject json = new JSONObject();
    json.put("results",
      new JSONArray(foundUsers.stream().map(User::toJSON).collect(Collectors.toList())));
    json.put("searched", searchResult == null ? new JSONArray() : searchResult.mappingJson());

    skillService.registerSkillSearch(searchResult.mappedSkills());

    return new ResponseEntity<>(json.toString(), HttpStatus.OK);
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

  /**
   * Get locations
   */
  @Operation(summary = "Get locations", description = "Get locations from users")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Failure"),
  })
  @GetMapping(path = "/locations")
  public ResponseEntity<List<String>> getLocations(
  ) {

    List<String> locations = userService.getLocations();

    return new ResponseEntity<>(locations, HttpStatus.OK);
  }

  /**
   * Get companies
   */
  @Operation(summary = "Get companies", description = "Get companies from users")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Success"),
    @ApiResponse(responseCode = "400", description = "Bad Request"),
    @ApiResponse(responseCode = "404", description = "Not Found"),
    @ApiResponse(responseCode = "500", description = "Failure"),
  })
  @GetMapping(path = "/companies")
  public ResponseEntity<List<String>> getCompanies(
  ) {

    List<String> companies = userService.getCompanies();

    return new ResponseEntity<>(companies, HttpStatus.OK);
  }

}
