package com.inggarciabaldo.carburo.backend.entities.converter;

import com.inggarciabaldo.carburo.backend.entities.enums.fromcode.FromCode;
import com.inggarciabaldo.carburo.backend.entities.enums.fromcode.FromCode.GetCodeEnumInterface;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter genérico para enums que implementan {@link GetCodeEnumInterface}.
 * <p>
 * Permite almacenar en base de datos el valor devuelto por {@link GetCodeEnumInterface#getCodigo()}
 * y recuperar la instancia correspondiente del enum desde el código.
 *
 * @param <T> Tipo de enum que implementa {@link GetCodeEnumInterface}
 */
@Converter(autoApply = true)
public class EnumConverter<T extends Enum<T> & GetCodeEnumInterface>
		implements AttributeConverter<T, String> {

	/**
	 * Clase del enum que se va a convertir.
	 * <p>
	 * Necesaria para poder llamar a {@link FromCode#getInstance(Class)}.
	 */
	private final Class<T> enumClass;

	/**
	 * Constructor que recibe la clase del enum.
	 *
	 * @param enumClass clase del enum que se convertirá
	 */
	public EnumConverter(Class<T> enumClass) {
		this.enumClass = enumClass;
	}

	/**
	 * Convierte el enum a la columna de la base de datos.
	 *
	 * @param attribute instancia del enum
	 * @return código asociado al enum, o null si el enum es null
	 */
	@Override
	public String convertToDatabaseColumn(T attribute) {
		return attribute != null ? attribute.getCodigo() : null;
	}

	/**
	 * Convierte el valor de la base de datos a la instancia del enum.
	 *
	 * @param dbData valor almacenado en la base de datos
	 * @return instancia del enum correspondiente al código, o null si dbData es null
	 */
	@Override
	public T convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
		// Se obtiene la instancia de FromCode del enum correspondiente y se busca por código
		return FromCode.getInstance(enumClass).fromCode(dbData);
	}
}
