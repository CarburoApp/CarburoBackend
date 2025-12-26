package com.inggarciabaldo.carburo.CarburoBackend.services;

import com.inggarciabaldo.carburo.CarburoBackend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.CarburoBackend.repositories.ComunidadAutonomaRepoository;
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
		List<ComunidadAutonoma> CCAA = new ArrayList<>();
		comunidadAutonomaRepository.findAll().forEach(CCAA::add);
		return CCAA;
	}

	public Optional<ComunidadAutonoma> getComunidadAutonomaById(short idCCAA) {
		return comunidadAutonomaRepository.findComunidadAutonomaById(idCCAA);
	}
}
