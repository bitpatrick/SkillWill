package com.sinnerschrader.skillwill.mock;

import com.sinnerschrader.skillwill.misc.StatusJSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@CrossOrigin
@Scope("prototype")
public class OAuthMock {

  @Value("${mockOAuth}")
  private String mockOAuth;

  @RequestMapping(path = "/oauthmock", method = RequestMethod.GET)
  public ResponseEntity<String> getOAuthMock(@CookieValue("_oauth2_proxy") String oAuthToken) {
    if (StringUtils.isEmpty(mockOAuth) || !mockOAuth.equals("true")) {
      return new ResponseEntity<>(new StatusJSON("mocking disabled").getJSON().toString(), HttpStatus.LOCKED);
    }

    if (oAuthToken.equals("YWFhLmFhYUBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar") || oAuthToken.equals("YmJiLmJiYkBzaW5uZXJzY2hyYWRlci5jb20=|foo|bar")) {
      return new ResponseEntity<>(new StatusJSON("ok").getJSON().toString(), HttpStatus.ACCEPTED);
    }

    return new ResponseEntity<>(new StatusJSON("forbidden").getJSON().toString(), HttpStatus.FORBIDDEN);
  }

}
