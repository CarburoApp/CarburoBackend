document.addEventListener('DOMContentLoaded', () => {
    let changingLanguage = false;
    const languageButtons = document.querySelectorAll('.dropdown-menu [data-value]');

    document.querySelectorAll('.language-picker').forEach((picker) => {
        const toggle = picker.querySelector('.language-toggle');
        const popoverId = toggle?.getAttribute('popovertarget');
        const popover = popoverId ? document.getElementById(popoverId) : null;
        if (!toggle || !popover) return;

        const positionPopover = () => {
            if (!popover.matches(':popover-open')) return;

            const button = toggle.getBoundingClientRect();
            const width = popover.offsetWidth;
            const height = popover.offsetHeight;
            const margin = 8;
            const left = Math.max(
                margin,
                Math.min(button.right - width, window.innerWidth - width - margin)
            );
            const below = button.bottom + 6;
            const top = below + height <= window.innerHeight - margin
                ? below
                : Math.max(margin, button.top - height - 6);

            popover.style.left = `${left}px`;
            popover.style.top = `${top}px`;
            popover.style.right = 'auto';
            popover.style.bottom = 'auto';
        };

        toggle.addEventListener('click', () => requestAnimationFrame(positionPopover));
        popover.addEventListener('toggle', (event) => {
            if (event.newState === 'open') requestAnimationFrame(positionPopover);
        });
        window.addEventListener('resize', positionPopover);
        window.addEventListener('scroll', positionPopover, true);
    });

    languageButtons.forEach((button) => {
        button.addEventListener('click', async () => {
            if (changingLanguage) return;

            const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
            if (!csrfToken || !csrfHeader) {
                console.error('No se encontró el token CSRF para cambiar el idioma.');
                return;
            }

            changingLanguage = true;
            languageButtons.forEach((option) => {
                option.disabled = true;
            });

            try {
                const response = await fetch('/idioma', {
                    method: 'POST',
                    credentials: 'same-origin',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        [csrfHeader]: csrfToken
                    },
                    body: new URLSearchParams({
                        newLanguage: String(button.dataset.value).toLowerCase()
                    })
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const url = new URL(globalThis.location.href);
                url.searchParams.delete('lang');
                globalThis.history.replaceState(globalThis.history.state, '', url.pathname + url.search + url.hash);
                globalThis.location.reload();
            } catch (error) {
                console.error('No se pudo cambiar el idioma.', error);
                changingLanguage = false;
                languageButtons.forEach((option) => {
                    option.disabled = false;
                });
            }
        });
    });
});
