package com.sinnerschrader.skillwill.controller;

import com.sinnerschrader.skillwill.exception.UserIdException;
import com.sinnerschrader.skillwill.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ExceptionController {

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseStatusException userNameNotFoundException(UserNotFoundException exception) {
		
		return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "prova");
	}
	
	@ExceptionHandler(UserIdException.class)
	public ResponseStatusException userIdException(UserIdException exception) {
		
		return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "impossible to update username because an id");
	}
	
}
