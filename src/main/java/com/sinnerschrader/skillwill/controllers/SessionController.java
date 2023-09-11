package com.sinnerschrader.skillwill.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.misc.StatusResponseEntity;
import com.sinnerschrader.skillwill.services.SessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Session", description = "Manage current session")
@Controller
public class SessionController {

	private static final Logger logger = LoggerFactory.getLogger(SkillController.class);

	@Autowired
	private SessionService sessionService;

	@Operation(summary = "Utente della sessione", description = "Crea utente della sessione")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Operazione riuscita"),
        @ApiResponse(responseCode = "401", description = "Non autorizzato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping("/session/user")
    public ResponseEntity<String> getCurrentUser(
            @Parameter(description = "Nome da cercare", in = ParameterIn.QUERY) 
            @RequestParam(required = false) String search,

            @Parameter(description = "Non restituire competenze nascoste", in = ParameterIn.QUERY) 
            @RequestParam(name = "exclude_hidden", defaultValue = "true") boolean exclude_hidden,

            @Parameter(description = "Limita il numero di competenze da trovare", in = ParameterIn.QUERY) 
            @RequestParam(required = false) Integer count,

            @Parameter(description = "Token OAuth2", in = ParameterIn.COOKIE) 
            @CookieValue(value = "_oauth2_proxy", required = false) String oAuthToken
    
    		) {

		User userDetailsImpl = null;

		if (oAuthToken != null && !oAuthToken.isBlank()) {
			logger.debug("Getting user from session {}", oAuthToken);
			userDetailsImpl = sessionService.getUserByToken(oAuthToken);
			if (userDetailsImpl == null) {
				return new StatusResponseEntity("no current session", HttpStatus.UNAUTHORIZED);
			}

		} else {
			userDetailsImpl = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		}

		return new ResponseEntity<>(userDetailsImpl.toJSON().toString(), HttpStatus.OK);
	}

}
