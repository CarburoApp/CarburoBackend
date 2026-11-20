import {sessionRequest} from './http/session-client.js';

const BASE_URL = '/perfil/estaciones-de-servicio/favoritas';

export const favoritosService = {
    listarIds: () => sessionRequest(`${BASE_URL}/estado`),

    guardarEstado: (id, favorita) => sessionRequest(`${BASE_URL}/${id}/estado`, {
        method: 'POST',
        body: new URLSearchParams({favorita})
    })
};
