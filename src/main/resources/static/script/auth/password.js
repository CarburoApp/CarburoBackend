document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('password-input');
    const toggleBtn = document.getElementById('password-toggle-btn');
    const eyeOpen = toggleBtn.querySelector('[data-eye-open]');
    const eyeClosed = toggleBtn.querySelector('[data-eye-closed]');

    const showLabel = input.dataset.showLabel; // "Mostrar contraseña"
    const hideLabel = input.dataset.hideLabel; // "Ocultar contraseña"
    const placeholderHidden = input.dataset.placeholderHidden;
    const placeholderVisible = input.dataset.placeholderVisible;

    toggleBtn.addEventListener('click', () => {
        const isPassword = input.type === 'password';
        // Cambiar tipo
        input.type = isPassword ? 'text' : 'password';
        // Cambiar placeholder
        input.placeholder = isPassword ? placeholderVisible : placeholderHidden;
        // Cambiar iconos
        eyeOpen.classList.toggle('hidden', isPassword);
        eyeClosed.classList.toggle('hidden', !isPassword);
        // Cambiar aria-label
        toggleBtn.setAttribute('aria-label', isPassword ? hideLabel : showLabel);
    });
});