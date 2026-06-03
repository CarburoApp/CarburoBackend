package app.carburo.backend.services;

import app.carburo.backend.entities.ComunidadAutonoma;
import app.carburo.backend.entities.Provincia;
import app.carburo.backend.repositories.ProvinciaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio encargado de la gestión de provincias.
 * <p>
 * Proporciona métodos de acceso a los datos relacionados con la entidad
 * {@link Provincia}, encapsulando la lógica de acceso al repositorio.
 * </p>
 */
@Service
public class ProvinciaService {

	/**
	 * Repositorio de acceso a datos de provincias.
	 */
	private final ProvinciaRepository provinciaRepository;

	/**
	 * Constructor del servicio con inyección de dependencias.
	 *
	 * @param provinciaRepository repositorio de provincias
	 */
	public ProvinciaService(ProvinciaRepository provinciaRepository) {
		this.provinciaRepository = provinciaRepository;
	}

	/**
	 * Obtiene todas las provincias ordenadas alfabéticamente por su denominación.
	 *
	 * @return lista de provincias ordenadas por denominación;
	 *         si no existen provincias, se devuelve una lista vacía
	 */
	public List<Provincia> getProvinciasOrderByDenominacion() {
		List<Provincia> provincias = new ArrayList<>();
		provinciaRepository.findAllOrderByDenominacion()
				.forEach(provincias::add);
		return provincias;
	}

	/**
	 * Obtiene todas las provincias pertenecientes a una comunidad autónoma concreta.
	 *
	 * @param comunidadAutonoma comunidad autónoma por la que se desea filtrar
	 * @return lista de provincias asociadas a la comunidad autónoma indicada;
	 *         si no existen coincidencias, se devuelve una lista vacía
	 * @throws IllegalArgumentException si {@code comunidadAutonoma} es {@code null}
	 */
	public List<Provincia> getProvinciasByComunidadAutonoma(
			ComunidadAutonoma comunidadAutonoma) {

		if (comunidadAutonoma == null) {
			throw new IllegalArgumentException("La comunidad autónoma no puede ser null.");
		}

		List<Provincia> provincias = new ArrayList<>();
		provinciaRepository.findAllByComunidadAutonoma(comunidadAutonoma)
				.forEach(provincias::add);
		return provincias;
	}

	/**
	 * Obtiene el número total de provincias registradas en el sistema.
	 *
	 * @return número total de provincias
	 */
	public long getTotalProvincias() {
		return provinciaRepository.count();
	}

	/**
	 * Busca una provincia por su identificador.
	 *
	 * @param provinciaFavorita identificador de la provincia
	 * @return {@link Optional} que contiene la provincia si existe;
	 *         {@link Optional#empty()} en caso contrario
	 */
	public Optional<Provincia> getProvinciaById(short provinciaFavorita) {
		return provinciaRepository.findProvinciaById(provinciaFavorita);
	}
}
