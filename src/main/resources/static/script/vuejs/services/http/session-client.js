// Transporte del backend web autenticado, con la sesión y el CSRF del sitio.
export async function sessionRequest(url, {method = 'GET', body} = {}) {
    const headers = {Accept: 'application/json'};

    if (method !== 'GET') {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;

        headers['Content-Type'] = 'application/x-www-form-urlencoded';

        if (token && header) headers[header] = token;
    }

    const response = await fetch(url, {
        method, body, headers,
        credentials: 'same-origin', redirect: 'error',
        signal: AbortSignal.timeout(20000)
    });

    if (!response.ok) throw new Error(`Error HTTP ${response.status}`);

    return response.json();
}
