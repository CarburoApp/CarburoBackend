package app.carburo.backend.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gestiona la preferencia de idioma de la web mediante una cookie.
 */
@RestController
public class ConfigurationRestController {

    private final LocaleResolver localeResolver;
    private final Set<String> availableLanguages;

    /**
     * Inicializa el controlador con el resolvedor de idioma y los idiomas admitidos.
     *
     * @param localeResolver     resolvedor encargado de actualizar y conservar el idioma
     * @param availableLanguages códigos de idioma definidos en {@code app.language.supported}, separados por comas.
     */
    public ConfigurationRestController(LocaleResolver localeResolver,
                                       @Value("${app.language.supported:es,en,fr,it,pt,ca,gl,eu,oc,ast,an}") String availableLanguages) {
        this.localeResolver = localeResolver;
        this.availableLanguages =
                Arrays.stream(availableLanguages.split(","))
                        .map(String::trim)
                        .map(value -> value.toLowerCase(Locale.ROOT))
                        .filter(value -> !value.isEmpty())
                        .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Actualiza la preferencia de idioma mediante una petición {@code POST /idioma}.
     *
     * @param newLanguage código del idioma solicitado.
     * @param request petición HTTP actual.
     * @param response respuesta HTTP en la que se escribe la cookie de idioma.
     * @return respuesta {@code 204 No Content}, con la directiva de caché {@code no-store}.
     * @throws ResponseStatusException con estado {@code 400 Bad Request} si el idioma solicitado no está admitido.
     */
    @PostMapping("/idioma")
    public ResponseEntity<Void> setIdioma(@RequestParam("newLanguage") String newLanguage,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        String language = newLanguage.trim().toLowerCase(Locale.ROOT);
        if (!availableLanguages.contains(language))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El idioma no está disponible.");

        localeResolver.setLocale(request, response, Locale.forLanguageTag(language));
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }
}
