package com.inggarciabaldo.carburo.CarburoBackend.repositories;

import com.inggarciabaldo.carburo.CarburoBackend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.CarburoBackend.entities.Provincia;
import org.springframework.data.repository.CrudRepository;

public interface ProvinciaRepository extends CrudRepository<Provincia, Short> {

	Iterable<Provincia> findAllByComunidadAutonoma(ComunidadAutonoma comunidadAutonoma);
}
