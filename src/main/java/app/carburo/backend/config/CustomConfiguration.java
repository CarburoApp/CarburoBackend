package app.carburo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Configuración personalizada de Spring MVC para la aplicación.
 *
 * <p>
 * Esta clase centraliza configuraciones globales relacionadas con:
 * </p>
 * <ul>
 *     <li>Paginación por defecto en controladores</li>
 *     <li>Gestión de internacionalización (i18n)</li>
 *     <li>Resolución y cambio dinámico del idioma</li>
 * </ul>
 * <p>
 * Implementa {@link WebMvcConfigurer} para extender el comportamiento
 * por defecto de Spring MVC.
 */
@Configuration
public class CustomConfiguration implements WebMvcConfigurer {

	// Página inicial por defecto para la paginación.
	public static final int PAGINATION_INITIAL_PAGE = 0;
	// Tamaño de página por defecto para la paginación.
	public static final int PAGINATION_SIZE = 10;
	// Idioma por defecto de la aplicación.
	@Value("${app.language.default:es-ES}")
	private String idiomaPredeterminado;

	// Dependencias
	private final TokenExpiryInterceptor tokenExpiryInterceptor;

	public CustomConfiguration(TokenExpiryInterceptor tokenExpiryInterceptor) {
		this.tokenExpiryInterceptor = tokenExpiryInterceptor;
	}

	/**
	 * Configura resolutores de argumentos personalizados para los métodos
	 * de los controladores.
	 *
	 * <p>
	 * En este caso, se define un resolutor de paginación con valores
	 * por defecto cuando no se proporcionan parámetros en la petición.
	 * </p>
	 *
	 * @param argumentResolvers lista de resolutores de argumentos de Spring MVC
	 */
	@Override
	public void addArgumentResolvers(
			List<HandlerMethodArgumentResolver> argumentResolvers) {
		PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
		resolver.setFallbackPageable(PageRequest.of(PAGINATION_INITIAL_PAGE, PAGINATION_SIZE));
		argumentResolvers.add(resolver);
	}

	/**
	 * Define el {@link LocaleResolver} encargado de determinar el language actual del usuario.
	 * <p>
	 * Se utiliza una implementación basada en cookie, estableciendo un language por defecto para toda la aplicación.
	 *
	 * @return resolvedor de language basado en cookie
	 */
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver cookieResolver = new CookieLocaleResolver();
		Locale idioma = Locale.forLanguageTag(idiomaPredeterminado);
		cookieResolver.setDefaultLocale(idioma);
		cookieResolver.setCookieMaxAge(Duration.ofDays(30));  // 30 días
		return cookieResolver;
	}


	/**
	 * Registra interceptores personalizados dentro del ciclo de vida
	 * de las peticiones web.
	 *
	 * @param registry registro de interceptores de Spring MVC
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tokenExpiryInterceptor);
	}
}
