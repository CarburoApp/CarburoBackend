$(document).ready(function () {
    // Dropdown desktop
    $('#btnLanguage').on('click keydown', function(e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            const menu = $('#languageDropdownMenuButton');
            menu.toggle();
            $(this).attr('aria-expanded', menu.is(':visible'));
        }
    });

    $('#languageDropdownMenuButton button').on('click', function(e) {
        e.preventDefault();
        let languageSelectedText = $(this).text().trim();
        let languageSelectedValue = $(this).data("value");
        $('#btnLanguage span').text(languageSelectedText);
        globalThis.location.replace('?lang=' + languageSelectedValue);
        $('#languageDropdownMenuButton').hide();
        $('#btnLanguage').attr('aria-expanded', 'false');
    });

    // Dropdown móvil
    $('#btnLanguageMobile').on('click keydown', function(e) {
        if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            const menu = $('#languageDropdownMobile');
            menu.toggle();
            $(this).attr('aria-expanded', menu.is(':visible'));
        }
    });

    $('#languageDropdownMobile button').on('click', function(e) {
        e.preventDefault();
        let languageSelectedText = $(this).text().trim();
        let languageSelectedValue = $(this).data("value");
        $('#btnLanguageMobile span').text(languageSelectedText);
        globalThis.location.replace('?lang=' + languageSelectedValue);
        $('#languageDropdownMobile').hide();
        $('#btnLanguageMobile').attr('aria-expanded', 'false');
    });

    // Cerrar dropdowns al hacer click fuera
    $(document).click(function (event) {
        if (!$(event.target).closest('#btnLanguage, #languageDropdownMenuButton').length) {
            $('#languageDropdownMenuButton').hide();
            $('#btnLanguage').attr('aria-expanded', 'false');
        }
        if (!$(event.target).closest('#btnLanguageMobile, #languageDropdownMobile').length) {
            $('#languageDropdownMobile').hide();
            $('#btnLanguageMobile').attr('aria-expanded', 'false');
        }
    });

    // Escape cierra ambos dropdowns
    $(document).keydown(function(e) {
        if (e.key === 'Escape') {
            $('#languageDropdownMenuButton').hide();
            $('#btnLanguage').attr('aria-expanded', 'false');
            $('#languageDropdownMobile').hide();
            $('#btnLanguageMobile').attr('aria-expanded', 'false');
        }
    });

    // Menú hamburguesa móvil
    $('button[onclick*="mobileMenu"]').on('click', function() {
        $('#mobileMenu').toggle();
    });
});
