package com.dbp.democarpultec.config.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurtyConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	return http
		.csrf(csrf -> csrf.disable())
		.cors(Customizer.withDefaults())
		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authorizeHttpRequests(authorize -> authorize
			.requestMatchers(
				"/api/auth/**",
				"/v3/api-docs/**",
				"/api/v3/api-docs/**",
				"/swagger-ui/**",
				"/api/swagger-ui/**",
				"/swagger-ui.html",
				"/api/swagger-ui.html",
				"/api/docs",
				"/api/docs/",
				"/api/docs/**",
				"/ws/**"
			).permitAll()
			.anyRequest().authenticated()
		)
		.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
		.build();
    }
}
