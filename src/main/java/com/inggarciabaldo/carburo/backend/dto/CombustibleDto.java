package com.inggarciabaldo.carburo.backend.dto;

import com.inggarciabaldo.carburo.backend.entities.Combustible;
import lombok.Getter;

@Getter
public class CombustibleDto {

	public short id;
	public String denominacion;
	public String codigo;
	public boolean isSeleccionado;

	public CombustibleDto(short id, String denominacion, String codigo) {
		this.id             = id;
		this.denominacion   = denominacion;
		this.codigo         = codigo;
		this.isSeleccionado = false;
	}

	public CombustibleDto(Combustible combustible) {
		this.id             = combustible.getId();
		this.denominacion   = combustible.getDenominacion();
		this.codigo         = combustible.getCodigo();
		this.isSeleccionado = false;
	}
}
