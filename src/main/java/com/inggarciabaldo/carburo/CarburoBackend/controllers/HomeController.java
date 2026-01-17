package com.inggarciabaldo.carburo.CarburoBackend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador principal de la aplicación que maneja las rutas de inicio.
 *
 * <p>Proporciona métodos para mostrar la página de inicio, y
 * redirigir la raíz del sitio web a la página de inicio.</p>
 */
@Controller
public class HomeController {

	/**
	 * Maneja las solicitudes GET a "/home".
	 *
	 * @return el nombre de la vista "home" que se mostrará al usuario.
	 */
	@GetMapping("/home")
	public String doGetHome(Model model) {
		model.addAttribute("pagina", "home");
		return "home";
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
