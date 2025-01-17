package com.sinnerschrader.skillwill.config;

import com.sinnerschrader.skillwill.repository.UserRepository;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class MyWebSecurityConfig  {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}
	
	@Bean
	OAuthAuthenticationSuccessHandler oAuthAuthenticationSuccessHandler() {
		return new OAuthAuthenticationSuccessHandler(userRepository, securityContextRepository());
	}
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		http
			.authenticationManager(providerManager())
			.cors(corsCustomizer -> {
				corsCustomizer.configurationSource(corsConfigurationSource -> {
					
					CorsConfiguration corsConfiguration = new CorsConfiguration();
					corsConfiguration.addAllowedOrigin("http://localhost:8888");
					corsConfiguration.addAllowedOrigin("http://127.0.0.1:8888");
					corsConfiguration.setAllowCredentials(true);
					corsConfiguration.setAllowedMethods(List.of("*"));
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
							.anyRequest().authenticated();
					
			})
			.formLogin(formLoginCustomizer -> { formLoginCustomizer
				
				// invio lo status 200 dopo che ha effettuato il login con successo
				.successHandler( (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {

          response.setStatus(HttpStatus.OK.value());
				})
				.loginProcessingUrl("/login"); // path gestita dal filter ( se arriva una request POST con path /login allora il filtro gestisce questa request )
				
				
			})
//			.oauth2Login(
//					o -> { o
//						
//						.successHandler(oAuthAuthenticationSuccessHandler());
//			})
			.logout(logoutCustomizer -> { logoutCustomizer
				
        .logoutSuccessHandler((HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {

          response.setStatus(HttpStatus.OK.value());
        });
				
			})
			.requestCache(requestCacheCustomizer -> { requestCacheCustomizer
				
				.disable();
			
			})
			.exceptionHandling(eh -> eh
					
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("http://localhost:8888/login")
			));
		
		return http.build();
	}
	
	// crea un parent
	@Bean
	DaoAuthenticationProvider daoAuthenticationProvider() {
		
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		
		/*
		 * implementazione aggiunta per il jwt token
		 */
		String encodingId = "bcrypt";
		Map<String, PasswordEncoder> encoders = new HashMap<>();
		encoders.put(encodingId, new BCryptPasswordEncoder());
		encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
		encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
		encoders.put("MD5", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
		encoders.put("noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
		encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_5());
		encoders.put("pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
		encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v4_1());
		encoders.put("scrypt@SpringSecurity_v5_8", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
		encoders.put("SHA-1", new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
		encoders.put("SHA-256",
				new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
		encoders.put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
		encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_2());
		encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());

		MyCustomPasswordEncoder myCustomPasswordEncoder = new MyCustomPasswordEncoder(encodingId, encoders);
		
		provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
//		provider.setPasswordEncoder(new BCryptPasswordEncoder());

		return provider;
	}
	
//	@Bean
//	AnonymousAuthenticationProvider anonymousAuthenticationProvider() {
//		return new AnonymousAuthenticationProvider(null);
//	}
	
	@Bean
	AuthenticationManager providerManager() {
		List<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(daoAuthenticationProvider());
//		providers.add(new OAuth2LoginAuthenticationProvider(new DefaultAuthorizationCodeTokenResponseClient(),
//				new DefaultOAuth2UserService()))
		;
		return new ProviderManager(providers);
	}


}
