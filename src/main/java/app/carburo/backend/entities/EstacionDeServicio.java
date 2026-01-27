package app.carburo.backend.entities;

import app.carburo.backend.entities.enums.Margen;
import app.carburo.backend.entities.enums.Remision;
import app.carburo.backend.entities.enums.Venta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Entidad que representa una estación de servicio (EESS) en el sistema.
 * Contiene información de ubicación, horarios, tipo de venta, combustibles disponibles
 * y precios históricos.
 */
@Entity
@Table(name = "eess")
@Getter
@NoArgsConstructor
public class EstacionDeServicio {

	// ==============================
	// CAMPOS
	// ==============================

	/**
	 * Identificador único de la estación (clave primaria)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	/**
	 * Código externo único
	 */
	@Column(name = "ext_code", unique = true, nullable = false)
	private int extCode;

	/**
	 * Nombre visible de la estación (rótulo)
	 */
	@Column(nullable = false, length = 100)
	private String rotulo;

	/**
	 * Horario de apertura
	 */
	@Column(nullable = false)
	private String horario;

	/**
	 * Dirección de la estación
	 */
	@Column(nullable = false)
	private String direccion;

	/**
	 * Localidad de la estación
	 */
	@Column(nullable = false)
	private String localidad;

	/**
	 * Código postal
	 */
	@Column(name = "codigo_postal", nullable = false, length = 100)
	private int codigoPostal;

	/**
	 * Relación Muchos a Uno con Municipio
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_municipio", nullable = false)
	private Municipio municipio;

	/**
	 * Relación Muchos a Uno con Provincia
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_provincia", nullable = false)
	private Provincia provincia;

	/**
	 * Coordenadas geográficas (latitud y longitud) de la estación.
	 * Se usa un converter para mapear a un tipo de base de datos único (PostGIS Point).
	 * <p>
	 * Se almacenan en BD como POINT (PostGIS)
	 * Se representan en Java como un Value Object `Coordenada`
	 * La conversión la hace `CoordenadaConverter`
	 */
	@Column(name = "coordenadas", nullable = false)
	private Coordenada coordenada;


	/**
	 * Margen de la estación, usando enumerado con converter
	 */
	@Column(nullable = false)
	private Margen margen;

	/**
	 * Tipo de remisión de la estación, enumerado
	 */
	@Column(nullable = false)
	private Remision remision;

	/**
	 * Tipo de venta de la estación, enumerado
	 */
	@Column(nullable = false)
	private Venta venta;

	/**
	 * Porcentaje de bioetanol en combustible
	 */
	@Column(name = "x100_bio_etanol")
	private double x100BioEtanol;

	/**
	 * Porcentaje de éster metílico en combustible
	 */
	@Column(name = "x100_ester_metilico")
	private double x100EsterMetilico;

	/**
	 * Relación Muchos a Muchos con Combustible (disponibles en la estación)
	 */
	@ManyToMany
	@JoinTable(
			name = "combustibledisponible", joinColumns = @JoinColumn(name = "id_eess"),
			inverseJoinColumns = @JoinColumn(name = "id_combustible")
	)
	private final Set<Combustible> combustiblesDisponibles = new HashSet<>();

	/**
	 * Relación Uno a Muchos con PrecioCombustible (histórico de precios)
	 */
	@OneToMany(
			mappedBy = "estacion", cascade = CascadeType.ALL, orphanRemoval = true,
			fetch = FetchType.LAZY
	)
	private final Set<PrecioCombustible> preciosCombustibles = new HashSet<>();


	// ==============================
	// CONSTRUCTORES
	// ==============================

