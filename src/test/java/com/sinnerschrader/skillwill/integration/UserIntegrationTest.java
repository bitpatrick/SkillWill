package com.sinnerschrader.skillwill.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.controller.UserController;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserController userController;

  @Test
  void test() {
    assertThat(userController).isNotNull();
  }

  @Test
  void loginSuccessful() throws Exception {

    // given
    String username = "isawer";
    String password = "password";

    // when
    MockHttpServletResponse response = mvc
      .perform(post("/login")
        .param("username",username)
        .param("password",password))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void loginFailure() throws Exception {

    // given
    String username = "isawer";
    String password = "password123";

    // when
    MockHttpServletResponse response = mvc
      .perform(post("/login")
        .param("username",username)
        .param("password",password))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isEqualTo("/login?error");
  }

  @Test
  void getUsers() throws Exception {

    // when
    MockHttpServletResponse response = mvc
      .perform(get("/users"))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void getUser() throws Exception {

    // given
    String username = "isawer";

    // when
    MockHttpServletResponse response = mvc
      .perform(get("/user/{username}", username))
      .andDo(print())
      .andReturn()
      .getResponse();

  }

}
