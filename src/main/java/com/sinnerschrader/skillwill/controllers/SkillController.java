package com.sinnerschrader.skillwill.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.user.Role;
import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.EmptyArgumentException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.misc.StatusResponseEntity;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller handling /skills/{foo}
 */
@Tag(name = "Skills", description = "Manage all skills")
@RestController
public class SkillController {

  private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

  @Autowired
  private SkillService skillService;

  @Autowired
  private SessionService sessionService;
  
  @InitBinder("count")
  public void initBinder(WebDataBinder binder) {
      binder.registerCustomEditor(Integer.class, new CountPropertyEditor());
  }


  /**
   * get/suggest skills based on search query -> can be used for autocompletion when user started
   * typing
   */
  @Operation(summary = "suggest skills", description = "suggest skills")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @GetMapping(path = "/skills", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Page<Skill>> getSkills(
      @Parameter(description = "Name to search") @RequestParam(required = false) String search,
      @Parameter(description = "Do not return hidden skills", example = "true") @RequestParam(defaultValue = "true") Boolean exclude_hidden,
      @Parameter(description = "Limit the number of skills to find", example = "0") @RequestParam(defaultValue = "0") Integer count
   
      ) {
	  
	  // retrieve skills
	  List<Skill> skills = skillService.findSkill(search, exclude_hidden, count);
	  
	  // pagineted skills
	  Page<Skill> skillsPage = new PageImpl<Skill>(skills);
	  
	  return new ResponseEntity<Page<Skill>>(skillsPage, HttpStatus.OK);
  }
  
  /**
   * Get a skill by its name
   */
  @Operation(summary = "get skill", description = "get skill")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "404", description = "Not Found"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @GetMapping(path = "/skills/{skill}", produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Skill> getSkill(
      @PathVariable(name = "skill") String name
      ) {
	  
	  // retrive skill
	  Skill skill = skillService.getSkillByName(name);
    
	  return new ResponseEntity<Skill>(skill, HttpStatus.OK);
  }

  /**
   * suggest next skill to enter -> This is not the autocomplete for skill search (see
   * getSkills(true) for that) -> Recommender System: "Users who searched this also searched
   * for that"
   */
  @Operation(summary = "suggest next skill", description = "suggest next skill")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @GetMapping(path = "/skills/next")
  public ResponseEntity<Page<Skill>> getNext(
      @Parameter(description = "Names of skills already entered, separated by comma") @RequestParam(required = false) String search,
      @Parameter(description = "Count of recommendations to get", required = false, example = "10") @RequestParam(defaultValue = "10") Integer count
      ) {
	  
	  List<String> searchItems = StringUtils.hasText(search) ? List.of(search.split("\\s*,\\s*")) : Collections.emptyList();
      
	  // retrieve suggestionSkills
	  List<Skill> suggestionSkills = skillService.getSuggestionSkills(searchItems, count);
      
//	  List<JSONObject> suggestionJsons = suggestionSkills.stream().map(Skill::toJSON).collect(Collectors.toList());

//	  logger.debug("Successfully got {} suggestions for search [{}]", suggestionJsons.size(), search);
      
	  // pagineted skills
	  Page<Skill> suggestionSkillsPage = new PageImpl<Skill>(suggestionSkills);
	  
      return new ResponseEntity<Page<Skill>>(suggestionSkillsPage, HttpStatus.OK);
    
  }
  
  /**
   * create new skill
   */
  @Operation(summary = "add skill", description = "add a skill; Caution: parameter name is NOT the new skill's ID")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @PostMapping(path = "/skills")
  public ResponseEntity<String> addSkill(
      @Parameter(description = "new skill's name", required = true) @RequestParam String name,
      @Parameter(description = "Skill description")  @RequestParam(required = false) String description,
      @Parameter(description = "hide skill in search/suggestions", example = "false") @RequestParam(required = false, defaultValue = "false") Boolean hidden,
      @Parameter(description = "list of subskills (separated with comma)") @RequestParam(required = false, defaultValue = "") String subSkills,
      @Parameter(description = "session token of the current user", required = true) @CookieValue(value="_oauth2_proxy", required = true) String oAuthToken
  ) {

    if (!sessionService.checkTokenRole(oAuthToken, Role.ADMIN)) {
      return new StatusResponseEntity("invalid session token or user is not admin", HttpStatus.FORBIDDEN);
    }

    try {
      skillService.createSkill(name, description, hidden, createSubSkillSet(subSkills));
      logger.info("Successfully created new skill {}", name);
      return new StatusResponseEntity("success", HttpStatus.OK);
    } catch (EmptyArgumentException | DuplicateSkillException e) {
      logger.debug("Failed to create skill {}: argument empty or skill already exists", name);
      return new StatusResponseEntity("argument empty or skill already existing", HttpStatus.BAD_REQUEST);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to add skill {}, subskill not found", name);
      return new StatusResponseEntity("subskill not found", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * delete skill
   */
  @Operation(summary = "delete skill", description = "parameter must be a valid skill Id")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "404", description = "Not Found"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @DeleteMapping(path = "/skills/{skill}")
  public ResponseEntity<String> deleteSkill(
      @Parameter(description = "ID of the skill to be deleted", required = true)
      @PathVariable String skill, 

      @Parameter(description = "session token of the current user", required = true) 
      @CookieValue("_oauth2_proxy") String oAuthToken, 

      @Parameter(description = "skill to which old levels will be migrated")
      @RequestParam(required = false) String migrateTo
  ) {
	  
    if (!sessionService.checkTokenRole(oAuthToken, Role.ADMIN)) {
      return new StatusResponseEntity("invalid session token or user is not admin", HttpStatus.FORBIDDEN);
    }

    try {
      skillService.deleteSkill(skill, migrateTo);
      logger.info("Successfully deleted skill {}", skill);
      return new StatusResponseEntity("success", HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to delete skill {}: not found", skill);
      return new StatusResponseEntity("skill not found", HttpStatus.NOT_FOUND);
    } catch (IllegalArgumentException e) {
      logger.debug("Failed to delete skill {}: illegal argument");
      return new StatusResponseEntity("illegal argument", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * edit skill
   */
  @Operation(summary = "edit skill", description = "Update skill details")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Success"),
      @ApiResponse(responseCode = "400", description = "Bad Request"),
      @ApiResponse(responseCode = "404", description = "Not Found"),
      @ApiResponse(responseCode = "500", description = "Failure")
  })
  @PostMapping(path = "/skills/{skill}")
  public ResponseEntity<String> updateSkill(
      @Parameter(description = "ID of the skill to be edited", required = true)
      @PathVariable String skill,
      
      @Parameter(description = "skill's new name")
      @RequestParam(required = false) String name,
      
      @Parameter(description = "Description of the skill")
      @RequestParam(required = false) String description,
      
      @Parameter(description = "hide skill")
      @RequestParam(required = false) Boolean hidden,
      
      @Parameter(description = "skill's new subskills")
      @RequestParam(required = false) String subskills,
      
      @Parameter(description = "session token of the current user", required = true)
      @CookieValue("_oauth2_proxy") String oAuthToken
  ) {

    if (!sessionService.checkTokenRole(oAuthToken, Role.ADMIN)) {
      return new StatusResponseEntity("invalid session or not admin", HttpStatus.FORBIDDEN);
    }

    try {
      skillService.updateSkill(skill, name, description, hidden, createSubSkillSet(subskills));
      return new StatusResponseEntity("success", HttpStatus.OK);
    } catch (SkillNotFoundException e) {
      logger.debug("Failed to update skill {}: not found", skill);
      return new StatusResponseEntity("skill not found", HttpStatus.NOT_FOUND);
    } catch (DuplicateSkillException | IllegalArgumentException e) {
      logger.debug("Failed to update skill {}: illegal argument or skill already exists", skill);
      return new StatusResponseEntity("skill already existing or invalid", HttpStatus.BAD_REQUEST);
    }
  }

  private Set<String> createSubSkillSet(String s) {
		return new HashSet<>(Arrays.asList(StringUtils.tokenizeToStringArray(s, ",")));
	}
  
  public static class CountPropertyEditor extends PropertyEditorSupport {

	    @Override
	    public void setAsText(String text) throws IllegalArgumentException {
	        int value = Integer.parseInt(text);
	        if (value < 0) {
	            throw new IllegalArgumentException("Count cannot be negative.");
	        }
	        setValue(value);
	    }

  }

}
