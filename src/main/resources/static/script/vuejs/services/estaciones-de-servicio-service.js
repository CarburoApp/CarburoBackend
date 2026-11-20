import {apiGet} from './http/api-client.js';

const BASE_URL = '/api/v1/public/estaciones-de-servicio';

export const estacionesDeServicioService = {
    listar: () => apiGet(BASE_URL),

    contar: () => apiGet(`${BASE_URL}/count`),

    obtenerPorId: (id, {latitud, longitud} = {}) => apiGet(`${BASE_URL}/${id}`, {latitud, longitud}),

    buscarCercanas: (latitud, longitud, limite = 1) =>
        apiGet(`${BASE_URL}/cercanas`, {latitud, longitud, limite}),

    listarPorMunicipio: id => apiGet(`${BASE_URL}/municipio/${id}`),

    listarPorProvincia: id => apiGet(`${BASE_URL}/provincia/${id}`),

    listarPorComunidad: id => apiGet(`${BASE_URL}/comunidad-autonoma/${id}`),

    obtenerPreciosHistoricos: (id, dias = 5) =>
        apiGet(`${BASE_URL}/${id}/precios-combustibles`, {dias}),

    obtenerPreciosPorFecha: (id, fecha) =>
        apiGet(`${BASE_URL}/${id}/precios-combustibles/${encodeURIComponent(fecha)}`)
};
