package app.carburo.backend.config;

import app.carburo.backend.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

@Component
public class TokenExpiryInterceptor implements HandlerInterceptor {

    // Dependencias
    private final AuthService authService;

    // Constructor con inyección de dependencia
    public TokenExpiryInterceptor(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Función que se ejecuta antes de que el request llegue al controller.
     * Aquí se comprueba si el token de Supabase ha caducado.
     *
     * @param request  la petición HTTP entrante
     * @param response la respuesta HTTP
     * @param handler  el handler que procesará la petición (controller)
     * @return true → continuar con la ejecución normal del request
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        authService.getTokenActual().ifPresent(token -> {
            if (isTokenCaducado(token)) {
                // Token caducado → limpiar sesión
                SecurityContextHolder.clearContext();
                HttpSession session = request.getSession(false);
                if (session != null) session.invalidate();
            }
        });
        // Seguir con el request
        return true;
    }

    /**
     * Comprueba si un JWT está caducado basándose en el claim 'exp'.
     *
     * @param jwt JWT de Supabase
     * @return true si el token está caducado o inválido
     */
    private boolean isTokenCaducado(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) return true; // inválido

            // Decodificar payload base64
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            ObjectMapper mapper = new ObjectMapper();

            // Obtener exp
            Number exp = (Number) mapper.readValue(payloadJson, Map.class).get("exp");
            if (exp == null) return true;

            long now = System.currentTimeMillis() / 1000L; // segundos
            return now >= exp.longValue(); // true si expirado

        } catch (Exception e) {
            return true; // cualquier error = token inválido
        }
    }
}
