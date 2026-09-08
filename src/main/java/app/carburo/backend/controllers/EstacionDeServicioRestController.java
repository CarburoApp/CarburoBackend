package app.carburo.backend.controllers;

import app.carburo.backend.entities.EstacionDeServicio;
import app.carburo.backend.services.EstacionDeServicioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class EstacionDeServicioRestController {

	private final EstacionDeServicioService estacionDeServicioService;

	public EstacionDeServicioRestController(
			EstacionDeServicioService estacionDeServicioService) {
		this.estacionDeServicioService = estacionDeServicioService;
	}

	/**
	 * Devuelve en texto plano los toString() de las 10 primeras estaciones de servicio.
	 *
	 * @return String con cada estación en una línea
	 */
	@GetMapping("/api/estaciones-de-servicio/top10")
	public String getTop10Estaciones() {
		List<EstacionDeServicio> estaciones = estacionDeServicioService.getEstacionesDeServicio();

		return estaciones.stream()
				.limit(10) // solo las 10 primeras
				.map(EstacionDeServicio::toString) // convertir a texto
				.collect(Collectors.joining("\n\n")); // unir con salto de línea
	}
}