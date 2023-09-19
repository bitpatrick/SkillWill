package com.sinnerschrader.skillwill.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import com.sinnerschrader.skillwill.exceptions.DuplicateSkillException;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;
import com.sinnerschrader.skillwill.jobs.LdapSyncJob;
import com.sinnerschrader.skillwill.mock.MockData;
import com.sinnerschrader.skillwill.repositories.UserRepository;

@ContextConfiguration
@DataMongoTest
class SkillServiceTest {

	/*
	 * APPLICATION CONTEXT
	 */
	@TestConfiguration
	static class ImplTestContextConfiguration {

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
	private SkillService skillService;

	@MockBean
	private LdapSyncJob ldapSyncJob;
	
	@Test
	void createSkillWithRightSubSkills() {

		// given
		String skillName1 = "superJava";
		String skillName2 = "superJavaScript";
		String description = "Description of skill name";
		Set<String> subSkills = Set.of("Java");

		// when
		Throwable throwable1 = catchThrowable(() -> skillService.createSkill(skillName1, description, false, subSkills));
		Throwable throwable2 = catchThrowable(() -> skillService.createSkill(skillName2, description, true, subSkills));

		// then
		assertThat(throwable1).doesNotThrowAnyException();
		assertThat(throwable2).doesNotThrowAnyException();
	}

	@Test
	void throwExceptionWhenCreateSkillWithOneSubSkillNotPresent() {

		// given
		String skillName1 = "SuperPython";
		String description = "Description of skill name";
		Set<String> subSkills = Set.of("Java", "Python", "K");

		// when
		Throwable throwable = catchThrowable(() -> skillService.createSkill(skillName1, description, false, subSkills));

		// then
		assertThat(throwable).isExactlyInstanceOf(SkillNotFoundException.class);
	}

	@Test
	void throwExceptionWhenCreateAlreadySkillExist() {

		// given
		String skillName = "Java";
		String description = "Description of skill name";
		Set<String> subSkills = Set.of("Python", "Java", "Javascript");

		// when
		Throwable throwable1 = catchThrowable(() -> skillService.createSkill(skillName, description, false, subSkills));
		Throwable throwable2 = catchThrowable(() -> skillService.createSkill(skillName, description, true, subSkills));

		// then
		assertThat(throwable1).isExactlyInstanceOf(DuplicateSkillException.class);
		assertThat(throwable2).isExactlyInstanceOf(DuplicateSkillException.class);
	}
	
	@Test
	void createSkillWithoutSubskills() {
		
		// given
		String name = "SuperRuby";
		String description = "Description of skill name";
		Set<String> subSkills = Collections.emptySet();

		// when
		Throwable throwable = catchThrowable(() -> skillService.createSkill(name, description, true, subSkills));
		
		// then
		assertThat(throwable).doesNotThrowAnyException();
	}
	
	@Test
	void throwNullPointExceptionWhenCreateSkillWithNullSetSubskills() {
		
		// given
		String name = "SuperHTML";
		String description = "Description of skill name";
		Set<String> subSkills = null;

		// when
		Throwable throwable = catchThrowable(() -> skillService.createSkill(name, description, true, subSkills));
				
		// then
		assertThat(throwable).isExactlyInstanceOf(NullPointerException.class);
	}
	
	

}
