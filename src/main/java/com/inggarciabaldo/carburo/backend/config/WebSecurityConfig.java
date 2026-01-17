package com.inggarciabaldo.carburo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> {})

				.authorizeHttpRequests(auth -> auth
						// Recursos estáticos (públicos)
						.requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**",
										 "/webjars/**", "/favicon.ico").permitAll()
						// Páginas públicas principales
						.requestMatchers("/", "/inicio/ubicacion", "/contacto")
						.permitAll()
						// Estaciones de servicio
						.requestMatchers("/estacionDeServicio", "/estacionDeServicio/*")
						.permitAll()
						// Cualquier otra petición (por ahora público)
						.anyRequest().permitAll()).formLogin(form -> form.disable())
				.logout(logout -> logout.disable());
		return http.build();
	}


	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
