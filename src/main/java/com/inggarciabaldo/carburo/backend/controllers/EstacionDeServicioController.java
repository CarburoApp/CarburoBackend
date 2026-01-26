package com.inggarciabaldo.carburo.backend.controllers;

import com.inggarciabaldo.carburo.backend.dto.CombustibleDto;
import com.inggarciabaldo.carburo.backend.entities.*;
import com.inggarciabaldo.carburo.backend.services.*;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;

@Controller
@RequestMapping("/estacionesDeServicio")
public class EstacionDeServicioController {

	private final EstacionDeServicioService estacionDeServicioService;
	private final ComunidadAutonomaService comunidadAutonomaService;
	private final ProvinciaService provinciaService;
	private final MunicipioService municipioService;
	private final CombustibleService combustibleService;

	/**
	 * Constructor con inyección de dependencias de servicios y validadores.
	 */
	public EstacionDeServicioController(
			EstacionDeServicioService estacionDeServicioService,
			ComunidadAutonomaService comunidadAutonomaService,
			ProvinciaService provinciaService, MunicipioService municipioService,
			CombustibleService combustibleService) {
		this.estacionDeServicioService = estacionDeServicioService;
		this.comunidadAutonomaService  = comunidadAutonomaService;
		this.provinciaService          = provinciaService;
		this.municipioService          = municipioService;
		this.combustibleService        = combustibleService;
	}


