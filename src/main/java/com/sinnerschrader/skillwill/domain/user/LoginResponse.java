package com.sinnerschrader.skillwill.domain.user;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {

	private String token;
	private final String type = "Bearer";
	private String username;
	private List<String> roles;

}
