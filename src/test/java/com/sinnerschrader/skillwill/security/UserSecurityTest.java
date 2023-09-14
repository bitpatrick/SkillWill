package com.sinnerschrader.skillwill.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.controllers.UserController;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;
import com.sinnerschrader.skillwill.services.UserService;

@WebMvcTest(UserController.class)
@Import(MyWebSecurityConfig.class)
class UserSecurityTest {
	
	@Autowired
    private MockMvc mvc;

	@Autowired
	private UserController userController;

	@MockBean
	private UserService userService;

	@MockBean
	private SkillService skillService;

	@MockBean
	private SessionService sessionService;
	
	@MockBean
	private UserRepository userRepository;
	
	private UserDto userDto;
	
	@BeforeEach
	void setup() {
		
		userDto =  UserDto.builder().username("pippo").password("pwd").build();
	}
	
	@WithMockUser(roles="WRONG")
	@Test
	void throwExceptionWhenCreateUserWithoutAdminRole() {
	    
		// When
		Throwable throwable = catchThrowable(() -> userController.createUser(userDto));
		
		// Then 1
		then(throwable)
//		.as("An IAE should be thrown if a city with ID is passed")
		.isExactlyInstanceOf(AccessDeniedException.class);
//		.as("Check that message contains the city name")
//		.hasMessageContaining(inputCity.getName());
		
		// Then 2 
//		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(() -> userController.createUser(user));
	}
	
	@WithMockUser(roles = "ADMIN")
	@Test
	void createUserSeccessfulWithAdminRole() {
		
		assertThatNoException().isThrownBy(() -> userController.createUser(userDto));
	}
	
	@WithAnonymousUser
	@Test
	void throwExceptionWhenCallingUpdateUserWithoutAuthentication() {
		
		Throwable throwable = catchThrowable(() -> userController.updateUser("pippo", userDto));
		then(throwable)
//		.as("An IAE should be thrown if a city with ID is passed")
		.isExactlyInstanceOf(AccessDeniedException.class);
	}
	
	@WithAnonymousUser
	@Test
	void whenCallingUpdateUserSkillsWithoutAuthenticationRedirectToLoginPage() throws Exception {
		
		// given
//        given(superHeroRepository.getSuperHero(2))
//                .willReturn(new SuperHero("Rob", "Mannon", "RobotMan"));
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("skill", "value1A");
        queryParams.add("skill_level", "2");
        queryParams.add("will_level", "3");
        queryParams.add("mentor", "true");
		
        // when
        MockHttpServletResponse response = mvc
        		.perform(patch("/users/{user}/skills","pippo").queryParams(queryParams))
                .andReturn().getResponse();

        // then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(response.getRedirectedUrl()).isEqualTo("http://127.0.0.1:8888/login");
		
	}

}
