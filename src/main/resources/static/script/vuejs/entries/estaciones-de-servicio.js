import {createCarburoApp} from '../app/create-app.js';
import {estacionesDeServicioController} from '../controllers/estaciones-de-servicio-controller.js';

createCarburoApp(estacionesDeServicioController).mount('#app');
