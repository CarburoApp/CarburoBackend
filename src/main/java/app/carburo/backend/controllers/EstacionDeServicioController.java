package app.carburo.backend.controllers;

import app.carburo.backend.seo.SitemapGroup;
import app.carburo.backend.seo.SitemapUrlProvider;
import app.carburo.backend.services.EstacionDeServicioService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static app.carburo.backend.controllers.EstacionDeServicioController.RUTA_ESTACIONES_DE_SERVICIO;

@Controller
@RequestMapping(RUTA_ESTACIONES_DE_SERVICIO)
public class EstacionDeServicioController implements SitemapUrlProvider {

    @Override
    public SitemapGroup getSitemapGroup() { return SitemapGroup.ESTACIONES; }

    @Override
    public List<String> getSitemapPaths() {
		return estacionDeServicioService.getIds()
				.stream()
				.map(id -> RUTA_ESTACION_DE_SERVICIO + id)
				.toList();
    }

	// Constantes de vistas
	public static final String VISTA_EESS_DESCRIPTION = "eess/estacionDeServicio";
	public static final String VISTA_EESS_LIST = "eess/estaciones-de-servicio";

	// Rutas
	public static final String RUTA_REDIRECT = "redirect:";
	public static final String RUTA_ESTACIONES_DE_SERVICIO = "/estaciones-de-servicio";
	public static final String RUTA_ESTACION_DE_SERVICIO = "/estaciones-de-servicio/";
	// A las siguientes rutas les hace falta al comienzo /perfil/....
	public static final String RUTA_ESTACIONES_DE_SERVICIO_FAVORITAS_SIN_PERFIL = "/estaciones-de-servicio/favoritas";
	public static final String RUTA_ESTACION_DE_SERVICIO_FAVORITA_SIN_PERFIL = "/estaciones-de-servicio/favoritas/";

	private final EstacionDeServicioService estacionDeServicioService;

	/**
	 * Constructor con inyección de dependencias de servicios y validadores.
	 */
	public EstacionDeServicioController(EstacionDeServicioService estacionDeServicioService) {
		this.estacionDeServicioService = estacionDeServicioService;
	}

	@GetMapping("")
	public String doGetEstacionesDeServicios() {
		return VISTA_EESS_LIST;
	}

	/**
	 * Maneja las solicitudes GET a "/{id}" para mostrar los detalles de una estación de servicio.
	 *
	 * @param id          ID de la estación de servicio (debe ser mayor que 0)
	 * @param model       modelo de Spring para pasar atributos a la vista
	 * @return nombre de la vista de detalle de la estación
	 * @throws ResponseStatusException con estado 404 si el ID no es válido o la estación no existe
	 */
	@GetMapping("/{id:[0-9]+}")
	public String doGet(@PathVariable Long id, Model model) {
		if (id <= 0 || id > Integer.MAX_VALUE || estacionDeServicioService.getEstacionDeServicioById(id.intValue()) == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);

		model.addAttribute("estacionId", id);
		return VISTA_EESS_DESCRIPTION;
	}
}
