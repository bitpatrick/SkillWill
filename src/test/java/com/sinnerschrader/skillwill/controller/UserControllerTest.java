package com.sinnerschrader.skillwill.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.config.JwtUtils;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.SkillSearchResult;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.SessionService;
import com.sinnerschrader.skillwill.service.SkillService;
import com.sinnerschrader.skillwill.service.UserService;

//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.HttpStatus;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.sinnerschrader.skillwill.domain.skill.Skill;
//import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
//import com.sinnerschrader.skillwill.misc.EmbeddedLdap;
//import com.sinnerschrader.skillwill.repository.SessionRepository;
//import com.sinnerschrader.skillwill.repository.SkillRepository;
//import com.sinnerschrader.skillwill.repository.UserRepository;
//import com.sinnerschrader.skillwill.service.LdapService;
//import com.sinnerschrader.skillwill.session.Session;
//import com.unboundid.ldap.sdk.LDAPException;

/**
 * Integration test for UserController
 *
 * @author torree
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest
@WebMvcTest(controllers = UserController.class)
//@AutoConfigureMockMvc(addFilters = false)
@Import(MyWebSecurityConfig.class)
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
	

	private UserDto userDto;

	@BeforeEach
	void setup() {

		userDto = UserDto.builder().username("pippo").password("pwd").build();
	}


	@WithMockUser(roles = "ADMIN")
	@Test
	void testGetUsersReturnJson() throws Exception {
		
		//given
		User user1= User.builder().username("pippo").build();
		User user2= User.builder().username("anna").build();
		User user3= User.builder().username("mario").build();
		
		List<User> users = List.of(user2,user1,user3);
		
		//parametri da passare nella request
		String skills = "JAVA,SQL,PHP";
		String company = "Leonardo";
		String location = "Roma";
		
		String skillName1 = "JAVA";
		String skillName2 = "SQL";
		String skillName3 = "PHP";
		
		List<String> skillNames= List.of(skillName1,skillName2,skillName3);
		
		
		Skill skill1 = new Skill(skillName1);
		Skill skill2 = new Skill(skillName2);
		Skill skill3 = new Skill(skillName3);
		
		Map<String, Skill> map = new HashMap<String, Skill>();
		map.put(skillName1, skill1);
		map.put(skillName2, skill2);
		map.put(skillName3, skill3);
		
		Set<String> set = new HashSet<String>();
		set.add(skillName1);
		
		SkillSearchResult skillSearchResult = new SkillSearchResult(map, set);
		
		given(skillService.searchSkillsByNames(skillNames, true)).willReturn(skillSearchResult);
		given(userService.getUsers(skillSearchResult, company, location)).willReturn(users);
		doNothing().when(skillService).registerSkillSearch(skillSearchResult.mappedSkills());

		
		//devo passare ora i parametri da me voluti
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("skills", skills);
		params.add("location", location);
		params.add("company", company);
		
		//when
		MockHttpServletResponse response =	mvc.perform(get("/users").params(params).accept(MediaType.APPLICATION_JSON_VALUE))
		 		.andDo(print())
			    .andReturn()
			    .getResponse();
		//then
		 		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
				assertThat(response.getContentAsString());	
				
				// Verifica che l'header Content-Type sia impostato su application/json
			    assertThat(response.getHeader("Content-Type")).isEqualTo(MediaType.APPLICATION_JSON_VALUE);

			    // Verifica che il contenuto della response sia un JSON valido
			    ObjectMapper objectMapper = new ObjectMapper();
			    try {
			        objectMapper.readTree(response.getContentAsString());
			    } catch (JsonProcessingException e) {
			        fail("La response non è in formato JSON valido.");
			    }
				
	}
	
	@WithMockUser(roles = "WRONG")
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
		MockHttpServletResponse response = mvc.perform(patch("/users/{user}/skills", "pippo").queryParams(queryParams))
				.andReturn().getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
		assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");

	}

	@WithMockUser(roles = "USER")
	@Test
	void throwExceptionWhenCallingDeleteUserWithoutAdminRole() {
		
		Throwable throwable = catchThrowable(() -> userController.deleteUser("pippo"));
		then(throwable)
				.isExactlyInstanceOf(AccessDeniedException.class);
		
	}

	@WithMockUser(roles = "ADMIN")
	@Test
	void deleteUserSeccessfulWithAdminRole() {
		
		assertThatNoException().isThrownBy(() -> userController.deleteUser("pippo"));
		
	}

	
	
	
	
	


	

//  @Autowired
//  private UserController userController;
//
//  @Autowired
//  private UserRepository userRepo;
//
//  @Autowired
//  private SkillRepository skillRepo;
//
//  @Autowired
//  private SessionRepository sessionRepo;
//
//  @Autowired
//  private EmbeddedLdap embeddedLdap;
//
//  @Autowired
//  private LdapService ldapService;
//
//  @Before
//  public void setUp() throws LDAPException {
//    embeddedLdap.reset();
//    skillRepo.deleteAll();
//    userRepo.deleteAll();
//    sessionRepo.deleteAll();
//
//    skillRepo.insert(new Skill("Java"));
//    skillRepo.insert(new Skill("hidden", "", new ArrayList<>(), true, new HashSet<>()));
//
//    var userUser = new UserDetailsImpl("aaaaaa");
//    userUser.addUpdateSkill("Java", 2, 3, false, false);
//    userUser.addUpdateSkill("hidden", 0, 1, true, false);
//    userRepo.insert(userUser);
//
//    var adminUser = new UserDetailsImpl("bbbbbb");
//    userRepo.insert(adminUser);
//
//    ldapService.syncUsers(userRepo.findAll(), true);
//
//    var userSession = new Session("YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    sessionRepo.insert(userSession);
//
//    var adminSession = new Session("YmJiLmJiYkBleGFtcGxlLmNvbQ==|foo|bar");
//    sessionRepo.insert(adminSession);
//  }
//
//  @Test
//  public void testGetUserValid() throws JSONException {
//    var response = userController.getUser("aaaaaa");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//
//    assertTrue(new JSONObject(response.getBody()).has("id"));
//    assertTrue(new JSONObject(response.getBody()).get("id").equals("aaaaaa"));
//
//    assertTrue(new JSONObject(response.getBody()).has("firstName"));
//    assertTrue(new JSONObject(response.getBody()).get("firstName").equals("Fooberius"));
//
//    assertTrue(new JSONObject(response.getBody()).has("lastName"));
//    assertTrue(new JSONObject(response.getBody()).get("lastName").equals("Barblub"));
//  }
//
//  @Test
//  public void testGetUserInvalid() {
//    assertEquals(HttpStatus.NOT_FOUND, userController.getUser("barfoo").getStatusCode());
//  }
//
//  @Test
//  public void testGetUsersValid() throws JSONException {
//    var response = userController.getUsers("Java", null, "Hamburg");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals("Java", new JSONObject(response.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertTrue(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersHideHidden() throws JSONException {
//    var response = userController.getUsers("Java", null, "");
//    var skillJsonArray = new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getJSONArray("skills");
//    assertEquals(1, skillJsonArray.length());
//    assertEquals("Java", skillJsonArray.getJSONObject(0).getString("name"));
//  }
//
//  @Test
//  public void testGetUsersSkillsEmpty() throws JSONException {
//    var response = userController.getUsers("", null, "Hamburg");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertFalse(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals(2, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertFalse(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersSkillsNull() throws JSONException {
//    var response = userController.getUsers(null, null, "Hamburg");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals(2, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertFalse(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersSkillUnknownOnly() throws JSONException {
//    var response = userController.getUsers("Unknown, More unknown", null, "");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals(0, new JSONObject(response.getBody()).getJSONArray("searched").length());
//    assertTrue(new JSONObject(response.getBody()).has("results"));
//     assertEquals(0, new JSONObject(response.getBody()).getJSONArray("results").length());
//  }
//
//  @Test
//  public void testGetUsersSkillUnknown() throws JSONException {
//    var response = userController.getUsers("Unknown,Java", null, "");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("searched").length());
//    assertEquals("Java", new JSONObject(response.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
//    assertTrue(new JSONObject(response.getBody()).has("results"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//  }
//
//  @Test
//  public void testGetUsersNoFitnessInEmptySearch() throws JSONException {
//    var response = userController.getUsers("", null, "Hamburg");
//    assertFalse(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersLocationEmpty() throws JSONException {
//    var response = userController.getUsers("Java", null, "");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals("Java", new JSONObject(response.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertTrue(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersSkillsEmptyLocationEmpty() throws JSONException {
//    var response = userController.getUsers("", null, "");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals(2, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertFalse(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersIgnoreSkillCase() throws JSONException {
//    var response = userController.getUsers("JaVa", null, "Hamburg");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals("Java", new JSONObject(response.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertTrue(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersIgnoreNonAlphanumerics() throws JSONException {
//    var response = userController.getUsers("j#a)_V®a", null, "Hamburg");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertTrue(new JSONObject(response.getBody()).has("searched"));
//    assertEquals("Java", new JSONObject(response.getBody()).getJSONArray("searched").getJSONObject(0).getString("found"));
//    assertEquals(1, new JSONObject(response.getBody()).getJSONArray("results").length());
//    assertEquals("aaaaaa", new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).getString("id"));
//    assertTrue(new JSONObject(response.getBody()).getJSONArray("results").getJSONObject(0).has("fitness"));
//  }
//
//  @Test
//  public void testGetUsersLocationUnknown() throws JSONException {
//    var response = userController.getUsers("Java", null, "IAmUnknown");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(0, new JSONObject(response.getBody()).getJSONArray("results").length());
//  }
//
//  @Test
//  public void testModifySkillsValid() {
//    var response = userController.updateSkills("aaaaaa", "Java", "3", "0", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(0, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsLevelsZero() {
//    var response = userController.updateSkills("aaaaaa", "Java", "0", "0", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test
//  public void testModifySkillsLevelOverMax() {
//    var response = userController.updateSkills("aaaaaa", "Java", "0", "4", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test
//  public void testModifySkillsSessionInvalid() {
//    var response = userController.updateSkills("aaaaaa", "Java", "0", "0", false, "InvalidSession");
//    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//    assertEquals(2, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsUserUnknown() {
//    sessionRepo.deleteAll();
//    var session = new Session("2342");
//    sessionRepo.insert(session);
//
//    var response = userController.updateSkills("IAmUnknown", "Java", "0", "0", false, "2342");
//    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//    assertEquals(2, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsSkillUnknown() {
//    var response = userController.updateSkills("aaaaaa", "UnknownSkill", "0", "0", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    assertEquals(2, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsSkillLevelOutOfRange() {
//    var response = userController.updateSkills("aaaaaa", "Java", "5", "0", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    assertEquals(2, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsWillLevelOutOfRange() {
//    var response = userController.updateSkills("aaaaaa", "Java", "0", "5", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//    assertEquals(2, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getSkillLevel());
//    assertEquals(3, userRepo.findByUsernameIgnoreCase("aaaaaa").getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testModifySkillsHidden() {
//    assertEquals(HttpStatus.BAD_REQUEST, userController.updateSkills("aaaaaa", "hidden", "0", "3", false, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar").getStatusCode());
//    assertNull(userRepo.findByUsernameIgnoreCase("aaaaaa").getSkill("hidden", true));
//  }
//
//  @Test
//  public void testModifiyMentorExisitingSkill() throws JSONException {
//    assertEquals("Java", new JSONObject(userController.getUser("aaaaaa").getBody())
//      .getJSONArray("skills")
//      .getJSONObject(0)
//      .getString("name"));
//
//    assertFalse(new JSONObject(userController.getUser("aaaaaa").getBody())
//      .getJSONArray("skills")
//      .getJSONObject(0)
//      .getBoolean("mentor"));
//
//    assertEquals(HttpStatus.OK, userController.updateSkills("aaaaaa", "Java", "3", "3", true, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar").getStatusCode());
//
//    assertTrue(new JSONObject(userController.getUser("aaaaaa").getBody())
//      .getJSONArray("skills")
//      .getJSONObject(0)
//      .getBoolean("mentor"));
//  }
//
//  @Test
//  public void testSetMentorNewSkill() throws JSONException {
//    assertEquals(HttpStatus.OK, userController.removeSkill("aaaaaa", "Java", "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar").getStatusCode());
//
//    assertEquals(0, new JSONObject(userController.getUser("aaaaaa").getBody())
//      .getJSONArray("skills")
//      .length());
//
//    assertEquals(HttpStatus.OK, userController.updateSkills("aaaaaa", "Java", "3", "3", true, "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar").getStatusCode());
//
//    assertTrue(new JSONObject(userController.getUser("aaaaaa").getBody())
//      .getJSONArray("skills")
//      .getJSONObject(0)
//      .getBoolean("mentor"));
//  }
//
//  @Test
//  public void testRemoveSkill() {
//    var response = userController.removeSkill("aaaaaa", "Java", "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.OK, response.getStatusCode());
//  }
//
//  @Test
//  public void testRemoveSkillSkillUnknown() {
//    var response = userController.removeSkill("aaaaaa", "UNKNOWN", "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }
//
//  @Test
//  public void testRemoveSkillUserUnknown() {
//    var response = userController.removeSkill("IAmUnknown", "Java", "YWFhLmFhYUBleGFtcGxlLmNvbQ==|foo|bar");
//    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//  }
//
//  @Test
//  public void testRemoveSkillUserForbidden() {
//    var response = userController.removeSkill("aaaaaa", "Java", "IAmUnknown");
//    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//  }
//
//  @Test
//  public void testGetSimilarUser() throws JSONException {
//    var user1 = new UserDetailsImpl("abc");
//    user1.addUpdateSkill("Java", 1, 2, false, false);
//    user1.addUpdateSkill(".NET", 3, 2, false, false);
//    user1.addUpdateSkill("Text", 1, 3, false, false);
//    userRepo.insert(user1);
//
//    var user2 = new UserDetailsImpl("def");
//    user2.addUpdateSkill("Java", 3, 2, false, false);
//    userRepo.insert(user2);
//
//    var user3 = new UserDetailsImpl("ghi");
//    user3.addUpdateSkill("Java", 1, 0, false, false);
//    user3.addUpdateSkill(".NET", 3, 2, false, false);
//    userRepo.insert(user3);
//
//    var response = userController.getSimilar("abc", 1);
//    assertEquals(1, new JSONArray(response.getBody()).length());
//    assertEquals("ghi", new JSONArray(response.getBody()).getJSONObject(0).getString("id"));
//  }
//
//  @Test
//  public void testGetSimilarUserNotFound() {
//    var response = userController.getSimilar("IAmUnknown", 42);
//    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
//  }
//
//  @Test
//  public void testGetSimilarUserCountNegative() {
//    var response = userController.getSimilar("aaaaaa", -1);
//    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
//  }

}
