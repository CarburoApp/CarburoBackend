package app.carburo.backend.services;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.entities.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de autenticación encargado de gestionar todos los servicios relacionados con la autenticación del usuario.
 *
 * <p>
 * La autenticación se realiza contra Supabase Auth y posteriormente se integra
 * manualmente con Spring Security y almacenándolo en sesión.
 * </p>
 */
@Service
public class AuthService {

    // Constantes para los headers
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";

    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_BEARER = "Bearer %s";

    // Constantes para la autorización
    private static final String AUTH_USER_EMAIL = "email";
    private static final String AUTH_USER_PASSWORD = "password";
    private static final String AUTH_USER_TOKEN = "access_token";
    private static final String AUTH_USER = "user";
    private static final String AUTH_USERS = "users";
    private static final String AUTH_USER_ID = "id";
    private static final String AUTH_USER_CREATED = "created_at";
    private static final String AUTH_USER_UPDATED = "updated_at";

    // Constantes para los endpoints
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PUT = "PUT";
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String AUTH_ENDPOINT_SIGNUP = "/signup";
    private static final String AUTH_ENDPOINT_RECOVER = "/recover";
    private static final String AUTH_ENDPOINT_LOGIN = "/token?grant_type=password";
    private static final String AUTH_ENDPOINT_LOGOUT = "/logout";
    private static final String AUTH_ENDPOINT_ADMIN_USERS = "/admin/users";
    private static final String AUTH_ENDPOINT_USER = "/user";

    // Inyección de dependencias
    private final UsuarioService usuarioService;

    /**
     * Service Role Key de Supabase (acceso admin)
     */
    @Value("${SERVICE_ROLE_KEY}")
    private String SERVICE_ROLE_KEY;

    /**
     * URL base del servicio Supabase Auth
     */
    @Value("${SUPABASE_AUTH_URL}")
    private String SUPABASE_AUTH_URL;


