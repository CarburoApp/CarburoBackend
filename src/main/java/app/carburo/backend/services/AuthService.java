package app.carburo.backend.services;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.entities.Usuario;
import app.carburo.backend.repositories.CombustibleRepository;
import app.carburo.backend.repositories.ProvinciaRepository;
import app.carburo.backend.services.api.model.SupabaseUserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.Method;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static app.carburo.backend.controllers.AuthController.RUTA_RESTABLECER_CONTRASENA;


@Slf4j
@Service
public class AuthService {

    // ===================== HEADERS =====================
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String API_KEY = "apikey";
    private static final String BEARER = "Bearer %s";

    // ===================== JSON KEYS =====================
    private static final String AUTH_USER = "user";
    private static final String AUTH_USERS = "users";
    private static final String AUTH_USER_ID = "id";
    private static final String AUTH_USER_EMAIL = "email";
    private static final String AUTH_USER_TOKEN = "access_token";
    private static final String AUTH_USER_CREATED = "created_at";
    private static final String AUTH_USER_UPDATED = "updated_at";

    // ===================== ENDPOINTS =====================
    private static final String LOGIN = "/token?grant_type=password";
    private static final String SIGNUP = "/signup";
    private static final String LOGOUT = "/logout";
    private static final String ADMIN_USERS = "/admin/users";
    private static final String USER = "/user";
    private static final String RECOVER = "/recover";
    private static final String RESET = "/user/reset";

    private final String urlRestablecerContrasena;
    private final UsuarioService usuarioService;
    private final ProvinciaRepository provinciaRepository;
    private final CombustibleRepository combustibleRepository;

    @Value("${security.supabase.auth.url}")
    private String SUPABASE_AUTH_URL;

    @Value("${security.supabase.auth.role-key}")
    private String SERVICE_ROLE_KEY;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public AuthService(@Value("${app.url}") String appUrl,
                       UsuarioService usuarioService,
                       ProvinciaRepository provinciaRepository,
                       CombustibleRepository combustibleRepository) {
        this.urlRestablecerContrasena = appUrl + RUTA_RESTABLECER_CONTRASENA;

        this.usuarioService        = usuarioService;
        this.provinciaRepository   = provinciaRepository;
        this.combustibleRepository = combustibleRepository;
    }

    // =====================================================
    // SPRING SECURITY HELPERS
    // =====================================================