	/**
	 * Constructor lógico completo sin lista de combustibles.
	 */
	public EstacionDeServicio(int id, int extCode, String rotulo, String horario,
							  String direccion, String localidad, int codigoPostal,
							  Municipio municipio, Provincia provincia, double latitud,
							  double longitud, Margen margen, Remision remision,
							  Venta venta, double x100BioEtanol,
							  double x100EsterMetilico) {
		setId(id);
		setExtCode(extCode);
		setRotulo(rotulo);
		setHorario(horario);
		setDireccion(direccion);
		setProvincia(provincia);
		setMunicipio(municipio);
		setLocalidad(localidad);
		setCodigoPostal(codigoPostal);
		this.coordenada = new Coordenada(latitud, longitud);
		setMargen(margen);
		setRemision(remision);
		setVenta(venta);
		setX100BioEtanol(x100BioEtanol);
		setX100EsterMetilico(x100EsterMetilico);
	}

	/**
	 * Constructor completo incluyendo la lista de combustibles disponibles.
	 */
	public EstacionDeServicio(int id, int extCode, String rotulo, String horario,
							  String direccion, String localidad, int codigoPostal,
							  Municipio municipio, Provincia provincia, double latitud,
							  double longitud, Margen margen, Remision remision,
							  Venta venta, double x100BioEtanol, double x100EsterMetilico,
							  List<Combustible> combustiblesDisponibles) {
		this(id, extCode, rotulo, horario, direccion, localidad, codigoPostal, municipio,
			 provincia, latitud, longitud, margen, remision, venta, x100BioEtanol,
			 x100EsterMetilico);
		setCombustiblesDisponibles(combustiblesDisponibles);
	}

	@PostLoad
	public void transformacionCoordenada() {
		if (coordenada != null) {
			coordenada.transformacionACoordenadas();
		}
	}


	/**
	 * Determina si la estación de servicio está abierta en el momento actual
	 * según el horario almacenado en el atributo 'horario'.
	 *
	 * @return true si está abierta, false si está cerrada o el horario no es válido.
	 */
	public boolean isAbierto() {
		if (horario == null || horario.isEmpty()) return false;

		// Normalizamos la cadena: eliminamos espacios y convertimos a mayúsculas
		String h = horario.trim().toUpperCase();

		// Caso especial: "24H" -> siempre abierto
		if (h.contains("24H")) return true;

		// Obtener día y hora actuales
		LocalDateTime ahora = LocalDateTime.now();
		DayOfWeek diaHoy = ahora.getDayOfWeek();
		LocalTime horaAhora = ahora.toLocalTime();

		// Mapa de abreviaturas a DayOfWeek
		Map<String, Set<DayOfWeek>> diasMap = Map.of("L", EnumSet.of(DayOfWeek.MONDAY),
													 "M", EnumSet.of(DayOfWeek.TUESDAY),
													 "X", EnumSet.of(DayOfWeek.WEDNESDAY),
													 "J", EnumSet.of(DayOfWeek.THURSDAY),
													 "V", EnumSet.of(DayOfWeek.FRIDAY),
													 "S", EnumSet.of(DayOfWeek.SATURDAY),
													 "D", EnumSet.of(DayOfWeek.SUNDAY));

		// Patrón para capturar días y intervalos: "L-V: 06:00-22:00 y 23:00-23:30"
		Pattern pattern = Pattern.compile(
				"([A-Z\\-]+):\\s*([0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}(?:\\s*y\\s*[0-9]{1,2}:[0-9]{2}-[0-9]{1,2}:[0-9]{2}){0,10})");
		Matcher matcher = pattern.matcher(h);

		// Formatter flexible: acepta horas con 1 o 2 dígitos
		DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder().appendValue(
						ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE).appendLiteral(':')
				.appendValue(ChronoField.MINUTE_OF_HOUR, 2).toFormatter();

		while (matcher.find()) {
			String dias = matcher.group(1);
			String intervalos = matcher.group(2);

			// Convertimos los días a un Set de DayOfWeek
			Set<DayOfWeek> diasSet = new HashSet<>();
			String[] diasPart = dias.split("-");
			if (diasPart.length == 2) { // rango L-V
				boolean add = false;
				for (String d : new String[]{"L", "M", "X", "J", "V", "S", "D"}) {
					if (d.equals(diasPart[0])) add = true;
					if (add) diasSet.add(diasMap.get(d).iterator().next());
					if (d.equals(diasPart[1])) break;
				}
			} else { // solo un día o días separados por coma
				for (String d : dias.split(",")) {
					diasSet.add(diasMap.get(d).iterator().next());
				}
			}

			// Si hoy no está en los días, seguimos con el siguiente bloque
			if (!diasSet.contains(diaHoy)) continue;

			// Procesamos los intervalos separados por "y"
			for (String intervalo : intervalos.split("y")) {
				String[] horas = intervalo.trim().split("-");
				LocalTime inicio = LocalTime.parse(horas[0].trim(), timeFormatter);
				LocalTime fin = LocalTime.parse(horas[1].trim(), timeFormatter);

				// Caso horario que pasa de medianoche
				if (!fin.isAfter(inicio)) {
					if (!horaAhora.isBefore(inicio) || !horaAhora.isAfter(fin)) {
						return true;
					}
				} else {
					if (!horaAhora.isBefore(inicio) && !horaAhora.isAfter(fin)) {
						return true;
					}
				}
			}
		}

		// Ningún intervalo coincide, por tanto cerrada
		return false;
	}


