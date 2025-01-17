package com.sinnerschrader.skillwill.domain.user;

import com.sinnerschrader.skillwill.domain.skill.Skill;
import com.sinnerschrader.skillwill.domain.skill.UserSkill;
import com.sinnerschrader.skillwill.dto.FitnessScoreDto;
import com.sinnerschrader.skillwill.dto.UserDto;
import com.sinnerschrader.skillwill.dto.UserLdapDetailsDto;
import com.sinnerschrader.skillwill.dto.UserSkillDto;
import com.sinnerschrader.skillwill.exception.SkillNotFoundException;
import lombok.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.Version;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class holding all information about a person
 *
 * @author torree
 */
@Data
@Builder
@EqualsAndHashCode(of = {"username"})
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {

	@Id
	private String username;
	
	private String email;

	private String password;

	private Set<UserSkill> skills;

	private String ldapDN;
	
	private List<GrantedAuthority> authorities;

	@Transient
	private FitnessScore fitnessScore;
	
	@Transient
	private boolean accountNonLocked = true;
	
	@Transient
	private boolean accountNonExpired = true;
	
	@Transient
	private boolean credentialsNonExpired = true;
	
	@Transient
	private boolean enabled = true;
	
	@Transient
	private LocalDateTime expirationTime;

	@Version
	private Long version;

	// LDAP Details will be updated regularly
	private UserLdapDetails ldapDetails;
	
	public static User createUser(UserDto userDto) {

		String username = userDto.username();
		String password = userDto.password();
		String ldapDN = userDto.ldapDN();
		Long version = userDto.version();
		List<GrantedAuthority> authorities = userDto.authorities().stream()
													.map(role -> {

                            return role.startsWith ("ROLE_") ? (GrantedAuthority) new SimpleGrantedAuthority(role) : (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role);

                          })
													.toList();
		
		return User.builder()
				.username(username)
				.password(password)
				.ldapDN(ldapDN)
				.version(version)
				.authorities(authorities)
				.build();
		
	}
	
	public void addSkill(String skillName, int skillLevel, int willLevel, boolean hidde, boolean mentor) {
		
		checkNullableUserSkills();
		
		UserSkill userSkill = new UserSkill(skillName, skillLevel, willLevel, hidde, mentor);
		skills.add(userSkill);
	}
	
	public void addSkills(String... skillNames) {
		
		checkNullableUserSkills();
		
		for (String skillName : skillNames) {
			UserSkill userSkill = new UserSkill(skillName);
			skills.add(userSkill);		
		}
	}
	
	private void checkNullableUserSkills() {
		
		if ( skills == null ) {
			skills = new HashSet<UserSkill>();
		}
	}
	
	public void update(UserDto userDto) {

		this.password = (userDto.password() != null) ? userDto.password() : this.password;
    this.authorities = (userDto.authorities() != null) ?  userDto.authorities().stream().map(auth -> (GrantedAuthority) new SimpleGrantedAuthority(auth)).toList() : this.authorities;
    this.ldapDN = (userDto.ldapDN() != null) ? userDto.ldapDN() : this.ldapDN;
    this.version = (userDto.version() != null) ? userDto.version() : this.version;
    this.skills = ( userDto.skills() != null ) ? userDto.skills().stream().map(UserSkill::createUserSkill).collect(Collectors.toSet()) : this.skills;
    // TODO fitness score
	}
	
	public User(String username, String password, List<GrantedAuthority> authorities) {
		super();
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.skills = new HashSet<>();
		this.ldapDetails = null;
		this.fitnessScore = null;
		this.ldapDN = null;
	}

	public User(String username) {
		this.username = username;
		this.password = null;
		this.skills = new HashSet<>();
		this.ldapDetails = null;
		this.fitnessScore = null;
		this.ldapDN = null;
	}

	public List<UserSkill> getSkills(boolean excludeHidden) {
		return this.skills.stream().filter(skill -> !excludeHidden || !skill.isHidden()).collect(Collectors.toList());
	}

	public UserSkill getSkill(String name, boolean excludeHidden) {
		return this.skills.stream().filter(s -> s.getName().equals(name)).filter(s -> !excludeHidden || !s.isHidden())
				.findFirst().orElse(null);
	}

	public boolean hasSkill(String skill) {
		return this.getSkill(skill, true) != null;
	}

	public void updateSkill(String name, int skillLevel, int willLevel, boolean hidden, boolean mentor) {

    UserSkill newUserSkill = new UserSkill(name, skillLevel, willLevel, hidden, mentor);

    this.skills.remove(newUserSkill); // Rimuovi l'elemento esistente basandoti sul nome
    this.skills.add(newUserSkill);    // Aggiungi il nuovo elemento
	}

	public boolean removeSkill(Skill skill) throws SkillNotFoundException {

    UserSkill userSkillToDelete = new UserSkill(skill.getName());

		return this.skills.remove(userSkillToDelete);
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
		
		if(skills!=null) {
			this.skills.stream().filter(s -> !s.isHidden()).sorted(Comparator.comparing(UserSkill::getName)).map(UserSkill::toJSON).forEach(skillsArr::put);

		}

		json.put("skills", skillsArr);
		return json;
	}

	public UserDto toUserDto() {
		
		UserLdapDetailsDto userLdapDetailsDto = null;
		List<UserSkillDto> skills = null;
		FitnessScoreDto fitnessScoreDto = null;
		
		// create ldap
		if (this.ldapDetails != null) {
			
			String firstName= ldapDetails.getFirstName();
			String lastName= ldapDetails.getLastName();
			String mail=ldapDetails.getMail();
			String phone=ldapDetails.getPhone();
			String location=ldapDetails.getLocation();
			String title=ldapDetails.getTitle();
			String company=ldapDetails.getCompany();
			String role=ldapDetails.getRole().name();
			
			userLdapDetailsDto = UserLdapDetailsDto.builder()
				.firstName(firstName)
				.lastName(lastName)
				.mail(mail)
				.phone(phone)
				.location(location)
				.title(title)
				.company(company)
				.role(role)
				.build();
			
		}
		
		// create skills list
		if ( this.skills != null ) {
			skills = this.skills.stream().map(UserSkill::toDto).toList();
		}
		
		if ( this.fitnessScore != null  ) {
			fitnessScoreDto = this.fitnessScore.toDto();
		}
		
		return UserDto.builder()
				.username(username)
				.password(password)
				.authorities(Optional.ofNullable(authorities).orElse(Collections.emptyList()).stream().map(auth -> {

          String role = auth.getAuthority();
          return role.startsWith("ROLE_") ? role.substring(5) : role;

        }).toList())
				.skills(skills)
				.ldapDN(ldapDN)
				.fitnessScore(fitnessScoreDto)
				.version(version)
				.build();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;
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
		return this.accountNonExpired;
	}

	@Override
	public boolean isAccountNonLocked() {
		return this.accountNonLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return this.credentialsNonExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}
	
}
