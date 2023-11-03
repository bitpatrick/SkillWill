package com.sinnerschrader.skillwill.service;

import com.sinnerschrader.skillwill.domain.skill.UserSkill;
import com.sinnerschrader.skillwill.domain.user.FitnessScoreProperties;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.exception.UserNotFoundException;
import com.sinnerschrader.skillwill.mock.MockData;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@DataMongoTest
public class UserServiceIntegrationTest {

  @TestConfiguration
  static class ApplicationContext {

    @Bean
    UserService userService() {
      return new UserService();
    }

    @Bean
    SkillService skillService() {
      return new SkillService();
    }

    @Bean
    MockData mockData() {
      return new MockData();
    }

  }

  @Autowired
  private UserService userService;

  @MockBean
  private FitnessScoreProperties fitnessScoreProperties;

  @Autowired
  private UserRepository userRepository;

  @Test
  void updateSkill() {

    // given
    String username = "isawer";
    String skillName = "JPA";
    int skillLevel = 1;
    int willLevel = 2;
    boolean mentor = false;
    UserSkill userSkillNotUpdated = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("user not found")).getSkill(skillName, true);

    // when
    userService.updateSkills(username, skillName, skillLevel, willLevel, mentor);
    UserSkill userSkillUpdated = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("user not found")).getSkill(skillName, true);

    // then
    assertThat(userSkillNotUpdated.getName()).isEqualTo(skillName);
    assertThat(userSkillNotUpdated.getSkillLevel()).isEqualTo(3);
    assertThat(userSkillNotUpdated.getWillLevel()).isEqualTo(3);

    assertThat(userSkillUpdated.getName()).isEqualTo(skillName);
    assertThat(userSkillUpdated.getSkillLevel()).isEqualTo(skillLevel);
    assertThat(userSkillUpdated.getWillLevel()).isEqualTo(willLevel);
  }

  @Test
  void removeUser() {

    // given
    String username = "isawer";
    User user = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("user not found"));

    // when
    Throwable deleteUserThrowable = catchThrowable(() -> userService.deleteUser(username)); // business logic
    Throwable getUserThrowable = catchThrowable(() -> userService.getUser(username));

    // then
    assertThat(user).isNotNull();
    assertThat(user.getUsername()).isEqualTo(username);
    assertThat(deleteUserThrowable).doesNotThrowAnyException();
    assertThat(getUserThrowable).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void removeSkill() {

    // given
    String username = "isawer";
    String skillName = "JPA";
    int skillLevel = 3;
    int willLevel = 3;
    boolean mentor = false;
    UserSkill userSkill = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("user not found")).getSkill(skillName, true);

    // when
    userService.removeSkill(username, userSkill.getName());
    UserSkill userNullSkill = userRepository.findById(username).orElseThrow(() -> new UserNotFoundException("user not found")).getSkill(skillName, true);

    // then
    assertThat(userSkill.getName()).isEqualTo(skillName);
    assertThat(userNullSkill).isNull();
  }

}
