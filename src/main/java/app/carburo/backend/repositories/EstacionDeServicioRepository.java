package app.carburo.backend.repositories;

import app.carburo.backend.entities.EstacionDeServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad EstacionDeServicio.
 * Contiene métodos para obtener estaciones por ID, ubicación geográfica y paginación.
 */
@Repository
public interface EstacionDeServicioRepository extends CrudRepository<EstacionDeServicio, Integer> {

	/** Devuelve todas las ids de las estaciones de servicio ordenadas ascendentemente.*/
	@Query("SELECT e.id FROM EstacionDeServicio e WHERE e.id > 0 ORDER BY e.id")
    List<Integer> findIds();

	/** Devuelve todas las estaciones de servicio paginadas */
	Page<EstacionDeServicio> findAll(Pageable pageable);

	/** Devuelve una estación de servicio según su ID */
	EstacionDeServicio findEstacionDeServicioById(int id);

	/**
	 * Devuelve las estaciones de un municipio específico.
	 * @param municipioId ID del municipio
	 */
	@Query("SELECT e FROM EstacionDeServicio e WHERE e.municipio.id = :municipioId")
	List<EstacionDeServicio> findEstacionDeServicioByMunicipio(@Param("municipioId") Short municipioId);

	/**
	 * Devuelve las estaciones de una provincia específica.
	 * @param provinciaId ID de la provincia
	 */
	@Query("SELECT e FROM EstacionDeServicio e WHERE e.provincia.id = :provinciaId")
	List<EstacionDeServicio> findEstacionDeServicioByProvincia(@Param("provinciaId") Short provinciaId);

	/**
	 * Devuelve las estaciones de una comunidad autónoma específica.
	 * @param ccaaId ID de la comunidad autónoma
	 */
	@Query("SELECT e FROM EstacionDeServicio e WHERE e.provincia.comunidadAutonoma.id = :ccaaId")
	List<EstacionDeServicio> findEstacionDeServicioByComunidadAutonoma(@Param("ccaaId") Short ccaaId);


	/**
	 * Devuelve la estación de servicio más cercana a unas coordenadas dadas.
	 *
	 * <p>Utiliza funciones espaciales de PostGIS y el operador de distancia
	 * optimizado {@code <->}, que hace uso del índice GIST sobre el campo
	 * {@code coordenadas}.</p>
	 *
	 * @param lat latitud WGS84
	 * @param lon longitud WGS84
	 * @return estación de servicio más cercana o null si no existe
	 */
	@Query(
			value = " SELECT * FROM eess ORDER BY coordenadas<-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography LIMIT 1",
			nativeQuery = true
	)
	EstacionDeServicio findEstacionDeServicioMasCercana(@Param("lat") double lat,
														@Param("lon") double lon);

}
