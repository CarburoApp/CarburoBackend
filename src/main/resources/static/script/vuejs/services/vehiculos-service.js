import { authenticatedApiRequest as request } from './http/api-client.js';

const BASE_URL = '/api/v1/vehiculos';
const usuarioUrl = uuid => BASE_URL + '/' + encodeURIComponent(uuid);
const vehiculoUrl = (uuid, id) => usuarioUrl(uuid) + '/' + encodeURIComponent(id);

export const vehiculosService = {
    listar: uuid => request(usuarioUrl(uuid)),

    obtenerPorId: (uuid, id) => request(vehiculoUrl(uuid, id)),

    crear: (uuid, vehiculo) => request(usuarioUrl(uuid), { method: 'POST', body: vehiculo }),

    actualizar: (uuid, id, cambios) => request(vehiculoUrl(uuid, id), { method: 'PATCH', body: cambios }),

    eliminar: (uuid, id) => request(vehiculoUrl(uuid, id), { method: 'DELETE' })
};
