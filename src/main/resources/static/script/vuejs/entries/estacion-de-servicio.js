import {createCarburoApp} from '../app/create-app.js';
import {estacionDeServicioController} from '../controllers/estacion-de-servicio-controller.js';

createCarburoApp(estacionDeServicioController).mount('#app');
