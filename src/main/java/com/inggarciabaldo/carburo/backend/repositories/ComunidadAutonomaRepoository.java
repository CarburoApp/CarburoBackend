package com.inggarciabaldo.carburo.backend.repositories;

import com.inggarciabaldo.carburo.backend.entities.ComunidadAutonoma;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ComunidadAutonomaRepoository
		extends CrudRepository<ComunidadAutonoma, Short> {
	Optional<ComunidadAutonoma> findComunidadAutonomaById(short idCCAA);
}
