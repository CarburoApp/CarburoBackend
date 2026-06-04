function iniciarSesionConGoogle() {
    const SUPABASE_URL = "https://cwkimswbyczukkjdvjbt.supabase.co";

    // Modificamos esto para que apunte directamente al login general:
    const REDIRECT_TO = "https://carburo.app/login";

    const urlAuth = `${SUPABASE_URL}/auth/v1/authorize?provider=google&redirect_to=${encodeURIComponent(REDIRECT_TO)}`;
    window.location.href = urlAuth;
}