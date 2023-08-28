package com.sinnerschrader.skillwill.domain.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.sinnerschrader.skillwill.domain.skills.Skill;
import com.sinnerschrader.skillwill.domain.skills.UserSkill;
import com.sinnerschrader.skillwill.exceptions.SkillNotFoundException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class holding all information about a person
 *
 * @author torree
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {

	@Id
	private String username;
	
	private String password;
	
	private List<UserSkill> skills;

	private String ldapDN;

	@Transient
	private FitnessScore fitnessScore;

	@Version
	private Long version;

	// LDAP Details will be updated regularly
	private UserLdapDetails ldapDetails;

	public UserDetailsImpl(String username, String password) {
		this.username = username;
		this.password = password;
		this.skills = new ArrayList<>();
		this.ldapDetails = null;
		this.fitnessScore = null;
		this.ldapDN = null;
	}
	
	public UserDetailsImpl(String username) {
		this.username = username;
		this.password = null;
		this.skills = new ArrayList<>();
		this.ldapDetails = null;
		this.fitnessScore = null;
		this.ldapDN = null;
	}

	public List<UserSkill> getSkills(boolean excludeHidden) {
		return this.skills.stream().filter(skill -> !excludeHidden || !skill.isHidden()).collect(Collectors.toList());
	}

	public UserSkill getSkill(String name, boolean excludeHidden) {
		return this.skills.stream().filter(s -> s.getName().equals(name)).filter(s -> !excludeHidden || !s.isHidden()).findFirst().orElse(null);
	}

	public boolean hasSkill(String skill) {
		return this.getSkill(skill, true) != null;
	}

	public void addUpdateSkill(String name, int skillLevel, int willLevel, boolean hidden, boolean mentor) {
		try {
			removeSkill(name);
		} catch (SkillNotFoundException e) {
			// user doesn't have skill yet -> add new skill
		}
		this.skills.add(new UserSkill(name, skillLevel, willLevel, hidden, mentor));
	}

	public void removeSkill(String name) throws SkillNotFoundException {
		var toRemove = skills.stream().filter(s -> s.getName().equals(name)).findAny()
				.orElseThrow(() -> new SkillNotFoundException("user does not have skill"));
		skills.remove(toRemove);
	}

	public void setFitnessScore(Collection<Skill> searchedSkills, FitnessScoreProperties props) {
		this.fitnessScore = new FitnessScore(this, searchedSkills, props);
	}

	public double getFitnessScoreValue() {
		
		if (this.fitnessScore == null) {
			throw new IllegalStateException("no fitness score set");
		}

		return this.fitnessScore.getValue();
	}

	public JSONObject toJSON() {
		var json = new JSONObject();
		json.put("id", this.username);

		if (this.ldapDetails != null) {
			json.put("firstName", ldapDetails.getFirstName());
			json.put("lastName", ldapDetails.getLastName());
			json.put("mail", ldapDetails.getMail());
			json.put("phone", ldapDetails.getPhone());
			json.put("location", ldapDetails.getLocation());
			json.put("title", ldapDetails.getTitle());
			json.put("company", ldapDetails.getCompany());
			json.put("role", ldapDetails.getRole());
		}

		if (this.fitnessScore != null) {
			json.put("fitness", this.fitnessScore.getValue());
		}

		var skillsArr = new JSONArray();
		this.skills.stream().filter(s -> !s.isHidden()).sorted(Comparator.comparing(UserSkill::getName))
				.map(UserSkill::toJSON).forEach(skillsArr::put);

		json.put("skills", skillsArr);
		return json;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		return Arrays.asList(new SimpleGrantedAuthority("USER"));
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
