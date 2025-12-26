package com.inggarciabaldo.carburo.CarburoBackend.entities.enums.fromcode;

import com.inggarciabaldo.carburo.CarburoBackend.entities.enums.fromcode.FromCode.GetCodeEnumInterface;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FromCode<T extends Enum<T> & GetCodeEnumInterface> {

	public interface GetCodeEnumInterface {

		/**
		 * Devuelve el código asociado a un enumerado en concreto enumerado.
		 *
		 * @return Una cadena con el código correspondiente a un enumerado del enumerado.
		 */
		String getCodigo();
	}

	// Cache global de instancias por tipo de enum
	private static final Map<Class<?>, FromCode<?>> INSTANCES = new ConcurrentHashMap<>();

	// Map estático de enumerados, pero se inicializa la primera vez que se usa
	private final Map<String, T> byCode;

	/**
	 * Constructor que inicializa la herramienta que facilita la obtención de enumerados
	 * mediante el código del mismo.
	 * <p>
	 * El objetivo es que sea más eficiente la obtención de enumerados a partir del código,
	 *
	 * @param enumClass Clase del enumerado del que se quieren obtener los valores.
	 */
	private FromCode(Class<T> enumClass) {
		this.byCode = Arrays.stream(enumClass.getEnumConstants()).collect(
				Collectors.toMap(e -> e.getCodigo().toUpperCase(), Function.identity()));
	}

	/**
	 * Obtiene la instancia singleton de FromCode para un enum determinado.
	 *
	 * @param enumClass Clase del enum
	 * @param <T>       Tipo del enum
	 * @return instancia única de FromCode para este tipo de enum
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T> & GetCodeEnumInterface> FromCode<T> getInstance(
			Class<T> enumClass) {
		return (FromCode<T>) INSTANCES.computeIfAbsent(enumClass,
													   c -> new FromCode<>((Class<T>) c));
	}

	/**
	 * Devuelve el enumerado a partir del código.
	 *
	 * @throws IllegalArgumentException Si el código es nulo o no existe.
	 */
	public T fromCode(String code) {
		if (code == null || code.isEmpty()) throw new IllegalArgumentException(
				"El código del enumerado planteado no es válido. Nulo o vacío.");
		T value = byCode.get(code.toUpperCase());
		if (value == null) throw new IllegalArgumentException(
				"Enumerado no encontrado según el código: " + code);
		return value;
	}
}