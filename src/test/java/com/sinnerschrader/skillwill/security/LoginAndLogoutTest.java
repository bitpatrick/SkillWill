package com.sinnerschrader.skillwill.security;

import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@Import(MyWebSecurityConfig.class)
@WebMvcTest(excludeFilters = @ComponentScan.Filter(
  type = FilterType.ANNOTATION,
  classes = {RestController.class, Service.class, Component.class}
))
public class LoginAndLogoutTest {

  @TestConfiguration
  static class ApplicationContext {

    @Bean
    @Primary
    public UserDetailsService userDetailsServiceTest() {
      UserDetails user = User.builder()
        .username("pippo")
        .password("password")
        .roles("USER")
        .build();
      return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @Primary
    DaoAuthenticationProvider daoAuthenticationProviderTest() {

      DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
      provider.setUserDetailsService(userDetailsServiceTest());
      provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());

      return provider;
    }

  }

  @Autowired
  private MockMvc mvc;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private JwtUtils jwtUtils;

  @Test
  public void getHttpStatus200LoginWithRightCredentials() throws Exception {

    // given
    String username = "pippo";
    String password = "password";

    // when
    MockHttpServletResponse response = mvc.perform(post("/login")
      .param("username",username)
      .param("password",password))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  public void getErrorLoginWithBadCredentials() throws Exception {

    // given
    String username = "pippo";
    String password = "password123";

    // when
    MockHttpServletResponse response = mvc.perform(post("/login")
        .param("username",username)
        .param("password",password))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isEqualTo("/login?error");

  }

  @Test
  @WithAnonymousUser
  public void getRedirectUrlWhenGettingProtectedUrlWithAnonymousUser() throws Exception {

    // when
    MockHttpServletResponse response = mvc.perform(get("/session/user"))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");

  }

  @Test
  @WithMockUser(username = "pippo")
  public void successfullLogoutWithAuthenticatedUser() throws Exception {

    // when
    MockHttpServletResponse response = mvc.perform(get("/logout"))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/", "http://localhost:8888/");
  }

  @Test
  @WithAnonymousUser
  public void successfullLogoutWithAnonymousUser() throws Exception {

    // when
    MockHttpServletResponse response = mvc.perform(get("/logout"))
      .andDo(print()).andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
//    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/", "http://localhost:8888/");
  }





}
