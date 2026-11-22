import {EstadoUbicacion} from '../enums/estados.js';

class UbicacionError extends Error {
    constructor(code) {
        super('No se ha podido obtener la ubicación');
        this.name = 'UbicacionError';
        this.code = code;
    }
}

export const ubicacionService = {
    obtenerActual() {
        return new Promise((resolve, reject) => {
            if (!navigator.geolocation) {
                reject(new UbicacionError(EstadoUbicacion.NO_COMPATIBLE));
                return;
            }

            // El tiempo del navegador no siempre incluye la espera del permiso.
            let finalizada = false;
            const finalizar = (callback, resultado) => {
                if (finalizada) return;
                finalizada = true;
                clearTimeout(temporizador);
                callback(resultado);
            };

            const temporizador = setTimeout(() => finalizar(reject, new UbicacionError(EstadoUbicacion.TIEMPO)), 17000);

            navigator.geolocation.getCurrentPosition(
                ({coords}) => finalizar(resolve, {latitud: coords.latitude, longitud: coords.longitude}),
                ({code}) => {
                    let estado = EstadoUbicacion.NO_DISPONIBLE;
                    if (code === 1)
                        estado = EstadoUbicacion.DENEGADA;
                    else if (code === 3)
                        estado = EstadoUbicacion.TIEMPO;
                    finalizar(reject, new UbicacionError(estado));
                },
                {enableHighAccuracy: true, timeout: 15000, maximumAge: 300000}
            );
        });
    }
};