package com.sinnerschrader.skillwill.controller;

import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

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
  @PutMapping(value = "/user")
  @ResponseStatus(HttpStatus.CREATED)
  public void createUser(
    @Parameter(description = "Informations to create a user") @RequestBody UserDto userDto
  ) {
    userService.create(userDto);
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
  @PreAuthorize("hasRole('ADMIN')")
  public void deleteUser(
    @Parameter(description = "User identifier") @PathVariable("user") String username
  ) {
    userService.deleteUser(username);
  }

}
