import {authenticatedApiRequest as request} from './http/api-client.js';

const BASE_URL = '/api/v1/usuarios';
const usuarioUrl = uuid => BASE_URL + '/' + encodeURIComponent(uuid);

export const usuariosService = {
    obtenerPorUuid: uuid => request(usuarioUrl(uuid)),

    existe: uuid => request(usuarioUrl(uuid), {method: 'HEAD'}),

    crear: usuario => request(BASE_URL, {method: 'POST', body: usuario}),

    obtenerProvinciaFavorita: uuid => request(usuarioUrl(uuid) + '/provincia-favorita'),

    actualizarProvinciaFavorita: (uuid, provinciaId) => request(usuarioUrl(uuid) + '/provincia-favorita/' + encodeURIComponent(provinciaId), {method: 'PATCH'}),

    listarCombustiblesFavoritos: uuid => request(usuarioUrl(uuid) + '/combustibles-favoritos'),

    actualizarCombustiblesFavoritos: (uuid, ids) => request(usuarioUrl(uuid) + '/combustibles-favoritos', {
        method: 'PATCH',
        body: Array.from(ids)
    }),

    listarEstacionesFavoritas: uuid => request(usuarioUrl(uuid) + '/estaciones-de-servicio-favoritas'),

    agregarEstacionFavorita: (uuid, estacionId) => request(usuarioUrl(uuid) + '/estaciones-de-servicio-favoritas/' + encodeURIComponent(estacionId), {method: 'PATCH'}),

    eliminarEstacionFavorita: (uuid, estacionId) => request(usuarioUrl(uuid) + '/estaciones-de-servicio-favoritas/' + encodeURIComponent(estacionId), {method: 'DELETE'})
};
