import {EstadoUbicacion, EstadoVista} from '../enums/estados.js';
import {favoritosMixin} from '../mixins/favoritos-mixin.js';
import {estacionCardMixin} from '../mixins/estacion-card-mixin.js';
import {fechaLocal, formatearDistancia, formatearPrecio} from '../utils/formato.js';

export const estacionDeServicioController = {
    mixins: [favoritosMixin, estacionCardMixin],
    inject: [
        'viewConfig',
        'estacionesDeServicioService',
        'ubicacionService',
        'geografiaService',
        'combustiblesService'
    ],
    data() {
        const hoy = new Date();
        const minimo = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - 29);
        const inicio = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - 4);

        return {
            estacion: null,
            estadoEstacion: EstadoVista.CARGANDO,
            errorCatalogos: false,
            combustibles: [],
            provincias: [],
            municipios: [],
            filtros: {combustibles: []},
            estadoDistancia: EstadoVista.CARGANDO,
            distanciaMetros: null,
            solicitudDistancia: 0,
            preciosHistoricos: [],
            estadoHistorico: EstadoVista.CARGANDO,
            solicitudHistorico: 0,
            fechaInicio: fechaLocal(inicio),
            fechaMinima: fechaLocal(minimo),
            fechaMaxima: fechaLocal(hoy),
            desmontado: false
        };
    },
    computed: {
        distanciaFormateada() {
            return formatearDistancia(this.distanciaMetros, this.viewConfig.locale);
        },
        coordenadasValidas() {
            const latitud = this.estacion?.latitud;
            const longitud = this.estacion?.longitud;
            return latitud != null && longitud != null && Number.isFinite(Number(latitud))
                && Number.isFinite(Number(longitud)) && Math.abs(Number(latitud)) <= 90 && Math.abs(Number(longitud)) <= 180;
        },
        enlaceMapa() {
            if (!this.coordenadasValidas) return null;

            return 'https://www.google.com/maps/search/?' + new URLSearchParams({
                api: 1,
                query: this.estacion.latitud + ',' + this.estacion.longitud
            });
        },
        enlaceRuta() {
            if (!this.coordenadasValidas) return null;

            return 'https://www.google.com/maps/dir/?' + new URLSearchParams({
                api: 1,
                destination: this.estacion.latitud + ',' + this.estacion.longitud,
                travelmode: 'driving'
            });
        },
        combustiblesHistorico() {
            return [...new Set(this.preciosHistoricos.map(p => p.id_combustible))]
                .sort((a, b) => Number(a) - Number(b));
        },
        filasHistorico() {
            const fechas = new Map();

            for (const precio of this.preciosHistoricos) {
                if (!fechas.has(precio.fecha))
                    fechas.set(precio.fecha, {});

                fechas.get(precio.fecha)[precio.id_combustible] = precio.precio;
            }

            return [...fechas].sort(([a], [b]) => b.localeCompare(a)).map(([fecha, precios]) => ({fecha, precios}));
        }
    },
    mounted() {
        const fecha = new URLSearchParams(window.location.search).get('fechaInicio');

        if (fecha && /^\d{4}-\d{2}-\d{2}$/.test(fecha) && !Number.isNaN(Date.parse(fecha))
            && fecha >= this.fechaMinima && fecha <= this.fechaMaxima) this.fechaInicio = fecha;

        this.cargarEstacion();
    },
    beforeUnmount() {
        this.desmontado = true;
        this.solicitudHistorico++;
        this.solicitudDistancia++;
    },
    methods: {
        async cargarEstacion() {
            this.estadoEstacion = EstadoVista.CARGANDO;

            try {
                const estacion = await this.estacionesDeServicioService.obtenerPorId(this.viewConfig.stationId);
                if (this.desmontado) return;

                this.estacion = estacion;
                this.estadoEstacion = EstadoVista.LISTO;

                document.title = estacion.rotulo + ' · ' + this.viewConfig.appName;

                await Promise.all([
                    this.cargarCatalogos(),
                    this.cargarHistorico(),
                    this.calcularDistancia()
                ]);
            } catch (error) {
                if (!this.desmontado)
                    this.estadoEstacion = error.status === 404 ? EstadoVista.NO_ENCONTRADO : EstadoVista.ERROR;
            }
        },
        async cargarCatalogos() {
            this.errorCatalogos = false;

            const respuestas = await Promise.allSettled([
                this.combustiblesService.listar(), this.geografiaService.listarProvincias(),
                this.geografiaService.listarMunicipiosPorProvincia(this.estacion.id_provincia)
            ]);


            if (this.desmontado) return;
            for (const [indice, campo] of ['combustibles', 'provincias', 'municipios'].entries()) {
                if (respuestas[indice].status === 'fulfilled') this[campo] = respuestas[indice].value;
                else this.errorCatalogos = true;
            }
        },
        async cargarHistorico() {
            if (!this.fechaInicio || this.fechaInicio < this.fechaMinima || this.fechaInicio > this.fechaMaxima
                || Number.isNaN(Date.parse(this.fechaInicio))) return;

            const solicitud = ++this.solicitudHistorico;

            this.estadoHistorico = EstadoVista.CARGANDO;
            this.preciosHistoricos = [];

            const dias = Math.round((Date.parse(this.fechaMaxima) - Date.parse(this.fechaInicio)) / 86400000) + 1;

            try {
                const precios = await this.estacionesDeServicioService.obtenerPreciosHistoricos(this.viewConfig.stationId, dias);
                if (solicitud !== this.solicitudHistorico) return;

                this.preciosHistoricos = precios;
                this.estadoHistorico = precios.length ? EstadoVista.LISTO : EstadoVista.VACIO;

                const url = new URL(window.location.href);

                url.searchParams.set('fechaInicio', this.fechaInicio);
                window.history.replaceState(null, '', url);
            } catch {
                if (solicitud === this.solicitudHistorico)
                    this.estadoHistorico = EstadoVista.ERROR;
            }
        },
        async calcularDistancia() {
            const solicitud = ++this.solicitudDistancia;
            this.estadoDistancia = EstadoVista.CARGANDO;
            this.distanciaMetros = null;
            let coordenadas;

            try {
                coordenadas = await this.ubicacionService.obtenerActual();
            } catch (error) {
                if (solicitud === this.solicitudDistancia)
                    this.estadoDistancia = Object.values(EstadoUbicacion).includes(error.code) ? error.code : EstadoUbicacion.NO_DISPONIBLE;
                return;
            }

            if (solicitud !== this.solicitudDistancia) return;

            try {
                const estacion = await this.estacionesDeServicioService.obtenerPorId(this.viewConfig.stationId, coordenadas);
                if (solicitud !== this.solicitudDistancia) return;

                const metros = estacion.distancia_metros;

                if (metros == null || !Number.isFinite(Number(metros)) || Number(metros) < 0)
                    throw new Error('Distancia no disponible');

                this.distanciaMetros = Number(metros);
                this.estadoDistancia = EstadoVista.LISTO;
            } catch {
                if (solicitud === this.solicitudDistancia) this.estadoDistancia = EstadoVista.ERROR;
            }
        },
        precioFormateado(precio) {
            return formatearPrecio(precio, this.viewConfig.locale);
        },
        fechaFormateada(fecha) {
            return new Intl.DateTimeFormat(this.viewConfig.locale).format(new Date(fecha + 'T12:00:00'));
        },
        coordenadaFormateada(valor) {
            return new Intl.NumberFormat(this.viewConfig.locale, {
                minimumFractionDigits: 5,
                maximumFractionDigits: 5
            }).format(valor);
        },
        porcentaje(valor) {
            return Math.min(100, Math.max(0, Number(valor) || 0));
        }
    }
};
