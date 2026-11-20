import {apiGet} from './http/api-client.js';

const BASE_URL = '/api/v1/public';

export const combustiblesService = {
    listar: () => apiGet(`${BASE_URL}/combustibles`),

    listarGrupos: () => apiGet(`${BASE_URL}/grupos-de-combustibles`),

    listarAgrupados: () => apiGet(`${BASE_URL}/grupos-de-combustibles/combustibles`)
};
