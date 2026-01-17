package com.inggarciabaldo.carburo.CarburoBackend.controllers;

import com.inggarciabaldo.carburo.CarburoBackend.entities.EstacionDeServicio;
import com.inggarciabaldo.carburo.CarburoBackend.services.EstacionDeServicioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador principal de la aplicación que maneja las rutas de inicio.
 *
 * <p>Proporciona métodos para mostrar la página de inicio, y
 * redirigir la raíz del sitio web a la página de inicio.</p>
 */
@Controller
public class HomeController {

	private final EstacionDeServicioService estacionDeServicioService;
	private final SpringTemplateEngine templateEngine;

	public HomeController(EstacionDeServicioService estacionDeServicioService,
						  SpringTemplateEngine templateEngine) {
		this.estacionDeServicioService = estacionDeServicioService;
		this.templateEngine            = templateEngine;
	}


	/**
	 * Maneja las solicitudes GET a "/home".
	 *
	 * <p>Carga la página principal de la aplicación y añade al modelo
	 * una lista de búsquedas populares para mostrarlas en la vista,
	 * siempre que existan.</p>
	 *
	 * @param model modelo utilizado para pasar atributos a la vista
	 * @return el nombre de la vista "home"
	 */
	@GetMapping("/home")
	public String doGetHome(Model model) {
		model.addAttribute("pagina", "home");

		// Búsquedas populares: clave = texto visible, valor = URL de destino
		Map<String, String> busquedasPopulares = new LinkedHashMap<>();
		busquedasPopulares.put("Madrid",
							   "/estacionDeServicio/list?comunidadSeleccionada=13");
		busquedasPopulares.put("Barcelona",
							   "/estacionDeServicio/list?comunidadSeleccionada=9&provinciaSeleccionada=8");
		busquedasPopulares.put("Asturias",
							   "/estacionDeServicio/list?comunidadSeleccionada=3");
		busquedasPopulares.put("León",
							   "/estacionDeServicio/list?comunidadSeleccionada=8&provinciaSeleccionada=24");

		// Solo se añade al modelo si el mapa no es nulo ni está vacío
		model.addAttribute("busquedasPopulares", busquedasPopulares);

		// Por defecto, no hay estación (null)
		// Si no hay estación pasada por flash, por defecto null
		if (!model.containsAttribute("estacionCercana")) {
			model.addAttribute("estacionCercana", null);
		}


		return "home";
	}

	/**
	 * Recibe la ubicación del usuario y devuelve la estación de servicio más cercana.
	 *
	 * @param lat latitud del usuario (WGS84)
	 * @param lon longitud del usuario (WGS84)
	 * @return estación de servicio más cercana
	 */
	@PostMapping("/home/ubicacion")
	public String obtenerEstacionMasCercanaFragment(@RequestParam double lat,
													@RequestParam double lon,
													Model model) {
		EstacionDeServicio e = estacionDeServicioService.getEstacionDeServicioMasProxima(
				lat, lon);
		model.addAttribute("estacion", e);
		return "home :: estacionCercanaFragment";
	}



	/**
	 * Maneja las solicitudes GET a la raíz del sitio web "/".
	 *
	 * <p>Redirige automáticamente a "/home".</p>
	 *
	 * @return un string de redirección a "/home".
	 */
	@GetMapping("/")
	public String doGetBase() {
		return "redirect:/home";
	}

	// Inyección del correo desde application.properties
	@Value("${contacto.email}")
	private String emailContacto;

	@GetMapping("/contacto")
	public String quienesSomos(Model model) {
		// Añadimos el correo al modelo para que la vista pueda mostrarlo
		model.addAttribute("pagina", "contacto");
		model.addAttribute("email", emailContacto);
		return "contacto";
	}
}
