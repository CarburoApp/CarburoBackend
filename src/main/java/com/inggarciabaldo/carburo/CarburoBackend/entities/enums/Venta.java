package com.inggarciabaldo.carburo.CarburoBackend.entities.enums;

import com.inggarciabaldo.carburo.CarburoBackend.entities.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

/**
 * Enum que representa el tipo de venta de combustible de la estación.
 * <p>
 * Cada valor tiene:
 * <ul>
 *     <li>codigo: Código interno de la aplicación</li>
 *     <li>codigoMensaje: Código de referencia para i18n / messages.properties</li>
 * </ul>
 */
@Getter
public enum Venta implements GetCodeEnumInterface {

	PUBLICA("PUBLICA", "venta.publica"),
	RESTRINGIDA("RESTRINGIDA", "venta.restringida");

	// ==============================
	// ATRIBUTOS
	// ==============================

	/**
	 * Código interno del enum
	 */
	private final String codigo;

	/**
	 * Código de referencia para messages.properties
	 */
	private final String codigoMensaje;

	// ==============================
	// CONSTRUCTOR
	// ==============================
	Venta(String codigo, String codigoMensaje) {
		this.codigo        = codigo;
		this.codigoMensaje = codigoMensaje;
	}

}
