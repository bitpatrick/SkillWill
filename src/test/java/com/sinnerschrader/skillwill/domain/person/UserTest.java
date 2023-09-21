package com.sinnerschrader.skillwill.domain.person;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.sinnerschrader.skillwill.domain.skill.UserSkill;
import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;

//import static org.junit.Assert.assertEquals;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import com.sinnerschrader.skillwill.domain.user.Role;
//import com.sinnerschrader.skillwill.domain.user.UserDetailsImpl;
//import com.sinnerschrader.skillwill.domain.user.UserLdapDetails;

/**
 * Partial unit tests for User
 *
 * @author torree
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest

public class UserTest {
	
	User user;
	User anonymousUser;
	
	Set<UserSkill> userSkills;
	
	@BeforeEach
	public void setup () {
		
		UserSkill userSkill1 = new UserSkill("Java", 1, 2, false, true);
		UserSkill userSkill2 = new UserSkill("JavaScript", 2, 3, true, false);
		UserSkill userSkill3 = new UserSkill("Python", 0, 1, true, true);
	    
		userSkills = Set.of(userSkill1, userSkill2, userSkill3);
		
		// create user
		user = User.builder()
				.username("pippo")
				.password("password") 
				.skills(userSkills)
				.ldapDN("ldapDN")
				.authorities(List.of((GrantedAuthority) new SimpleGrantedAuthority("USER")))
				.fitnessScore(null)
				.version(1l)
				.ldapDetails(null)
				.build();

		// create anonymous user
		anonymousUser = User.builder().build();
	}
	
	@Test
	public void test() {
	
		// given
		UserSkillDto expectedUserSkill1 = new UserSkillDto("Java", 1, 2, false, true);
		UserSkillDto expectedUserSkill2 = new UserSkillDto("JavaScript", 2, 3, true, false);
		UserSkillDto expectedUserSkill3 = new UserSkillDto("Python", 0, 1, true, true);
		
		UserDto expectedUser = UserDto.builder()
				.username("pippo")
				.password("password")
				.skills(List.of(expectedUserSkill1, expectedUserSkill2, expectedUserSkill3))
				.ldapDN("ldapDN")
				.authorities(List.of("USER"))
				.fitnessScore(null)
				.version(1l)
				.userLdapDto(null)
				.build();
				
		// when
		UserDto actualUser = user.toUserDto();
		
		// Then
		assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
	}
	
	@Test
	public void test2() {
		
		// given
		UserDto expectedUser = UserDto.builder().authorities(Collections.emptyList()).build();
		
		// when
		UserDto actualUser = anonymousUser.toUserDto();		
		
		// then
		assertThat(actualUser).usingRecursiveComparison().isEqualTo(expectedUser);
	}

//  private UserDetailsImpl userDetailsImpl;
//
//  @Before
//  public void init() {
//    userDetailsImpl = new UserDetailsImpl("foobar");
//    userDetailsImpl.addUpdateSkill("skillname", 2, 3, false, false);
//  }
//
//  @Test
//  public void testAddUpdateNewSkill() {
//    userDetailsImpl.addUpdateSkill("new skill", 2, 3, false, false);
//    assertEquals(2, userDetailsImpl.getSkills(true).size());
//  }
//
//  @Test
//  public void testAddUpdateKnownSkill() {
//    userDetailsImpl.addUpdateSkill("skillname", 0, 1, false, false);
//    assertEquals(1, userDetailsImpl.getSkills(true).size());
//    assertEquals(0, userDetailsImpl.getSkills(true).get(0).getSkillLevel());
//    assertEquals(1, userDetailsImpl.getSkills(true).get(0).getWillLevel());
//  }
//
//  @Test
//  public void testToJson() throws JSONException {
//    userDetailsImpl.setLdapDetails(
//        new UserLdapDetails(
//            "Fooberius",
//            "Barblub",
//            "fooberius.barblub@sinnerschrader.com",
//            "+49 666 666",
//            "Hamburg",
//            "Senior Web Unicorn",
//            "Firma",
//            Role.ADMIN
//        )
//    );
//    JSONObject obj = userDetailsImpl.toJSON();
//
//    assertEquals("foobar", obj.getString("id"));
//    assertEquals("ADMIN", obj.getString("role"));
//    assertEquals("Fooberius", obj.getString("firstName"));
//    assertEquals("Barblub", obj.getString("lastName"));
//    assertEquals("+49 666 666", obj.getString("phone"));
//    assertEquals("Senior Web Unicorn", obj.getString("title"));
//    assertEquals("Hamburg", obj.getString("location"));
//    assertEquals("fooberius.barblub@sinnerschrader.com", obj.getString("mail"));
//    assertEquals("skillname", obj.getJSONArray("skills").getJSONObject(0).getString("name"));
//    assertEquals(2, obj.getJSONArray("skills").getJSONObject(0).getInt("skillLevel"));
//    assertEquals(3, obj.getJSONArray("skills").getJSONObject(0).getInt("willLevel"));
//  }

}
