package app.carburo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una Comunidad Autónoma (CCAA) en España.
 * <p>
 * Cada Comunidad Autónoma tiene:
 * - Un identificador único (PK)
 * - Una denominación
 * - Un código externo único (extCode)
 * - Un conjunto de provincias asociadas
 */
@Entity
@Table(name = "ccaa")
@Getter
@NoArgsConstructor
public class ComunidadAutonoma {

	// ==============================
	// CAMPOS
	// ==============================

	/** Identificador de la CCAA (clave primaria) */
	@Id
	private short id;

	/** Denominación de la CCAA (máximo 30 caracteres, no nulo) */
	@Column(length = 30, nullable = false)
	private String denominacion;

	/** Código externo único para la CCAA */
	@Column(name = "ext_code", unique = true, nullable = false)
	private short extCode;

	/**
	 * Conjunto de provincias que pertenecen a la CCAA.
	 * <p>
	 * Relación uno a muchos, cargada de forma perezosa (lazy) para evitar
	 * cargar todas las provincias al recuperar la CCAA.
	 */
	@Setter
	@OneToMany(mappedBy = "comunidadAutonoma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Provincia> provincias = new HashSet<>();


	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor lógico con validación de campos.
	 *
	 * @param id           Identificador de la CCAA
	 * @param denominacion Denominación de la CCAA
	 * @param extCode      Código externo de la CCAA
	 */
	public ComunidadAutonoma(short id, String denominacion, short extCode) {
		setId(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
	}

	// ==============================
	// MÉTODOS DE ACCESO A COLECCIONES
	// ==============================

	/**
	 * Devuelve una copia defensiva del conjunto de provincias.
	 * <p>
	 * Evita modificaciones externas directas sobre la colección interna.
	 *
	 * @return Copia de las provincias de la CCAA
	 */
	public Set<Provincia> getProvincias() {
		return new HashSet<>(provincias);
	}

	/**
	 * Getter interno que devuelve la colección interna sin copiar.
	 * Solo para uso interno.
	 */
	Set<Provincia> _getProvincia() {
		return provincias;
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(short id) {
		if (id <= 0) throw new IllegalArgumentException(
				"El ID de la CCAA debe ser un número positivo.");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.isBlank())
			throw new IllegalArgumentException(
					"La denominación de la CCAA no puede estar vacía.");
		if (denominacion.length() > 30)
			throw new IllegalArgumentException(
					"La denominación no puede superar los 30 caracteres.");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode <= 0)
			throw new IllegalArgumentException(
					"El ExtCode de la CCAA debe ser un número positivo.");
		this.extCode = extCode;
	}


	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	/**
	 * Igualdad basada únicamente en la clave primaria (id).
	 *
	 * @param o Objeto a comparar
	 * @return true si son la misma CCAA
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ComunidadAutonoma ca)) return false;
		return Objects.equals(id, ca.id);
	}

	/**
	 * Hashcode basado únicamente en la clave primaria (id)
	 * para mantener consistencia con equals y evitar problemas en colecciones.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Representación en cadena de la CCAA.
	 */
	@Override
	public String toString() {
		return "CCAA{" +
				"id=" + id +
				", denominacion='" + denominacion + '\'' +
				", extCode=" + extCode +
				'}';
	}
}
