$(document).ready(function () {

    // Función genérica para manejar dropdowns
    function setupDropdown(buttonId, menuId) {
        const $button = $(buttonId);
        const $menu = $(menuId);

        // Abrir/cerrar dropdown al click o Enter/Space
        $button.on('click keydown', function(e) {
            if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                $menu.toggle();
                $button.attr('aria-expanded', $menu.is(':visible'));
            }
        });

        // Seleccionar idioma
        $menu.find('button').on('click', function(e) {
            e.preventDefault();
            const selectedText = $(this).text().trim();
            const selectedValue = $(this).data("value");

            $button.find('span').text(selectedText);
            globalThis.location.replace('?lang=' + selectedValue);

            $menu.hide();
            $button.attr('aria-expanded', 'false');
        });

        // Cerrar dropdown al hacer click fuera
        $(document).click(function(event) {
            if (!$(event.target).closest(buttonId + ', ' + menuId).length) {
                $menu.hide();
                $button.attr('aria-expanded', 'false');
            }
        });
    }

    // Inicializamos ambos dropdowns
    setupDropdown('#btnLanguage', '#languageDropdownMenuButton');
    setupDropdown('#btnLanguageMobile', '#languageDropdownMobile');

    // Escape cierra todos los dropdowns
    $(document).keydown(function(e) {
        if (e.key === 'Escape') {
            $('#languageDropdownMenuButton, #languageDropdownMobile').hide();
            $('#btnLanguage, #btnLanguageMobile').attr('aria-expanded', 'false');
        }
    });

    // Menú hamburguesa móvil
    $('button[onclick*="mobileMenu"]').on('click', function() {
        $('#mobileMenu').toggle();
    });
});
