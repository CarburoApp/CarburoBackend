package com.inggarciabaldo.carburo.backend.services;

import com.inggarciabaldo.carburo.backend.entities.ComunidadAutonoma;
import com.inggarciabaldo.carburo.backend.entities.Provincia;
import com.inggarciabaldo.carburo.backend.repositories.ProvinciaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProvinciaService {
	private final ProvinciaRepository provinciaRepository;

	public ProvinciaService(ProvinciaRepository provinciaRepository) {
		this.provinciaRepository = provinciaRepository;
	}

	public List<Provincia> getProvincias() {
		List<Provincia> provincias = new ArrayList<>();
		provinciaRepository.findAll().forEach(provincias::add);
		return provincias;
	}

	public List<Provincia> getProvinciasByComunidadAutonoma(
			ComunidadAutonoma comunidadAutonoma) {
		List<Provincia> provincias = new ArrayList<>();
		provinciaRepository.findAllByComunidadAutonoma(comunidadAutonoma)
				.forEach(provincias::add);
		return provincias;
	}
}
