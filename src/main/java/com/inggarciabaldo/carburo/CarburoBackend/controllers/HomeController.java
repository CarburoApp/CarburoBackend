package com.inggarciabaldo.carburo.CarburoBackend.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/login")
	public String index() {
		return "home";
	}

	@GetMapping(value = {"/home"})
	public String home() {
		return "home";
	}
}