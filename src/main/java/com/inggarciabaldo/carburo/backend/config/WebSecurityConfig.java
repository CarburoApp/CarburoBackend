package com.inggarciabaldo.carburo.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad web de la aplicación.
 *
 * <p>
 * Esta clase define las reglas de seguridad basadas en Spring Security,
 * incluyendo:
 * </p>
 * <ul>
 *     <li>Gestión de sesiones</li>
 *     <li>Control de acceso a rutas públicas y privadas</li>
 *     <li>Configuración del sistema de autenticación</li>
 *     <li>Configuración del proceso de logout</li>
 * </ul>
 * <p>
 * Se utiliza una configuración basada en {@link SecurityFilterChain},
 * siguiendo el enfoque recomendado en versiones modernas de Spring Security.
 */
@Configuration
public class WebSecurityConfig {

	/**
	 * Define la cadena de filtros de seguridad de la aplicación.
	 *
	 * <p>
	 * Se configuran las políticas de seguridad HTTP, incluyendo
	 * la gestión de sesiones, autorización de peticiones,
	 * desactivación del formulario de login por defecto y el proceso
	 * de cierre de sesión.
	 * </p>
	 *
	 * @param http objeto {@link HttpSecurity} utilizado para configurar
	 *             la seguridad web
	 * @return cadena de filtros de seguridad configurada
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http.csrf(csrf -> {
				})
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
				)
				.authorizeHttpRequests(auth -> auth
						// Recursos estáticos (públicos)
						.requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico").permitAll()
						// Páginas públicas principales
						.requestMatchers("/", "/login", "/registro", "/logout", "/recuperacion", "/contacto").permitAll()
						// Estaciones de servicio
						.requestMatchers("/estacionesDeServicio", "/estacionesDeServicio/*").permitAll()
						// Sistema de Auth de Supabase
						.requestMatchers("/recoverPassword/*").permitAll()
						// Rutas que requieren autenticación
						.requestMatchers("/perfil", "/perfil/**").hasRole("USER")
						.requestMatchers("/vehiculos/**", "/repostajes/**", "/perfil/estacionesDeServicio/favoritas/*").hasRole("USER")
						// Cualquier otra petición (por ahora público)
						.anyRequest().permitAll())
				// Formulario de login personalizado deshabilitado
				.formLogin(AbstractHttpConfigurer::disable)
				// Funcionalidad de logout
				.logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").invalidateHttpSession(true).clearAuthentication(true));
		return http.build();
	}

	/**
	 * Define el codificador de contraseñas utilizado por la aplicación.
	 *
	 * <p>
	 * Se emplea el algoritmo BCrypt, recomendado por Spring Security
	 * por su resistencia frente a ataques de fuerza bruta.
	 * </p>
	 *
	 * @return codificador de contraseñas BCrypt
	 */
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Expone el {@link AuthenticationManager} utilizado por Spring Security
	 * para el proceso de autenticación.
	 *
	 * <p>
	 * Este gestor se obtiene a partir de la configuración de autenticación
	 * proporcionada por el propio framework.
	 * </p>
	 *
	 * @param authConfig configuración de autenticación de Spring Security
	 * @return gestor de autenticación de la aplicación
	 */
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authConfig) {
		return authConfig.getAuthenticationManager();
	}
}
