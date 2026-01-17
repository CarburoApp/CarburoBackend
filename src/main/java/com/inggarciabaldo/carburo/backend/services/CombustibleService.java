package com.inggarciabaldo.carburo.backend.services;

import com.inggarciabaldo.carburo.backend.dto.CombustibleDto;
import com.inggarciabaldo.carburo.backend.entities.Combustible;
import com.inggarciabaldo.carburo.backend.repositories.CombustibleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CombustibleService {

	private final CombustibleRepository combustibleRepository;

	public CombustibleService(CombustibleRepository combustibleRepository) {
		this.combustibleRepository = combustibleRepository;
	}

	public List<CombustibleDto> getCombustiblesDto() {
		List<CombustibleDto> combustibles = new ArrayList<>();
		combustibleRepository.findAll().forEach(
				combustible -> combustibles.add(new CombustibleDto(combustible)));
		return combustibles;
	}

	public List<Combustible> getCombustibles() {
		List<Combustible> combustibles = new ArrayList<>();
		combustibleRepository.findAll().forEach(combustibles::add);
		return combustibles;
	}
}