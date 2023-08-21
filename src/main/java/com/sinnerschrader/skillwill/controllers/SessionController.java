package com.sinnerschrader.skillwill.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
import com.sinnerschrader.skillwill.misc.StatusResponseEntity;
import com.sinnerschrader.skillwill.services.SessionService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Session", description = "Manage current session")
@Controller
@Scope("prototype")
public class SessionController {

	private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

	private final SessionService sessionService;

	@Autowired
	public SessionController(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	@ApiOperation(value = "session/user", nickname = "create session user", notes = "create session user")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 401, message = "Unauthorized"),
			@ApiResponse(code = 500, message = "Failure") })
	@ApiImplicitParams({ @ApiImplicitParam(name = "search", value = "Name to search", paramType = "query"),
			@ApiImplicitParam(name = "exclude_hidden", value = "Do not return hidden skills", paramType = "query", defaultValue = "true"),
			@ApiImplicitParam(name = "count", value = "Limit the number of skills to find", paramType = "query"), })
	@RequestMapping(path = "/session/user", method = RequestMethod.GET)
	public ResponseEntity<String> getCurrentUser(
			@CookieValue(value = "_oauth2_proxy", required = false) String oAuthToken) {

		UserDetailsImpl userDetailsImpl = null;

		if (oAuthToken != null && !oAuthToken.isBlank()) {
			logger.debug("Getting user from session {}", oAuthToken);
			userDetailsImpl = sessionService.getUserByToken(oAuthToken);
			if (userDetailsImpl == null) {
				return new StatusResponseEntity("no current session", HttpStatus.UNAUTHORIZED);
			}

		} else {
			userDetailsImpl = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}

		return new ResponseEntity<>(userDetailsImpl.toJSON().toString(), HttpStatus.OK);
	}

}
