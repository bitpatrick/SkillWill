package com.sinnerschrader.skillwill.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private UserRepository userRepository;

	private SecurityContextRepository securityContextRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		/*
		 * recupero l'IdentityProvider
		 */
		String uri = request.getRequestURI();
		String[] parts = uri.split("/");
		String result = parts[parts.length - 1]; // Prende l'ultimo elemento dell'array

		DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) authentication.getPrincipal();

		/*
		 * recupero l'username all'interno del OAuthToken ( oggetto creato da Spring
		 * quando mi autentico con successo)
		 */
		String username = switch (result) {
		case "github": {
			yield defaultOAuth2User.getAttribute("login");
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + result);
		};

		try {

			checkIfUserExist(username);

		} catch (UsernameNotFoundException e) {

			// create new user
			User newUser = new User(username);

			// new user persistence
			userRepository.save(newUser);

			// create token user
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(newUser, null, null);

			// set security context
			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(usernamePasswordAuthenticationToken);
			SecurityContextHolder.setContext(securityContext);
			securityContextRepository.saveContext(securityContext, request, response);

			// redirect
			response.setStatus(HttpStatus.OK.value());;
		}
	}

	private void checkIfUserExist(String username) throws UsernameNotFoundException {

		// recupero l'user dal repository
		Optional<User> userDetails = userRepository.findById(username);

		// verifico se l'user esiste
		if (userDetails.isEmpty() || !userDetails.isPresent()) {
			throw new UsernameNotFoundException(username);
		}
	}

}
