package com.sinnerschrader.skillwill.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.editor.SanitizeEditor;
import com.sinnerschrader.skillwill.editor.SearchEditor;
import com.sinnerschrader.skillwill.editor.SubSkillsEditor;
import com.sinnerschrader.skillwill.exception.SkillNotFoundException;
import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.SkillService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.WebDataBinder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(controllers = SkillController.class) // bean vero
@AutoConfigureJsonTesters
@Import(MyWebSecurityConfig.class)
public class SkillControllerTest2 {

	@Autowired
	private MockMvc mvc;

	// This object will be initialized thanks to @AutoConfigureJsonTesters
	@Autowired
	private JacksonTester<Skill> jsonSkill; // produce i json, che tipo di json? Skill

	@MockBean
	private SkillService skillService;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private UserDetailsService userDetailsService;

	private List<Skill> skillsInMemory;

	private ObjectMapper objectMapper;

	@BeforeEach
    public void setup() throws IOException, JSONException {

		objectMapper = new ObjectMapper();

    	// Carica il file JSON dalla directory delle risorse dei test
        ClassPathResource cpr = new ClassPathResource("mockdata/skills.json");
        byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
        String data = new String(bdata, StandardCharsets.UTF_8);

        assertThat(data).isNotBlank();
        assertThat(objectMapper).isNotNull();
        // Ora puoi utilizzare il contenuto del file 'data' come desideri

        JSONArray skillsJson = new JSONArray(data);
        int skillsQty = skillsJson.length();
        skillsInMemory = new ArrayList<Skill>(skillsQty);

        for (int i = 0; i < skillsQty; i++) {

			// recupero la singola skill
			JSONObject skillJson = skillsJson.getJSONObject(i);

			// Skill json -> java
			Skill skill = new Skill(skillJson.getString("name"));
			skill.setHidden(skillJson.getBoolean("hidden"));
			JSONArray subskillsJsonArray = skillJson.getJSONArray("subskills");

			for (int j = 0; j < subskillsJsonArray.length(); j++) {
				skill.addSubSkillName(subskillsJsonArray.getString(j));
			}
			skillsInMemory.add(skill);
		}

	}

	private Skill getSkillInMemory(String name) {

    	return skillsInMemory.stream().filter(skill -> skill.getName().equalsIgnoreCase(name)).findFirst().orElseThrow();
    }

	private Skill getRandomSkillInMemory() {
	    if (skillsInMemory.isEmpty()) {
	        throw new NoSuchElementException("La lista di competenze è vuota");
	    }
	    Random rand = new Random();
	    int randomIndex = rand.nextInt(skillsInMemory.size());

	    return skillsInMemory.get(randomIndex);
	}

	@Test
	@WithAnonymousUser
	@DisplayName("Get skill by anonymous user")
	public void getSkill() throws Exception {

		// given
		Skill skill = getRandomSkillInMemory();
		given(skillService.getSkillByName("java")).willReturn(skill);

		// when
		MockHttpServletResponse response = mvc
				.perform(get("/skills/{skill}", "java").accept(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
				.andReturn().getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonSkill.write(skill).getJson());
	}

	@Test
	@DisplayName("Test of data binding")
	public void initBinder() {

		// given
		String name = "Super Java!";
		String search = "Ja va, Java Script, PyTHon  ";
		String subSkills = "J aVa  , Ja va Scr IPT , R u B y ";

		WebDataBinder webDataBinder = new WebDataBinder(null); // contenitore vuoto
		webDataBinder.registerCustomEditor(String.class, new SanitizeEditor()); // name
		webDataBinder.registerCustomEditor(List.class, new SearchEditor()); // search
		webDataBinder.registerCustomEditor(Set.class, new SubSkillsEditor()); // subskills

		// when
		String actualName = (String) webDataBinder.convertIfNecessary(name, String.class);
		List<String> actualSearch = (List<String>) webDataBinder.convertIfNecessary(search, List.class);
		Set<String> actualSubSkills = (Set<String>) webDataBinder.convertIfNecessary(subSkills, Set.class);

		// then
		assertThat(actualName).isEqualTo("SuperJava"); // Add this assert to check the value
		assertThat(actualSearch).containsSequence("Java", "Javascript", "Python");
		assertThat(actualSubSkills).containsSequence("Java", "Javascript", "Ruby");
	}

	@Test
	@WithMockUser(roles = { "ADMIN" })
	@DisplayName("Create skill by admin")
	public void createSkillWithRoleAdmin() throws Exception {

		// given
		String nameSkill = "super Java!";
		String descriptionSkill = "description of super java";
		String isHidden = "false";
		String subSkills = "subSkill1, subSkill2, subSkill3";

		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("name", nameSkill);
		params.add("description", descriptionSkill);
		params.add("hidden", isHidden);
		params.add("subSkills", subSkills);

		// when
		doNothing().when(this.skillService).createSkill(nameSkill, descriptionSkill, false,
				Set.of(subSkills.split("//s*,//s*")));

		MockHttpServletResponse response = mvc
				.perform(post("/skills").params(params).accept(MediaType.APPLICATION_JSON_VALUE)).andDo(print())
				.andReturn().getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.value());
	}

