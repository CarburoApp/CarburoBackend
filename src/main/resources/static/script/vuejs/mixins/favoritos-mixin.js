export const favoritosMixin = {
    inject: ['favoritosService', 'favoritosConfig'],
    data: () => ({
        favoritas: [],
        favoritasPendientes: [],
        cargandoFavoritas: true,
        errorFavoritas: false,
        textoRetirarFavorita: "",
        textoAnadirFavorita: ""
    }),
    mounted() {
        const config = this.favoritosConfig;
        if (config) {
            this.textoRetirarFavorita = config.retirar;
            this.textoAnadirFavorita = config.anadir;
            this.cargarFavoritas();
        } else this.cargandoFavoritas = false;
    },
    methods: {
        esFavorita(id) {
            return this.favoritas.includes(Number(id));
        },
        async cargarFavoritas() {
            if (!this.favoritosConfig) return;
            this.cargandoFavoritas = true;
            this.errorFavoritas = false;
            try {
                this.favoritas = (await this.favoritosService.listarIds()).map(Number);
            } catch {
                this.errorFavoritas = true;
            } finally {
                this.cargandoFavoritas = false;
            }
        },
        async cambiarFavorita(id) {
            id = Number(id);
            if (!this.favoritosConfig || !Number.isSafeInteger(id) || id <= 0
                || this.cargandoFavoritas || this.errorFavoritas || this.favoritasPendientes.includes(id)) return;
            this.favoritasPendientes.push(id);
            try {
                const estado = await this.favoritosService.guardarEstado(id, !this.esFavorita(id));
                this.favoritas = this.favoritas.filter(favorita => favorita !== Number(id));
                if (estado.favorita) this.favoritas.push(Number(id));
            } catch {
                this.errorFavoritas = true;
            } finally {
                this.favoritasPendientes = this.favoritasPendientes.filter(pendiente => pendiente !== id);
            }
        }
    }
};
