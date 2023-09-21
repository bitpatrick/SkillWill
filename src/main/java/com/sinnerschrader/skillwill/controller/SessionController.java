package com.sinnerschrader.skillwill.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.sinnerschrader.skillwill.dto.UserDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Session", description = "Manage current session")
@Controller
public class SessionController {

	private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

	@Operation(summary = "Session User", description = "Create session user")
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Operazione riuscita"),
        @ApiResponse(responseCode = "401", description = "Non autorizzato"),
        @ApiResponse(responseCode = "500", description = "Errore interno del server")
    })
    @GetMapping(value = "/session/user", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getCurrentUser() {

		// recupera l'utente dal contesto di sicurezza
		UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			
		UserDto userDto = UserDto.builder()
				.username(user.getUsername())
				.password(user.getPassword())
				.authorities(user.getAuthorities().stream().map(authority -> authority.getAuthority()).toList())
				.build();
		

		return new ResponseEntity<UserDto>(userDto, HttpStatus.OK);
	}

}
