// Transporte de la API pública. Las URLs de dominio viven en sus servicios.
let configuration = {};
let accessTokenProvider;

// Se evalúa en cada petición para admitir tokens renovados.
export function configureApiAuthentication(getAccessToken) {
    if (typeof getAccessToken !== 'function') throw new TypeError('Se requiere un proveedor de access token');
    accessTokenProvider = getAccessToken;
}

export class ApiError extends Error {
    constructor(message, status, code) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.code = code;
    }
}

export function configureApiClient({apiUrl, apiKey}) {
    configuration = {apiUrl, apiKey};
}

async function request(endpoint, {parameters = {}, method = 'GET', body, authenticated = false} = {}) {
    const {apiUrl, apiKey} = configuration;
    const query = new URLSearchParams(Object.entries(parameters)
        .filter(([, value]) => value !== undefined && value !== null));
    const queryString = query.size ? '?' + query : '';
    const url = `${(apiUrl || '').replace(/\/$/, '')}${endpoint}${queryString}`;
    const headers = {Accept: 'application/json'};

    if (authenticated) {
        const token = await accessTokenProvider?.();

        if (!token) throw new ApiError('Se requiere una sesión autenticada', 401, 'ERR_UNAUTHORIZED');

        headers.Authorization = `Bearer ${token}`;
    } else if (apiKey) {
        headers['X-API-KEY'] = apiKey;
    }

    if (body !== undefined) headers['Content-Type'] = 'application/json';

    const response = await fetch(url, {
        method, headers, body: body === undefined ? undefined : JSON.stringify(body),
        signal: AbortSignal.timeout(20000)
    });

    if (method === 'HEAD') {
        if (response.status === 404) return false;
        if (!response.ok) throw new ApiError(`Error HTTP ${response.status}`, response.status);
        return true;
    }

    const json = await response.json().catch(() => null);

    if (!response.ok || !json?.success)
        throw new ApiError(json?.error?.message || `Error HTTP ${response.status}`, response.status, json?.error?.error);

    return json.data;
}

export const apiGet = (endpoint, parameters = {}) => request(endpoint, {parameters});
export const authenticatedApiRequest = (endpoint, options = {}) => request(endpoint, {...options, authenticated: true});
