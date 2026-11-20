import {apiGet} from './http/api-client.js';

const BASE_URL = '/api/v1/public';

export const geografiaService = {
    listarComunidades: () => apiGet(`${BASE_URL}/comunidades-autonomas`),

    obtenerArbolGeografico: () => apiGet(`${BASE_URL}/comunidades-autonomas/provincias/municipios`),

    listarProvincias: () => apiGet(`${BASE_URL}/provincias`),

    listarProvinciasPorComunidad: id => apiGet(`${BASE_URL}/provincias/comunidad-autonoma/${id}`),

    listarMunicipios: () => apiGet(`${BASE_URL}/municipios`),

    listarMunicipiosPorProvincia: id => apiGet(`${BASE_URL}/municipios/provincia/${id}`),

    listarMunicipiosConEstacionesPorProvincia: id =>
        apiGet(`${BASE_URL}/municipios/provincia/${id}/con-estaciones-de-servicio`),

    listarMunicipiosConEstacionesPorComunidad: id =>
        apiGet(`${BASE_URL}/municipios/comunidad-autonoma/${id}/con-estaciones-de-servicio`)

};
