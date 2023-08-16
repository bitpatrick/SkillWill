package com.sinnerschrader.skillwill.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .cors()
        .configurationSource(ccs -> {
        	CorsConfiguration corsConfiguration = new CorsConfiguration();
        	corsConfiguration.applyPermitDefaultValues();
        	return corsConfiguration;
        })
        .and()
        .csrf().disable()
        .authorizeRequests()
            .antMatchers("/session/user").authenticated()
            .anyRequest().permitAll()
            .and()
            .formLogin().loginPage("http://127.0.0.1:8888/login").defaultSuccessUrl("http://127.0.0.1:8888/my-profile").loginProcessingUrl("/login")
            .and()
            .logout().logoutSuccessUrl("http://127.0.0.1:8888/")
            .and()
            .requestCache().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        builder.inMemoryAuthentication().passwordEncoder(passwordEncoder)
               .withUser("joe").password(passwordEncoder.encode("123")).roles("USER")
               .and()
               .withUser("sara").password(passwordEncoder.encode("234")).roles("ADMIN")
        ;
    }
}
