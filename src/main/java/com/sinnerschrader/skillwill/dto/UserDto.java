package com.sinnerschrader.skillwill.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record UserDto(
		String username,
		String password,
		List<String> authorities,
		String ldapDN, 
		Long version,
		List<UserSkillDto> skills,
		UserLdapDetailsDto userLdapDto,
		FitnessScoreDto fitnessScore
		) {
	
}
