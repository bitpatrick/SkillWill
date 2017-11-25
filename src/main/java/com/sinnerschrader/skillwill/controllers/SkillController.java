package com.sinnerschrader.skillwill.controllers;

import com.sinnerschrader.skillwill.domain.person.Person;
import com.sinnerschrader.skillwill.domain.person.Role;
import com.sinnerschrader.skillwill.domain.skills.KnownSkill;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.misc.StatusJSON;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling /skills/{foo}
 *
 * @author torree
 */
@Api(tags = "Skills", description = "Manage all skills")
@Controller
@CrossOrigin
@Scope("prototype")
public class SkillController {

  private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

  @Autowired
  private SkillService skillService;

  @Autowired
  private SessionService sessionService;

  /**
   * get/suggest skills based on search query -> can be used for autocompletion when user started
   * typing
   */
  @ApiOperation(value = "suggest skills", nickname = "suggest skills", notes = "suggest skills")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "search", value = "Name to search", paramType = "query"),
    @ApiImplicitParam(name = "exclude_hidden", value = "Do not return hidden skills", paramType = "query", defaultValue = "true"),
    @ApiImplicitParam(name = "count", value = "Limit the number of skills to find", paramType = "query"),
  })
  @RequestMapping(path = "/skills", method = RequestMethod.GET)
  public ResponseEntity<String> getSkills(@RequestParam(required = false) String search,
    @RequestParam(required = false, defaultValue = "true") boolean exclude_hidden,
    @RequestParam(required = false, defaultValue = "-1") int count) {

    JSONArray skillsArr = new JSONArray(
      skillService.getSkills(search, exclude_hidden, count)
        .stream()
        .map(KnownSkill::toJSON)
        .collect(Collectors.toList())
    );
    return new ResponseEntity<>(skillsArr.toString(), HttpStatus.OK);
  }

  /**
   * Get a skill by its name
   */
  @ApiOperation(value = "get skill", nickname = "get skill")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.GET)
  public ResponseEntity<String> getSkill(@PathVariable(value = "skill") String name) {
    KnownSkill skill = skillService.getSkillByName(name);

    if (skill == null) {
      logger.debug("Failed to get skill {}: not found", name);
      return new ResponseEntity<>(new StatusJSON("skill not found").toString(),
        HttpStatus.NOT_FOUND);
    }

    logger.debug("Successfully got skill {}", name);
    return new ResponseEntity<>(skill.toJSON().toString(), HttpStatus.OK);
  }

  /**
   * suggest next skill to enter -> This is not the autocomplete for skill search (see
   * getSkillsExcludeHidden for that) -> Recommender System: "Users who searched this also searched
   * for that"
   */
  @ApiOperation(value = "suggest next skill", nickname = "suggest next skill", notes = "suggest next skill")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "search", value = "Names of skills already entered, separated by comma", paramType = "query", required = false),
    @ApiImplicitParam(name = "count", value = "Count of recommendations to get", paramType = "query", defaultValue = "10")
  })
  @RequestMapping(path = "/skills/next", method = RequestMethod.GET)
  public ResponseEntity<String> getNext(@RequestParam(required = false) String search,
    @RequestParam(defaultValue = "10") int count) {

    if (count < 1) {
      logger.debug("Failed to get suggestions for skills {}: count less than zero", search);
      return new ResponseEntity<>(
        new StatusJSON("count must be a positive integer (or zero)").toString(),
        HttpStatus.BAD_REQUEST);
    }

    try {
      List<String> searchItems = StringUtils.isEmpty(search)
        ? new ArrayList<>()
        : Arrays.asList(search.split("\\s*,\\s*"));
      List<KnownSkill> suggestionSkills = skillService.getSuggestionSkills(searchItems, count);
      List<JSONObject> suggestionJsons = suggestionSkills.stream()
        .map(KnownSkill::toJSON)
        .collect(Collectors.toList());

      logger.debug("Successfully got {} suggestions for search [{}]", suggestionJsons.size(), search);
      return new ResponseEntity<>(new JSONArray(suggestionJsons).toString(), HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to get suggestions for skills {}: serach contains inkown skill", search);
      return new ResponseEntity<>(new StatusJSON("search contains unknown skill").toString(),
        HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * create new skill
   */
  @ApiOperation(value = "add skill", nickname = "add skill", notes = "add a skill; Caution: parameter name is NOT the new skill's ID")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "name", value = "new skill's name", paramType = "form", required = true),
    @ApiImplicitParam(name = "hidden", value = "hide skill in search/suggestions", paramType = "form", defaultValue = "false"),
    @ApiImplicitParam(name = "subskills", value = "list of subskills (separated with comma)", paramType = "form"),
    @ApiImplicitParam(name = "sessionKey", value = "session of the current user", paramType = "form")
  })
  @RequestMapping(path = "/skills", method = RequestMethod.POST)
  public ResponseEntity<String> addSkill(
    @RequestParam String name,
    @RequestParam(required = false, defaultValue = "false") boolean hidden,
    @RequestParam(required = false, defaultValue = "") String subSkills,
    @RequestParam String sessionKey) {

    if (!sessionService.check(sessionKey, Role.ADMIN)) {
      return new ResponseEntity<>(new StatusJSON("invalid sessionKey or user is not admin").toString(), HttpStatus.FORBIDDEN);
    }

    try {
      skillService.createSkill(name, hidden, createSubSkillSet(subSkills));
      logger.info("Successfully created new skill {}", name);
      return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
    } catch (EmptyArgumentException | DuplicateSkillException e) {
      logger.debug("Failed to create skill {}: argument empty or skill already exists", name);
      return new ResponseEntity<>(new StatusJSON(e.getMessage()).toString(),
        HttpStatus.BAD_REQUEST);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to add skill {}, subskill not found", name);
      return new ResponseEntity<>(new StatusJSON(e.getMessage()).toString(),
        HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * delete skill
   */
  @ApiOperation(value = "delete skill", nickname = "delete skill", notes = "parameter must be a valid skill Id")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "sessionKey", value = "session of the current user", paramType = "form", required = true),
    @ApiImplicitParam(name = "migrateTo", value = "skill to which old levels will be migrated", paramType = "form")
  })
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteSkill(@PathVariable String skill, @RequestParam String sessionKey, @RequestParam(required = false) String migrateTo) {
    if (!sessionService.check(sessionKey, Role.ADMIN)) {
      return new ResponseEntity<>(new StatusJSON("invalid sessionKey or user is not admin").toString(), HttpStatus.FORBIDDEN);
    }

    try {
      skillService.deleteSkill(skill, migrateTo);
      logger.info("Successfully deleted skill {}", skill);
      return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to delete skill {}: not found", skill);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to delete skill {}: illegal argument");
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * edit skill
   */
  @ApiOperation(value = "edit skill", nickname = "edit skill")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Success"),
    @ApiResponse(code = 400, message = "Bad Request"),
    @ApiResponse(code = 404, message = "Not Found"),
    @ApiResponse(code = 500, message = "Failure")
  })
  @ApiImplicitParams({
    @ApiImplicitParam(name = "name", value = "skill's new name", paramType = "form", required = false),
    @ApiImplicitParam(name = "hidden", value = "hide skill", paramType = "form", required = false),
    @ApiImplicitParam(name = "subskills", value = "skill's new subskills", paramType = "form", required = false),
    @ApiImplicitParam(name = "sessionKey", value = "session of the current user", paramType = "form")
  })
  @RequestMapping(path = "/skills/{skill}", method = RequestMethod.POST)
  public ResponseEntity<String> updateSkill(@PathVariable String skill,
    @RequestParam(required = false) String name,
    @RequestParam(required = false) Boolean hidden,
    @RequestParam(required = false) String subskills,
    @RequestParam String sessionKey) {

    if (!sessionService.check(sessionKey, Role.ADMIN)) {
      return new ResponseEntity<>(new StatusJSON("invalid sessionKey or user is not admin").toString(), HttpStatus.FORBIDDEN);
    }

    try {
      skillService.updateSkill(skill, name, hidden, createSubSkillSet(subskills));
      return new ResponseEntity<>(new StatusJSON("success").toString(), HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to update skill {}: not found", skill);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    } catch (DuplicateSkillException | IllegalArgumentException e) {
      logger.debug("Failed to update skill {}: illegal argument or skill already exists", skill);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  private Set<String> createSubSkillSet(String s) {
    return StringUtils.isEmpty(s)
      ? new HashSet<>()
      : new HashSet<>(Arrays.asList(StringUtils.tokenizeToStringArray(s, ",")));
  }

}
