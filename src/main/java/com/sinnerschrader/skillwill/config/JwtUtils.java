package com.sinnerschrader.skillwill.config;

import java.time.LocalDateTime;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.sinnerschrader.skillwill.domain.user.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	private SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
	
	@Value("${jwt.secret}")
	private String jwtSecret;
	
	@Value("${jwt.expirationsec}")
	private long jwtExpirationSec;

	public String generateJwtToken(Authentication authentication) {

		User user = (User) authentication.getPrincipal();
		
		// set jwt expiration time
		LocalDateTime exp = LocalDateTime.now().plusSeconds(jwtExpirationSec);
		user.setExpirationTime(exp);
		
		String jwtToken = Jwts.builder().setSubject(user.getUsername()).signWith(key).compact();

		return jwtToken;
	}

	public String getUsernameFromJwtToken(String token) {

		String usernameFromToken = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();

		return usernameFromToken;
	}

	public boolean validateJwtToken(String authToken) {

		try {

			Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
			return true;

		} catch (Exception e) {
			
			logger.error("Failed to validate JWT token", e.getMessage());
		}
		return false;
	}

}
