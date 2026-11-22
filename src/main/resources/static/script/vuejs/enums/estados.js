// Estados internos numéricos compartidos por controladores y plantillas.
export const EstadoVista = Object.freeze({
    SELECCION: 0,
    CARGANDO: 1,
    LISTO: 2,
    VACIO: 3,
    ERROR: 4,
    NO_ENCONTRADO: 5,
    UBICACION_DENEGADA: 6,
    UBICACION_NO_COMPATIBLE: 7,
    UBICACION_TIEMPO: 8,
    UBICACION_ERROR: 9
});

export const EstadoUbicacion = Object.freeze({
    DENEGADA: 10,
    NO_COMPATIBLE: 11,
    TIEMPO: 12,
    NO_DISPONIBLE: 13
});

export const EstadoApertura = Object.freeze({
    INDEFERENTE: 0,
    ABIERTO: 1,
    CERRADO: 2
});

export const estadosErrorUbicacion = Object.freeze([
    EstadoVista.UBICACION_DENEGADA,
    EstadoVista.UBICACION_NO_COMPATIBLE,
    EstadoVista.UBICACION_TIEMPO,
    EstadoVista.UBICACION_ERROR
]);
