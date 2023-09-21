package com.sinnerschrader.skillwill.domain.skill;

import com.sinnerschrader.skillwill.dto.SuggestionSkillDto;

import lombok.Builder;

/**
 * A suggestable skill used by Skill
 */
@Builder
public class SuggestionSkill {

  private String name;

  private int count;

  public SuggestionSkill(String name, int count) {
    this.name = name;
    this.count = count;
  }

  public SuggestionSkill(String name) {
    this(name, 0);
  }

  public SuggestionSkill() {
    this(null, 0);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCount() {
    return this.count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public void incrementCount() {
    this.count += 1;
  }

  public void incrementCount(int add) {
    this.count += add;
  }
  
  public SuggestionSkillDto toDto() {
	  
	  return SuggestionSkillDto.builder()
			  .name(name)
			  .count(count)
			  .build();
  }
  public static SuggestionSkill fromDto(SuggestionSkillDto dto) {
      return SuggestionSkill.builder()
                            .name(dto.name())
                            .count(dto.count())
                            .build();
  }

}