    // Mapper JSON reutilizable
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    // Constructor inyectando el repositorio de usuarios
    public AuthService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Devuelve el usuario actualmente autenticado.
     *
     * @return Optional con el usuario o vacío si no hay sesión
     */
    public Optional<Usuario> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated()) return Optional.empty();
        return usuarioService.findByUUID(UUID.fromString((String) auth.getPrincipal()));
    }

    /**
     * Devuelve el token actual del usuario logueado (JWT de Supabase)
     *
     * @return Optional con el token o vacío si no hay sesión
     */
    public Optional<String> getTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Optional.empty();

        Object credentials = auth.getCredentials();
        if (credentials instanceof String token && !token.isBlank()) {
            return Optional.of(token);
        }
        return Optional.empty();
    }


    /**
     * Realiza el login manual en Spring Security utilizando el token
     * devuelto por Supabase.
     *
     * @param request     petición HTTP actual
     * @param token       JWT de Supabase
     * @param usuarioUuid UUID del usuario autenticado
     */
    public void login(HttpServletRequest request, String token, UUID usuarioUuid) {
        if (usuarioUuid == null)
            throw new IllegalArgumentException("Usuario inválido para el inicio de sesión.");
        Authentication authentication = getAuthentication(request, token, usuarioUuid);
        // Inyectar autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * Crea y almacena manualmente el objeto {@link Authentication}
     * dentro del contexto de Spring Security y de la sesión HTTP.
     */
    private Authentication getAuthentication(HttpServletRequest request, String token, UUID usuarioUuid) {
        if (token == null || token.isEmpty())
            throw new IllegalArgumentException("Token de sesión inválido.");
        // Crear authorities mínimo
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER") // rol básico para todas las rutas protegidas
        );
        // Crear autenticación manual
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuarioUuid.toString(), // principal
                token,            // credentials
                authorities       // authorities
        );
        //authenticationManager.authenticate(authentication);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        request.getSession(true)
                .setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        context
                );
        return authentication;
    }

    /**
     * Inicia sesión un usuario utilizando email/contraseña.
     * Aquí normalmente se haría la petición a Supabase Auth para verificar credenciales.
     *
     * @param email    Email del usuario
     * @param password Contraseña del usuario
     * @return token de sesión (JWT) si el login es exitoso, en caso contrarío null
     * @throws IOException si hay errores de conexión
     */
    public String login(HttpServletRequest request, String email, String password)
            throws IOException, IllegalStateException {
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_LOGIN, HTTP_METHOD_POST);
        conn.setDoOutput(true);
        // Crear JSON de registro
        String jsonBody = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);
        // Enviar JSON
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST)
            return null;
        if (responseCode == HttpURLConnection.HTTP_OK) {

            // Parsear la respuesta JSON
            JsonNode root = OBJECT_MAPPER.readTree(conn.getInputStream());

            if (!root.has(AUTH_USER_TOKEN) || !root.has(AUTH_USER) || root.get(AUTH_USER) == null ||
                    !root.get(AUTH_USER).has(AUTH_USER_ID))
                throw new IllegalStateException(
                        "No se recibió información de usuario en la respuesta");

            String uuidStr = root.get(AUTH_USER).get(AUTH_USER_ID).asText();
            String accessToken = root.get(AUTH_USER_TOKEN).asText();

            // Realizar el login
            login(request, accessToken, UUID.fromString(uuidStr));
            return accessToken;
        }
        throw new IllegalStateException(
                "Error al registrar usuario: HTTP " + responseCode);
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * @param tokenActualDelUsuario Token de autenticación del usuario
     * @return true si el logout fue exitoso, false en caso contrario
     * @throws IOException si hay errores de conexión
     */
    public boolean logout(String tokenActualDelUsuario) throws IOException, IllegalStateException {
        if (tokenActualDelUsuario == null || tokenActualDelUsuario.isEmpty())
            throw new IllegalStateException("Token inválido para el logout.");
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_LOGOUT, HTTP_METHOD_POST);
        // Headers complementarios
        conn.setRequestProperty(AUTHORIZATION,
                String.format(AUTHORIZATION_BEARER, tokenActualDelUsuario));
        int responseCode = conn.getResponseCode();
        return responseCode == HttpURLConnection.HTTP_NO_CONTENT;
    }

    /**
     * Registra un usuario nuevo.
     * Aquí se haría la creación en Supabase y luego la persistencia en la base de datos local.
     *
     * @param usuario Objeto Usuario con datos a registrar
     * @return DTO con información del usuario registrado
     * @throws IOException si hay errores de conexión
     */
    @Transactional
    public UsuarioDto registro(UsuarioDto usuario)
            throws IOException, IllegalStateException {
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_SIGNUP, HTTP_METHOD_POST);
        conn.setDoOutput(true);
        // Crear JSON de registro
        String jsonBody = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, usuario.getCorreo(), usuario.getContrasena());

        // Enviar JSON
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
            throw new IllegalStateException(
                    "Error al registrar usuario: HTTP " + responseCode);
        }

        // Parsear la respuesta JSON
        JsonNode root = OBJECT_MAPPER.readTree(conn.getInputStream());

        if (!root.has(AUTH_USER_TOKEN) || !root.has(AUTH_USER) || root.get(AUTH_USER) == null ||
                !root.get(AUTH_USER).has(AUTH_USER_ID)) throw new IllegalStateException(
                "No se recibió información de usuario en la respuesta");

        // Extraer campos importantes
        String accessToken = root.get(AUTH_USER_TOKEN).asText();
        JsonNode userNode = root.get(AUTH_USER);
        String uuidStr = userNode.get(AUTH_USER_ID).asText();
        String createdAtStr = userNode.get(AUTH_USER_CREATED).textValue();
        String updatedAtStr = userNode.get(AUTH_USER_UPDATED).asText();

        // Parsear fechas ISO 8601 a OffsetDateTime
        OffsetDateTime createdAt = OffsetDateTime.parse(createdAtStr);
        OffsetDateTime updatedAt = OffsetDateTime.parse(updatedAtStr);

        return new UsuarioDto(UUID.fromString(uuidStr), usuario.getNombre(), usuario.getCorreo(), createdAt, updatedAt, usuario.getProvinciaFavorita(), accessToken);
    }

    /**
     * Verifica si el sistema de autenticación contiene un email registrado.
     *
     * @param email Email a comprobar
     * @return true si el email existe, false en caso contrario
     * @throws IOException si hay errores de conexión
     */
    public boolean containsEmail(String email) throws IOException {
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_ADMIN_USERS, HTTP_METHOD_GET);
        // Headers complementarios
        conn.setRequestProperty(AUTHORIZATION,
                                String.format(AUTHORIZATION_BEARER, SERVICE_ROLE_KEY));

        int responseCode = conn.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Parsear la respuesta JSON
            JsonNode root = OBJECT_MAPPER.readTree(conn.getInputStream());

            // "users" es el array que nos interesa
            JsonNode usersNode = root.get(AUTH_USERS);

            if (usersNode != null && usersNode.isArray())
                // Buscar email exacto en el array
                for (JsonNode userNode : usersNode)
                    if (userNode.has(AUTH_USER_EMAIL) && email.equalsIgnoreCase(userNode.get(AUTH_USER_EMAIL).asText()))
                        return true;
        }
        // No encontrado → email no existe
        return false;
    }

    /**
     * Envía un email de recuperación de contraseña al correo indicado.
     *
     * @param correo Correo electrónico del usuario a recuperar.
     * @return true si la solicitud fue exitosa, false en caso contrario.
     * @throws IOException si hay errores de conexión
     */
    public boolean recuperacionDeContrasena(String correo) throws IOException {
        if (correo == null || correo.isBlank())
            throw new IllegalArgumentException("Correo inválida para la recuperación.");
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_RECOVER, HTTP_METHOD_POST);
        conn.setDoOutput(true);
        // Crear JSON de registro
        String jsonBody = String.format("""
                {
                    "email": "%s"
                }
                """, correo);
        // Enviar JSON
        conn.getOutputStream().write(jsonBody.getBytes(StandardCharsets.UTF_8));

        return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    /**
     * Encargado de modificar la contraseña de un usuario a través de una petición HTTP
     * PUT a la API REST de Supabase Auth.
     *
     * @param tokenDelUsuario Token de autenticación del usuario
     * @param contrasenaNueva Nueva contraseña a establecer
     * @throws IOException si hay errores de conexión
     */
    public boolean modificarContrasenaUsuario(String tokenDelUsuario,
                                              String contrasenaNueva) throws IOException {
        // Conexión HTTP
        HttpURLConnection conn = getHttpURLConnectionToSupabaseAuthWithJSONContent(AUTH_ENDPOINT_USER, HTTP_METHOD_PUT);
        // Headers complementarios
        conn.setRequestProperty(AUTHORIZATION,
                                String.format(AUTHORIZATION_BEARER, tokenDelUsuario));
        conn.setDoOutput(true);
        // Enviar JSON
        conn.getOutputStream().write(String.format("""
                                                   {
                                                       "password": "%s"
                                                   }
                                                   """, contrasenaNueva)
                                             .getBytes(StandardCharsets.UTF_8));
        return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    /**
     * Comprueba si existe un usuario autenticado en el contexto de seguridad.
     */
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return !(auth == null || auth.getPrincipal() == null || auth.getPrincipal().equals("anonymousUser"));
    }

    /**
     * Retorna una conexión HttpURLConnection ya creada y configurada hacia la url de Supabase Auth base + /path
     * introducida. Configura también el http method deseado, en función del introducido por parámetro y declara en el
     * header el Content-Type:application/json.
     * Por defecto deja el body a false, pudiendo cambiarlo si se desea -> conn.setDoOutput(false);
     *
     * @param path       path complementario de la petición http base de supabase.
     * @param metodoHTTP función http deseada.
     * @return Objeto HttpURLConnection ya preconfigurado
     * @throws IOException Si hay errores de conexión.
     */
    private HttpURLConnection getHttpURLConnectionToSupabaseAuthWithJSONContent(String path, String metodoHTTP) throws IOException {
        // Construir la URL completa del endpoint admin
        URL url = URI.create(SUPABASE_AUTH_URL + path).toURL();
        // Conexión HTTP
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(metodoHTTP);
        // Headers
        conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
        // Body a false, si se desea se puede modificar.
        conn.setDoOutput(false);
        return conn;
    }
}
