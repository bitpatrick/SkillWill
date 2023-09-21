package com.sinnerschrader.skillwill.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.LoginRequest;
import com.sinnerschrader.skillwill.dto.LoginResponse;
import com.sinnerschrader.skillwill.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api")
public class ApiController {

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private JwtUtils jwtUtils;

	@Operation(summary = "Authenticate User", description = "Permit user to authenticate")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
			@ApiResponse(responseCode = "400", description = "Bad Request"),
			@ApiResponse(responseCode = "403", description = "Forbidden"),
			@ApiResponse(responseCode = "404", description = "Not Found"),
			@ApiResponse(responseCode = "500", description = "Failure"), })
	@GetMapping(value = "/getJwt", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LoginResponse> authenticateUser(
			@Parameter(description = "Username and password to authenticate") @RequestBody LoginRequest request
			) { 

		// tenta autenticazione
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// set security context
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		// retrive user
		User user = (User) authentication.getPrincipal();

		String jwt = jwtUtils.generateJwtToken(authentication);
		String username = user.getUsername();
		String email = user.getEmail();
		List<String> roles = Optional.ofNullable(user.getAuthorities()).orElse(Collections.emptyList()).stream().map(Object::toString).toList();
		LocalDateTime expirationeTime = user.getExpirationTime();
		
		return ResponseEntity.ok(new LoginResponse(jwt, username, email, roles, expirationeTime));
	}

}
