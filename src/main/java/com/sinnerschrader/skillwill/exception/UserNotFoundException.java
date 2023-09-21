package com.sinnerschrader.skillwill.exception;

public class UserNotFoundException extends IllegalArgumentException {

  public UserNotFoundException(String message) {
    super(message);
  }

}