    public Optional<Usuario> getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated()) return Optional.empty();

        Optional<Usuario> usuarioOpt = usuarioService.findByUUID(
                UUID.fromString((String) auth.getPrincipal()));

        if (usuarioOpt.isEmpty()) return Optional.empty();

        Usuario usuario = usuarioOpt.get();

        Optional<String> tokenOpt = getTokenActual();
        if (tokenOpt.isPresent()) {
            try {
                SupabaseUserResponse sbUser = getSupabaseUserData(tokenOpt.get());

                // 1. Asignar Email de la raíz de Supabase
                usuario.setEmail(sbUser.email());

                // 2. Procesar Metadatos del Usuario (Nombre y Foto)
                if (sbUser.userMetadata() != null) {
                    SupabaseUserResponse.UserMetadata meta = sbUser.userMetadata();

                    // Lógica para el Nombre (Prioridad a Name -> Full Name -> Prefijo Email)
                    String nombreFinal =
                            meta.name() != null ? meta.name() : meta.fullName();
                    if (nombreFinal == null || nombreFinal.isBlank()) {
                        nombreFinal = sbUser.email() != null ? sbUser.email()
                                .split("@")[0] : "Usuario";
                    }
                    usuario.setNombre(nombreFinal);

                    // Lógica para el Avatar/Imagen (Prioridad a avatarUrl -> picture)
                    String fotoFinal =
                            meta.avatarUrl() != null ? meta.avatarUrl() : meta.picture();
                    usuario.setAvatarUrl(fotoFinal);
                }

            } catch (IOException e) {
                log.error("Error al enriquecer usuario dinámicamente con Supabase {}",
                          usuario.getUuid(), e);
            }
        }

        return Optional.of(usuario);
    }

    public SupabaseUserResponse getSupabaseUserData(String token) throws IOException {
        HttpURLConnection conn = open(USER, Method.GET);
        conn.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        int code = conn.getResponseCode();
        if (code != 200) {
            String error = readError(conn);
            log.error("Error cargando info de usuario Supabase code={}. Detalle: {}",
                      code, error);
            throw new IOException("Failed to fetch user data from Supabase");
        }

        return MAPPER.readValue(conn.getInputStream(), SupabaseUserResponse.class);
    }

    public Optional<String> getTokenActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();

        Object cred = auth.getCredentials();
        if (cred instanceof String t && !t.isBlank()) return Optional.of(t);

        return Optional.empty();
    }

    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() != null &&
                !"anonymousUser".equals(auth.getPrincipal());
    }

    // =====================================================
    // LOGIN SPRING
    // =====================================================

    public void login(HttpServletRequest request, String token, UUID uuid) {
        login(request, token, uuid, false);
    }

    public void login(HttpServletRequest request, String token, UUID uuid,
                      boolean oAuthGoogle) {
        if (oAuthGoogle && usuarioService.findByUUID(uuid).isEmpty()) {
            log.info(
                    "Detectado primer acceso OAuth de Google para el UUID: {}. Creando registro local...",
                    uuid);
            try {
                Usuario nuevoUsuario = new Usuario(uuid,
                                                   provinciaRepository.getProvinciaById(
														   (short) 1));
                nuevoUsuario.setCombustiblesFavoritos(
                        new HashSet<>(combustibleRepository.findAll()));

                usuarioService.save(nuevoUsuario);
                log.info("Usuario guardado en base de datos local con éxito.");
            } catch (Exception e) {
                log.error(
                        "Fallo crítico al sincronizar de forma persistente el usuario de Google con la BD local",
                        e);
                throw new IllegalStateException(
                        "No se pudo crear el usuario en el entorno local", e);
            }
        }

        Authentication auth = buildAuth(request, token, uuid);

        SecurityContextHolder.getContext().setAuthentication(auth);
        log.debug("Login Spring OK user={}", uuid);
    }

    private Authentication buildAuth(HttpServletRequest request, String token,
                                     UUID uuid) {

        Authentication auth = new UsernamePasswordAuthenticationToken(uuid.toString(),
                                                                      token,
                                                                      List.of(new SimpleGrantedAuthority(
                                                                              "ROLE_USER")));

        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);

        return auth;
    }

    // =====================================================
    // LOGIN SUPABASE
    // =====================================================

    public String login(HttpServletRequest request, String email, String password)
            throws IOException {

        HttpURLConnection conn = open(LOGIN, Method.POST);

        String body = """
                      {
                          "email": "%s",
                          "password": "%s"
                      }
                      """.formatted(email, password);

        write(conn, body);

        int code = conn.getResponseCode();
        log.debug("LOGIN Supabase code={}", code);

        if (code == 400) return null;

        if (code != 200) {
            log.error("LOGIN error {}", readError(conn));
            throw new IllegalStateException("Login failed");
        }

        JsonNode root = MAPPER.readTree(conn.getInputStream());

        String token = root.get(AUTH_USER_TOKEN).asString();
        String uuid = root.get(AUTH_USER).get(AUTH_USER_ID).asString();

        login(request, token, UUID.fromString(uuid));

        return token;
    }

    // =====================================================
    // REGISTRO
    // =====================================================

    @Transactional
    public UUID registro(UsuarioDto u) throws IOException {

        HttpURLConnection conn = open(SIGNUP, Method.POST);

        String body = """
                      {
                          "email": "%s",
                          "password": "%s",
                          "data": {
                                                    "name": "%s",
                                                    "full_name": "%s"
                                                }
                      }
                      """.formatted(u.getCorreo(), u.getContrasena(), u.getNombre(),
                                    u.getNombre());

        write(conn, body);

        int code = conn.getResponseCode();
        log.info("REGISTRO Supabase code={}", code);

        if (code != 200 && code != 201) {
            log.error("REGISTRO error {}", readError(conn));
            throw new IllegalStateException("Signup error");
        }

        JsonNode root = MAPPER.readTree(conn.getInputStream());

        JsonNode user = root.get(AUTH_USER);

        return UUID.fromString(user.get(AUTH_USER_ID).asString());
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    public boolean logout(String token) throws IOException {

        HttpURLConnection conn = open(LOGOUT, Method.POST);
        conn.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        int code = conn.getResponseCode();

        log.debug("LOGOUT code={}", code);

        return code == 204;
    }

    // =====================================================
    // ADMIN EMAIL CHECK
    // =====================================================

    public boolean containsEmail(String email) throws IOException {

        HttpURLConnection conn = open(ADMIN_USERS, Method.GET);

        conn.setRequestProperty(AUTHORIZATION, "Bearer " + SERVICE_ROLE_KEY);
        conn.setRequestProperty(API_KEY, SERVICE_ROLE_KEY);

        int code = conn.getResponseCode();

        log.debug("containsEmail code={}", code);

        if (code != 200) {
            log.error("containsEmail error {}", readError(conn));
            return false;
        }

        JsonNode root = MAPPER.readTree(conn.getInputStream());

        for (JsonNode u : root.get(AUTH_USERS)) {
            if (email.equalsIgnoreCase(u.get(AUTH_USER_EMAIL).asString())) {
                return true;
            }
        }

        return false;
    }

    // =====================================================
    // RECUPERACIÓN EMAIL
    // =====================================================

    public boolean recuperacionDeContrasena(String correo) throws IOException {

        HttpURLConnection conn = open(RECOVER, Method.POST);

        write(conn, """
                    {
                     "email": "%s",
                     "redirect_to": "%s",
                     "redirectTo": "%s",
                     "options": {
                                     "redirect_to": "%s",
                                     "redirectTo": "%s",
                                     "redirectUrl": "%s"
                                 }
                    }
                """.formatted(correo,
                urlRestablecerContrasena,
                urlRestablecerContrasena,
                urlRestablecerContrasena,
                urlRestablecerContrasena,
                urlRestablecerContrasena));

        int code = conn.getResponseCode();

        log.debug("RECOVER code={}", code);

        return code == 200;
    }

    // =====================================================
    // RESET PASSWORD (TOKEN EMAIL)
    // =====================================================

    public boolean restablecerContrasena(String token, String pass)
            throws IOException {

        HttpURLConnection conn = open(USER, Method.PUT);
        conn.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        write(conn, """
                    {"password": "%s"}
                    """.formatted(pass));

        int code = conn.getResponseCode();
        log.debug("RESET (PUT /user) code={}", code);

        if (code != 200) {
            log.error("RESET error {}", readError(conn));
        }

        return code == 200;
    }

    // =====================================================
    // CHANGE PASSWORD AUTH USER
    // =====================================================

    public boolean modificarContrasenaUsuario(String token, String pass)
            throws IOException {

        HttpURLConnection conn = open(USER, Method.PUT);

        conn.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        write(conn, """
                    { "password": "%s" }
                    """.formatted(pass));

        int code = conn.getResponseCode();

        log.debug("CHANGE PASS code={}", code);

        return code == 200;
    }

    /**
     * Actualiza el nombre de un usuario existente.
     *
     * @param token                   identificador UUID del usuario
     * @param nuevoNombre nuevo nombre a asignar
     * @throws IllegalArgumentException si alguno de los parámetros es inválido
     */
    public boolean modificarNombreUsuario(String token, String nuevoNombre)
            throws IOException {
        if (token == null || nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new IllegalArgumentException(
                    "Parámetros inválidos para la actualización del nombre.");
        }
        HttpURLConnection conn = open(USER, "PUT");
        conn.setRequestProperty(AUTHORIZATION, String.format(BEARER, token));

        // Los datos del perfil como el nombre en Supabase se guardan bajo "data"
        String body = """
                      {
                          "data": {
                              "name": "%s",
                              "full_name": "%s"
                          }
                      }
                      """.formatted(nuevoNombre.trim(), nuevoNombre.trim());

        write(conn, body);

        int code = conn.getResponseCode();
        log.debug("UPDATE USER NAME code={}", code);

        if (code != 200) {
            log.error("Error al modificar nombre en Supabase: {}", readError(conn));
        }

        return code == 200;
    }

    // =====================================================
    // HTTP HELPERS
    // =====================================================

    private HttpURLConnection open(String path, String method) throws IOException {

        URL url = URI.create(SUPABASE_AUTH_URL + path).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty(CONTENT_TYPE, CONTENT_TYPE_JSON);
        conn.setRequestProperty(API_KEY, SERVICE_ROLE_KEY);

        if (!Method.GET.equalsIgnoreCase(method)) {
            conn.setDoOutput(true);
        }

        log.debug("HTTP {} {}", method, url);

        return conn;
    }

    private void write(HttpURLConnection conn, String body) throws IOException {
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readError(HttpURLConnection conn) {
        try {
            if (conn.getErrorStream() == null) return "";
            return new String(conn.getErrorStream().readAllBytes());
        } catch (Exception e) {
            return "error-read";
        }
    }
}