  @Test
  @WithMockUser(roles = { "ADMIN" })
  @DisplayName("Update skill by admin")
  public void updateSkillWithRoleAdmin() throws Exception {

    // given
    String name = "java";
    String newName = "java plus";
    String description = "description of java plus";
    String isHidden = "false";
    String subSkills = "subSkill1, subSkill2, subSkill3";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("newName", newName);
    params.add("description", description);
    params.add("hidden", isHidden);
    params.add("subSkills", subSkills);

    // when
    doNothing().when(this.skillService).updateSkill(name, newName, description, Boolean.valueOf(isHidden), Arrays.stream(subSkills.split("\\s*,\\s*")).collect(Collectors.toSet()));

    MockHttpServletResponse response = mvc
      .perform(put("/skills/{skill}", name)
        .params(params)
        .accept(MediaType.APPLICATION_JSON_VALUE))
        .andDo(print())
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    assertThat(response.getRedirectedUrl()).isIn("http://127.0.0.1:8888/login", "http://localhost:8888/login");
  }

  @Test
  @WithAnonymousUser
  @DisplayName("Try to Update skill by anonumous user")
  public void redirectToLoginPageUpdatingSkillWithAnonymousUser() throws Exception {

    // given
    String name = "java";
    String newName = "java plus";
    String description = "description of java plus";
    String isHidden = "false";
    String subSkills = "subSkill1, subSkill2, subSkill3";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
    params.add("newName", newName);
    params.add("description", description);
    params.add("hidden", isHidden);
    params.add("subSkills", subSkills);

    // when
    doNothing().when(this.skillService).updateSkill(name, newName, description, Boolean.valueOf(isHidden), Arrays.stream(subSkills.split("\\s*,\\s*")).collect(Collectors.toSet()));

    MockHttpServletResponse response = mvc
      .perform(put("/skills/{skill}", name)
        .params(params)
        .accept(MediaType.APPLICATION_JSON_VALUE))
      .andDo(print())
      .andReturn().getResponse();

    // then
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
    assertThat(response.getRedirectedUrl()).isIn();
  }

	@Test
	@WithAnonymousUser
	@DisplayName("Retrieve skill in json format by anonymous user")
	public void retrieveSkillWhenExists() throws Exception {

		// given
		String name = "java";
		Skill skill = getSkillInMemory(name);
		given(skillService.getSkillByName(name)).willReturn(skill);

		// when
		MockHttpServletResponse response = mvc.perform(get("/skills/{parameter}", name).accept(APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn().getResponse();

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    	assertThat(response.getContentAsString()).isEqualTo(jsonSkill.write(skill).getJson());
	}

	@Test
	@WithAnonymousUser
	@DisplayName("Throw exception if you try getting not present skill")
    public void throwSkillNotFoundExceptionIfSkillDoesntExist() throws Exception {

    	// given
    	String name = "Blockchain";
    	given(skillService.getSkillByName(name)).willThrow(new SkillNotFoundException("Skill not found:" + name));

    	// when
    	MockHttpServletResponse response = mvc.perform(get("/skills/{parameter}", name).accept(APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn().getResponse();
    	String detail = objectMapper.readTree(response.getContentAsString()).get("detail").asText();

    	// then
    	assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    	assertThat(detail).isEqualTo("Skill not found:" + name);
	}

	@Test
	@WithAnonymousUser
	public void getSkillsByAnonymousUser() throws Exception {

		// given
		String search = null;
		boolean exclude_hidden = true;
		int count = 0;
		given(skillService.findSkills(search, exclude_hidden, count)).willReturn(skillsInMemory);

		// when
		MockHttpServletResponse response = mvc.perform(get("/skills").accept(APPLICATION_JSON)).andDo(MockMvcResultHandlers.print()).andReturn().getResponse();
		String jsonListString = response.getContentAsString();
		List<Skill> skills = objectMapper.readValue(jsonListString, new TypeReference<List<Skill>>(){});

		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(skills).isEqualTo(skillsInMemory);
	}

}
