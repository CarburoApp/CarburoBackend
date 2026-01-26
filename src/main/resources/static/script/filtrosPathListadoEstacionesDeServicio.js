const form = document.getElementById('formFiltros');

// Enviar al cambiar cualquier select o checkbox
form.querySelectorAll('select, input[type="checkbox"]').forEach(el => {
    el.addEventListener('change', () => form.submit());
});

// Buscador: enviar al presionar Enter
const inputBusqueda = document.getElementById('filtro_buscador');
inputBusqueda.addEventListener('keydown', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        form.submit();
    }
});

// Status slider: actualizar radio oculto y enviar
document.querySelectorAll('#filtro_estado button').forEach(btn => {
    btn.addEventListener('click', () => {
        const value = btn.dataset.value;
        const radio = form.querySelector(`input[name="estadoSeleccionado"][value="${value}"]`);
        if (radio) {
            radio.checked = true;
        }
        form.submit();
    });
});