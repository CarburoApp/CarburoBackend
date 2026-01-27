package app.carburo.backend.repositories;

import app.carburo.backend.entities.Municipio;
import app.carburo.backend.entities.Provincia;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio para la entidad Municipio.
 * Permite operaciones CRUD básicas y consultas personalizadas.
 */
public interface MunicipioRepository extends CrudRepository<Municipio, Short> {

	/**
	 * Devuelve todos los municipios que pertenecen a una provincia concreta y
	 * que tienen al menos una estación de servicio asociada, ordenados por denominación ascendente (A → Z).
	 * <p>
	 * Se usa DISTINCT para evitar duplicados si un municipio tiene varias estaciones.
	 * Se realiza un INNER JOIN con la colección de estaciones de servicio.
	 *
	 * @param provincia Provincia a la que pertenecen los municipios
	 * @return Lista de municipios con al menos una estación de servicio, ordenados alfabéticamente
	 */
	@Query("SELECT DISTINCT m " +
			"FROM Municipio m " +
			"JOIN m.estacionesDeServicio e " +
			"WHERE m.provincia = :provincia " +
			"ORDER BY m.denominacion ASC")
	List<Municipio> findMunicipioByProvinciaConEESS(@Param("provincia") Provincia provincia);
}
