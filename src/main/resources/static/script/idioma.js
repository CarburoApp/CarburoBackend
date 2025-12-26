<!-- JS jQuery para dropdown y cambio de idioma -->
$(document).ready(function () {
    // Toggle dropdown
    $("#btnLanguage").click(function (e) {
        e.preventDefault();
        $("#languageDropdownMenuButton").toggle();
    });

    // Selección de idioma
    $("#languageDropdownMenuButton a").click(function (e) {
        e.preventDefault(); // cancel link
        let languageSelectedText = $(this).text().trim();
        let languageSelectedValue = $(this).attr("value");
        $("#btnLanguage span").text(languageSelectedText);
        window.location.replace('?lang=' + languageSelectedValue);
    });

    // Cerrar dropdown al click fuera
    $(document).click(function (event) {
        if (!$(event.target).closest('#btnLanguage, #languageDropdownMenuButton').length) {
            $("#languageDropdownMenuButton").hide();
        }
    });
});
$('#btnLanguage').on('click keydown', function(e) {
    if (e.type === 'click' || e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        const menu = $('#languageDropdownMenuButton');
        menu.toggle();
        $(this).attr('aria-expanded', !menu.hasClass('hidden'));
    }
});