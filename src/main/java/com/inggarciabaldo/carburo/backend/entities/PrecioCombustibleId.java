package com.inggarciabaldo.carburo.backend.entities;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Clase embebida que representa la clave primaria compuesta
 * de la entidad {@link PrecioCombustible}.
 * <p>
 * La clave primaria está formada por:
 * <ul>
 *     <li>idEess: Identificador de la estación de servicio</li>
 *     <li>idCombustible: Identificador del tipo de combustible</li>
 *     <li>fecha: Fecha del precio registrado</li>
 * </ul>
 * <p>
 * Esta clase debe implementar equals() y hashCode() para que
 * JPA pueda identificar correctamente la entidad.
 */
@Embeddable
@Getter
@NoArgsConstructor
public class PrecioCombustibleId {

	/**
	 * Identificador de la estación de servicio
	 */
	private int idEess;

	/**
	 * Identificador del tipo de combustible
	 */
	private short idCombustible;

	/**
	 * Fecha del precio registrado
	 */
	private LocalDate fecha;

	/**
	 * Constructor completo.
	 *
	 * @param idEess        Identificador de la estación de servicio
	 * @param idCombustible Identificador del combustible
	 * @param fecha         Fecha del precio
	 */
	public PrecioCombustibleId(Integer idEess, Short idCombustible, LocalDate fecha) {
		this.idEess        = idEess;
		this.idCombustible = idCombustible;
		this.fecha         = fecha;
	}

	/**
	 * Equals basado en todos los campos de la clave.
	 *
	 * @param o Objeto a comparar
	 * @return true si los tres campos coinciden, false en caso contrario
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PrecioCombustibleId that)) return false;
		return Objects.equals(idEess, that.idEess) &&
				Objects.equals(idCombustible, that.idCombustible) &&
				Objects.equals(fecha, that.fecha);
	}

	/**
	 * HashCode basado en todos los campos de la clave.
	 *
	 * @return código hash de la clave compuesta
	 */
	@Override
	public int hashCode() {
		return Objects.hash(idEess, idCombustible, fecha);
	}

	/**
	 * Representación en cadena de la clave compuesta.
	 *
	 * @return String con idEess, idCombustible y fecha
	 */
	@Override
	public String toString() {
		return idEess + " " + idCombustible + " " + fecha;
	}
}
