package app.carburo.backend.repositories;

import app.carburo.backend.entities.ComunidadAutonoma;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ComunidadAutonomaRepoository
		extends CrudRepository<ComunidadAutonoma, Short> {
	Optional<ComunidadAutonoma> findComunidadAutonomaById(short idCCAA);
}
