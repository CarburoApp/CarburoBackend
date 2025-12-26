package com.inggarciabaldo.carburo.CarburoBackend.repositories;

import com.inggarciabaldo.carburo.CarburoBackend.entities.EstacionDeServicio;
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
}
