package com.inggarciabaldo.carburo.CarburoBackend.entities.enums;

import com.inggarciabaldo.carburo.CarburoBackend.entities.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

/**
 * Enum que representa el origen de los datos de remisión de precios de combustible.
 * <p>
 * Cada valor tiene:
 * <ul>
 *     <li>codigo: Código interno de la aplicación</li>
 *     <li>codigoMensaje: Código de referencia para i18n / messages.properties</li>
 * </ul>
 */
@Getter
public enum Remision implements GetCodeEnumInterface {

	OM("OM", "remision.om"),
	DM("DM", "remision.dm"); // valor por defecto

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
	Remision(String codigo, String codigoMensaje) {
		this.codigo        = codigo;
		this.codigoMensaje = codigoMensaje;
	}

}
