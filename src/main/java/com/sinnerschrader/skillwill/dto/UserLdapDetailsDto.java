package com.sinnerschrader.skillwill.dto;

import lombok.Builder;

@Builder
public record UserLdapDetailsDto(
		String firstName, 
		String lastName, 
		String mail, 
		String phone, 
		String location,
		String title, 
		String company, 
		String role) {

}
