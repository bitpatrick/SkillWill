package com.sinnerschrader.skillwill.config;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
public final class JwtUtils {

	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	private static final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);

	public static String generateJwtToken(Authentication authentication) {

		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		String jwtToken = Jwts.builder().setSubject(userPrincipal.getUsername()).signWith(key).compact();

		return jwtToken;
	}
	
	

	public static String getUsernameFromJwtToken(String token) {

		String usernameFromToken = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();

		return usernameFromToken;
	}

	public static boolean validateJwtToken(String authToken) {

		try {

			Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
			return true;

		} catch (Exception e) {
			logger.error("Failed to validate JWT token", e);

		}

		return false;

	}

}
