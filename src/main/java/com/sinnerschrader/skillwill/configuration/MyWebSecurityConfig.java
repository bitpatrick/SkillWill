package com.sinnerschrader.skillwill.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import com.sinnerschrader.skillwill.repositories.UserRepository;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class MyWebSecurityConfig {

	@Autowired
	private UserRepository userRepository;
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.cors(corsCustomizer -> {
				corsCustomizer.configurationSource(corsConfigurationSource -> {
					CorsConfiguration corsConfiguration = new CorsConfiguration();
					corsConfiguration.applyPermitDefaultValues();
					return corsConfiguration;
				});
			})
			.csrf(csrfCustomizer -> csrfCustomizer.disable())
			.securityMatcher("/**")
			.authorizeHttpRequests(  
					
					authorize -> {
						
						authorize
							.dispatcherTypeMatchers(DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
							.requestMatchers(HttpMethod.GET, "/**").permitAll()
							.anyRequest().permitAll();
					
					}
					
					)
			.formLogin(formLoginCustomizer -> {
				formLoginCustomizer
					.loginPage("http://127.0.0.1:8888/login")
					.defaultSuccessUrl("http://127.0.0.1:8888/my-profile")
					.loginProcessingUrl("/login");
			})
			.logout(logoutCustomizer -> {
				
				logoutCustomizer
					.logoutSuccessUrl("http://127.0.0.1:8888/");
				
			})
			.requestCache(requestCacheCustomizer -> {
				requestCacheCustomizer.disable();
			});
		
		return http.build();
		
	}

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http
//		.cors().configurationSource(ccs -> {
//			CorsConfiguration corsConfiguration = new CorsConfiguration();
//			corsConfiguration.applyPermitDefaultValues();
//			return corsConfiguration;
//		})
//		.and()
//		.csrf().disable()
//		.authenticationProvider(daoAuthenticationProvider())
//		.authorizeRequests()
//				.antMatchers("/session/user").authenticated()
//				.and()
//				.authorizeRequests().antMatchers(HttpMethod.GET, "/users/**").permitAll()
//				.and()
//				.authorizeRequests().antMatchers("/users/**").authenticated()
//				.anyRequest().permitAll()
//				.and()
//				.formLogin()
//					.loginPage("http://127.0.0.1:8888/login")
//					.defaultSuccessUrl("http://127.0.0.1:8888/my-profile")
//					.loginProcessingUrl("/login")
//				.and()
//				.logout()
//					.logoutSuccessUrl("http://127.0.0.1:8888/")
//				.and()
//				.requestCache().disable();
//	}

//	@Override
//	protected void configure(AuthenticationManagerBuilder builder) throws Exception {
//		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//		builder.inMemoryAuthentication().passwordEncoder(passwordEncoder).withUser("joe")
//				.password(passwordEncoder.encode("123")).roles("USER").and().withUser("sara")
//				.password(passwordEncoder.encode("234")).roles("ADMIN");
//
//		builder.authenticationProvider(daoAuthenticationProvider());
//	}

	@Bean
	AuthenticationProvider daoAuthenticationProvider() {
		
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(new CustomUserDetailsService(userRepository));
//		provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
		provider.setPasswordEncoder(new BCryptPasswordEncoder());

		return provider;
	}

}
