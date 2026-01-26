document.addEventListener("DOMContentLoaded", function () {
    if (!("geolocation" in navigator)) return;

    const csrfToken = document
        .querySelector('meta[name="_csrf"]')
        .getAttribute('content');

    const csrfHeader = document
        .querySelector('meta[name="_csrf_header"]')
        .getAttribute('content');

    const geoOptions = {
        enableHighAccuracy: true,
        timeout: 15000,    // 15 segundos
        maximumAge: 300000 // 5 minutos
    };

    navigator.geolocation.getCurrentPosition(
        function (position) {
            const lat = position.coords.latitude;
            const lon = position.coords.longitude;

            fetch("/inicio/ubicacion", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                    [csrfHeader]: csrfToken
                },
                body: `lat=${lat}&lon=${lon}`
            })
                .then(res => res.text())
                .then(html => {
                    const container = document.getElementById("estacion-container");
                    container.innerHTML = html; // insertamos fragmento Thymeleaf
                    // Ocultar loader siempre
                    const loader = document.getElementById("loading-estacion");
                    if (loader) loader.remove();
                })
                .catch(err => console.error("Error en fetch:", err));

        },
        function (error) {
            console.error("Error al obtener la ubicación:", error);
            const container = document.getElementById("estacion-container");
            container.innerHTML = `<div class="text-center text-text-secondary text-lg font-medium mt-4">
                No hemos podido localizar la estación más cercana.
            </div>`;
        },
        geoOptions
    );

});