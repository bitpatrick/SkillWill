package com.sinnerschrader.skillwill.service;

import com.sinnerschrader.skillwill.domain.user.User;
import com.sinnerschrader.skillwill.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = userRepository.findById(username).orElseThrow(() -> new UsernameNotFoundException("user not found by username: " + username));
		
		return user;
	}

}
