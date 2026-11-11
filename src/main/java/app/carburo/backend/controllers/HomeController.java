package app.carburo.backend.controllers;

import app.carburo.backend.entities.EstacionDeServicio;
import app.carburo.backend.services.EstacionDeServicioService;
import app.carburo.backend.seo.SitemapUrlProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static app.carburo.backend.controllers.EstacionDeServicioController.RUTA_ESTACIONES_DE_SERVICIO;

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
public class HomeController implements SitemapUrlProvider {

    @Override
    public List<String> getSitemapPaths() {
        return List.of("/", RUTA_CONTACTO);
    }

	// Constantes de vistas
	public static final String VISTA_INICIO = "inicio";
	public static final String VISTA_CONTACTO = "contacto";

	// Rutas
	public static final String RUTA_CONTACTO = "/contacto";
	public static final String RUTA_INICIO_UBICACION = "/inicio/ubicacion";

	// Dependencias
	private final EstacionDeServicioService estacionDeServicioService;

	/**
	 * Constructor del controlador para la inyección de dependencias.
	 *
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

		return VISTA_INICIO;
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
							   RUTA_ESTACIONES_DE_SERVICIO + "?comunidadSeleccionada=13");
		busquedasPopulares.put("Barcelona", RUTA_ESTACIONES_DE_SERVICIO +
				"?comunidadSeleccionada=9&provinciaSeleccionada=8");
		busquedasPopulares.put("Asturias",
							   RUTA_ESTACIONES_DE_SERVICIO + "?comunidadSeleccionada=3");
		busquedasPopulares.put("León", RUTA_ESTACIONES_DE_SERVICIO +
				"?comunidadSeleccionada=8&provinciaSeleccionada=24");
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
	@PostMapping(RUTA_INICIO_UBICACION)
	public String obtenerEstacionMasCercanaFragment(@RequestParam double lat,
													@RequestParam double lon,
													Model model) {
		// Obtenemos la estación más cercana usando el servicio
		EstacionDeServicio e = estacionDeServicioService.getEstacionDeServicioMasProxima(lat, lon);
		model.addAttribute("estacion", e);
		return VISTA_INICIO + " :: estacionCercanaFragment";
	}

	/**
	 * Maneja las solicitudes GET a todas las URLs no registradas.
	 *
	 * <p>Redirige automáticamente toda url no controlada a '/' - Inicio.</p>
	 *
	 * @return un string de redirección a "/".
	 */
	@GetMapping("/{path:[^.]{1,100}}")
	public String doGetBase(@PathVariable(required = false) String path ) {
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
	@GetMapping(RUTA_CONTACTO)
	public String quienesSomos(Model model) {
		model.addAttribute("pagina", "contacto");
		model.addAttribute("email", emailContacto);
		return VISTA_CONTACTO;
	}
}
