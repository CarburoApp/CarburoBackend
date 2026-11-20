export const estacionCardMixin = {
    methods: {
        nombreCombustible(id) {
            return this.combustibles.find(c => Number(c.id) === Number(id))?.denominacion || `Combustible ${id}`;
        },
        nombreMunicipio(id) {
            return this.municipios.find(m => Number(m.id) === Number(id))?.denominacion || '';
        },
        nombreProvincia(id) {
            return this.provincias.find(p => Number(p.id) === Number(id))?.denominacion || '';
        },
        preciosOrdenados(estacion) {
            const seleccionados = this.filtros.combustibles.map(Number);
            return [...(estacion.precios_de_combustibles || [])]
                .filter(p => p.precio != null && Number.isFinite(Number(p.precio)))
                .filter(p => !seleccionados.length || seleccionados.includes(Number(p.id_combustible)))
                .sort((a, b) => Number(a.id_combustible) - Number(b.id_combustible));
        }
    }
};
