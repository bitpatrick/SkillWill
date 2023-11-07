package com.sinnerschrader.skillwill.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.controller.SkillController;
import com.sinnerschrader.skillwill.controller.UserController;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SkillUtils;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;
import com.sinnerschrader.skillwill.repository.SkillRepository;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import javax.swing.text.html.Option;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class SkillIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private SkillController skillController;

  @Autowired
  private SkillRepository skillRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  @WithMockUser(roles = {"ADMIN"})
  void createSkillSuccessfullWithoutSubSkillsByAdmin() throws Exception {

    // given
    String name = "Super Skill";
    String nameStem = SkillUtils.generateStemName(name);
    String description = "Description of Super Skill";
    String hidden = "false";
    String subSkills = "";

    // when
    MockHttpServletResponse response = mvc
      .perform(post("/skills")
        .param("name", name)
        .param("description", description)
        .param("hidden", hidden)
        .param("subSkills", subSkills))
      .andDo(print())
      .andReturn()
      .getResponse();

    Throwable throwable = catchThrowable(() -> skillRepository.findById(nameStem).get());

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
    assertThat(throwable).doesNotThrowAnyException();
  }

  @Test
  @WithMockUser(roles = {"USER"})
  void createSkillFaiulureByUser() throws Exception {

    // given
    String name = "Super Skill Bis";
    String nameStem = SkillUtils.generateStemName(name);
    String description = "Description of Super Skill Bis";
    String hidden = "false";
    String subSkills = "";

    // when
    MockHttpServletResponse response = mvc
      .perform(post("/skills")
        .param("name", name)
        .param("description", description)
        .param("hidden", hidden)
        .param("subSkills", subSkills))
      .andDo(print())
      .andReturn()
      .getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
  }

  @Test
  @WithUserDetails(value = "isawer")
  void createSkillFailureByUserDetailWithRoleUser() throws Exception {

    // given
    String name = "Super Skill Four";
    String nameStem = SkillUtils.generateStemName(name);
    String description = "Description of Super Skill Four";
    String hidden = "false";
    String subSkills = "";

    // when
    MockHttpServletResponse response = mvc
      .perform(post("/skills")
        .param("name", name)
        .param("description", description)
        .param("hidden", hidden)
        .param("subSkills", subSkills))
      .andDo(print())
      .andReturn()
      .getResponse();

    Optional<Skill> skillNotSaved = skillRepository.findById(nameStem);

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(skillNotSaved).isEmpty();
  }

  @Test
  @WithUserDetails(value = "fabbra")
  void updateSkillSuccessfullByUserDetailWithRoleAdmin() throws Exception {

    // given
    String skill = "JAVA";
    String name = "SUPER JAVA";
    String nameStem = SkillUtils.generateStemName(name);
    String description = "New Description about Super Java";
    String hidden = "false";
    String subSkills = "";

    Skill superJava = Skill.builder()
      .name(name)
      .nameStem(nameStem)
      .description(description)
      .hidden(Boolean.valueOf(hidden))
      .subSkillNames(Set.of(""))
      .build();

    // when
    MockHttpServletResponse response = mvc
      .perform(put("/skills/{skill}", skill)
        .queryParam("name", name)
        .queryParam("description", description)
        .queryParam("hidden", hidden)
        .queryParam("subSkills", subSkills))
      .andDo(print())
      .andReturn()
      .getResponse();

    Optional<Skill> skillSaved = skillRepository.findById(nameStem);

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(skillSaved).isNotEmpty();
    assertThat(skillSaved.get()).usingRecursiveAssertion().ignoringAllNullFields().isEqualTo(superJava);
  }

  @Test
  @WithUserDetails(value = "isawer")
  void updateSkillFailureByUserDetailWithRoleUser() throws Exception {

    // given
    String skill = "Python";
    String name = "Super Python";
    String nameStem = SkillUtils.generateStemName(name);
    String description = "New Description about Super Python";
    String hidden = "false";
    String subSkills = "";

    Skill superJava = Skill.builder()
      .name(name)
      .nameStem(nameStem)
      .description(description)
      .hidden(Boolean.valueOf(hidden))
      .subSkillNames(Set.of(""))
      .build();

    // when
    MockHttpServletResponse response = mvc
      .perform(put("/skills/{skill}", skill)
        .queryParam("name", name)
        .queryParam("description", description)
        .queryParam("hidden", hidden)
        .queryParam("subSkills", subSkills))
      .andDo(print())
      .andReturn()
      .getResponse();

    Optional<Skill> skillNotSaved = skillRepository.findById(nameStem);

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(skillNotSaved).isEmpty();
  }







}
