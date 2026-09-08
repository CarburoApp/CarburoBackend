const {createApp} = Vue;

createApp({
    data() {
        return {
            textoIntroduccion: "",
            textoPrimera: "",
            textoUltima: "",

            // Paginación.
            paginaActual: 1,
            elementosPorPagina: 10,

            comunidades: [],
            provincias: [],

            // Todos los municipios de la CCAA seleccionada.
            // Se utilizan para mostrar el nombre del municipio de cada EESS.
            municipios: [],

            // Municipios de la provincia seleccionada.
            // Se utilizan para el selector de municipio.
            municipiosProvincia: [],

            // Todos los combustibles devueltos por la API.
            combustibles: [],

            // Combustibles que son posibles con los filtros actuales.
            combustiblesDisponibles: [],

            // Marcas que son posibles con los filtros actuales.
            marcas: [],

            // Todas las EESS recuperadas de la CCAA seleccionada.
            eessRecuperadas: [],

            eessFiltradas: [],

            // Máximo 10 EESS que cumplen los filtros actuales.
            eessMostradas: [],

            // Caché de EESS por CCAA.
            eessPorCCAA: {},

            filtros: {
                ccaa: "0",
                provincia: "0",
                municipio: "0",
                estado: "INDEFERENTE",
                combustibles: [],
                marca: "0NOTSELECTED",
                busqueda: "",
                orden: "name_asc"
            },

            timeoutBusqueda: null,

            cargandoComunidades: false,
            cargandoCombustibles: false,
            cargandoProvincias: false,
            cargandoMunicipios: false,
            cargandoEstaciones: false,

            errorEstaciones: false
        };
    },

    async mounted() {
        const contadorEess = document.getElementById('contadorEess');

        this.textoIntroduccion =
            contadorEess.dataset.texto;

        const paginacion = document.getElementById('paginacion');

        this.textoPrimera =
            paginacion?.dataset.primera || "";

        this.textoUltima =
            paginacion?.dataset.ultima || "";

        await Promise.all([
            this.cargarComunidades(),
            this.cargarCombustibles(),
        ]);

        await this.inicializarFiltrosDesdeURL();

        this.actualizarTextoIntroduccion();
    },

    watch: {
        // Estos filtros se aplican inmediatamente al cambiar.
        'filtros.estado'() {
            this.aplicarFiltros();
        },

        'filtros.marca'() {
            this.aplicarFiltros();
        },

        'filtros.municipio'() {
            this.aplicarFiltros();
        },

        'filtros.combustibles': {
            deep: true,
            handler() {
                this.aplicarFiltros();
            }
        },

        // La búsqueda se aplica 500 ms después de la última modificación.
        'filtros.busqueda'(nuevoValor) {
            clearTimeout(this.timeoutBusqueda);

            // Si se borra la búsqueda, se aplica inmediatamente.
            if (nuevoValor.trim() === "") {
                this.aplicarFiltros();
                return;
            }

            this.timeoutBusqueda = setTimeout(() => {
                this.aplicarFiltros();
            }, 500);
        },

        'filtros.orden'() {
            this.aplicarFiltros();
        },
    },

    methods: {

        async inicializarFiltrosDesdeURL() {
            const params = new URLSearchParams(window.location.search);

            const comunidadSeleccionada =
                params.get('comunidadSeleccionada');

            const provinciaSeleccionada =
                params.get('provinciaSeleccionada');

            const municipioSeleccionado =
                params.get('municipioSeleccionado');

            // No hay CCAA en la URL.
            if (
                !comunidadSeleccionada ||
                comunidadSeleccionada === "0" ||
                comunidadSeleccionada === "0NOTSELECTED"
            ) {
                return;
            }

            const idCCAA = Number(comunidadSeleccionada);

            // CCAA no válida.
            if (!Number.isInteger(idCCAA) || idCCAA <= 0) {
                return;
            }

            // Seleccionar CCAA y ejecutar exactamente
            // la misma lógica que si el usuario la hubiese seleccionado.
            this.filtros.ccaa = String(idCCAA);

            await this.cambioCCAA();

            // Provincia.
            if (
                provinciaSeleccionada &&
                provinciaSeleccionada !== "0" &&
                provinciaSeleccionada !== "0NOTSELECTED"
            ) {
                const idProvincia = Number(provinciaSeleccionada);

                if (
                    Number.isInteger(idProvincia) &&
                    idProvincia > 0
                ) {
                    this.filtros.provincia = String(idProvincia);

                    await this.cargarMunicipiosProvincia(
                        String(idProvincia)
                    );
                }
            }

            // Municipio.
            if (
                municipioSeleccionado &&
                municipioSeleccionado !== "0" &&
                municipioSeleccionado !== "0NOTSELECTED"
            ) {
                const idMunicipio = Number(municipioSeleccionado);

                if (
                    Number.isInteger(idMunicipio) &&
                    idMunicipio > 0
                ) {
                    this.filtros.municipio = String(idMunicipio);
                }
            }

            // Aplicar provincia/municipio si venían en la URL.
            this.aplicarFiltros();
        },

        /*
         * Realiza una petición GET a la API.
         */
        async apiGet(endpoint) {

            const app = document.getElementById('app');
            const apiKey = app.dataset.apiKey;
            const apiURL = app.dataset.apiUrl;

            const response = await fetch(
                `${apiURL}${endpoint}`,
                {
                    headers: {
                        'X-API-KEY': apiKey,
                        'Accept': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`Error HTTP ${response.status}`);
            }

            const json = await response.json();

            if (!json.success) {
                throw new Error(
                    json.error?.message || 'Error desconocido de la API'
                );
            }

            return json.data;
        },

        /*
         * Carga las comunidades autónomas.
         */
        async cargarComunidades() {
            this.cargandoComunidades = true;

            try {
                this.comunidades = await this.apiGet(
                    '/api/v1/public/comunidades-autonomas'
                );
            } catch (error) {
                console.error('Error cargando CCAA:', error);
                this.comunidades = [];
            } finally {
                this.cargandoComunidades = false;
            }
        },

        /*
         * Cambio de comunidad autónoma.
         *
         * Al cambiar de CCAA se reinician todos los datos
         * dependientes de la selección anterior.
         */
        async cambioCCAA() {
            const idCCAA = this.filtros.ccaa;

            // Los filtros dependientes de la CCAA dejan de ser válidos.
            this.filtros.provincia = "0";
            this.filtros.municipio = "0";
            this.filtros.combustibles = [];
            this.filtros.marca = "0NOTSELECTED";

            // Limpiar datos anteriores.
            this.provincias = [];
            this.municipios = [];
            this.municipiosProvincia = [];

            this.eessRecuperadas = [];
            this.eessMostradas = [];

            this.combustiblesDisponibles = [];
            this.marcas = [];

            this.errorEstaciones = false;

            // 0 significa que no hay CCAA seleccionada.
            if (idCCAA === "0") {
                return;
            }

            // Estas tres peticiones son independientes.
            await Promise.all([
                this.cargarProvinciasCCAA(idCCAA),
                this.cargarMunicipiosCCAA(idCCAA),
                this.cargarEstacionesCCAA(idCCAA)
            ]);

            this.aplicarFiltros();
        },

        /*
         * Carga las provincias de la CCAA seleccionada.
         *
         * Si solamente existe una provincia, se selecciona
         * automáticamente y se cargan sus municipios.
         */
        async cargarProvinciasCCAA(idCCAA) {
            this.cargandoProvincias = true;

            try {
                this.provincias = await this.apiGet(
                    `/api/v1/public/provincias/comunidad-autonoma/${idCCAA}`
                );

                // Si solo hay una provincia, la seleccionamos automáticamente.
                if (this.provincias.length === 1) {
                    const idProvincia = String(
                        this.provincias[0].id
                    );

                    this.filtros.provincia = idProvincia;

                    await this.cargarMunicipiosProvincia(idProvincia);
                }
            } catch (error) {
                console.error(
                    'Error cargando provincias:',
                    error
                );

                this.provincias = [];
            } finally {
                this.cargandoProvincias = false;
            }
        },

        /*
         * Cambio de provincia.
         *
         * Las EESS no se vuelven a pedir a la API porque ya tenemos
         * todas las EESS de la CCAA. Solo necesitamos cargar los
         * municipios de la nueva provincia.
         */
        async cambioProvincia() {
            const idProvincia = this.filtros.provincia;

            // El municipio seleccionado anteriormente deja de ser válido.
            this.filtros.municipio = "0";

            this.municipiosProvincia = [];

            // 0 significa que no hay provincia seleccionada.
            if (idProvincia === "0") {
                this.aplicarFiltros();
                return;
            }

            await this.cargarMunicipiosProvincia(idProvincia);

            this.aplicarFiltros();
        },

        /*
         * Carga los municipios de una provincia.
         *
         * Si solamente existe un municipio, se selecciona
         * automáticamente.
         */
        async cargarMunicipiosProvincia(idProvincia) {
            this.cargandoMunicipios = true;

            try {
                this.municipiosProvincia = await this.apiGet(
                    `/api/v1/public/municipios/provincia/${idProvincia}/con-estaciones-de-servicio`
                );

                // Si solo hay un municipio, lo seleccionamos automáticamente.
                if (this.municipiosProvincia.length === 1) {
                    this.filtros.municipio = String(
                        this.municipiosProvincia[0].id
                    );
                }
            } catch (error) {
                console.error(
                    'Error cargando municipios de la provincia:',
                    error
                );

                this.municipiosProvincia = [];
            } finally {
                this.cargandoMunicipios = false;
            }
        },

        /*
         * Carga todos los municipios de la CCAA.
         *
         * Esta lista se utiliza para poder obtener el nombre del municipio
         * de cualquier EESS, independientemente de la provincia seleccionada.
         */
        async cargarMunicipiosCCAA(idCCAA) {
            try {
                this.municipios = await this.apiGet(
                    `/api/v1/public/municipios/comunidad-autonoma/${idCCAA}/con-estaciones-de-servicio`
                );
            } catch (error) {
                console.error(
                    'Error cargando municipios de la CCAA:',
                    error
                );

                this.municipios = [];
            }
        },

        /*
         * Carga todos los combustibles disponibles.
         *
         * Esta lista nunca se filtra.
         * Para las opciones del formulario se utiliza
         * combustiblesDisponibles.
         */
        async cargarCombustibles() {
            this.cargandoCombustibles = true;

            try {
                this.combustibles = await this.apiGet(
                    '/api/v1/public/combustibles'
                );
            } catch (error) {
                console.error(
                    'Error cargando combustibles:',
                    error
                );

                this.combustibles = [];
            } finally {
                this.cargandoCombustibles = false;
            }
        },

        /*
         * Carga las EESS de una CCAA.
         *
         * Las EESS se almacenan en caché para evitar repetir la petición
         * si el usuario vuelve posteriormente a esa CCAA.
         */
        async cargarEstacionesCCAA(idCCAA) {
            this.errorEstaciones = false;

            // Si ya tenemos las EESS de esta CCAA en caché,
            // no hacemos otra petición.
            if (
                Object.prototype.hasOwnProperty.call(
                    this.eessPorCCAA,
                    idCCAA
                )
            ) {
                this.eessRecuperadas =
                    this.eessPorCCAA[idCCAA];

                return;
            }

            this.cargandoEstaciones = true;

            try {
                const estaciones = await this.apiGet(
                    `/api/v1/public/estaciones-de-servicio/comunidad-autonoma/${idCCAA}`
                );

                // Guardar en caché.
                this.eessPorCCAA[idCCAA] = estaciones;

                // EESS de la CCAA actualmente seleccionada.
                this.eessRecuperadas = estaciones;

            } catch (error) {
                console.error(
                    'Error cargando estaciones:',
                    error
                );

                this.eessRecuperadas = [];
                this.eessMostradas = [];

                this.marcas = [];
                this.combustiblesDisponibles = [];

                this.errorEstaciones = true;

            } finally {
                this.cargandoEstaciones = false;
            }
        },

        /*
         * Filtra las EESS según los filtros actuales.
         *
         * excluirFiltro permite calcular las opciones disponibles
         * de un filtro sin aplicar ese mismo filtro.
         *
         * Por ejemplo:
         * - Para calcular las marcas se ignora la marca seleccionada.
         * - Para calcular los combustibles se ignoran los combustibles
         *   seleccionados.
         */
        filtrarEstaciones(excluirFiltro = null) {
            let estaciones = [...this.eessRecuperadas];

            // -------------------------
            // Provincia
            // -------------------------

            if (
                excluirFiltro !== 'provincia' &&
                this.filtros.provincia !== "0"
            ) {
                const idProvincia =
                    Number(this.filtros.provincia);

                estaciones = estaciones.filter(
                    estacion =>
                        Number(estacion.id_provincia) === idProvincia
                );
            }

            // -------------------------
            // Municipio
            // -------------------------

            if (
                excluirFiltro !== 'municipio' &&
                this.filtros.municipio !== "0"
            ) {
                const idMunicipio =
                    Number(this.filtros.municipio);

                estaciones = estaciones.filter(
                    estacion =>
                        Number(estacion.id_municipio) === idMunicipio
                );
            }

            // -------------------------
            // Estado
            // -------------------------

            if (excluirFiltro !== 'estado') {
                if (this.filtros.estado === "ABIERTO") {
                    estaciones = estaciones.filter(
                        estacion => estacion.abierto
                    );
                } else if (this.filtros.estado === "CERRADO") {
                    estaciones = estaciones.filter(
                        estacion => !estacion.abierto
                    );
                }
            }

            // -------------------------
            // Combustibles
            // -------------------------

            if (
                excluirFiltro !== 'combustibles' &&
                this.filtros.combustibles.length > 0
            ) {
                const seleccionados =
                    this.filtros.combustibles.map(Number);

                estaciones = estaciones.filter(estacion => {
                    const disponibles =
                        estacion.id_combustibles_disponibles || [];

                    return seleccionados.every(
                        id =>
                            disponibles
                                .map(Number)
                                .includes(id)
                    );
                });
            }

            // -------------------------
            // Marca
            // -------------------------

            if (
                excluirFiltro !== 'marca' &&
                this.filtros.marca !== "0NOTSELECTED"
            ) {
                estaciones = estaciones.filter(
                    estacion =>
                        estacion.rotulo === this.filtros.marca
                );
            }

            // -------------------------
            // Búsqueda
            // -------------------------

            if (excluirFiltro !== 'busqueda') {
                const busqueda =
                    this.filtros.busqueda.trim().toLowerCase();

                if (busqueda !== "") {
                    estaciones = estaciones.filter(estacion => {
                        const rotulo =
                            (estacion.rotulo || "").toLowerCase();

                        const direccion =
                            (estacion.direccion || "").toLowerCase();

                        const localidad =
                            (estacion.localidad || "").toLowerCase();

                        const cp =
                            String(estacion.codigo_postal || "");

                        // Buscar el municipio correspondiente al ID
                        // de la EESS dentro de todos los municipios de la CCAA.
                        const municipioObj =
                            this.municipios.find(
                                municipio =>
                                    Number(municipio.id) ===
                                    Number(estacion.id_municipio)
                            );

                        const municipio =
                            municipioObj
                                ? municipioObj.denominacion.toLowerCase()
                                : "";

                        const id =
                            String(estacion.id);

                        // Mantiene exactamente la lógica de búsqueda
                        // que existía en el backend.
                        return rotulo.includes(busqueda) ||
                            direccion.includes(busqueda) ||
                            localidad.includes(busqueda) ||
                            municipio.includes(busqueda) ||
                            cp.includes(busqueda) ||
                            id === busqueda;
                    });
                }
            }

            return estaciones;
        },

        /*
         * Calcula las marcas posibles teniendo en cuenta todos
         * los filtros excepto la propia marca.
         */
        actualizarMarcas() {
            const estaciones =
                this.filtrarEstaciones('marca');

            this.marcas = [
                ...new Set(
                    estaciones
                        .map(estacion => estacion.rotulo)
                        .filter(Boolean)
                )
            ].sort();
        },

        /*
         * Calcula los combustibles posibles teniendo en cuenta todos
         * los filtros excepto los propios combustibles seleccionados.
         */
        actualizarCombustiblesDisponibles() {
            const estaciones =
                this.filtrarEstaciones('combustibles');

            const idsDisponibles = new Set();

            estaciones.forEach(estacion => {
                (
                    estacion.id_combustibles_disponibles || []
                ).forEach(id => {
                    idsDisponibles.add(Number(id));
                });
            });

            this.combustiblesDisponibles =
                this.combustibles.filter(
                    combustible =>
                        idsDisponibles.has(
                            Number(combustible.id)
                        )
                );
        },

        /*
         * Comprueba que los filtros seleccionados siguen siendo válidos.
         *
         * Si una combinación de filtros hace que un combustible o una marca
         * deje de estar disponible, se elimina automáticamente.
         *
         * Los filtros que siguen siendo válidos se mantienen.
         */
        validarFiltrosDisponibles() {
            let cambios = false;

            // -------------------------
            // Combustibles
            // -------------------------

            const combustiblesValidos =
                new Set(
                    this.combustiblesDisponibles.map(
                        combustible =>
                            String(combustible.id)
                    )
                );

            const combustiblesActuales =
                this.filtros.combustibles;

            const combustiblesFiltrados =
                combustiblesActuales.filter(
                    id =>
                        combustiblesValidos.has(
                            String(id)
                        )
                );

            if (
                combustiblesFiltrados.length !==
                combustiblesActuales.length
            ) {
                this.filtros.combustibles =
                    combustiblesFiltrados;

                cambios = true;
            }

            // -------------------------
            // Marca
            // -------------------------

            if (
                this.filtros.marca !== "0NOTSELECTED" &&
                !this.marcas.includes(this.filtros.marca)
            ) {
                this.filtros.marca =
                    "0NOTSELECTED";

                cambios = true;
            }

            return cambios;
        },

        /*
         * Aplica todos los filtros y actualiza las opciones dinámicas.
         */
        aplicarFiltros() {
            // Primero calculamos las opciones posibles de cada filtro
            // teniendo en cuenta el resto de filtros.
            this.actualizarCombustiblesDisponibles();
            this.actualizarMarcas();

            // Si algún filtro seleccionado ha dejado de ser válido,
            // lo eliminamos.
            const cambios =
                this.validarFiltrosDisponibles();

            // Al eliminar un filtro pueden cambiar las opciones disponibles,
            // por lo que recalculamos las listas dinámicas.
            if (cambios) {
                this.actualizarCombustiblesDisponibles();
                this.actualizarMarcas();
            }

            // Todas las EESS que cumplen los filtros.
            this.eessFiltradas = this.filtrarEstaciones();

            // Ordenamos las EESS antes de paginar.
            this.eessFiltradas = this.ordenarEstaciones(
                this.eessFiltradas
            );

            // Cada vez que cambian los filtros volvemos a la primera página.
            this.paginaActual = 1;

            // Mostrar únicamente las EESS de la página actual.
            this.actualizarPagina();

            this.actualizarTextoIntroduccion();
        },

        /*
         * Devuelve el nombre de un combustible a partir de su ID.
         */
        nombreCombustible(id) {
            const combustible =
                this.combustibles.find(
                    c =>
                        Number(c.id) === Number(id)
                );

            return combustible
                ? combustible.denominacion
                : `Combustible ${id}`;
        },

        /*
         * Devuelve el nombre de un municipio a partir de su ID.
         */
        nombreMunicipio(id) {
            const municipio =
                this.municipios.find(
                    m =>
                        Number(m.id) === Number(id)
                );

            return municipio
                ? municipio.denominacion
                : '';
        },

        /*
         * Devuelve el nombre de una provincia a partir de su ID.
         */
        nombreProvincia(id) {
            const provincia =
                this.provincias.find(
                    p =>
                        Number(p.id) === Number(id)
                );

            return provincia
                ? provincia.denominacion
                : '';
        },

        /*
         * Devuelve los precios de una EESS ordenados por ID de combustible.
         */
        preciosOrdenados(estacion) {
            let precios = [
                ...(estacion.precios_de_combustibles || [])
            ];

            if (this.filtros.combustibles.length > 0) {
                const seleccionados =
                    this.filtros.combustibles.map(Number);

                precios = precios.filter(precio =>
                    seleccionados.includes(
                        Number(precio.id_combustible)
                    )
                );
            }

            return precios.sort(
                (a, b) =>
                    Number(a.id_combustible) -
                    Number(b.id_combustible)
            );
        },

        /*
         * Devuelve el precio que se utilizará para ordenar una EESS.
         *
         * Si hay combustibles seleccionados, se utiliza el precio
         * más bajo de esos combustibles.
         *
         * Si no hay combustibles seleccionados, se utiliza
         * el precio más bajo de todos los combustibles.
         */
        precioOrdenacion(estacion) {
            const precios = estacion.precios_de_combustibles || [];

            const preciosValidos = precios.filter(precio =>
                precio.precio !== null &&
                precio.precio !== undefined &&
                Number.isFinite(Number(precio.precio))
            );

            if (preciosValidos.length === 0) {
                return null;
            }

            let preciosAConsiderar = preciosValidos;

            if (this.filtros.combustibles.length > 0) {
                const combustiblesSeleccionados =
                    this.filtros.combustibles.map(Number);

                preciosAConsiderar = preciosValidos.filter(precio =>
                    combustiblesSeleccionados.includes(
                        Number(precio.id_combustible)
                    )
                );
            }

            if (preciosAConsiderar.length === 0) {
                return null;
            }

            return Math.min(
                ...preciosAConsiderar.map(precio =>
                    Number(precio.precio)
                )
            );
        },

        /*
         * Ordena las estaciones según la opción seleccionada.
         */
        ordenarEstaciones(estaciones) {
            const resultado = [...estaciones];

            if (this.filtros.orden === "name_asc") {
                resultado.sort((a, b) =>
                    (a.rotulo || "").localeCompare(
                        b.rotulo || "",
                        'es',
                        { sensitivity: 'base' }
                    )
                );

                return resultado;
            }

            if (this.filtros.orden === "name_desc") {
                resultado.sort((a, b) =>
                    (b.rotulo || "").localeCompare(
                        a.rotulo || "",
                        'es',
                        { sensitivity: 'base' }
                    )
                );

                return resultado;
            }

            if (
                this.filtros.orden === "price_asc" ||
                this.filtros.orden === "price_desc"
            ) {
                resultado.sort((a, b) => {
                    const precioA = this.precioOrdenacion(a);
                    const precioB = this.precioOrdenacion(b);

                    // Sin precio -> al final.
                    if (precioA === null && precioB === null) {
                        return 0;
                    }

                    if (precioA === null) {
                        return 1;
                    }

                    if (precioB === null) {
                        return -1;
                    }

                    if (precioA !== precioB) {
                        return this.filtros.orden === "price_asc"
                            ? precioA - precioB
                            : precioB - precioA;
                    }

                    // Si tienen el mismo precio, orden alfabético.
                    return (a.rotulo || "").localeCompare(
                        b.rotulo || "",
                        'es',
                        { sensitivity: 'base' }
                    );
                });

                return resultado;
            }

            return resultado;
        },

        actualizarTextoIntroduccion() {
            const contadorEess =
                document.getElementById('contadorEess');

            if (!contadorEess) {
                return;
            }

            contadorEess.textContent =
                this.textoIntroduccion
                    .replace('{0}', this.eessMostradas.length)
                    .replace('{1}', this.eessFiltradas.length);
        },

        totalPaginas() {
            return Math.ceil(
                this.eessFiltradas.length / this.elementosPorPagina
            );
        },

        actualizarPagina() {
            const inicio =
                (this.paginaActual - 1) * this.elementosPorPagina;

            const fin =
                inicio + this.elementosPorPagina;

            this.eessMostradas =
                this.eessFiltradas.slice(inicio, fin);
        },

        irAPagina(pagina) {
            if (
                pagina < 1 ||
                pagina > this.totalPaginas()
            ) {
                return;
            }

            this.paginaActual = pagina;
            this.actualizarPagina();
            this.actualizarTextoIntroduccion();
        },

        paginaSiguiente() {
            if (this.paginaActual < this.totalPaginas()) {
                this.paginaActual++;
                this.actualizarPagina();
                this.actualizarTextoIntroduccion();
            }
        },

        paginaAnterior() {
            if (this.paginaActual > 1) {
                this.paginaActual--;
                this.actualizarPagina();
                this.actualizarTextoIntroduccion();
            }
        },

        paginasVisibles() {
            const total = this.totalPaginas();

            if (total === 0) {
                return [];
            }

            const cantidad = 7;
            const mitad = 3;

            let inicio = this.paginaActual - mitad;
            let fin = this.paginaActual + mitad;

            if (inicio < 1) {
                inicio = 1;
                fin = Math.min(total, cantidad);
            }

            if (fin > total) {
                fin = total;
                inicio = Math.max(1, total - cantidad + 1);
            }

            const paginas = [];

            for (let i = inicio; i <= fin; i++) {
                paginas.push(i);
            }

            return paginas;
        }
    }
}).mount('#app');