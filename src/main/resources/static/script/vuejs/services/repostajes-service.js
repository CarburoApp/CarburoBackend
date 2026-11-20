import {authenticatedApiRequest as request} from './http/api-client.js';

const BASE_URL = '/api/v1/vehiculos';
const usuarioUrl = uuid => BASE_URL + '/' + encodeURIComponent(uuid);
const repostajesUrl = (uuid, vehiculoId) => usuarioUrl(uuid) + '/' + encodeURIComponent(vehiculoId) + '/repostajes';
const repostajeUrl = (uuid, vehiculoId, id) => repostajesUrl(uuid, vehiculoId) + '/' + encodeURIComponent(id);

export const repostajesService = {
    listarPorUsuario: uuid => request(usuarioUrl(uuid) + '/repostajes'),

    listarPorVehiculo: (uuid, vehiculoId) => request(repostajesUrl(uuid, vehiculoId)),

    crear: (uuid, vehiculoId, repostaje) => request(repostajesUrl(uuid, vehiculoId), {method: 'POST', body: repostaje}),

    actualizar: (uuid, vehiculoId, id, cambios) => request(repostajeUrl(uuid, vehiculoId, id), {
        method: 'PATCH',
        body: cambios
    }),

    eliminar: (uuid, vehiculoId, id) => request(repostajeUrl(uuid, vehiculoId, id), {method: 'DELETE'})
};
