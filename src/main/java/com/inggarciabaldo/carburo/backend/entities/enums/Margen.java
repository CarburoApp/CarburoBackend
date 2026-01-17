package com.inggarciabaldo.carburo.backend.entities.enums;

import com.inggarciabaldo.carburo.backend.entities.enums.fromcode.FromCode.GetCodeEnumInterface;
import lombok.Getter;

/**
 * Enum que representa los posibles márgenes de una estación de servicio.
 * <p>
 * Cada valor tiene:
 * <ul>
 *     <li>codigo: Código interno para la aplicación</li>
 *     <li>codigoMensaje: Código de referencia para i18n / messages.properties</li>
 * </ul>
 */
@Getter
public enum Margen implements GetCodeEnumInterface {

	DERECHO("DERECHO", "margen.derecho"),

	IZQUIERDO("IZQUIERDO", "margen.izquierdo"),

	NO_APLICA("NO_APLICA", "margen.no_aplica"); // valor por defecto

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
	Margen(String codigo, String codigoMensaje) {
		this.codigo        = codigo;
		this.codigoMensaje = codigoMensaje;
	}

}
