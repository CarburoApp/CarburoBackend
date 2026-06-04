package app.carburo.backend.controllers;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.entities.Provincia;
import app.carburo.backend.entities.Usuario;
import app.carburo.backend.repositories.CombustibleRepository;
import app.carburo.backend.services.AuthService;
import app.carburo.backend.services.ProvinciaService;
import app.carburo.backend.services.UsuarioService;
import app.carburo.backend.validators.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static app.carburo.backend.controllers.UsuarioController.RUTA_PERFIL;
import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CONTRASENA;
import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CORREO;

/**
 * Controlador de autenticación y gestión de acceso de usuarios.
 *
 * <p>
 * Centraliza los flujos de login, registro, recuperación de contraseña
 * y acceso al perfil del usuario autenticado.
 * </p>
 */
@Slf4j
@Controller
public class AuthController {

    // Constantes de vistas
    public static final String VISTA_LOGIN = "auth/login";
    public static final String VISTA_REGISTRO = "auth/registro";
    public static final String VISTA_RECUPERACION_CONTRASENA = "auth/recuperacion";
    public static final String VISTA_CAMBIAR_CONTRASENA = "auth/perfil/editarContrasena";
    public static final String VISTA_RESTABLECER_CONTRASENA = "auth/restablecerContrasena";

    // Rutas
    public static final String RUTA_REDIRECT = "redirect:";
    public static final String RUTA_LOGIN = "/login";
    public static final String RUTA_REGISTRO = "/registro";
    public static final String RUTA_RECUPERACION = "/recuperacion";
    public static final String RUTA_RESTABLECER_CONTRASENA = "/auth/reset-password";
    public static final String RUTA_CAMBIAR_CONTRASENA = "/change-password";


    @Value("${app.url}")
    public static String URL;

    public static final String URL_RESTABLECER_CONTRASENA =
            URL + RUTA_RESTABLECER_CONTRASENA;

    private final ProvinciaService provinciaService;
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RegistroFormValidator registroFormValidator;
    private final LoginFormValidator loginFormValidator;
    private final RecuperacionFormValidator recuperacionFormValidator;
    private final PerfilRestablecerContrasenaFormValidator restablecerContrasenaFormValidator;
    private final PerfilCambioContrasenaFormValidator perfilCambioContrasenaFormValidator;
    private final CombustibleRepository combustibleRepository;

    /**
     * Constructor con inyección de dependencias de servicios y validadores.
     */
    public AuthController(AuthService authService,
                          UsuarioService usuarioService,
                          ProvinciaService provinciaService,
                          RegistroFormValidator registroFormValidator,
                          LoginFormValidator loginFormValidator,
                          RecuperacionFormValidator recuperacionFormValidator,
                          PerfilRestablecerContrasenaFormValidator restablecerContrasenaFormValidator,
                          PerfilCambioContrasenaFormValidator perfilCambioContrasenaFormValidator,
                          CombustibleRepository combustibleRepository) {
        this.authService                         = authService;
        this.usuarioService                      = usuarioService;
        this.provinciaService                    = provinciaService;
        this.registroFormValidator               = registroFormValidator;
        this.loginFormValidator                  = loginFormValidator;
        this.recuperacionFormValidator           = recuperacionFormValidator;
        this.restablecerContrasenaFormValidator = restablecerContrasenaFormValidator;
        this.perfilCambioContrasenaFormValidator = perfilCambioContrasenaFormValidator;
        this.combustibleRepository              = combustibleRepository;
    }

