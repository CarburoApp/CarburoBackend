// JS jQuery para dropdown y cambio de idioma
$(document).ready(function () {

    // Toggle dropdown con click o teclado
    $('#btnLanguage').on('click keydown', function(e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            const menu = $('#languageDropdownMenuButton');
            menu.toggle(); // mostrar/ocultar dropdown
            $(this).attr('aria-expanded', menu.is(':visible'));
        }
    });

    // Selección de idioma usando <a>
    $('#languageDropdownMenuButton a').on('click keydown', function(e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault(); // cancelar acción por defecto
            let languageSelectedText = $(this).text().trim();
            let languageSelectedValue = $(this).data("value");

            // Actualizar texto del botón principal
            $('#btnLanguage span').text(languageSelectedText);

            // Redireccionar cambiando parámetro lang
            globalThis.location.replace('?lang=' + languageSelectedValue);

            // Cerrar dropdown
            $('#languageDropdownMenuButton').hide();
            $('#btnLanguage').attr('aria-expanded', 'false');
        }
    });

    // Cerrar dropdown al hacer click fuera
    $(document).click(function (event) {
        if (!$(event.target).closest('#btnLanguage, #languageDropdownMenuButton').length) {
            $('#languageDropdownMenuButton').hide();
            $('#btnLanguage').attr('aria-expanded', 'false');
        }
    });

    // Cerrar dropdown con Escape
    $(document).keydown(function(e) {
        if (e.key === 'Escape') {
            $('#languageDropdownMenuButton').hide();
            $('#btnLanguage').attr('aria-expanded', 'false');
        }
    });
});
