package com.sinnerschrader.skillwill.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import com.sinnerschrader.skillwill.config.MyWebSecurityConfig;
import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.repositories.UserRepository;
import com.sinnerschrader.skillwill.services.SessionService;
import com.sinnerschrader.skillwill.services.SkillService;

@WebMvcTest(controllers = SkillController.class) // bean vero
@AutoConfigureJsonTesters
@Import(MyWebSecurityConfig.class)
class SkillControllerTest2 {
	
	@Autowired
	private MockMvc mvc;
	
	 // This object will be initialized thanks to @AutoConfigureJsonTesters
    @Autowired
    private JacksonTester<Skill> jsonSkill; // produce i json, che tipo di json? Skill
    
    @MockBean
    private SkillService skillService;

    @MockBean
    private SessionService sessionService;
    
    @MockBean
    private UserRepository userRepository;

	@Test
	void test() throws Exception {
		
		// given
		Skill java = new Skill("java"); // create skill
		given(skillService.getSkillByName("java")).willReturn(java); // dato che il metodo è finto dovrà tornare un oggetto invetato da me
		
		// when
		MockHttpServletResponse response = mvc
				.perform(
						get("/skills/{skill}", "java").accept(MediaType.APPLICATION_JSON_VALUE))
				.andReturn()
				.getResponse();
		
		// then
		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getContentAsString()).isEqualTo(jsonSkill.write(java).getJson());
		
	}

}
