package app.carburo.backend.controllers;

import app.carburo.backend.entities.Combustible;
import app.carburo.backend.entities.EstacionDeServicio;
import app.carburo.backend.entities.Usuario;
import app.carburo.backend.seo.SitemapGroup;
import app.carburo.backend.seo.SitemapUrlProvider;
import app.carburo.backend.services.AuthService;
import app.carburo.backend.services.EstacionDeServicioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	private final AuthService authService;

	/**
	 * Constructor con inyección de dependencias de servicios y validadores.
	 */
	public EstacionDeServicioController(
			EstacionDeServicioService estacionDeServicioService,
			AuthService authService) {
		this.estacionDeServicioService = estacionDeServicioService;
		this.authService = authService;
	}

	@GetMapping("")
	public String doGetEstacionesDeServicios() {
		return VISTA_EESS_LIST;
	}

	/**
	 * Maneja las solicitudes GET a "/{id}" para mostrar los detalles de una estación de servicio.
	 *
	 * <p>Realiza las siguientes tareas:</p>
	 * <ul>
	 *     <li>Valida que el ID de la estación sea mayor que cero; en caso contrario redirige a la página principal.</li>
	 *     <li>Obtiene la estación de servicio correspondiente al ID proporcionado; si no existe, redirige a "/estacionDeServicio/list".</li>
	 *     <li>Calcula la fecha de inicio para mostrar los precios históricos de combustible:
	 *         <ul>
	 *             <li>Si no se proporciona fecha o es futura, se toma como fecha inicio hace 5 días.</li>
	 *             <li>Si la fecha proporcionada es anterior a 30 días atrás, se ajusta al límite inferior.</li>
	 *         </ul>
	 *     </li>
	 *     <li>Añade al modelo los atributos necesarios para la vista:
	 *         <ul>
	 *             <li>"estacion": la estación de servicio seleccionada.</li>
	 *             <li>"fechaInicio": la fecha de inicio calculada.</li>
	 *             <li>"preciosCombMap": un mapa de precios históricos de todos los combustibles desde la fecha inicio.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param id          ID de la estación de servicio (debe ser mayor que 0)
	 * @param fechaInicio fecha de inicio opcional para filtrar los precios históricos (formato ISO, por ejemplo "2026-01-17")
	 * @param model       modelo de Spring para pasar atributos a la vista
	 * @return nombre de la vista "/eess/estacionDeServicio" o redirección a "/estaciones-de-servicio" si el ID no es válido o la estación no existe
	 */
	@GetMapping("/{id:[0-9]+}")
	public String doGet(@PathVariable Long id,
						@RequestParam(name = "fechaInicio", required = false)
						@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
						LocalDate fechaInicio, Model model) {
		// Validación rápida del ID
		if (id <= 0) return RUTA_REDIRECT + RUTA_ESTACIONES_DE_SERVICIO;

		// Se obtiene la estación de servicio por ID
		EstacionDeServicio estacion = estacionDeServicioService.getEstacionDeServicioById(
				Math.toIntExact(id));

		// Si la estación no existe, se redirige a la página principal
		if (estacion == null) {
			return RUTA_REDIRECT + RUTA_ESTACIONES_DE_SERVICIO;
		}

		LocalDate hoy = LocalDate.now();

		// Añadimos la estación al modelo para la vista
		model.addAttribute("estacion", estacion);

		// Defino si tiene posibilidad de estar en favoritas
		// Usuario autenticado
		Optional<Usuario> usuario = authService.getUsuarioAutenticado();
		usuario.ifPresent(value -> model.addAttribute("favorita", value.getEessFavoritas()
				.contains(estacion)));
		// Si no se proporciona fechaInicio o es futura, se toman los últimos 5 días
		if (fechaInicio == null || fechaInicio.isAfter(hoy)) {
			fechaInicio = hoy.minusDays(5);
		}

		// Límite inferior de 30 días atrás
		LocalDate limiteInferior = hoy.minusDays(30);
		if (fechaInicio.isBefore(limiteInferior)) {
			fechaInicio = limiteInferior;
		}

		// Añadimos la fecha de inicio calculada al modelo
		model.addAttribute("fechaInicio", fechaInicio);

		// Obtenemos los precios históricos de todos los combustibles desde la fecha de inicio
		Map<LocalDate, Map<Combustible, Double>> preciosCombMap = estacion.getAllPreciosCombustiblesMap(fechaInicio);

		// Añadimos el mapa de precios al modelo para la vista
		model.addAttribute("preciosCombMap", preciosCombMap);

		// Retornamos la vista correspondiente a la estación de servicio
		return VISTA_EESS_DESCRIPTION;
	}
}

