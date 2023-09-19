package com.sinnerschrader.skillwill.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;

@ControllerAdvice
public class MyControllerAdvice {
	
	@ExceptionHandler(value = SkillNotFoundException.class)
	public ResponseStatusException handleSkillNotFoundException(Exception exception) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
	}
	
	@ExceptionHandler(value = DuplicateSkillException.class)
	public ResponseStatusException handleDuplicateSkillException(Exception exception) {
	    return new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
	}
	
}
