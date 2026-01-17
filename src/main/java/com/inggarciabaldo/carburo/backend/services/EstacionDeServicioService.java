package com.inggarciabaldo.carburo.backend.services;

import com.inggarciabaldo.carburo.backend.entities.*;
import com.inggarciabaldo.carburo.backend.repositories.EstacionDeServicioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar estaciones de servicio.
 * Permite obtener estaciones, filtrarlas por ubicación, combustible, estado, marca y ordenarlas.
 */
@Service
public class EstacionDeServicioService {

	private final EstacionDeServicioRepository estacionDeServicioRepository;
	private final ComunidadAutonomaService comunidadAutonomaService;
	private final CombustibleService combustibleService;

	public EstacionDeServicioService(
			EstacionDeServicioRepository estacionDeServicioRepository,
			ComunidadAutonomaService comunidadAutonomaService,
			CombustibleService combustibleService) {
		this.estacionDeServicioRepository = estacionDeServicioRepository;
		this.comunidadAutonomaService     = comunidadAutonomaService;
		this.combustibleService           = combustibleService;
	}

	/**
	 * Devuelve todas las estaciones de servicio sin paginación
	 */
	public List<EstacionDeServicio> getEstacionesDeServicio() {
		List<EstacionDeServicio> estaciones = new ArrayList<>();
		estacionDeServicioRepository.findAll().forEach(estaciones::add);
		return estaciones;
	}

	/**
	 * Devuelve todas las estaciones de servicio paginadas
	 */
	public Page<EstacionDeServicio> getEstacionesDeServicio(Pageable pageable) {
		return estacionDeServicioRepository.findAll(pageable);
	}


	/**
	 * Obtiene la estación de servicio más cercana a una ubicación dada.
	 *
	 * <p>La búsqueda se realiza utilizando coordenadas geográficas
	 * en sistema WGS84 (latitud y longitud). El cálculo de proximidad
	 * se delega al repositorio, que hace uso de funciones espaciales
	 * de PostGIS.</p>
	 *
	 * @param latitud  latitud del punto de referencia (WGS84)
	 * @param longitud longitud del punto de referencia (WGS84)
	 * @return la estación de servicio más cercana a las coordenadas indicadas,
	 *         o {@code null} si no existe ninguna
	 */
	public EstacionDeServicio getEstacionDeServicioMasProxima(double latitud, double longitud) {
		return estacionDeServicioRepository.findEstacionDeServicioMasCercana(latitud, longitud);
	}



	/**
	 * Devuelve las estaciones de servicio de una Comunidad Autónoma específica
	 */
	public List<EstacionDeServicio> getEstacionesDeServicioByComunidadAutonoma(Short id) {
		return new ArrayList<>(
				estacionDeServicioRepository.findEstacionDeServicioByComunidadAutonoma(
						id));
	}

	/**
	 * Devuelve las estaciones de servicio de una Provincia específica
	 */
	public List<EstacionDeServicio> getEstacionesDeServicioByProvincia(Short id) {
		return new ArrayList<>(
				estacionDeServicioRepository.findEstacionDeServicioByProvincia(id));
	}

	/**
	 * Devuelve las estaciones de servicio de un Municipio específico
	 */
	public List<EstacionDeServicio> getEstacionesDeServicioByMunicipio(Short id) {
		return new ArrayList<>(
				estacionDeServicioRepository.findEstacionDeServicioByMunicipio(id));
	}

