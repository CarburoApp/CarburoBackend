import {usuariosService} from './usuarios-service.js';
import {vehiculosService} from './vehiculos-service.js';
import {repostajesService} from './repostajes-service.js';
import {estacionesDeServicioService} from './estaciones-de-servicio-service.js';
import {geografiaService} from './geografia-service.js';
import {combustiblesService} from './combustibles-service.js';
import {favoritosService} from './favoritos-service.js';

// Registro único de servicios compartidos por todas las vistas.
export const services = {
    estacionesDeServicioService,
    geografiaService,
    combustiblesService,
    favoritosService,
    usuariosService,
    vehiculosService,
    repostajesService
};
