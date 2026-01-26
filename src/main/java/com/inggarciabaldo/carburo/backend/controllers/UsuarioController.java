package com.inggarciabaldo.carburo.backend.controllers;

import com.inggarciabaldo.carburo.backend.dto.UsuarioDto;
import com.inggarciabaldo.carburo.backend.entities.EstacionDeServicio;
import com.inggarciabaldo.carburo.backend.entities.Provincia;
import com.inggarciabaldo.carburo.backend.entities.Usuario;
import com.inggarciabaldo.carburo.backend.services.AuthService;
import com.inggarciabaldo.carburo.backend.services.EstacionDeServicioService;
import com.inggarciabaldo.carburo.backend.services.ProvinciaService;
import com.inggarciabaldo.carburo.backend.services.UsuarioService;
import com.inggarciabaldo.carburo.backend.validators.PerfilCambioDeNombreFormValidator;
import com.inggarciabaldo.carburo.backend.validators.PerfilCambioDeProvinciaFavoritaFormValidator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

import static com.inggarciabaldo.carburo.backend.validators.RegistroFormValidator.USUARIO_DTO_FIELD_PROVINCIA;


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
@RequestMapping("/perfil")
public class UsuarioController {

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
        if (usuario.isEmpty()) return "redirect:/login";
        model.addAttribute("nombre", usuario.get().getNombre());
        model.addAttribute("correo", usuario.get().getEmail());
        model.addAttribute("provincia", usuario.get().getProvinciaFavorita().getDenominacion());
        return "auth/perfil/perfil";
    }

    /**
     * Muestra el formulario de cambio de nombre del perfil.
     *
     * <p>
     * Inicializa el DTO con los datos actuales del usuario.
     * </p>
     */
    @GetMapping("/editarNombre")
    public String doGetCambioDeNombrePerfil(Model model) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/login";
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setUuid(usuario.get().getUuid());
        usuarioDto.setNombre(usuario.get().getNombre());
        model.addAttribute("usuario", usuarioDto);
        return "auth/perfil/editarNombre";
    }

    /**
     * Procesa el cambio de nombre del usuario.
     *
     * <p>
     * Valida el nuevo nombre y actualiza el registro del usuario
     * si los datos son correctos.
     * </p>
     */
    @PostMapping("/editarNombre")
    public String doPostCambioDeNombrePerfil(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                                             BindingResult bindingResult) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/login";
        // Validación del formulario
        perfilCambioDeNombreFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors()) return "auth/perfil/editarNombre";
        //  Registro del usuario
        usuarioService.updateNombreDeUsuario(usuario.get().getUuid(), usuarioDto.getNombre());
        return "redirect:/perfil";
    }

    /**
     * Muestra el formulario de cambio de provincia favorita.
     *
     * <p>
     * Carga el listado de provincias y establece la provincia
     * actual del usuario como valor inicial.
     * </p>
     */
    @GetMapping("/editarProvincia")
    public String doGetCambioDeProvinciaFavoritaPerfil(Model model) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/login";
        // Creo el DTO con los datos necesarios del usuario
        UsuarioDto usuarioDto = new UsuarioDto();
        usuarioDto.setUuid(usuario.get().getUuid());
        usuarioDto.setProvinciaFavorita(usuario.get().getProvinciaFavorita().getId());
        // Paso los datos necesarios
        model.addAttribute("provincias", provinciaService.getProvinciasOrderByDenominacion());
        model.addAttribute("usuario", usuarioDto);
        return "auth/perfil/editarProvinciaFavorita";
    }

    /**
     * Procesa el cambio de provincia favorita del usuario.
     *
     * <p>
     * Valida la selección y actualiza la provincia asociada
     * al usuario en la base de datos.
     * </p>
     */
    @PostMapping("/editarProvincia")
    public String doPostCambioDeProvinciaFavoritaPerfil(@ModelAttribute("usuario") @Validated UsuarioDto usuarioDto,
                                                        BindingResult bindingResult) {
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/login";
        // Validación del formulario
        perfilCambioDeProvinciaFavoritaFormValidator.validate(usuarioDto, bindingResult);
        // Si hay errores, volvemos a la vista de la recuperación de contraseña
        if (bindingResult.hasErrors()) return "auth/perfil/editarProvinciaFavorita";

        Optional<Provincia> provincia = provinciaService.getProvinciaById(usuarioDto.getProvinciaFavorita());
        // Error inesperado en el servicio de autenticación
        if (provincia.isEmpty()) {
            bindingResult.rejectValue(USUARIO_DTO_FIELD_PROVINCIA, "Error.auth.login.registro.fallo");
            return "auth/perfil/editarProvinciaFavorita";
        }
        usuarioService.updateProvinciaFavoritaUsuario(usuario.get().getUuid(), provincia.get());
        return "redirect:/perfil";
    }

    /**
     * Muestra la vista de cambio de contraseña del perfil.
     *
     * <p>
     * Actualmente pendiente de implementación.
     * </p>
     */
    @GetMapping("/change-password")
    public String doGetCambioDeContrasena(Model model) {
        if (!authService.isAuthenticated()) return "redirect:/login";
        model.addAttribute("usuario", new UsuarioDto());
        return "auth/perfil/editarContrasena";
    }

    /**
     * Redirige al listado de estaciones de servicio favoritas por usuario
     */
    @GetMapping("/estacionesDeServicioFavoritas")
    public String doGetEstacionesDeServicioFavoritas(Model model) {
        model.addAttribute("pagina", "eessFav");
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/EstacionesDeServicio";
        Set<EstacionDeServicio> eess = usuario.get().getEessFavoritas();
        model.addAttribute("eess", eess);
        return "eess/favoritas";
    }

    /**
     * Se encarga de la gestión de la estación de servicio favorita del usuario.
     * Añade o retira a las estaciones de servicio favoritas del usuario la estación
     * de servicio cuyo id se recibe como parámetro.
     */
    @PostMapping("/estacionesDeServicio/favorita/{id}")
    public String doPostAddRemoveEstacionDeServicioFavorita(@PathVariable Long id) {
        // Usuario autenticado
        Optional<Usuario> usuario = authService.getUsuarioAutenticado();
        if (usuario.isEmpty()) return "redirect:/login";

        // Validación rápida del ID
        if (id <= 0) return "redirect:/estacionesDeServicioFavoritas";

        // Se obtiene la estación de servicio por ID
        EstacionDeServicio estacion = estacionDeServicioService.getEstacionDeServicioById(
                Math.toIntExact(id));

        // Si la estación no existe, se redirige a la página principal
        if (estacion == null) {
            return "redirect:/estacionesDeServicioFavoritas";
        }

        if (usuarioService.isEstacionDeServicioFavorita(usuario.get(), estacion)) {
            // La estación ya es favorita, se elimina
            usuarioService.removeEstacionDeServicioFavorita(usuario.get(), estacion);
        } else {
            // La estación no es favorita, se añade
            usuarioService.addEstacionDeServicioFavorita(usuario.get(), estacion);
        }
        return "redirect:/estacionesDeServicio/" + id;
    }
}