	/**
	 * Lista estaciones de servicio con filtros opcionales y paginación.
	 *
	 * @param model                 Modelo para la vista
	 * @param pageable              Paginación automática
	 * @param comunidadSeleccionada ID de Comunidad Autónoma para filtrar
	 * @param provinciaSeleccionada ID de Provincia para filtrar
	 * @param municipioSeleccionado ID de Municipio para filtrar
	 * @param busqueda              Texto libre para búsqueda
	 * @param combustibles          Array de códigos de combustibles
	 * @param estadoSeleccionado    Estado de la estación: ABIERTO, CERRADO o INDEFERENTE
	 * @param marcaSeleccionada     Marca de la estación
	 * @param ordenSeleccionado     Criterio de orden: name_asc, name_desc, price_asc, price_desc
	 * @return Vista "eess/list" con la información filtrada
	 */
	@GetMapping("")
	public String doGetEstacionesDeServicio(Model model, Pageable pageable,
											@RequestParam(required = false)
										  String comunidadSeleccionada,
											@RequestParam(required = false)
										  String provinciaSeleccionada,
											@RequestParam(required = false)
										  String municipioSeleccionado,
											@RequestParam(required = false)
										  String ordenSeleccionado,
											@RequestParam(required = false)
										  String marcaSeleccionada,
											@RequestParam(required = false)
											String busqueda,
											@RequestParam(required = false)
										  String[] combustibles,
											@RequestParam(required = false)
										  String estadoSeleccionado) {
		model.addAttribute("pagina", "eess");

		// -------------------------
		// Valores por defecto al principio
		// -------------------------
		model.addAttribute("comunidadSeleccionada",
						   comunidadSeleccionada != null ? comunidadSeleccionada : "0");
		model.addAttribute("provinciaSeleccionada",
						   provinciaSeleccionada != null ? provinciaSeleccionada : "0");
		model.addAttribute("municipioSeleccionado",
						   municipioSeleccionado != null ? municipioSeleccionado : "0");

		String ordenFinal = ordenSeleccionado != null ? ordenSeleccionado : "name_asc";
		String estadoFinal =
				estadoSeleccionado != null ? estadoSeleccionado : "INDEFERENTE";
		model.addAttribute("ordenSeleccionado", ordenFinal);
		model.addAttribute("estadoSeleccionado", estadoFinal);
		model.addAttribute("busqueda", busqueda != null ? busqueda : "");
		model.addAttribute("marcaSeleccionada",
						   marcaSeleccionada != null ? marcaSeleccionada : "0NOTSELECTED");

		// --- Combustibles disponibles ---
		List<CombustibleDto> combustiblesDTO = combustibleService.getCombustiblesDto();

		// Si no hay selección, simplemente pasamos la lista tal cual
		if (combustibles != null && combustibles.length > 0)
			// Recorremos la lista de DTOs
			for (CombustibleDto dto : combustiblesDTO)
				// Si el código del DTO está en el array de parámetros "combustibles", lo marcamos
				dto.isSeleccionado = Arrays.asList(combustibles)
						.contains(dto.getCodigo());

		model.addAttribute("combustibles", combustiblesDTO);


		// --- Comunidades autónomas ---
		List<ComunidadAutonoma> comunidadesAutonomas = comunidadAutonomaService.getComunidadesAutonomas();
		model.addAttribute("comunidades", comunidadesAutonomas);

		// --- Provincias y municipios según selección ---
		List<Provincia> provincias = Collections.emptyList();
		List<Municipio> municipiosList = Collections.emptyList();
		model.addAttribute("provincias", provincias);
		model.addAttribute("municipios", municipiosList);


		if (comunidadSeleccionada != null && !comunidadSeleccionada.equals("0")) {
			short idCCAA = Short.parseShort(comunidadSeleccionada);
			Optional<ComunidadAutonoma> ccaaOpt = comunidadesAutonomas.stream()
					.filter(c -> c.getId() == idCCAA).findFirst();
			if (ccaaOpt.isPresent()) {
				ComunidadAutonoma ccaa = ccaaOpt.get();
				model.addAttribute("comunidadSeleccionada", ccaa.getId());

				provincias = provinciaService.getProvinciasByComunidadAutonoma(ccaa);
				model.addAttribute("provincias", provincias);
				if (provincias.size() == 1) provinciaSeleccionada = String.valueOf(
						provincias.getFirst().getId());

				if (provinciaSeleccionada != null && !provinciaSeleccionada.equals("0")) {
					short idProvincia = Short.parseShort(provinciaSeleccionada);
					Optional<Provincia> provOpt = provincias.stream()
							.filter(p -> p.getId() == idProvincia).findFirst();

					if (provOpt.isPresent()) {
						Provincia prov = provOpt.get();
						model.addAttribute("provinciaSeleccionada", prov.getId());

						municipiosList = municipioService.getMunicipiosByProvinciaConEESSAsociadas(
								prov);
						model.addAttribute("municipios", municipiosList);

						if (municipioSeleccionado != null &&
								!municipioSeleccionado.equals("0")) {
							short idMunicipio = Short.parseShort(municipioSeleccionado);
							municipiosList.stream().filter(m -> m.getId() == idMunicipio)
									.findFirst().ifPresent(
											m -> model.addAttribute("municipioSeleccionado",
																	m.getId()));
						}
					}
				}
			}
		}

		// --- Filtrado y paginación ---
		List<EstacionDeServicio> estaciones = estacionDeServicioService.filtrarEstaciones(
				comunidadSeleccionada, provinciaSeleccionada, municipioSeleccionado,
				busqueda, combustibles, estadoSeleccionado, marcaSeleccionada,
				ordenSeleccionado);
		model.addAttribute("page",
						   estacionDeServicioService.toPage(estaciones, pageable));

		// --- Marcas disponibles ---
		List<String> marcas = estacionDeServicioService.getMarcasFromEESS(estaciones);
		model.addAttribute("marcas", marcas);

		// --- Marca seleccionada ---
		String marcaFinal = (marcaSeleccionada != null && marcas.contains(
				marcaSeleccionada)) ? marcaSeleccionada : "0NOTSELECTED"; // Si no hay selección o la selección no es válida
		model.addAttribute("marcaSeleccionada", marcaFinal);

		return "eess/list";
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
	 * @return nombre de la vista "/eess/estacionDeServicio" o redirección a "/estacionDeServicio/list" si el ID no es válido o la estación no existe
	 */
	@GetMapping("/{id}")
	public String doGet(@PathVariable Long id,
						@RequestParam(name = "fechaInicio", required = false)
						@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
						LocalDate fechaInicio, Model model) {
		// Validación rápida del ID
		if (id <= 0) return "redirect:/estacionDeServicio/list";

		// Se obtiene la estación de servicio por ID
		EstacionDeServicio estacion = estacionDeServicioService.getEstacionDeServicioById(
				Math.toIntExact(id));

		// Si la estación no existe, se redirige a la página principal
		if (estacion == null) {
			return "redirect:/estacionDeServicio/list";
		}

		LocalDate hoy = LocalDate.now();

		// Añadimos la estación al modelo para la vista
		model.addAttribute("estacion", estacion);

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
		return "/eess/estacionDeServicio";
	}
}

