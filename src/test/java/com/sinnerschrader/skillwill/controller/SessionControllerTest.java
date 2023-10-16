package com.sinnerschrader.skillwill.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.controller.SessionController;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repository.UserRepository;

@Import(MyWebSecurityConfig.class)
@WebMvcTest(SessionController.class)
@AutoConfigureJsonTesters
class SessionControllerTest {
	
	private static final Logger logger = LoggerFactory.getLogger(SessionControllerTest.class);
	
	@MockBean
	private UserRepository userRepository;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@MockBean
	private JwtUtils jwtUtils;
	
	@Autowired
	private MockMvc mvc;
	
	@Autowired
	private JacksonTester<UserDto> jsonUserDto;

	@WithAnonymousUser
	@Test
	@DisplayName("Redirect to login page when user is not authenticated")
	void redirectToLoginPageWhenUserIsNotAuthenticated() throws Exception {
		
		logger.info("\n\nStarting test: redirect_To_Login_Page_When_User_Is_Not_Authenticated:");
		
		// given
		
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
		logger.info("\n\nStarting test: getOwnInfoUserWhenYouAreAuthenticated:");

		// given
		SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority("ADMIN");
		SimpleGrantedAuthority userRole = new SimpleGrantedAuthority("USER");
		
		User user = new User("Pippo", "secret", List.of(adminRole, userRole));
		UserDto userDto = user.toUserDto();
		
		// when
		MockHttpServletResponse response = mvc
				.perform(get("/session/user").accept(MediaType.APPLICATION_JSON_VALUE))
				.andDo(print())
				.andReturn()
				.getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString())
		.isEqualTo(jsonUserDto.write(userDto).getJson());
	}

}
