package com.sinnerschrader.skillwill.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.exception.DuplicateSkillException;
import com.sinnerschrader.skillwill.exception.SkillNotFoundException;
import com.sinnerschrader.skillwill.mock.MockData;
import com.sinnerschrader.skillwill.repository.UserRepository;
import com.sinnerschrader.skillwill.service.SkillService;

@ContextConfiguration
@DataMongoTest
class SkillServiceIntegrationTestWithRepositories {

	@TestConfiguration
	static class ApplicationContext {

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
	private UserRepository userRepository;

	@Autowired
	private SkillService skillService;

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
	
	@Test
	@DisplayName("Delete existing skill and migrate to other existing skill")
	void deleteExistingSkillWithMigrationUsers() {
		
		// given
		String from = "JPA";
		String migrateTo = "Java";
		Integer usersWithJustFromSkill = userRepository.countUsersWithSkillAndWithoutMigrateSkill(from, migrateTo); // numberOfUsersWithJPAWithoutJava 190
		Integer usersWithJustMigrateToSkill = userRepository.countUsersWithSkillAndWithoutMigrateSkill(migrateTo, from); // numbersOfUsersWithJavaWithoutJPA 
		Integer usersWithJPAandJava = userRepository.countUsersWith2Skill(from, migrateTo);
		
		// when
		Throwable throwable = catchThrowable(() -> skillService.deleteSkill(from, migrateTo));
		Integer newUsersWithJustFromSkill = userRepository.countBySkillId(from); // deve essere 0
		Integer newUsersWithMigrateToSkill = userRepository.countBySkillId(migrateTo); // deve essere 190 + (quelli che avevano prima java e  anche java + JPA )
		
		// then
		assertThat(throwable).doesNotThrowAnyException();
		assertThat(newUsersWithJustFromSkill).isNull();
		assertThat(newUsersWithMigrateToSkill).isEqualTo(usersWithJustFromSkill + usersWithJustMigrateToSkill + usersWithJPAandJava);
	}
	
	@Test
	void deleteExistingSkillWithoutMigrationUsers() {
		
		//given
		String name = "Solidity";
		Integer usersWithNewSkillBeforeCreate = userRepository.countBySkillId(name);
		
		// when
		
		Throwable getSkillBeforeCreate = catchThrowable(() -> skillService.getSkillByName(name));
		Throwable createSkillThrowable = catchThrowable(() -> skillService.createSkill(name, "description of new Skill", false, Collections.emptySet()));
		Skill newlySkill = skillService.getSkillByName(name);
		Throwable deleteSkillThrowable = catchThrowable(() -> skillService.deleteSkill(name, null));
		Integer usersWithNewSkillAfterCreate = userRepository.countBySkillId(name);
		
		// then
		assertThat(usersWithNewSkillBeforeCreate).isNull();;
		assertThat(getSkillBeforeCreate).isExactlyInstanceOf(SkillNotFoundException.class);
		assertThat(createSkillThrowable).doesNotThrowAnyException();
		assertThat(newlySkill).isNotNull();
		assertThat(newlySkill).isEqualTo(new Skill(name, "description of new Skill", null, false, null));
		assertThat(deleteSkillThrowable).doesNotThrowAnyException();
		assertThat(usersWithNewSkillAfterCreate).isNull();
	}
	
	

}
