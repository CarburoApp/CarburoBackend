document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-password-toggle]').forEach((toggle) => {
        const inputId = toggle.getAttribute('aria-controls');
        const input = inputId ? document.getElementById(inputId) : null;
        const eyeOpen = toggle.querySelector('[data-eye-open]');
        const eyeClosed = toggle.querySelector('[data-eye-closed]');

        if (!input || !eyeOpen || !eyeClosed) return;

        toggle.addEventListener('click', () => {
            const showPassword = input.type === 'password';
            input.type = showPassword ? 'text' : 'password';

            if (input.dataset.placeholderHidden && input.dataset.placeholderVisible) {
                input.placeholder = showPassword
                    ? input.dataset.placeholderVisible
                    : input.dataset.placeholderHidden;
            }

            eyeOpen.classList.toggle('is-hidden', showPassword);
            eyeClosed.classList.toggle('is-hidden', !showPassword);
            toggle.setAttribute(
                'aria-label',
                showPassword ? toggle.dataset.hideLabel : toggle.dataset.showLabel
            );
        });
    });
});
