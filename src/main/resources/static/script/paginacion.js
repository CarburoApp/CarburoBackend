document.addEventListener("DOMContentLoaded", () => {
    const currentParams = new URLSearchParams(window.location.search);

    document.querySelectorAll(".pagination-link").forEach(link => {
        // Construimos un objeto URL completo usando window.location.origin
        const url = new URL(link.href, window.location.origin);
        const page = url.searchParams.get("page");

        if (page !== null) {
            // Copiamos todos los parámetros actuales
            const newParams = new URLSearchParams(currentParams);
            newParams.set("page", page);

            // Reconstruimos el href manteniendo solo el pathname y los parámetros
            link.href = url.pathname + "?" + newParams.toString();
        }
    });
});
