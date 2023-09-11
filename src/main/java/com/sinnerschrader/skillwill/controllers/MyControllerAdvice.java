package com.sinnerschrader.skillwill.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;

@ControllerAdvice
public class MyControllerAdvice {

	@ExceptionHandler(value = SkillNotFoundException.class)
	public ResponseStatusException handleException(Exception exception) {

		return new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
	}

}
