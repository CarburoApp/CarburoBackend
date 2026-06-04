package app.carburo.backend.services;

import app.carburo.backend.dto.CombustibleDto;
import app.carburo.backend.entities.Combustible;
import app.carburo.backend.repositories.CombustibleRepository;
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
		combustibles.addAll(combustibleRepository.findAll());
		return combustibles;
	}
}