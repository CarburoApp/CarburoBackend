import {EstadoVista} from '../enums/estados.js';
import {favoritosMixin} from '../mixins/favoritos-mixin.js';
import {estacionCardMixin} from '../mixins/estacion-card-mixin.js';

export const inicioController = {
    inject: [
        'estacionesDeServicioService',
        'geografiaService',
        'combustiblesService'
    ],
    mixins: [favoritosMixin, estacionCardMixin],
    data() {
        return {
            estadoEstaciones: EstadoVista.CARGANDO,
            modoManual: false, solicitud: 0,
            comunidades: [],
            provinciasTodas: [],
            municipios: [],
            combustibles: [],
            eessMostradas: [],
            cargandoComunidades: false,
            cargandoProvincias: false,
            errorGeografia: false,
            filtros: {
                ccaa: '0',
                provincia: '0',
                combustibles: []
            }
        };
    },
    computed: {
        provincias() {
            return this.modoManual
                ? this.provinciasTodas.filter(p => String(p.id_comunidad_autonoma) === this.filtros.ccaa)
                : this.provinciasTodas;
        },
        enlaceListado() {
            return `/estaciones-de-servicio?${
                new URLSearchParams({
                    comunidadSeleccionada: this.filtros.ccaa,
                    provinciaSeleccionada: this.filtros.provincia
                })}`;
        }
    },
    mounted() {
        this.cargarComunidades();
        this.localizar();
    },
    beforeUnmount() {
        this.solicitud++;
    },
    methods: {
        reintentarGeografia() {
            return this.cargarComunidades();
        },

        async cargarComunidades() {
            this.cargandoComunidades = true;
            this.errorGeografia = false;

            try {
                const [comunidades, provincias, municipios, combustibles] = await Promise.all([
                    this.geografiaService.listarComunidades(),
                    this.geografiaService.listarProvincias(),
                    this.geografiaService.listarMunicipios(),
                    this.combustiblesService.listar()
                ]);

                this.comunidades = comunidades;
                this.provinciasTodas = provincias;
                this.municipios = municipios;
                this.combustibles = combustibles;
            } catch {
                this.errorGeografia = true;
            } finally {
                this.cargandoComunidades = false;
            }
        },
        localizar() {
            const solicitud = ++this.solicitud;
            this.modoManual = false;
            this.filtros.ccaa = '0';
            this.filtros.provincia = '0';
            this.eessMostradas = [];
            this.estadoEstaciones = EstadoVista.CARGANDO;

            if (!navigator.geolocation) {
                this.estadoEstaciones = EstadoVista.UBICACION_NO_COMPATIBLE;
                return;
            }

            navigator.geolocation.getCurrentPosition(
                posicion => this.cargarCercana(posicion.coords, solicitud),
                error => {
                    if (solicitud !== this.solicitud) return;
                    switch (error.code) {
                        case 1: this.estadoEstaciones = EstadoVista.UBICACION_DENEGADA; break;
                        case 3: this.estadoEstaciones = EstadoVista.UBICACION_TIEMPO; break;
                        default: this.estadoEstaciones = EstadoVista.UBICACION_ERROR;
                    }
                },
                {enableHighAccuracy: true, timeout: 15000, maximumAge: 300000}
            );
        },
        async cargarCercana(coords, solicitud) {
            try {
                const estaciones = await this.estacionesDeServicioService.buscarCercanas(coords.latitude, coords.longitude);
                if (solicitud !== this.solicitud) return;
                if (!estaciones.length) {
                    this.estadoEstaciones = EstadoVista.VACIO;
                    return;
                }
                const cercana = estaciones[0];
                const detalle = await this.estacionesDeServicioService.obtenerPorId(cercana.id);

                if (solicitud !== this.solicitud) return;

                this.eessMostradas = [{...detalle, distancia_metros: cercana.distancia_metros}];
                this.estadoEstaciones = EstadoVista.LISTO;
            } catch {
                if (solicitud === this.solicitud)
                    this.estadoEstaciones = EstadoVista.ERROR;
            }
        },
        cambioCCAA() {
            this.modoManual = true;
            this.filtros.provincia = '0';
            return this.cargarSeleccion();
        },
        cambioProvincia() {
            return this.cargarSeleccion();
        },
        reintentarEstaciones() {
            return this.modoManual ? this.cargarSeleccion() : this.localizar();
        },
        async cargarSeleccion() {
            const solicitud = ++this.solicitud;
            this.eessMostradas = [];

            if (this.filtros.ccaa === '0') {
                this.estadoEstaciones = EstadoVista.SELECCION;
                return;
            }

            this.estadoEstaciones = EstadoVista.CARGANDO;

            try {
                const estaciones = this.filtros.provincia !== '0'
                    ? await this.estacionesDeServicioService.listarPorProvincia(this.filtros.provincia)
                    : await this.estacionesDeServicioService.listarPorComunidad(this.filtros.ccaa);

                if (solicitud !== this.solicitud) return;

                this.eessMostradas = estaciones.slice(0, 10);
                this.estadoEstaciones = estaciones.length ? EstadoVista.LISTO : EstadoVista.VACIO;
            } catch {
                if (solicitud === this.solicitud) this.estadoEstaciones = EstadoVista.ERROR;
            }
        }
    }
};
