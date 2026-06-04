package app.carburo.backend.controllers;

import app.carburo.backend.dto.UsuarioDto;
import app.carburo.backend.entities.EstacionDeServicio;
import app.carburo.backend.entities.Provincia;
import app.carburo.backend.entities.Usuario;
import app.carburo.backend.services.AuthService;
import app.carburo.backend.services.EstacionDeServicioService;
import app.carburo.backend.services.ProvinciaService;
import app.carburo.backend.services.UsuarioService;
import app.carburo.backend.validators.PerfilCambioDeNombreFormValidator;
import app.carburo.backend.validators.PerfilCambioDeProvinciaFavoritaFormValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static app.carburo.backend.controllers.AuthController.RUTA_CAMBIAR_CONTRASENA;
import static app.carburo.backend.controllers.AuthController.RUTA_LOGIN;
import static app.carburo.backend.controllers.EstacionDeServicioController.*;
import static app.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_PROVINCIA;


/**
 * Controlador de gestión del perfil del usuario autenticado.
 *
 * <p>
 * Permite consultar y modificar los datos personales del usuario,
 * como el nombre o la provincia favorita, así como acceder a
 * funcionalidades relacionadas con su perfil.
 * </p>
 */
@Controller
@RequestMapping(UsuarioController.RUTA_PERFIL)
public class UsuarioController {

    // Constantes de vistas
    public static final String VISTA_PERFIL = "auth/perfil/perfil";
    public static final String VISTA_EDITAR_NOMBRE = "auth/perfil/editarNombre";
    public static final String VISTA_EDITAR_CONTRASENA = "auth/perfil/editarContrasena";
    public static final String VISTA_EDITAR_PROVINCIA = "auth/perfil/editarProvinciaFavorita";
    public static final String VISTA_ESTACIONES_DE_SERVICIO_FAVORITAS = "eess/favoritas";

    // Ruta
    public static final String RUTA_REDIRECT = "redirect:";
    public static final String RUTA_PERFIL = "/perfil";
    // A las siguientes rutas les hace falta al comienzo /perfil/....
    public static final String RUTA_PERFIL_EDITAR_NOMBRE_SIN_PERFIL = "/editarNombre";
    public static final String RUTA_PERFIL_EDITAR_PROVINCIA_SIN_PERFIL = "/editarProvincia";

    private final ProvinciaService provinciaService;
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final EstacionDeServicioService estacionDeServicioService;
    private final PerfilCambioDeProvinciaFavoritaFormValidator perfilCambioDeProvinciaFavoritaFormValidator;
    private final PerfilCambioDeNombreFormValidator perfilCambioDeNombreFormValidator;