	/**
	 * Extrae todas las marcas únicas de una lista de estaciones
	 */
	public List<String> getMarcasFromEESS(List<EstacionDeServicio> estaciones) {
		return estaciones.stream().map(EstacionDeServicio::getRotulo).collect(
						Collectors.toCollection(
								() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))).stream()
				.toList();
	}

	/**
	 * Devuelve una estación de servicio según su ID
	 */
	public EstacionDeServicio getEstacionDeServicioById(int id) {
		return estacionDeServicioRepository.findEstacionDeServicioById(id);
	}

	/**
	 * Filtra estaciones según ubicación, búsqueda, combustible, estado, marca y ordenamiento.
	 * Devuelve un Page<EstacionDeServicio> paginable.
	 */
	public List<EstacionDeServicio> filtrarEstaciones(String comunidadId,
													  String provinciaId,
													  String municipioId, String busqueda,
													  String[] combustibles,
													  String estado, String marca,
													  String ordenarPor) {

		// 1️⃣ Filtrado por ubicación geográfica
		List<EstacionDeServicio> estaciones = filtrarPorUbicacion(comunidadId,
																  provinciaId,
																  municipioId);

		// 2️⃣ Aplicar filtros adicionales
		estaciones = filtrarPorBusqueda(estaciones, busqueda);
		estaciones = filtrarPorCombustibles(estaciones, combustibles);
		estaciones = filtrarPorEstado(estaciones, estado);
		estaciones = filtrarPorMarca(estaciones, marca);

		// 3️⃣ Ordenamiento
		//estaciones = ordenarEstaciones(estaciones, ordenarPor);

		// 4️⃣ Convertir a Page para paginación
		return estaciones;
	}

    /* ==========================
       Métodos privados modulares
       ========================== */

	/**
	 * Filtra estaciones según jerarquía geográfica: CCAA > Provincia > Municipio
	 */
	private List<EstacionDeServicio> filtrarPorUbicacion(String comunidadId,
														 String provinciaId,
														 String municipioId) {
		if (comunidadId != null && !comunidadId.equals("0")) {
			short idCCAA = Short.parseShort(comunidadId);
			Optional<ComunidadAutonoma> ccaa = comunidadAutonomaService.getComunidadAutonomaById(
					idCCAA);
			if (ccaa.isPresent()) {
				if (provinciaId != null && !provinciaId.equals("0")) {
					short idProvincia = Short.parseShort(provinciaId);
					Optional<Provincia> provincia = ccaa.get().getProvincias().stream()
							.filter(p -> p.getId() == idProvincia).findFirst();
					if (provincia.isPresent()) {
						if (municipioId != null && !municipioId.equals("0")) {
							short idMunicipio = Short.parseShort(municipioId);
							Optional<Municipio> municipio = provincia.get()
									.getMunicipios().stream()
									.filter(m -> m.getId() == idMunicipio).findFirst();
							return municipio.isPresent() ? getEstacionesDeServicioByMunicipio(
									idMunicipio) : getEstacionesDeServicioByProvincia(
									idProvincia);
						}
						return getEstacionesDeServicioByProvincia(idProvincia);
					}
				}
				return getEstacionesDeServicioByComunidadAutonoma(idCCAA);
			}
		}
		return getEstacionesDeServicio(); // Si no hay filtro, devuelve todas
	}

	/**
	 * Filtra estaciones según búsqueda por rótulo, ID, localidad o municipio
	 */
	private List<EstacionDeServicio> filtrarPorBusqueda(
			List<EstacionDeServicio> estaciones, String busqueda) {
		if (busqueda == null || busqueda.isBlank()) return estaciones;

		String busq = busqueda.toLowerCase();

		return estaciones.stream().filter(e -> {
			String rotulo = e.getRotulo().toLowerCase();
			String direccion = e.getDireccion().toLowerCase();
			String localidad = e.getLocalidad().toLowerCase();
			String municipio = e.getMunicipio().getDenominacion().toLowerCase();
			String cp = String.valueOf(e.getCodigoPostal());
			String id = String.valueOf(e.getId());

			return rotulo.contains(busq) || direccion.contains(busq) ||
					localidad.contains(busq) || municipio.contains(busq) ||
					cp.contains(busq) || id.equals(busq);
		}).collect(Collectors.toList());
	}


	/**
	 * Filtra estaciones que tengan al menos uno de los combustibles seleccionados
	 */
	private List<EstacionDeServicio> filtrarPorCombustibles(
			List<EstacionDeServicio> estaciones, String[] combustibles) {

		if (combustibles == null || combustibles.length == 0)
			return estaciones; // nada que filtrar

		// Convertimos el array de IDs seleccionados a Set para búsquedas rápidas
		Set<String> idsSeleccionados = Set.of(combustibles);

		// Obtenemos los combustibles oficiales que coinciden con la selección
		List<Combustible> combustiblesSeleccionados = combustibleService.getCombustibles()
				.stream().filter(c -> idsSeleccionados.contains(c.getCodigo()))
				.toList(); // si usas Java 8, cambia a .collect(Collectors.toList())

		if (combustiblesSeleccionados.isEmpty())
			return estaciones; // ningún combustible coincide

		// Creamos un Set con todas las estaciones de los combustibles seleccionados
		Set<EstacionDeServicio> estacionesFiltradasPorCombustible = combustiblesSeleccionados.stream()
				.flatMap(c -> c.getEstacionesDeServicio().stream()).collect(
						Collectors.toSet()); // Set para eliminar duplicados y acceso rápido

		// Solo devolvemos las estaciones que estaban en la lista original
		return estaciones.stream().filter(estacionesFiltradasPorCombustible::contains)
				.toList(); // Java 16+, si Java 8: .collect(Collectors.toList())
	}


	/**
	 * Filtra estaciones según su estado (abierto/cerrado). Si estado es "INDIFERENTE", no filtra
	 */
	private List<EstacionDeServicio> filtrarPorEstado(List<EstacionDeServicio> estaciones,
													  String estado) {
		if (estado == null || estado.equalsIgnoreCase("INDIFERENTE"))
			return estaciones; // No filtramos nada

		return estaciones.stream().filter(e -> switch (estado.toUpperCase()) {
			case "ABIERTO" -> e.isAbierto();   // Solo abiertos
			case "CERRADO" -> !e.isAbierto();  // Solo cerrados
			default -> true;                   // Por seguridad, si es otro valor, no filtra
		}).collect(Collectors.toList());
	}


	/**
	 * Filtra estaciones según la marca seleccionada. Si es "0NOTSELECTED", no filtra
	 */
	private List<EstacionDeServicio> filtrarPorMarca(List<EstacionDeServicio> estaciones,
													 String marca) {
		if (marca != null && !marca.equals("0NOTSELECTED")) {
			return estaciones.stream().filter(e -> e.getRotulo().equalsIgnoreCase(marca))
					.collect(Collectors.toList());
		}
		return estaciones;
	}

	/**
	 * Ordena estaciones por precio mínimo o nombre según el parámetro "ordenarPor"
	 */
	private List<EstacionDeServicio> ordenarEstaciones(
			List<EstacionDeServicio> estaciones, String ordenarPor) {
		estaciones.sort((e1, e2) -> {
			switch (ordenarPor != null ? ordenarPor : "name_asc") {
				case "price_asc" -> Double.compare(e1.getPreciosCombustibles().stream()
														   .mapToDouble(
																   PrecioCombustible::getPrecio)
														   .min().orElse(0),
												   e2.getPreciosCombustibles().stream()
														   .mapToDouble(
																   PrecioCombustible::getPrecio)
														   .min().orElse(0));
				case "price_desc" -> Double.compare(e2.getPreciosCombustibles().stream()
															.mapToDouble(
																	PrecioCombustible::getPrecio)
															.min().orElse(0),
													e1.getPreciosCombustibles().stream()
															.mapToDouble(
																	PrecioCombustible::getPrecio)
															.min().orElse(0));
				case "name_desc" -> e2.getRotulo().compareToIgnoreCase(e1.getRotulo());
				default -> e1.getRotulo().compareToIgnoreCase(e2.getRotulo());
			}
			return 0;
		});
		return estaciones;
	}

	/**
	 * Convierte una lista de estaciones en un {@link Page<EstacionDeServicio>} según el Pageable
	 */
	public Page<EstacionDeServicio> toPage(List<EstacionDeServicio> estaciones,
											Pageable pageable) {
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), estaciones.size());
		List<EstacionDeServicio> subList = (start <= end) ? estaciones.subList(start,
																			   end) : Collections.emptyList();
		return new PageImpl<>(subList, pageable, estaciones.size());
	}

}
