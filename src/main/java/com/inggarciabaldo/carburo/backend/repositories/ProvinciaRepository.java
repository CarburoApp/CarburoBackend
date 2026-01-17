package com.inggarciabaldo.carburo.backend.repositories;

import com.inggarciabaldo.carburo.backend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.backend.entities.Provincia;
import org.springframework.data.repository.CrudRepository;

public interface ProvinciaRepository extends CrudRepository<Provincia, Short> {

	Iterable<Provincia> findAllByComunidadAutonoma(ComunidadAutonoma comunidadAutonoma);
}