	public Set<PrecioCombustible> getPreciosCombustibles() {
		return new HashSet<>(preciosCombustibles.stream().filter(p -> p.getId().getFecha()
				.equals(LocalDate.now())).toList());
	}

	public Map<LocalDate, Map<Combustible, Double>> getAllPreciosCombustiblesMap(
			LocalDate fechaInicio) {

		Map<LocalDate, Map<Combustible, Double>> result = new HashMap<>();

		// 1️⃣ Rellenar con los datos reales
		for (PrecioCombustible pc : preciosCombustibles) {
			LocalDate fecha = pc.getId().getFecha();

			// Ignorar precios anteriores a la fecha inicio
			if (fecha.isBefore(fechaInicio)) {
				continue;
			}

			result.computeIfAbsent(fecha, k -> new HashMap<>())
					.put(pc.getCombustible(), pc.getPrecio());
		}

		// 2️⃣ Asegurar que existen TODOS los días desde fechaInicio hasta hoy
		LocalDate hoy = LocalDate.now();
		LocalDate fecha = fechaInicio;

		while (!fecha.isAfter(hoy)) {
			result.computeIfAbsent(fecha, k -> new HashMap<>());
			fecha = fecha.plusDays(1);
		}

		// 3️⃣ Ordenar por cercanía a hoy
		return result.entrySet().stream().sorted(Comparator.comparing(
				e -> Math.abs(ChronoUnit.DAYS.between(e.getKey(), hoy)))).collect(
				Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a,
								 LinkedHashMap::new));
	}



	// ==============================
	// MÉTODOS DE GESTIÓN DE COLECCIONES
	// ==============================

	public void addPrecioCombustible(double precio, Combustible combustible,
									 LocalDate fecha) {
		PrecioCombustible precioC = new PrecioCombustible(this, combustible, fecha,
														  precio);
		this.preciosCombustibles.add(precioC);
	}

	public void addCombustibleDisponible(Combustible combustible) {
		this.combustiblesDisponibles.add(combustible);
	}

	public boolean removeCombustibleDisponible(Combustible combustible) {
		return this.combustiblesDisponibles.remove(combustible);
	}


	// ==============================
	// SETTERS CON VALIDACIÓN
	// ==============================

	public void setId(int id) {
		if (id < 0) throw new IllegalArgumentException("El id debe ser positivo: " + id);
		this.id = id;
	}

	public void setExtCode(int extCode) {
		if (extCode < 0) throw new IllegalArgumentException(
				"El extCode debe ser positivo: " + extCode);
		this.extCode = extCode;
	}

	public void setRotulo(String rotulo) {
		if (rotulo == null || rotulo.isBlank() || rotulo.length() > 100)
			throw new IllegalArgumentException("Rótulo inválido: " + rotulo);
		this.rotulo = rotulo;
	}

	public void setHorario(String horario) {
		if (horario == null || horario.isBlank() || horario.length() > 100)
			throw new IllegalArgumentException("Horario inválido: " + horario);
		this.horario = horario;
	}

	public void setDireccion(String direccion) {
		if (direccion == null || direccion.isBlank() || direccion.length() > 200)
			throw new IllegalArgumentException("Dirección inválida: " + direccion);
		this.direccion = direccion;
	}

	public void setLocalidad(String localidad) {
		if (localidad == null || localidad.isBlank() || localidad.length() > 100)
			throw new IllegalArgumentException("Localidad inválida: " + localidad);
		this.localidad = localidad;
	}

	public void setMunicipio(Municipio municipio) {
		if (municipio == null)
			throw new IllegalArgumentException("El Municipio no puede ser nulo.");
		this.municipio = municipio;
	}

	public void setProvincia(Provincia provincia) {
		if (provincia == null)
			throw new IllegalArgumentException("La provincia no puede ser nula.");
		this.provincia = provincia;
	}

	public void setCodigoPostal(int codigoPostal) {
		if (codigoPostal < 1 || codigoPostal > 52999) throw new IllegalArgumentException(
				"Código postal no válido para España: " + codigoPostal);
		this.codigoPostal = codigoPostal;
	}

	public void setX100BioEtanol(double x100BioEtanol) {
		if (x100BioEtanol < 0 || x100BioEtanol > 100) throw new IllegalArgumentException(
				"El porcentaje de bioetanol debe estar entre 0 y 100: " + x100BioEtanol);
		this.x100BioEtanol = x100BioEtanol;
	}

	public void setX100EsterMetilico(double x100EsterMetilico) {
		if (x100EsterMetilico < 0 || x100EsterMetilico > 100)
			throw new IllegalArgumentException(
					"El porcentaje de éster metílico debe estar entre 0 y 100: " +
							x100EsterMetilico);
		this.x100EsterMetilico = x100EsterMetilico;
	}

	public void setMargen(Margen margen) {
		if (margen == null)
			throw new IllegalArgumentException("El margen no puede ser nulo.");
		this.margen = margen;
	}

	public void setRemision(Remision remision) {
		if (remision == null)
			throw new IllegalArgumentException("La remisión no puede ser nula.");
		this.remision = remision;
	}

	public void setVenta(Venta venta) {
		if (venta == null)
			throw new IllegalArgumentException("La venta no puede ser nula.");
		this.venta = venta;
	}

	private void setCombustiblesDisponibles(List<Combustible> combustiblesDisponibles) {
		if (combustiblesDisponibles == null) throw new IllegalArgumentException(
				"La lista de combustibles disponibles no puede ser nula.");
		this.combustiblesDisponibles.addAll(combustiblesDisponibles);
	}


	// ==============================
	// MÉTODOS COMUNES
	// ==============================

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EstacionDeServicio that)) return false;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ES{" + "id=" + id + ", extCode=" + extCode + ", rotulo='" + rotulo +
				'\'' + ", horario='" + horario + '\'' + ", direccion='" + direccion +
				'\'' + ", localidad='" + localidad + '\'' + ", codigoPostal=" +
				codigoPostal + ", municipio=" +
				(municipio != null ? municipio.getDenominacion() : "null") +
				", provincia=" +
				(provincia != null ? provincia.getDenominacion() : "null") +
				", coordenadas=" + getCoordenada() + ", margen=" + margen +
				", remision=" + remision + ", venta=" + venta + ", x100BioEtanol=" +
				x100BioEtanol + ", x100EsterMetilico=" + x100EsterMetilico + '}';
	}
}
