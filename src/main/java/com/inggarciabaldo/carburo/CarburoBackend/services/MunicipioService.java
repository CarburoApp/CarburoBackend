package com.inggarciabaldo.carburo.CarburoBackend.services;

import com.inggarciabaldo.carburo.CarburoBackend.entities.Municipio;
import com.inggarciabaldo.carburo.CarburoBackend.entities.Provincia;
import com.inggarciabaldo.carburo.CarburoBackend.repositories.MunicipioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MunicipioService {
	private final MunicipioRepository municipioRepository;

	public MunicipioService(MunicipioRepository municipioRepository) {
		this.municipioRepository = municipioRepository;
	}

	public List<Municipio> getMunicipios() {
		List<Municipio> municipios = new ArrayList<>();
		municipioRepository.findAll().forEach(municipios::add);
		return municipios;
	}

	public List<Municipio> getMunicipiosByProvinciaConEESSAsociadas(Provincia provincia) {
		return new ArrayList<>(
				municipioRepository.findMunicipioByProvinciaConEESS(provincia));
	}
}
