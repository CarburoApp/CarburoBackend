document.addEventListener("DOMContentLoaded", () => {
    const hash = window.location.hash;

    if (!hash) return;

    const inputToken = document.getElementById("token");
    if (!hash) {
        if (inputToken && !inputToken.value) {
            window.location.href = "/login";
        }
        return;
    }

    const params = new URLSearchParams(hash.substring(1));
    const token = params.get("access_token");

    if (!token) {
        // Solo redirigir si el input del backend tampoco tiene datos
        if (inputToken && !inputToken.value) {
            window.location.href = "/login";
        }
        return;
    }

    if (inputToken) {
        inputToken.value = token;
    }

    // Limpia la URL estéticamente para que el JWT no quede

    window.history.replaceState({}, document.title, window.location.pathname);
});