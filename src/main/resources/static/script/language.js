$(document).ready(function () {
    let cambiandoIdioma = false;

    // Función genérica para manejar dropdowns
    function setupDropdown(buttonId, menuId) {
        const $button = $(buttonId);
        const $menu = $(menuId);

        // Abrir/cerrar dropdown al click o Enter/Space
        $button.on('click keydown', function (e) {
            if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                $menu.toggle();
                $button.attr('aria-expanded', $menu.is(':visible'));
            }
        });

        // Seleccionar idioma
        $menu.find('button').on('click', async function (e) {
                e.preventDefault();
                if (cambiandoIdioma) return;

                const selectedValue = $(this).data("value");
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
                const $error = $menu.find('[data-idioma-error]');
                const $opciones = $('#languageDropdownMenuButton button, #languageDropdownMobile button');

                cambiandoIdioma = true;

                $opciones.prop('disabled', true);
                $error.prop('hidden', true);

                if (!csrfToken || !csrfHeader) {
                    $error.prop('hidden', false);
                    return;
                }

                if (!csrfToken || !csrfHeader)
                    throw new Error('Falta el token CSRF.');

                const response = await fetch('/idioma', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        [csrfHeader]: csrfToken
                    },
                    body: new URLSearchParams({
                        newLanguage: String(selectedValue).toLowerCase()
                    })
                });

                if (!response.ok)
                    throw new Error(`HTTP ${response.status}`);

                // La cookie ya contiene la preferencia; se elimina cualquier lang antiguo.
                const url = new URL(globalThis.location.href);
                url.searchParams.delete('lang');

                globalThis.history.replaceState(globalThis.history.state, '', url.pathname + url.search + url.hash);
                globalThis.location.reload();

                if (!response.ok) {
                    $error.prop('hidden', false);
                    return;
                }

                cambiandoIdioma = false;
                $opciones.prop('disabled', false);

            }
        );

        // Cerrar dropdown al hacer click fuera
        $(document).click(function (event) {
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
    $(document).keydown(function (e) {
        if (e.key === 'Escape') {
            $('#languageDropdownMenuButton, #languageDropdownMobile').hide();
            $('#btnLanguage, #btnLanguageMobile').attr('aria-expanded', 'false');
        }
    });

// Menú hamburguesa móvil
    $('button[onclick*="mobileMenu"]').on('click', function () {
        $('#mobileMenu').toggle();
    });
})
;
