package com.sinnerschrader.skillwill.dto;

import lombok.Builder;

@Builder
public record UserSkillDto(
  String name,
  int skillLevel,
  int willLevel,
  boolean hidden,
  boolean mentor
) {

}
