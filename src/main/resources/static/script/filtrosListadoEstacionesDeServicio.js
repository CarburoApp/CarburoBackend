document.addEventListener('DOMContentLoaded', function () {
    const btnMobile = document.getElementById('btnMobileFiltros');
    const aside = document.getElementById('asideFiltros');
    const overlay = document.getElementById('overlayFiltros');
    const cabecera = document.getElementById('cabeceraFiltros');
    const updateFiltersPanelHeight = () => {
        if (!aside) return;
        aside.style.setProperty('--filters-panel-height', `${aside.offsetHeight}px`);
    };

    if (aside && 'ResizeObserver' in window) {
        const filtersPanelObserver = new ResizeObserver(updateFiltersPanelHeight);
        filtersPanelObserver.observe(aside);
        aside.filtersPanelObserver = filtersPanelObserver;
        updateFiltersPanelHeight();
    } else {
        window.addEventListener('resize', updateFiltersPanelHeight);
        updateFiltersPanelHeight();
    }

    function abrirFiltros() {
        aside.classList.add('abierto');
        overlay.classList.add('visible');
        document.body.style.overflow = 'hidden'; // evitar scroll
    }

    function cerrarFiltros() {
        aside.classList.remove('abierto');
        overlay.classList.remove('visible');
        document.body.style.overflow = ''; // restaurar scroll
    }

    // Botón móvil abre/cierra
    btnMobile.addEventListener('click', function() {
        if (aside.classList.contains('abierto')) {
            cerrarFiltros();
        } else {
            abrirFiltros();
        }
    });

    // Overlay cierra
    overlay.addEventListener('click', cerrarFiltros);

    // Cabecera dentro del aside también cierra
    cabecera.addEventListener('click', cerrarFiltros);

    // Esc también cierra
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && aside.classList.contains('abierto')) {
            cerrarFiltros();
        }
    });
});
