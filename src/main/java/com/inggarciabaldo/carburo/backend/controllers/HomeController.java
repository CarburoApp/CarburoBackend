package com.inggarciabaldo.carburo.backend.controllers;

import com.inggarciabaldo.carburo.backend.entities.EstacionDeServicio;
import com.inggarciabaldo.carburo.backend.services.EstacionDeServicioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador principal de la aplicación que maneja las rutas de inicio y contacto.
 *
 * <p>Este controlador se encarga de:
 * <ul>
 *     <li>Redirigir la raíz "/".</li>
 *     <li>Mostrar la página principal con búsquedas populares y la estación más cercana.</li>
 *     <li>Obtener la estación de servicio más cercana a una ubicación proporcionada.</li>
 *     <li>Mostrar la página de contacto con el correo configurado en application.properties.</li>
 * </ul>
 * </p>
 */
@Controller
public class HomeController {

	// Servicio para manejar operaciones relacionadas con estaciones de servicio
	private final EstacionDeServicioService estacionDeServicioService;

	/**
	 * Constructor del controlador.
	 *
	 * @param estacionDeServicioService servicio para obtener información de estaciones de servicio
	 */
	public HomeController(EstacionDeServicioService estacionDeServicioService) {
		this.estacionDeServicioService = estacionDeServicioService;
	}

	/**
	 * Maneja las solicitudes GET a "/".
	 *
	 * <p>Carga la página principal de la aplicación y añade al modelo:
	 * <ul>
	 *     <li>El nombre de la página actual ("inicio").</li>
	 *     <li>Un mapa de búsquedas populares con nombre y URL correspondiente.</li>
	 *     <li>Un atributo "estacionCercana" inicializado en null si no existe.</li>
	 * </ul>
	 * </p>
	 *
	 * @param model modelo utilizado para pasar atributos a la vista
	 * @return el nombre de la vista "home"
	 */
	@GetMapping("/")
	public String doGetHome(Model model) {
		model.addAttribute("pagina", "inicio");

		// Obtenemos las búsquedas populares
		Map<String, String> busquedasPopulares = getBusquedasPopulares();

		// Se añade al modelo solo si el mapa contiene elementos
		model.addAttribute("busquedasPopulares", busquedasPopulares);

		// Inicializamos la estación cercana a null si no ha sido pasada
		if (!model.containsAttribute("estacionCercana")) {
			model.addAttribute("estacionCercana", null);
		}

		return "inicio";
	}

	/**
	 * Obtiene un mapa con las búsquedas populares para mostrar en la página de inicio.
	 *
	 * <p>El mapa mantiene el orden de inserción usando LinkedHashMap.</p>
	 *
	 * @return un mapa clave=nombre visible, valor=URL de destino
	 */
	private static Map<String, String> getBusquedasPopulares() {
		Map<String, String> busquedasPopulares = new LinkedHashMap<>();
		busquedasPopulares.put("Madrid",
							   "/estacionDeServicio/list?comunidadSeleccionada=13");
		busquedasPopulares.put("Barcelona",
							   "/estacionDeServicio/list?comunidadSeleccionada=9&provinciaSeleccionada=8");
		busquedasPopulares.put("Asturias",
							   "/estacionDeServicio/list?comunidadSeleccionada=3");
		busquedasPopulares.put("León",
							   "/estacionDeServicio/list?comunidadSeleccionada=8&provinciaSeleccionada=24");
		return busquedasPopulares;
	}

	/**
	 * Recibe la ubicación del usuario y devuelve la estación de servicio más cercana.
	 *
	 * <p>Se invoca mediante AJAX desde la vista, devolviendo solo
	 * el fragmento "estacionCercanaFragment".</p>
	 *
	 * @param lat latitud del usuario (WGS84)
	 * @param lon longitud del usuario (WGS84)
	 * @param model modelo para pasar la estación encontrada a la vista
	 * @return el fragmento "inicio :: estacionCercanaFragment" con la estación más cercana
	 */
	@PostMapping("/inicio/ubicacion")
	public String obtenerEstacionMasCercanaFragment(@RequestParam double lat,
													@RequestParam double lon,
													Model model) {
		// Obtenemos la estación más cercana usando el servicio
		EstacionDeServicio e = estacionDeServicioService.getEstacionDeServicioMasProxima(lat, lon);
		model.addAttribute("estacion", e);
		return "inicio :: estacionCercanaFragment";
	}

	/**
	 * Maneja las solicitudes GET a todas las URLs no registradas.
	 *
	 * <p>Redirige automáticamente toda url no controlada a '/' - Inicio.</p>
	 *
	 * @return un string de redirección a "/".
	 */
	@RequestMapping(value = {"/{path:[^.]*}", "/**/{path:[^.]*}"})
	public String doGetBase() {
		return "redirect:/";
	}

	// Inyección del correo de contacto desde application.properties
	@Value("${contacto.email}")
	private String emailContacto;

	/**
	 * Maneja las solicitudes GET a "/contacto".
	 *
	 * <p>Añade al modelo los atributos necesarios para mostrar la página de contacto:</p>
	 * <ul>
	 *     <li>"pagina": nombre de la página actual ("contacto")</li>
	 *     <li>"email": correo de contacto</li>
	 * </ul>
	 *
	 * @param model modelo para pasar atributos a la vista
	 * @return el nombre de la vista "contacto"
	 */
	@GetMapping("/contacto")
	public String quienesSomos(Model model) {
		model.addAttribute("pagina", "contacto");
		model.addAttribute("email", emailContacto);
		return "contacto";
	}
}
