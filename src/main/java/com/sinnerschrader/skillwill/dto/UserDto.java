package com.sinnerschrader.skillwill.dto;

import lombok.Builder;
import lombok.ToString;

import java.util.List;

@Builder
public record UserDto(

  String username,
  String password,
  List<String> authorities,
  String ldapDN,
  Long version,
  List<UserSkillDto> skills,
  FitnessScoreDto fitnessScore

) {

}
