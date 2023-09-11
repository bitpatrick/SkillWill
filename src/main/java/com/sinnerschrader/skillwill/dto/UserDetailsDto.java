package com.sinnerschrader.skillwill.dto;

import lombok.Builder;

@Builder
public record UserDetailsDto(
		String username,
		String password,
		String ldapDN, 
		Long version
		) {

}
