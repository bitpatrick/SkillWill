package com.sinnerschrader.skillwill.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

	private String token;
	private final String type = "Bearer";
	private String username;
	private String email;
	private List<String> roles;
	private LocalDateTime expirationTime;

}
