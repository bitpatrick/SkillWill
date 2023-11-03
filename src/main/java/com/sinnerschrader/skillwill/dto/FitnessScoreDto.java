package com.sinnerschrader.skillwill.dto;

import java.util.Collection;

import lombok.Builder;

@Builder
public record FitnessScoreDto(
		FitnessScorePropertiesDto props,
		UserDto user,
		Collection<SkillDto> searchedSkills, 
		double value
) {

}
