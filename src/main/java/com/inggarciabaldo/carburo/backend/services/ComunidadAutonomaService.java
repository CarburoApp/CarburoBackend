package com.inggarciabaldo.carburo.backend.services;

import com.inggarciabaldo.carburo.backend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.backend.repositories.ComunidadAutonomaRepoository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ComunidadAutonomaService {

	private final ComunidadAutonomaRepoository comunidadAutonomaRepository;

	public ComunidadAutonomaService(
			ComunidadAutonomaRepoository comunidadAutonomaRepository) {
		this.comunidadAutonomaRepository = comunidadAutonomaRepository;
	}

	public List<ComunidadAutonoma> getComunidadesAutonomas() {
		List<ComunidadAutonoma> ccaa = new ArrayList<>();
		comunidadAutonomaRepository.findAll().forEach(ccaa::add);
		return ccaa;
	}

	public Optional<ComunidadAutonoma> getComunidadAutonomaById(short idCCAA) {
		return comunidadAutonomaRepository.findComunidadAutonomaById(idCCAA);
	}
}
