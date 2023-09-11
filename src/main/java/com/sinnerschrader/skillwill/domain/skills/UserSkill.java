package com.sinnerschrader.skillwill.domain.skills;

import org.json.JSONObject;
import org.springframework.data.annotation.Id;

import com.sinnerschrader.skillwill.dto.UserSkillDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A skill owned by a person includes name, skill level and will level
 *
 * @author torree
 */
@Data
@EqualsAndHashCode(of = "name")
@AllArgsConstructor
@Builder
public class UserSkill {

	@Id
	private String name;

	private int skillLevel;

	private int willLevel;

	private boolean hidden;

	private boolean mentor;

  public JSONObject toJSON() {
    var json = new JSONObject();
    json.put("name", this.name);
    json.put("skillLevel", this.skillLevel);
    json.put("willLevel", this.willLevel);
    json.put("mentor", this.mentor);
    return json;
  }
  
  public UserSkillDto toDto() {
	  
	  return UserSkillDto.builder()
			  .name(name)
			  .skillLevel(skillLevel)
			  .willLevel(willLevel)
			  .hidden(hidden)
			  .mentor(mentor)
			  .build();
	  
  }
  
  public static UserSkill createUserSkill(UserSkillDto userSkillDto) {
	  
	  String name = userSkillDto.name();
	  int skillLevel = userSkillDto.skillLevel();
	  int willLevel = userSkillDto.willLevel();
	  boolean hidden = userSkillDto.hidden();
	  boolean mentor = userSkillDto.mentor();
	  
	  return UserSkill.builder()
			  .name(name)
			  .skillLevel(skillLevel)
			  .willLevel(willLevel)
			  .hidden(hidden)
			  .mentor(mentor)
			  .build();
  }

}