    /**
     * Procesa el formulario de login.
     *
     * <p>
     * Valida los datos introducidos y delega la autenticación en el
     * servicio de autenticación. Si el login es correcto, se inicia
     * la sesión y se redirige al perfil.
     * </p>
     */
    @PostMapping(RUTA_LOGIN)
    public String doPostLogin(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                              BindingResult bindingResult,
                              HttpServletRequest request) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        // Validación del formulario
        loginFormValidator.validate(usuarioDto, bindingResult);
        // hay errores -> vista de login
        if (bindingResult.hasErrors()) return VISTA_LOGIN;
        //Realizar el Login
        try {
            if (authService.login(request, usuarioDto.getCorreo(), usuarioDto.getContrasena()) == null) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                          "Error.auth.login.fallo.sesion");
                return VISTA_LOGIN;
            }
            return RUTA_REDIRECT + RUTA_PERFIL;
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                      "Error.auth.login.fallo.sesion");
            return VISTA_LOGIN;
        }
    }

    @GetMapping(RUTA_LOGIN)
    public String doGetLoginAndHandleGoogleOauth(
            @RequestParam(value = "access_token", required = false) String accessToken,
            @RequestParam(value = "error", required = false) String error, Model model,
            HttpServletRequest request) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;

        if (error != null) {
            log.error("Error devuelto desde el OAuth de Supabase: {}", error);
            return RUTA_REDIRECT + RUTA_PERFIL + "?error=oauth";
        }

        // Si Supabase envía los parámetros en el Query Server-Side:
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                var sbUser = authService.getSupabaseUserData(accessToken);
                UUID uuidUsuario = UUID.fromString(sbUser.id());

                // 2. Autenticamos al usuario en el contexto de Spring Security
                authService.login(request, accessToken, uuidUsuario, true);

                log.info("Usuario logueado con Google exitosamente: {}", uuidUsuario);
                return RUTA_REDIRECT + RUTA_PERFIL;

            } catch (Exception e) {
                log.error("Error al procesar el login de Google", e);
                return RUTA_REDIRECT + RUTA_LOGIN + "?error=true";
            }
        }
        model.addAttribute("usuario", new UsuarioDto());
        return VISTA_LOGIN;
    }

    /**
     * Controlador encargado del cierre de sesión de usuarios.
     * Primero se encarga de cerrar la sesión en Supabase Auth y luego
     * invalida la sesión web actual.
     *
     * @param request petición HTTP entrante
     * @return redirección a la página de login tras el logout
     */
    @PostMapping("/logout")
    public String doPostLogout(HttpServletRequest request) {
        Optional<String> token = authService.getTokenActual();
        if (token.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        // Llamar a Supabase Auth para cerrar sesión
        try {
            authService.logout(token.get());
        } catch (IOException | IllegalStateException e) {
            return RUTA_REDIRECT + RUTA_PERFIL;
        }
        // Borrar cualquier cookie/session web (por si acaso)
        request.getSession(false).invalidate();
        // Redirigir al login con mensaje de exito
        return RUTA_REDIRECT + RUTA_LOGIN + "?logout=true";
    }


    /**
     * Muestra el formulario de registro junto con los datos necesarios
     * para su cumplimentación (provincias).
     */
    @GetMapping(RUTA_REGISTRO)
    public String doGetRegistro(Model model) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        model.addAttribute("usuario", new UsuarioDto());
        model.addAttribute("pagina", "registro");
        model.addAttribute("provincias", provinciaService.getProvinciasOrderByDenominacion());
        return returnRegistroConDatos(model);
    }

    /**
     * Devuelve la vista de registro asegurando que el modelo contiene
     * los datos auxiliares necesarios.
     * <p>
     * Supone la reutilización del código para evitar duplicación
     */
    private String returnRegistroConDatos(Model model) {
        model.addAttribute("pagina", "registro");
        model.addAttribute("provincias", provinciaService.getProvinciasOrderByDenominacion());
        return VISTA_REGISTRO;
    }

    /**
     * Procesa el formulario de registro de usuario.
     *
     * <p>
     * Válida los datos, registra el usuario en el sistema de autenticación,
     * persiste el usuario en la base de datos local y realiza el login
     * automático tras un registro exitoso.
     * </p>
     */
    @PostMapping(RUTA_REGISTRO)
    public String doPostRegistro(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        // Validación del formulario
        registroFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de registro
        if (bindingResult.hasErrors()) {
            return returnRegistroConDatos(model);
        }
        //  Registro del usuario
        try {
            usuarioDto.setUuid(authService.registro(usuarioDto));
        } catch (IOException | IllegalStateException e) {
            log.error("Ha ocurrido un error", e);
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA, "Error.auth.login.registro.fallo");
            return returnRegistroConDatos(model);
        }
        Optional<Provincia> provincia = provinciaService.getProvinciaById(usuarioDto.getProvinciaFavorita());
        // Error inesperado en el servicio de autenticación
        if (provincia.isEmpty()) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA, "Error.auth.login.registro.fallo");
            model.addAttribute("usuario", new UsuarioDto());
            return returnRegistroConDatos(model);
        }
        //  Persistencia del usuario
        Usuario usuario = new Usuario(
                usuarioDto.getUuid(),
                provincia.get()
        );
        usuario.setNombre(usuarioDto.getNombre());
        usuario.setEmail(usuarioDto.getCorreo());
        usuario.setCombustiblesFavoritos(new HashSet<>(combustibleRepository.findAll()));
        usuarioService.save(usuario);

        //Realizar el Login
        try {
            if (authService.login(request, usuarioDto.getCorreo(), usuarioDto.getContrasena()) == null) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                          "Error.auth.login.fallo.sesion");
                return returnRegistroConDatos(model);
            }
            return RUTA_REDIRECT + RUTA_PERFIL;
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                      "Error.auth.login.fallo.sesion");
            usuarioService.delete(usuario.getUuid());
            return returnRegistroConDatos(model);
        }
    }

    /**
     * Muestra el formulario de recuperación de contraseña.
     */
    @GetMapping(RUTA_RECUPERACION)
    public String doGetRecuperacion(Model model) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        model.addAttribute("usuario", new UsuarioDto());
        return VISTA_RECUPERACION_CONTRASENA;
    }

    /**
     * Procesa la solicitud de recuperación de contraseña.
     *
     * <p>
     * Validación del correo introducido y delega en el servicio de autenticación
     * el envío del proceso de recuperación.
     * </p>
     */
    @PostMapping(RUTA_RECUPERACION)
    public String doPostRecuperacion(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult, Model model) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        // Validación del formulario
        recuperacionFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors()) return VISTA_RECUPERACION_CONTRASENA;

        //  Registro del usuario
        try {
            if (!authService.recuperacionDeContrasena(usuarioDto.getCorreo())) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                          "Error.auth.recuperacion.fallo");
            } else model.addAttribute("exito", true);
            return VISTA_RECUPERACION_CONTRASENA;
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                      "Error.auth.recuperacion.fallo");
            return VISTA_RECUPERACION_CONTRASENA;
        }
    }

    /**
     * Procesa el formulario de cambio de contraseña
     *
     * <p>
     * Válida los datos, registra el usuario en el sistema de autenticación,
     * persiste el usuario en la base de datos local y realiza el login
     * automático tras un registro exitoso.
     * </p>
     */
    @PostMapping(RUTA_CAMBIAR_CONTRASENA)
    public String doPostCambioDeContrasena(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult, HttpServletRequest request, Model model) {
        // Comprobamos que el usuario está autenticado
        Optional<Usuario> usuarioAutenticado = authService.getUsuarioAutenticado();
        if (usuarioAutenticado.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        // Validación del formulario
        perfilCambioContrasenaFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de registro

        if (bindingResult.hasErrors()) return VISTA_CAMBIAR_CONTRASENA;

        // Validaciones manuales
        // Se comprueba que el usuario existe en el sistema de autenticación con esa contraseña
        String token;
        try {
            token = authService.login(request, usuarioAutenticado.get().getEmail(),
                                      usuarioDto.getCorreo()); //dto.correo guarda la contraseña
            if (token == null) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                          "Error.auth.change.password.contrasena.actual.incorrecta");
                return VISTA_CAMBIAR_CONTRASENA;
            }
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                      "Error.auth.change.password.validacion.usuario.fallo");
            return VISTA_CAMBIAR_CONTRASENA;
        }
        //  Realizamos el cambio de contraseña del usuario
        try {
            if (!authService.modificarContrasenaUsuario(token,
                                                        usuarioDto.getContrasena())) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                          "Error.auth.change.password.validacion.usuario.fallo");
                return VISTA_CAMBIAR_CONTRASENA;
            }
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                      "Error.auth.change.password.fallo");
            return returnRegistroConDatos(model);
        }
        return RUTA_REDIRECT + RUTA_PERFIL + "?passwordChanged=true";
    }

    /**
     * Muestra la vista del formulario para restablecer la contraseña.
     * Se inicializa el objeto UsuarioDto vacío para que Thymeleaf pueda ligar los campos.
     */
    @GetMapping(RUTA_RESTABLECER_CONTRASENA)
    public String doGetRestablecerContrasena(Model model) {
        if (authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_PERFIL;
        model.addAttribute("usuario", new UsuarioDto());

        return VISTA_RESTABLECER_CONTRASENA;
    }


    /**
     * Procesa la solicitud de restablecimiento de la contraseña utilizando
     * el validador del Framework de Spring y el DTO correspondiente.
     * </p>
     */
    @PostMapping(RUTA_RESTABLECER_CONTRASENA)
    public String doPostRestablecerContrasena(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult, Model model) {
        restablecerContrasenaFormValidator.validate(usuarioDto, bindingResult);

        String token = usuarioDto.getToken();

        if (token == null || token.isBlank()) return RUTA_REDIRECT + RUTA_LOGIN;

        if (bindingResult.hasErrors()) return VISTA_RESTABLECER_CONTRASENA;


        try {
            boolean ok = authService.restablecerContrasena(token,
                                                           usuarioDto.getContrasena());

            if (!ok) {
                bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                          "Error.auth.change.password.fallo");
                return VISTA_RESTABLECER_CONTRASENA;
            }

        } catch (IOException e) {
            log.error("Error al restablecer contraseña en Supabase", e);
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                      "Error.auth.change.password.fallo");
            return VISTA_RESTABLECER_CONTRASENA;
        }

        // Redirección al login con éxito
        return RUTA_REDIRECT + RUTA_LOGIN + "?passwordChanged=true";
    }
}