    /**
     * Constructor con inyección de dependencias de servicios y validadores.
     */
    public UsuarioController(AuthService authService,
                             UsuarioService usuarioService,
                             ProvinciaService provinciaService,
                             EstacionDeServicioService estacionDeServicioService,
                             PerfilCambioDeProvinciaFavoritaFormValidator perfilCambioDeProvinciaFavoritaFormValidator,
                             PerfilCambioDeNombreFormValidator perfilCambioDeNombreFormValidator) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.provinciaService = provinciaService;
        this.estacionDeServicioService = estacionDeServicioService;
        this.perfilCambioDeProvinciaFavoritaFormValidator = perfilCambioDeProvinciaFavoritaFormValidator;
        this.perfilCambioDeNombreFormValidator = perfilCambioDeNombreFormValidator;
    }

    /**
     * Muestra la página de perfil del usuario autenticado.
     * <p>
     * Recupera el usuario autenticado y expone sus datos básicos
     * a la vista. Si no hay sesión válida, redirige al login.
     *
     * @param model Modelo MVC para la vista
     * @return Vista del perfil o redirección a login
     */
    @GetMapping("")
    public String doGetPerfil(Model model) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        model.addAttribute("avatar_url", usuario.get().getAvatarUrl());
        model.addAttribute("nombre", usuario.get().getNombre());
        model.addAttribute("correo", usuario.get().getEmail());
        model.addAttribute("provincia", usuario.get().getProvinciaFavorita().getDenominacion());
        return VISTA_PERFIL;
    }

    /**
     * Muestra el formulario de cambio de nombre del perfil.
     *
     * <p>
     * Inicializa el DTO con los datos actuales del usuario.
     * </p>
     */
    @GetMapping(RUTA_PERFIL_EDITAR_NOMBRE_SIN_PERFIL)
    public String doGetCambioDeNombrePerfil(Model model) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setUuid(usuario.get().getUuid());
        usuarioDto.setNombre(usuario.get().getNombre());
        model.addAttribute("usuario", usuarioDto);
        return VISTA_EDITAR_NOMBRE;
    }

    /**
     * Procesa el cambio de nombre del usuario.
     *
     * <p>
     * Valida el nuevo nombre y actualiza el registro del usuario
     * si los datos son correctos.
     * </p>
     */
    @PostMapping(RUTA_PERFIL_EDITAR_NOMBRE_SIN_PERFIL)
    public String doPostCambioDeNombrePerfil(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                                             BindingResult bindingResult)
            throws IOException {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        // Validación del formulario
        perfilCambioDeNombreFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors() || authService.getTokenActual().isEmpty())
            return VISTA_EDITAR_NOMBRE;
        //  Registro del usuario
        authService.modificarNombreUsuario(authService.getTokenActual().get(),
                                           usuarioDto.getNombre());
        return RUTA_REDIRECT + RUTA_PERFIL;
    }

    /**
     * Muestra el formulario de cambio de provincia favorita.
     *
     * <p>
     * Carga el listado de provincias y establece la provincia
     * actual del usuario como valor inicial.
     * </p>
     */
    @GetMapping(RUTA_PERFIL_EDITAR_PROVINCIA_SIN_PERFIL)
    public String doGetCambioDeProvinciaFavoritaPerfil(Model model) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        // Creo el DTO con los datos necesarios del usuario
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setUuid(usuario.get().getUuid());
        usuarioDto.setProvinciaFavorita(usuario.get().getProvinciaFavorita().getId());
        // Paso los datos necesarios
        model.addAttribute("provincias", provinciaService.getProvinciasOrderByDenominacion());
        model.addAttribute("usuario", usuarioDto);
        return VISTA_EDITAR_PROVINCIA;
    }

    /**
     * Procesa el cambio de provincia favorita del usuario.
     *
     * <p>
     * Valida la selección y actualiza la provincia asociada
     * al usuario en la base de datos.
     * </p>
     */
    @PostMapping(RUTA_PERFIL_EDITAR_PROVINCIA_SIN_PERFIL)
    public String doPostCambioDeProvinciaFavoritaPerfil(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                                                        BindingResult bindingResult) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;
        // Validación del formulario
        perfilCambioDeProvinciaFavoritaFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors()) return VISTA_EDITAR_PROVINCIA;

        Optional<Provincia> provincia = provinciaService.getProvinciaById(usuarioDto.getProvinciaFavorita());
        // Error inesperado en el servicio de autenticación
        if (provincia.isEmpty()) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_PROVINCIA, "Error.auth.login.registro.fallo");
            return VISTA_EDITAR_PROVINCIA;
        }
        usuarioService.updateProvinciaFavoritaUsuario(usuario.get().getUuid(), provincia.get());
        return RUTA_REDIRECT + RUTA_PERFIL;
    }

    /**
     * Muestra la vista de cambio de contraseña del perfil.
     *
     * <p>
     * Actualmente pendiente de implementación.
     * </p>
     */
    @GetMapping(RUTA_CAMBIAR_CONTRASENA)
    public String doGetCambioDeContrasena(Model model) {
        if (!authService.isAuthenticated()) return RUTA_REDIRECT + RUTA_LOGIN;
        model.addAttribute("usuario", new UsuarioDto());
        return VISTA_EDITAR_CONTRASENA;
    }

    /**
     * Redirige al listado de estaciones de servicio favoritas por usuario
     */
    @GetMapping(RUTA_ESTACIONES_DE_SERVICIO_FAVORITAS_SIN_PERFIL)
    public String doGetEstacionesDeServicioFavoritas(Model model) {
        model.addAttribute("pagina", "eessFav");
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_ESTACIONES_DE_SERVICIO;
        Set<EstacionDeServicio> eess = usuario.get().getEessFavoritas();
        model.addAttribute("eess", eess);
        return VISTA_ESTACIONES_DE_SERVICIO_FAVORITAS;
    }

    /**
     * Se encarga de la gestión de la estación de servicio favorita del usuario.
     * Añade o retira a las estaciones de servicio favoritas del usuario la estación
     * de servicio cuyo id se recibe como parámetro.
     */
    @PostMapping(RUTA_ESTACION_DE_SERVICIO_FAVORITA_SIN_PERFIL + "{id}")
    public String doPostAddRemoveEstacionDeServicioFavorita(@PathVariable Long id) {
        // Usuario autenticado
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return RUTA_REDIRECT + RUTA_LOGIN;

        // Validación rápida del ID
        if (id <= 0)
            return RUTA_REDIRECT + RUTA_ESTACIONES_DE_SERVICIO_FAVORITAS_SIN_PERFIL;

        // Se obtiene la estación de servicio por ID
        EstacionDeServicio estacion = estacionDeServicioService.getEstacionDeServicioById(
                Math.toIntExact(id));

        // Si la estación no existe, se redirige a la página principal
        if (estacion == null) {
            return RUTA_REDIRECT + RUTA_ESTACIONES_DE_SERVICIO_FAVORITAS_SIN_PERFIL;
        }

        if (usuarioService.isEstacionDeServicioFavorita(usuario.get(), estacion)) {
            // La estación ya es favorita, se elimina
            usuarioService.removeEstacionDeServicioFavorita(usuario.get(), estacion);
        } else {
            // La estación no es favorita, se añade
            usuarioService.addEstacionDeServicioFavorita(usuario.get(), estacion);
        }
        return RUTA_REDIRECT + RUTA_ESTACION_DE_SERVICIO + id;
    }
}