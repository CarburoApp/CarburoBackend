package com.inggarciabaldo.carburo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa el precio de un tipo de combustible
 * en una estación de servicio en una fecha concreta.
 * <p>
 * Se utiliza una clave primaria compuesta {@link PrecioCombustibleId},
 * formada por:
 * <ul>
 *     <li>idEess: ID de la estación de servicio</li>
 *     <li>idCombustible: ID del tipo de combustible</li>
 *     <li>fecha: Fecha del precio registrado</li>
 * </ul>
 */
@Entity
@Table(name = "preciocombustible")
@Getter
@NoArgsConstructor
public class PrecioCombustible {

	// ==============================
	// CAMPOS
	// ==============================

	/** Clave primaria compuesta */
	@EmbeddedId
	private PrecioCombustibleId id;

	/** Estación de servicio asociada (relación Muchos a Uno) */
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("idEess") // Mapea idEess de la clave embebida
	@JoinColumn(name = "id_eess")
	private EstacionDeServicio estacion;

	/** Combustible asociado (relación Muchos a Uno) */
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("idCombustible") // Mapea idCombustible de la clave embebida
	@JoinColumn(name = "id_combustible")
	private Combustible combustible;

	/** Precio del combustible en la fecha indicada */
	@Column(nullable = false)
	private double precio;

	// ==============================
	// CONSTRUCTOR COMPLETO
	// ==============================

	/**
	 * Constructor completo.
	 *
	 * @param estacionDeServicio Estación de servicio (no nula)
	 * @param combustible        Combustible (no nulo)
	 * @param fecha              Fecha del precio (no nula y no futura)
	 * @param precio             Precio del combustible (mayor que 0)
	 */
	public PrecioCombustible(EstacionDeServicio estacionDeServicio,
							 Combustible combustible, LocalDate fecha, double precio) {
		if (estacionDeServicio == null || combustible == null)
			throw new IllegalArgumentException("EESS y Combustible no pueden ser nulos");
		if (fecha == null || fecha.isAfter(LocalDate.now()))
			throw new IllegalArgumentException("Fecha inválida");
		if (precio <= 0)
			throw new IllegalArgumentException("El precio debe ser mayor que 0");

		this.estacion    = estacionDeServicio;
		this.combustible = combustible;
		this.precio      = precio;
		this.id          = new PrecioCombustibleId(
				estacion.getId(),
				combustible.getId(),
				fecha
		);
	}

	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrecioCombustible that)) return false;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PrecioCombustible{" +
				"EESS=" + (estacion != null ? estacion.getId() : null) +
				", Combustible=" + (combustible != null ? combustible.getId() : null) +
				", fecha=" + (id != null ? id.getFecha() : null) +
				", precio=" + precio +
				'}';
	}
}
