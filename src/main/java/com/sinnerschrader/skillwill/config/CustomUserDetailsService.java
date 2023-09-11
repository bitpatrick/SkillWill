package com.sinnerschrader.skillwill.config;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repositories.UserRepository;

public class CustomUserDetailsService implements UserDetailsService {

	private UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		Optional<User> userDetailsImplOpt = userRepository.findById(username);
		
		if (userDetailsImplOpt.isEmpty() || !userDetailsImplOpt.isPresent()) {
			throw new UsernameNotFoundException("user not found by username: " + username);
		}

		return userDetailsImplOpt.get();
	}

}
