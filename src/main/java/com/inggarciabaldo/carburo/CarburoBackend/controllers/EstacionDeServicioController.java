package com.inggarciabaldo.carburo.CarburoBackend.controllers;

import com.inggarciabaldo.carburo.CarburoBackend.dto.CombustibleDto;
import com.inggarciabaldo.carburo.CarburoBackend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.CarburoBackend.entities.EstacionDeServicio;
import com.inggarciabaldo.carburo.CarburoBackend.entities.Municipio;
import com.inggarciabaldo.carburo.CarburoBackend.entities.Provincia;
import com.inggarciabaldo.carburo.CarburoBackend.services.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class EstacionDeServicioController {

	private final EstacionDeServicioService estacionDeServicioService;
	private final ComunidadAutonomaService comunidadAutonomaService;
	private final ProvinciaService provinciaService;
	private final MunicipioService municipioService;
	private final CombustibleService combustibleService;

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
	 * @param principal             Usuario autenticado (no usado directamente aquí)
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
	@GetMapping("/estacionesDeServicio/list")
	public String getEstacionesDeServicio(Model model, Pageable pageable,
										  Principal principal,
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
										  @RequestParam(required = false) String busqueda,
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
		if (!(combustibles == null) && combustibles.length > 0)
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
}

