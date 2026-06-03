package app.carburo.backend.repositories;

import app.carburo.backend.entities.ComunidadAutonoma;
import app.carburo.backend.entities.Provincia;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repositorio para la entidad Provincia.
 * Contiene métodos de acceso y consulta de provincias,
 * incluyendo filtrado por comunidad autónoma, ordenación ...
 */
public interface ProvinciaRepository extends CrudRepository<Provincia, Short> {

	/**
	 * Devuelve todas las provincias pertenecientes a una comunidad autónoma.
	 *
	 * @param comunidadAutonoma comunidad autónoma asociada
	 */
	Iterable<Provincia> findAllByComunidadAutonoma(ComunidadAutonoma comunidadAutonoma);

	/**
	 * Devuelve una provincia según su identificador.
	 *
	 * @param provinciaFavorita ID de la provincia
	 */
	Optional<Provincia> findProvinciaById(short provinciaFavorita);

	/** Devuelve todas las provincias */
	Iterable<Provincia> findAll();

	/**
	 * Devuelve todas las provincias ordenadas alfabéticamente por denominación.
	 */
	@Query("SELECT p FROM Provincia p ORDER BY p.denominacion ASC")
	Iterable<Provincia> findAllOrderByDenominacion();
}
