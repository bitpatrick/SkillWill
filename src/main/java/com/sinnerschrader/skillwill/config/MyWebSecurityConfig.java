package com.sinnerschrader.skillwill.config;

import java.io.IOException;
import java.io.Writer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import com.sinnerschrader.skillwill.repositories.UserRepository;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
							.dispatcherTypeMatchers(DispatcherType.INCLUDE, DispatcherType.FORWARD, DispatcherType.ERROR).permitAll() // sto controllando solo le request che vengono da fuori
							.requestMatchers("/session/**").authenticated() // dentro session c'è GET
							.requestMatchers(HttpMethod.GET, "/**").permitAll()
							.anyRequest().authenticated();
					
			})
			.formLogin(formLoginCustomizer -> { formLoginCustomizer
				
				// invio lo status 200 dopo che ha effettuato il login con successo
				.successHandler( (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
					
					response.setStatus(HttpStatus.OK.value());
					
				})
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

	@Bean
	AuthenticationProvider daoAuthenticationProvider() {
		
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(new CustomUserDetailsService(userRepository));
		provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//		provider.setPasswordEncoder(new BCryptPasswordEncoder());

		return provider;
	}

}
