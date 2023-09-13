package com.sinnerschrader.skillwill.config;

import java.util.Arrays;

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
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sinnerschrader.skillwill.repositories.UserRepository;

import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class MyWebSecurityConfig {

	@Autowired
	private UserRepository userRepository;
	
	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}
	
	@Bean
	OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler() {
		return new OAuthAuthenticationSuccessHandler(userRepository, securityContextRepository());
	}
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.cors(corsCustomizer -> {
				corsCustomizer.configurationSource(corsConfigurationSource -> {
					CorsConfiguration corsConfiguration = new CorsConfiguration();
					corsConfiguration.addAllowedOrigin("http://127.0.0.1:8888");
					corsConfiguration.addAllowedOrigin("http://localhost:8888");
					corsConfiguration.setAllowCredentials(true);
					corsConfiguration.applyPermitDefaultValues();
					
					return corsConfiguration;
				});
			})
			.csrf(csrfCustomizer -> csrfCustomizer.disable())
			.securityContext(securityContextCustomizer -> {
				securityContextCustomizer.securityContextRepository(securityContextRepository());
			})
			.securityMatcher("/**")
			.authorizeHttpRequests(  
					
					authorize -> {
						
						authorize
							.dispatcherTypeMatchers(DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
							.requestMatchers("/session/**").authenticated()
							.requestMatchers(HttpMethod.GET, "/**").permitAll()
							.anyRequest().permitAll();
					
			})
			.formLogin(formLoginCustomizer -> { formLoginCustomizer
					
				.defaultSuccessUrl("http://127.0.0.1:8888/my-profile")
				.loginProcessingUrl("/login"); // path gestita dal filter ( se arriva una request POST con path /login allora il filtro gestisce questa request )
				
			})
			.oauth2Login(
					o -> { o
						
						.successHandler(oAuthAuthenticationSuccessHandler());
			})
			.logout(logoutCustomizer -> { logoutCustomizer
					
				.logoutSuccessUrl("http://127.0.0.1:8888/");
				
			})
			.requestCache(requestCacheCustomizer -> { requestCacheCustomizer
				
				.disable();
			
			}).exceptionHandling(eh -> eh
					
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("http://127.0.0.1:8888/login")
			));
		
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
