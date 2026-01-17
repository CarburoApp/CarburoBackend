package com.inggarciabaldo.carburo.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;

import java.io.Serializable;

/**
 * Clase embebida que representa una coordenada geográfica.
 * <p>
 * Se utiliza como atributo embebido en entidades que requieren
 * almacenar latitud y longitud, por ejemplo {@link com.inggarciabaldo.carburo.backend.entities.EstacionDeServicio}.
 * <p>
 * Se recomienda usar siempre junto con un converter si se guarda
 * en una columna de tipo PostGIS Point.
 */
@Getter
@Embeddable
public class Coordenada implements Serializable {

	// ==============================
	// CAMPOS
	// ==============================

	/**
	 * Valor REAL almacenado en base de datos.
	 * EWKB en hexadecimal
	 * Ejemplo: 0101000020E6100000863AAC70CBC704C07B116DC7D4954540
	 */
	@Column(name = "coordenadas", nullable = false)
	private String codigo;

	/**
	 * Latitud en grados decimales (-90 a 90)
	 */
	@Transient
	private double latitud;

	/**
	 * Longitud en grados decimales (-180 a 180)
	 */
	@Transient
	private double longitud;

	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor por defecto requerido por JPA
	 */
	public Coordenada() {}

	/**
	 * Constructor completo.
	 *
	 * @param latitud  Latitud en grados decimales
	 * @param longitud Longitud en grados decimales
	 */
	public Coordenada(double latitud, double longitud) {
		setLatitud(latitud);
		setLongitud(longitud);
	}

	public Coordenada(String codigo) {
		setCodigo(codigo);
	}

	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	/**
	 * Convierte de base de datos a entidad (Point -> Coordenada)
	 * Convierte EWKB hexadecimal a Coordenada
	 */

	public void setCodigo(String codigo) {
		if (codigo == null)
			throw new IllegalArgumentException("Código de la coordenada nulo");
		this.codigo = codigo;
		transformacionACoordenadas();
	}

	public void transformacionACoordenadas() {
		if (codigo == null)
			throw new IllegalArgumentException("Código de la coordenada nulo");
		try {
			byte[] wkb = WKBReader.hexToBytes(codigo);

			WKBReader reader = new WKBReader(new GeometryFactory());
			Point point = (Point) reader.read(wkb);

			setLatitud(point.getY());     // latitud
			setLongitud(point.getX());    // longitud
		} catch (Exception e) {
			throw new IllegalStateException(
					"No se pudo convertir EWKB a Coordenada. Valor: " + codigo, e);
		}
	}

	public void setLatitud(double latitud) {
		if (latitud < -90 || latitud > 90) {
			throw new IllegalArgumentException(
					"Latitud fuera de rango (-90 a 90): " + latitud);
		}
		this.latitud = latitud;
	}

	public void setLongitud(double longitud) {
		if (longitud < -180 || longitud > 180) {
			throw new IllegalArgumentException(
					"Longitud fuera de rango (-180 a 180): " + longitud);
		}
		this.longitud = longitud;
	}


	/**
	 * Convierte de entidad a base de datos
	 * Convierte Coordenada a EWKB hexadecimal
	 */
	public String getCodigo() {
		if (codigo != null && !codigo.isBlank()) return codigo;
		try {
			// OJO: X = longitud, Y = latitud
			Point point = new GeometryFactory().createPoint(
					new Coordinate(getLongitud(), getLatitud()));

			return WKBWriter.toHex(new WKBWriter().write(point));

		} catch (Exception e) {
			throw new IllegalStateException("No se pudo convertir Coordenada a EWKB", e);
		}
	}


	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	@Override
	public String toString() {
		return "Lat=" + latitud + ", Lon=" + longitud;
	}
}
