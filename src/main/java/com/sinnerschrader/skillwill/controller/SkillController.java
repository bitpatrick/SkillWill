package com.sinnerschrader.skillwill.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.editor.SanitizeEditor;
import com.sinnerschrader.skillwill.editor.SearchEditor;
import com.sinnerschrader.skillwill.editor.SubSkillsEditor;
import com.sinnerschrader.skillwill.service.SkillService;

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

	@InitBinder(value = {"name", "subSkills", "search"})
	private void initBinder(WebDataBinder webDataBinder) {
		
		// search
		webDataBinder.registerCustomEditor(List.class, new SearchEditor());
		
		// name
		webDataBinder.registerCustomEditor(String.class, new SanitizeEditor());
		
		// subSkills
		webDataBinder.registerCustomEditor(Set.class, new SubSkillsEditor());
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
	@ResponseStatus(HttpStatus.CREATED)
	@PutMapping(path = "/skills")
	@PreAuthorize("hasRole('ADMIN')")
	public void createSkill(
			@Parameter(description = "new skill's name", required = true) @RequestParam String name,
			@Parameter(description = "Skill description") @RequestParam(required = false) String description,
			@Parameter(description = "hide skill in search/suggestions", example = "false") @RequestParam(required = false, defaultValue = "false") Boolean hidden,
			@Parameter(description = "list of subSkills (separated with comma)") @RequestParam Set<String> subSkills
			) {
		
		skillService.createSkill(name, description, hidden, subSkills);
		logger.info("Successfully created new skill {}", name);
	}

	/**
	 * get/suggest skills based on search query -> can be used for autocompletion
	 * when user started typing
	 */
	@Operation(summary = "suggest skills", description = "suggest skills")
	@ApiResponses({ 
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "500", description = "Failure") 
		})
	@GetMapping(path = "/skills", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Skill>> getSkills(
			@Parameter(description = "Name to search") @RequestParam(required = false) String search,
			@Parameter(description = "Do not return hidden skills", example = "true") @RequestParam(defaultValue = "true") Boolean exclude_hidden,
			@Parameter(description = "Limit the number of skills to find", example = "0") @RequestParam(defaultValue = "0") Integer count
			) {

		// retrieve skills
		List<Skill> skills = skillService.findSkills(search, exclude_hidden, count);

		return new ResponseEntity<List<Skill>>(skills, HttpStatus.OK);
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
	 * suggest next skill to enter -> This is not the autocomplete for skill search
	 * (see getSkills(true) for that) -> Recommender System: "Users who searched
	 * this also searched for that"
	 */
	@Operation(summary = "suggest next skill", description = "suggest next skill")
	@ApiResponses({ 
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "500", description = "Failure") 
		})
	@GetMapping(path = "/skills/next", produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Skill>> getNext(
			@Parameter(description = "Names of skills already entered, separated by comma") @RequestParam(required = false) List<String> search,
			@Parameter(description = "Count of recommendations to get", required = false, example = "10") @RequestParam(defaultValue = "10") Integer count
			) {
		
		// retrieve suggestionSkills
		List<Skill> suggestionSkills = skillService.getSuggestionSkills(search, count);

		return new ResponseEntity<List<Skill>>(suggestionSkills, HttpStatus.OK);
	}
	
	/**
	 * edit skill
	 */
	@Operation(summary = "edit skill", description = "Update skill details")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure") })
	@PostMapping(path = "/skills/{skill}")
	@ResponseStatus(HttpStatus.OK)
	public void updateSkill(
			@Parameter(description = "ID of the skill to be edited", required = true) @PathVariable String skill,
			@Parameter(description = "skill's new name") @RequestParam(required = false) String name,
			@Parameter(description = "Description of the skill") @RequestParam(required = false) String description,
			@Parameter(description = "hide skill") @RequestParam(required = false) Boolean hidden,
			@Parameter(description = "skill's new subSkills") @RequestParam(required = false) Set<String> subSkills
			) {
			
			skillService.updateSkill(skill, name, description, hidden, subSkills);
			logger.info("Successfully updated skill {}", skill);
		}

	/**
	 * delete skill and migrate to new skill
	 */
	@Operation(summary = "delete skill", description = "parameter must be a valid skill Id")
	@ApiResponses({ 
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "404", description = "Not Found"),
		@ApiResponse(responseCode = "500", description = "Failure") 
		})
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping(path = "/skills/{skill}")
	@ResponseStatus(HttpStatus.OK)
	public void deleteSkill(
			@Parameter(description = "ID of the skill to be deleted", required = true) @PathVariable String skill,
			@Parameter(description = "skill to which old levels will be migrated") @RequestParam(required = false) String migrateTo
			) {

			skillService.deleteSkill(skill, migrateTo);
			logger.info("Successfully deleted skill {}", skill);
	}

}
