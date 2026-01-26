package com.inggarciabaldo.carburo.backend.controllers;

import com.inggarciabaldo.carburo.backend.dto.UsuarioDto;
import com.inggarciabaldo.carburo.backend.entities.Provincia;
import com.inggarciabaldo.carburo.backend.entities.Usuario;
import com.inggarciabaldo.carburo.backend.services.AuthService;
import com.inggarciabaldo.carburo.backend.services.ProvinciaService;
import com.inggarciabaldo.carburo.backend.services.UsuarioService;
import com.inggarciabaldo.carburo.backend.validators.LoginFormValidator;
import com.inggarciabaldo.carburo.backend.validators.PerfilCambioContrasenaFormValidator;
import com.inggarciabaldo.carburo.backend.validators.RecuperacionFormValidator;
import com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.Optional;

import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CONTRASENA;
import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_CORREO;

/**
 * Controlador de autenticación y gestión de acceso de usuarios.
 *
 * <p>
 * Centraliza los flujos de login, registro, recuperación de contraseña
 * y acceso al perfil del usuario autenticado.
 * </p>
 */
@Controller
public class AuthController {

    // Constantes de vistas
    public static final String VISTA_LOGIN = "auth/login";
    public static final String VISTA_REGISTRO = "auth/registro";
    public static final String VISTA_RECUPERACION_CONTRASENA = "auth/recuperacion";
    public static final String VISTA_CAMBIAR_CONTRASENA = "auth/perfil/editarContrasena";

    private final ProvinciaService provinciaService;
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final RegistroFormValidator registroFormValidator;
    private final LoginFormValidator loginFormValidator;
    private final RecuperacionFormValidator recuperacionFormValidator;
    private final PerfilCambioContrasenaFormValidator perfilCambioContrasenaFormValidator;

    /**
     * Constructor con inyección de dependencias de servicios y validadores.
     */
    public AuthController(AuthService authService,
                          UsuarioService usuarioService,
                          ProvinciaService provinciaService,
                          RegistroFormValidator registroFormValidator,
                          LoginFormValidator loginFormValidator,
                          RecuperacionFormValidator recuperacionFormValidator,
                          PerfilCambioContrasenaFormValidator perfilCambioContrasenaFormValidator) {
        this.authService                         = authService;
        this.usuarioService                      = usuarioService;
        this.provinciaService                    = provinciaService;
        this.registroFormValidator               = registroFormValidator;
        this.loginFormValidator                  = loginFormValidator;
        this.recuperacionFormValidator           = recuperacionFormValidator;
        this.perfilCambioContrasenaFormValidator = perfilCambioContrasenaFormValidator;
    }

    /**
     * Muestra la vista de login si el usuario no está autenticado.
     * Si ya existe sesión activa, redirige directamente al perfil.
     */
    @GetMapping("/login")
    public String doGetLogin(Model model) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
        model.addAttribute("usuario", new UsuarioDto());
        return VISTA_LOGIN;
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
    @PostMapping("/login")
    public String doPostLogin(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                              BindingResult bindingResult,
                              HttpServletRequest request) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
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
            return "redirect:/perfil";
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CONTRASENA,
                                      "Error.auth.login.fallo.sesion");
            return VISTA_LOGIN;
        }
    }

    /**
     * Muestra el formulario de registro junto con los datos necesarios
     * para su cumplimentación (provincias).
     */
    @GetMapping("/registro")
    public String doGetRegistro(Model model) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
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
    @PostMapping("/registro")
    public String doPostRegistro(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model
    ) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
        // Validación del formulario
        registroFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de registro
        if (bindingResult.hasErrors()) {
            return returnRegistroConDatos(model);
        }
        //  Registro del usuario
        try {
            usuarioDto = authService.registro(usuarioDto);
        } catch (IOException | IllegalStateException e) {
            bindingResult.rejectValue("contraseña", "Error.auth.login.registro.fallo");
            return returnRegistroConDatos(model);
        }
        Optional<Provincia> provincia = provinciaService.getProvinciaById(usuarioDto.getProvinciaFavorita());
        // Error inesperado en el servicio de autenticación
        if (usuarioDto == null || provincia.isEmpty()) {
            bindingResult.rejectValue("contraseña", "Error.auth.login.registro.fallo");
            model.addAttribute("usuario", new UsuarioDto());
            return returnRegistroConDatos(model);
        }
        //  Persistencia del usuario
        Usuario usuario = new Usuario(
                usuarioDto.getUuid(),
                usuarioDto.getNombre(),
                usuarioDto.getCorreo(),
                provincia.get()
        );
        usuarioService.save(usuario);

        //Realizar el Login
        authService.login(request, usuarioDto.getToken(), usuario.getUuid());

        return "redirect:/perfil";
    }

    /**
     * Muestra el formulario de recuperación de contraseña.
     */
    @GetMapping("/recuperacion")
    public String doGetRepuceracion(Model model) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
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
    @PostMapping("/recuperacion")
    public String doGetRepuceracion(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                                    BindingResult bindingResult) {
        if (authService.isAuthenticated()) return "redirect:/perfil";
        // Validación del formulario
        recuperacionFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors()) return VISTA_RECUPERACION_CONTRASENA;

        //  Registro del usuario
        try {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_CORREO,
                                      (authService.recuperacionDeContrasena(
                                              usuarioDto.getCorreo())) ?
                    "Error.auth.recuperacion.exito" : "Error.auth.recuperacion.fallo");
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
    @PostMapping("/change-password")
    public String doPostCambioDeContrasena(
            @ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
            BindingResult bindingResult, HttpServletRequest request, Model model) {
        // Comprobamos que el usuario está autenticado
        Optional<Usuario> usuarioAutenticado = authService.getUsuarioAutenticado();
        if (usuarioAutenticado.isEmpty()) return "redirect:/login";
        // Validación del formulario
        perfilCambioContrasenaFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de registro

        if (bindingResult.hasErrors()) return VISTA_CAMBIAR_CONTRASENA;

        // Validaciones manuales
        // Se comprueba que el usuario existe en el sistema de autenticación con esa contraseña
        String token = null;
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
        return "redirect:/perfil?passwordChanged=true";
    }
}
