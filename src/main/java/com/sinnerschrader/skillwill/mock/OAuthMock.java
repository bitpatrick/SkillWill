package com.sinnerschrader.skillwill.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import com.sinnerschrader.skillwill.misc.StatusResponseEntity;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import com.sinnerschrader.skillwill.services.SessionService;

@Controller
public class OAuthMock {

  @Value("${mockOAuth}")
  private String mockOAuth;

  @Autowired
  private SessionService sessionService;

  @Autowired
  private UserRepository userRepository;

  @GetMapping("/oauthmock")
  public ResponseEntity<String> getOAuthMock(@CookieValue("_oauth2_proxy") String oAuthToken) {
    if (StringUtils.isEmpty(mockOAuth) || !mockOAuth.equals("true")) {
      return new StatusResponseEntity("oauth mock disabled", HttpStatus.LOCKED);
    }

    if (userRepository.findByMail(sessionService.extractMail(oAuthToken)) != null) {
      return new StatusResponseEntity("success", HttpStatus.ACCEPTED);
    }

    return new StatusResponseEntity("authentication failed", HttpStatus.FORBIDDEN);
  }

}
