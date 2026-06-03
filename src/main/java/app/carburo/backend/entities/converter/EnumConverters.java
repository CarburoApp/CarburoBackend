package app.carburo.backend.entities.converter;

import app.carburo.backend.entities.enums.Margen;
import app.carburo.backend.entities.enums.Remision;
import app.carburo.backend.entities.enums.Venta;
import jakarta.persistence.Converter;


public class EnumConverters {

	// Constructor privado para evitar instancias innecesarias.
	private EnumConverters() {}

	/**
	 * Converter para el enum {@link Margen}.
	 * <p>
	 * Permite almacenar el código del enum en la base de datos y recuperarlo.
	 * Se aplica automáticamente a todos los campos Margen.
	 */
	@Converter(autoApply = true)
	public static class MargenConverter extends EnumConverter<Margen> {

		/**
		 * Constructor por defecto necesario para JPA.
		 * Pasa la clase del enum a la superclase {@link EnumConverter}.
		 */
		public MargenConverter() {
			super(Margen.class);
		}
	}

	/**
	 * Converter para el enum {@link Remision}.
	 * <p>
	 * Permite almacenar el código del enum en la base de datos y recuperarlo.
	 * Se aplica automáticamente a todos los campos Remision.
	 */
	@Converter(autoApply = true)
	public static class RemisionConverter extends EnumConverter<Remision> {

		/**
		 * Constructor por defecto necesario para JPA.
		 * Pasa la clase del enum a la superclase {@link EnumConverter}.
		 */
		public RemisionConverter() {
			super(Remision.class);
		}
	}

	/**
	 * Converter para el enum {@link Venta}.
	 * <p>
	 * Permite almacenar el código del enum en la base de datos y recuperarlo.
	 * Se aplica automáticamente a todos los campos Venta.
	 */
	@Converter(autoApply = true)
	public static class VentaConverter extends EnumConverter<Venta> {

		/**
		 * Constructor por defecto necesario para JPA.
		 * Pasa la clase del enum a la superclase {@link EnumConverter}.
		 */
		public VentaConverter() {
			super(Venta.class);
		}
	}
}