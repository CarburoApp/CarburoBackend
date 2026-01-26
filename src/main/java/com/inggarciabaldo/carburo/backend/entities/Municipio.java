package com.inggarciabaldo.carburo.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa un municipio dentro de una provincia.
 * <p>
 * Cada Municipio tiene:
 * - Un identificador único (id)
 * - Una denominación
 * - Un código externo único (extCode)
 * - Una referencia a la provincia a la que pertenece
 * - Colección de estaciones de servicio asociadas
 */
@Entity
@Getter
@NoArgsConstructor
public class Municipio {

	// ==============================
	// CAMPOS
	// ==============================

	/** Identificador del municipio (clave primaria, autogenerada) */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private short id;

	/** Denominación del municipio (máximo 60 caracteres, no nulo) */
	@Column(nullable = false, length = 60)
	private String denominacion;

	/** Código externo único del municipio */
	@Column(name = "ext_code", unique = true, nullable = false)
	private short extCode;

	/**
	 * Provincia a la que pertenece el municipio.
	 * Relación muchos a uno, cargada de forma perezosa para optimizar consultas.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_provincia", nullable = false)
	private Provincia provincia;

	/**
	 * Conjunto de estaciones de servicio que pertenecen al municipio.
	 * Relación uno a muchos, con cascade ALL para propagar operaciones.
	 */
	@Setter
	@OneToMany(mappedBy = "municipio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<EstacionDeServicio> estacionesDeServicio = new HashSet<>();


	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor lógico con validación de campos.
	 *
	 * @param id          Identificador del municipio
	 * @param denominacion Nombre del municipio
	 * @param extCode      Código externo
	 * @param provincia    Provincia a la que pertenece
	 */
	public Municipio(short id, String denominacion, short extCode, Provincia provincia) {
		setId(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
		setProvincia(provincia);
	}


	// ==============================
	// MÉTODOS DE ACCESO A COLECCIONES
	// ==============================

	/**
	 * Devuelve una copia defensiva del conjunto de estaciones de servicio.
	 */
	public Set<EstacionDeServicio> getEstacionesDeServicio() {
		return new HashSet<>(estacionesDeServicio);
	}

	/**
	 * Getter interno que devuelve la colección interna sin copiar.
	 * Solo para uso interno.
	 */
	Set<EstacionDeServicio> _getEstacionesDeServicio() {
		return estacionesDeServicio;
	}


	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(short id) {
		if (id < 0)
			throw new IllegalArgumentException("El ID de municipio no puede ser negativo");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.trim().isEmpty())
			throw new IllegalArgumentException("La denominación del municipio es obligatoria");
		if (denominacion.length() > 60)
			throw new IllegalArgumentException("La denominación no puede superar los 60 caracteres");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode <= 0)
			throw new IllegalArgumentException("El ExtCode del municipio debe ser un número positivo.");
		this.extCode = extCode;
	}

	public void setProvincia(Provincia provincia) {
		if (provincia == null)
			throw new IllegalArgumentException("El municipio debe estar asociado a una provincia");
		this.provincia = provincia;
	}


	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	/**
	 * Igualdad basada únicamente en la clave primaria (id).
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Municipio that)) return false;
		return Objects.equals(id, that.id);
	}

	/**
	 * Hashcode basado únicamente en la clave primaria (id) para mantener consistencia con equals.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Representación en cadena del municipio.
	 */
	@Override
	public String toString() {
		return "Municipio{" +
				"id=" + id +
				", denominacion='" + denominacion + '\'' +
				", extCode=" + extCode +
				", provincia=" + (provincia != null ? provincia.getId() : "null") +
				'}';
	}
}
