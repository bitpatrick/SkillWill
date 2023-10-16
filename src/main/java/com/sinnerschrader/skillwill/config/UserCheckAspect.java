package com.sinnerschrader.skillwill.config;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Aspect
@Component
public class UserCheckAspect {

  @Before("execution(* com.sinnerschrader.skillwill.controller.UserController.create*(..)) && args(username, ..) " +
    "|| execution(* com.sinnerschrader.skillwill.controller.UserController.update*(..)) && args(username, ..) " +
    "|| execution(* com.sinnerschrader.skillwill.controller.UserController.remove*(..)) && args(username, ..)")
  public void checkUser(String username) {

    // Ottenere l'utente autenticato
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String authenticatedUser = auth.getName();

    // Verificare se l'utente autenticato corrisponde all'utente specificato nell'URL
    if (!authenticatedUser.equals(username)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
  }
}
