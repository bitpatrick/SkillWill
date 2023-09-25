package com.sinnerschrader.skillwill.config;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {
	
	private JwtUtils jwtUtils;
	
	private UserRepository userRepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		try {
			
			String jwt = parseJwt(request);
			
			/*
			 * check if token is valid
			 */
			if ( jwt != null && jwtUtils.validateJwtToken(jwt) ) {
				
				// recupero username dal token
				String username = jwtUtils.getUsernameFromJwtToken(jwt);
				
				// recupero username dal repository
				User user = userRepository.findById(username).orElseThrow(() -> new UsernameNotFoundException(username));
				
				// creo un token per la matriosca, non si tratta di un vero e proprio token tipo il jwt ma sarebbe un contenitore che all'interno ha le informazioni dell'utente come principal e credentials , il parametro null sarebbe a password poiche in questo contesto non serve 
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

				// vari dettagli della request
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				
				// setto il token all'interno della matriosca
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
			
		} catch (Exception e) {
			
			logger.error("Cannot set user authentication: {}", e);
		}
		
		filterChain.doFilter(request, response);
	}
	
	private String parseJwt(HttpServletRequest request) {
		
		String headerAuth = request.getHeader("Authorization");
		if ( StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ") ) {
			return headerAuth.substring(7, headerAuth.length());
		}
		return null;
	}

}
