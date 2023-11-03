package com.sinnerschrader.skillwill.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.controller.SessionController;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class SessionIntegrationTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SessionController sessionController;

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

}
