package com.sinnerschrader.skillwill.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = AdminController.class)
@Import(value = {MyWebSecurityConfig.class})
public class AdminControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private AdminController adminController;

  @MockBean
  private UserService userService;

  @MockBean
  private UserDetailsService userDetailsService;

  @MockBean
  private SkillService skillService;

  @MockBean
  private SessionService sessionService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private JwtUtils jwtUtils;

  private List<User> users;

  private List<String> skillNames;

  private List<Skill> skills;

  private UserDto userDto;

  private ObjectMapper objectMapper = new ObjectMapper();

  @WithMockUser(roles = "USER")
  @Test
  void getForbiddenStatusWhenCreateUserWithoutAdminRole() throws Exception {

    // given
    UserDto userDto = UserDto.builder().username("pippo").build();
    doNothing().when(userService).create(userDto);

    // Convert the UserDto object to a JSON string
    String jsonBody = objectMapper.writeValueAsString(userDto);

    // when
    MockHttpServletResponse response = mvc.perform(put("/user").contentType(MediaType.APPLICATION_JSON).content(jsonBody)).andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @WithMockUser(roles = "ADMIN")
  @Test
  void createUserSeccessfulWithAdminRole() throws Exception {

    // given
    UserDto userDto = UserDto.builder().username("pippo").build();
    doNothing().when(userService).create(userDto);

    // Convert the UserDto object to a JSON string
    String jsonBody = objectMapper.writeValueAsString(userDto);

    // when
    MockHttpServletResponse response = mvc.perform(put("/user").contentType(MediaType.APPLICATION_JSON).content(jsonBody)).andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
  }

  @WithMockUser(roles = "ADMIN")
  @Test
  void deleteUserSeccessfulWithAdminRole() throws Exception {

    // given
    String username = "pippo";
    doNothing().when(userService).deleteUser(username);

    // when
    MockHttpServletResponse response = mvc.perform(delete("/users/{user}", username)).andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

}
