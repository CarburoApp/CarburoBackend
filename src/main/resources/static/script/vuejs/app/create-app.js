import {createApp} from 'vue/dist/vue.esm-bundler.js';
import {services} from '../services/index.js';
import {configureApiClient} from '../services/http/api-client.js';

export function createCarburoApp(controller) {
    const root = document.getElementById('app');
    const favorites = root.querySelector('[data-favoritos]');

    configureApiClient(root.dataset);

    const app = createApp(controller);

    app.provide('viewConfig', {...root.dataset});
    app.provide('favoritosConfig', favorites ? {...favorites.dataset} : null);

    for (const [name, service] of Object.entries(services)) {
        app.provide(name, service);
    }

    return app;
}
