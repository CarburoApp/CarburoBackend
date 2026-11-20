// Comportamiento del panel móvil. Los filtros y sus datos pertenecen al controlador.
export const filtrosPanelMixin = {
    data: () => ({filtrosVisibles: false, alturaPanelFiltros: 0}),
    mounted() {
        this.actualizarAlturaFiltros = () => {
            this.alturaPanelFiltros = this.$refs.panelFiltros?.offsetHeight || 0;
        };
        this.cerrarFiltrosConEscape = event => {
            if (event.key === 'Escape') this.filtrosVisibles = false;
        };
        document.addEventListener('keydown', this.cerrarFiltrosConEscape);
        if ('ResizeObserver' in window) {
            this.observadorFiltros = new ResizeObserver(this.actualizarAlturaFiltros);
            this.observadorFiltros.observe(this.$refs.panelFiltros);
        } else {
            window.addEventListener('resize', this.actualizarAlturaFiltros);
        }
        this.actualizarAlturaFiltros();
    },
    watch: {
        filtrosVisibles(visible) {
            document.body.style.overflow = visible ? 'hidden' : '';
        }
    },
    beforeUnmount() {
        this.observadorFiltros?.disconnect();
        document.removeEventListener('keydown', this.cerrarFiltrosConEscape);
        window.removeEventListener('resize', this.actualizarAlturaFiltros);
        document.body.style.overflow = '';
    }
};
