package app.carburo.backend.entities;

import app.carburo.backend.util.ConSlug;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad que representa una provincia dentro de una Comunidad Autónoma.
 * <p>
 * Cada Provincia tiene:
 * - Un identificador único (id)
 * - Una denominación
 * - Un código externo único (extCode)
 * - Una referencia a la Comunidad Autónoma a la que pertenece
 * - Colecciones de municipios y estaciones de servicio asociadas
 */
@Entity
@Getter
@NoArgsConstructor
public class Provincia implements ConSlug {

	// ==============================
	// CAMPOS
	// ==============================

	/** Identificador de la provincia (clave primaria) */
	@Id
	private short id;

	/** Denominación de la provincia (máximo 30 caracteres, no nulo) */
	@Column(length = 30, nullable = false)
	private String denominacion;

	/** Código externo único de la provincia */
	@Column(name = "ext_code", unique = true, nullable = false)
	private short extCode;

	/**
	 * Comunidad Autónoma a la que pertenece la provincia.
	 * Relación muchos a uno, cargada de forma perezosa para optimizar consultas.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_ccaa", nullable = false)
	private ComunidadAutonoma comunidadAutonoma;

	/**
	 * Conjunto de municipios que pertenecen a la provincia.
	 * Relación uno a muchos, con cascade ALL para propagar operaciones.
	 */
	@Setter
	@OneToMany(mappedBy = "provincia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Municipio> municipios = new HashSet<>();

	/**
	 * Conjunto de estaciones de servicio que pertenecen a la provincia.
	 * Relación uno a muchos, con cascade ALL para propagar operaciones.
	 */
	@Setter
	@OneToMany(mappedBy = "provincia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<EstacionDeServicio> estacionesDeServicio = new HashSet<>();


	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor lógico con validación de campos.
	 *
	 * @param id                 Identificador de la provincia
	 * @param denominacion       Nombre de la provincia
	 * @param extCode            Código externo
	 * @param comunidadAutonoma  Comunidad Autónoma a la que pertenece
	 */
	public Provincia(short id, String denominacion, short extCode,
					 ComunidadAutonoma comunidadAutonoma) {
		setId(id);
		setDenominacion(denominacion);
		setExtCode(extCode);
		setComunidadAutonoma(comunidadAutonoma);
	}


	// ==============================
	// MÉTODOS DE ACCESO A COLECCIONES
	// ==============================

	/**
	 * Devuelve una copia defensiva del conjunto de municipios.
	 */
	public Set<Municipio> getMunicipios() {
		return new HashSet<>(municipios);
	}

	/**
	 * Getter interno que devuelve la colección interna sin copiar.
	 * Solo para uso interno.
	 */
	Set<Municipio> _getMunicipios() {
		return municipios;
	}

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
		if (id <= 0)
			throw new IllegalArgumentException("El ID de la provincia debe ser un número positivo.");
		this.id = id;
	}

	public void setDenominacion(String denominacion) {
		if (denominacion == null || denominacion.isBlank())
			throw new IllegalArgumentException("La denominación de la provincia no puede estar vacía.");
		if (denominacion.length() > 30)
			throw new IllegalArgumentException("La denominación no puede superar los 30 caracteres.");
		this.denominacion = denominacion.trim();
	}

	public void setExtCode(short extCode) {
		if (extCode <= 0)
			throw new IllegalArgumentException("El Ext_Code de la provincia debe ser un número positivo.");
		this.extCode = extCode;
	}

	public void setComunidadAutonoma(ComunidadAutonoma comunidadAutonoma) {
		if (comunidadAutonoma == null)
			throw new IllegalArgumentException("La provincia debe pertenecer a una CCAA.");
		this.comunidadAutonoma = comunidadAutonoma;
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
		if (!(o instanceof Provincia that)) return false;
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
	 * Representación en cadena de la provincia.
	 */
	@Override
	public String toString() {
		return "Provincia{" +
				"id=" + id +
				", denominacion='" + denominacion + '\'' +
				", extCode=" + extCode +
				", ccaa=" + (comunidadAutonoma != null ? comunidadAutonoma.getDenominacion() : null) +
				'}';
	}
}
