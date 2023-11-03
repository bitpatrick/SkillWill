package com.sinnerschrader.skillwill.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.config.UserCheckAspect;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = SearchController.class)
@Import(value = {MyWebSecurityConfig.class, UserCheckAspect.class})
public class SearchControllerTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private SearchController searchController;

  @MockBean
  private UserService userService;

  @MockBean
  private UserDetailsService userDetailsService;

  @MockBean
  private SkillService skillService;

  @MockBean
  private UserRepository userRepository;

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

}
