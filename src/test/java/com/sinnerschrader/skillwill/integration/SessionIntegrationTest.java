package com.sinnerschrader.skillwill.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class SessionIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JacksonTester<UserDto> jsonUserDto;

  ObjectMapper objectMapper = new ObjectMapper();

  @WithUserDetails(value = "isawer")
  @Test
  public void test() throws Exception {

    /*
     * given
     */
    String username = "isawer";
    User userFromRepo = userRepository.findById(username).orElseThrow();
    UserDto userDtoFromRepo = userFromRepo.toUserDto();

    /*
     * when
     */
    MockHttpServletResponse response = mvc.perform(get("/session/user"))
      .andDo(print()).andReturn().getResponse();
    String user = response.getContentAsString();
    UserDto userDto = objectMapper.readValue(user, UserDto.class);

    /*
     * then
     */
    assertThat(userDto).usingRecursiveComparison().isEqualTo(userDtoFromRepo);
  }

  @WithAnonymousUser
  @Test
  @DisplayName("Redirect to login page when user is not authenticated")
  void redirectToLoginPageWhenUserIsNotAuthenticated() throws Exception {

    // when
    MockHttpServletResponse response = mvc
      .perform(get("/session/user").accept(MediaType.APPLICATION_JSON_VALUE))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");
  }

  @WithMockUser(username = "Pippo", password = "secret", authorities = {"ADMIN", "USER"})
  @Test
  @DisplayName("Get own info user when you are authenticated")
  void getOwnInfoUserWhenYouAreAuthenticated() throws Exception {

    // given
    SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority("ROLE_ADMIN");
    SimpleGrantedAuthority userRole = new SimpleGrantedAuthority("ROLE_USER");

    User user = new User("Pippo", "secret", List.of(adminRole, userRole));

    // when
    User userSaved = userRepository.save(user);
    UserDto userDto = userSaved.toUserDto();

    MockHttpServletResponse response = mvc
      .perform(get("/session/user").accept(MediaType.APPLICATION_JSON_VALUE))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getContentAsString()).isEqualTo(jsonUserDto.write(userDto).getJson());
  }

}
