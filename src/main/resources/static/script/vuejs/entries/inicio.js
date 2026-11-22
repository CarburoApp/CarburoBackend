import {createCarburoApp} from '../app/create-app.js';
import {inicioController} from '../controllers/inicio-controller.js';

createCarburoApp(inicioController).mount('#app');
