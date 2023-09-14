package com.sinnerschrader.skillwill.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.services.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Session", description = "Manage current session")
@Controller
public class SessionController {

	private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

	@Autowired
	private SessionService sessionService;

	@Operation(summary = "Utente della sessione", description = "Crea utente della sessione")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Operazione riuscita"),
        @ApiResponse(responseCode = "401", description = "Non autorizzato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping(value = "/session/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getCurrentUser(
//            @Parameter(description = "Token OAuth2", in = ParameterIn.COOKIE) @CookieValue(value = "_oauth2_proxy", required = false) String oAuthToken
    		) {

		UserDto userDto;

//		if (oAuthToken != null && !oAuthToken.isBlank()) {
//			logger.debug("Getting user from session {}", oAuthToken);
//			userDetailsImpl = sessionService.getUserByToken(oAuthToken);
//			if (userDetailsImpl == null) {
//				return new StatusResponseEntity("no current session", HttpStatus.UNAUTHORIZED);
//				throw new AccessDeniedException("no current session");
//			}
//		}
			
		UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
		userDto = UserDto.builder()
				.username(user.getUsername())
				.password(user.getPassword())
				.authorities(user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList())
				.build();
		

		return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
	}
	
	

}
