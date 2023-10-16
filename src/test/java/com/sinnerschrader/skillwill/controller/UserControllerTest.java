package com.sinnerschrader.skillwill.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.config.UserCheckAspect;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = UserController.class)
@Import(value = {MyWebSecurityConfig.class, UserCheckAspect.class})
public class UserControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserController userController;

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

  @BeforeEach
  void setup() {

    // create skillNames
    String skillName1 = "JAVA";
    String skillName2 = "SQL";
    String skillName3 = "PHP";

    // create users
    User user1 = User.builder().username("Pippo").build();
    User user2 = User.builder().username("Anna").build();
    User user3 = User.builder().username("Mario").build();

    // set users property
    users = List.of(user2, user1, user3);

    // add skills to users
    user1.addSkills(skillName1, skillName3);
    user2.addSkills(skillName2);
    user3.addSkills(skillName1);

    // set skillNames property
    skillNames = List.of(skillName1, skillName2, skillName3);

    // create skills
    Skill skill1 = new Skill(skillName1);
    Skill skill2 = new Skill(skillName2);
    Skill skill3 = new Skill(skillName3);

    // set skills property
    skills = List.of(skill1, skill2, skill3);

//		userDto = UserDto.builder().username("pippo").password("pwd").build();
  }

  @WithAnonymousUser
  @Test
  void getJsonUsers() throws Exception {

    /*
     * given
     */

    // create SkillSearchResult
    Map<String, Skill> mapped = new HashMap<String, Skill>();
    mapped.put(skillNames.get(0), this.skills.get(0));
    mapped.put(skillNames.get(1), this.skills.get(1));
    mapped.put(skillNames.get(2), this.skills.get(2));

    Set<String> unmapped = new HashSet<String>();
    unmapped.add(skillNames.get(0));
    unmapped.add(skillNames.get(1));
    unmapped.add(skillNames.get(2));

    SkillSearchResult skillSearchResult = new SkillSearchResult(mapped, unmapped);

    // params request
    String skills = "JAVA, SQL, PHP";
    String company = "Leonardo";
    String location = "Roma";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("skills", skills);
    params.add("location", location);
    params.add("company", company);

    given(skillService.searchSkillsByNames(skillNames, true)).willReturn(skillSearchResult);
    given(userService.getUsers(skillSearchResult, company, location)).willReturn(users);
    doNothing().when(skillService).registerSkillSearch(skillSearchResult.mappedSkills());

    // when
    MockHttpServletResponse response = mvc
      .perform(get("/users").params(params).accept(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getHeader("Content-Type")).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

    // Verifica che il contenuto della response sia un JSON valido
    objectMapper.readTree(response.getContentAsString());
  }

  @WithAnonymousUser
  @Test
  void getRedirectToLoginPageWhenAnonymousUserUpdatingUserDetails() throws Exception {

    // given
    UserDto userDto = UserDto.builder().username("pippo").build();
    doNothing().when(userService).updateUserDetails(userDto);

    // Convert the UserDto object to a JSON string
    String jsonBody = objectMapper.writeValueAsString(userDto);

    // when
    MockHttpServletResponse response = mvc
      .perform(patch("/users/{user}/skills", "pippo").contentType(MediaType.APPLICATION_JSON).content(jsonBody))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");
  }

  @WithMockUser(username = "isawer")
  @Test
  void removeSkillOwnUser() throws Exception {

    // given
    String username = "isawer";
    String skill = "JPA";
    doNothing().when(userService).removeSkills(username, skill);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("username", username);
    queryParams.add("skill", skill);

    // when
    MockHttpServletResponse response = mvc.perform(delete("/users/{username}/skills", username).queryParams(queryParams))
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @WithMockUser(username = "pippo")
  @Test
  void removeSkillOfOtherUser() throws Exception {

    // given
    String username = "isawer";
    String skill = "JPA";
    doNothing().when(userService).removeSkills(username, skill);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("username", username);
    queryParams.add("skill", skill);

    // when
    MockHttpServletResponse response = mvc.perform(delete("/users/{username}/skills", username).queryParams(queryParams))
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @WithMockUser(username = "pippo")
  @Test
  void updateSkillOfOtherUser() throws Exception {

    // given
    String user = "isawer";
    String skill = "JPA";
    int skill_level = 1;
    int will_level = 2;
    boolean mentor = false;
    doNothing().when(userService).updateSkills(user, skill, skill_level, will_level, mentor);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("user", user);
    queryParams.add("skill", skill);
    queryParams.add("skill_level", String.valueOf(skill_level));
    queryParams.add("will_level", String.valueOf(will_level));
    queryParams.add("mentor", String.valueOf(mentor));

    // when
    MockHttpServletResponse response = mvc.perform(patch("/users/{user}/skills", user).queryParams(queryParams))
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @WithAnonymousUser
  @Test
  void getLoginPageIfRemoveUserSkillWithAnonymousUser() throws Exception {

    // given
    String user = "isawer";
    String skill = "JPA";
    doNothing().when(userService).removeSkills(user, skill);
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("user", user);
    queryParams.add("skill", skill);

    // when
    MockHttpServletResponse response = mvc.perform(patch("/users/{user}/skills", "pippo").queryParams(queryParams))
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");
  }

}
