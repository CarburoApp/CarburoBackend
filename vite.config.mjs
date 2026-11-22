import {defineConfig} from 'vite';
import {fileURLToPath} from 'node:url';

export default defineConfig({
    base: '/assets/',
    publicDir: false,
    define: {
        __VUE_OPTIONS_API__: true,
        __VUE_PROD_DEVTOOLS__: false,
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: false
    },
    build: {
        outDir: 'target/generated-resources/frontend/static/assets',
        emptyOutDir: true,
        rolldownOptions: {
            input: {
                'estacion-de-servicio': fileURLToPath(new URL(
                    './src/main/resources/static/script/vuejs/entries/estacion-de-servicio.js'
                    , import.meta.url
                )),
                'inicio': fileURLToPath(new URL(
                    './src/main/resources/static/script/vuejs/entries/inicio.js'
                    , import.meta.url
                )),
                'estaciones-de-servicio': fileURLToPath(new URL(
                    './src/main/resources/static/script/vuejs/entries/estaciones-de-servicio.js',
                    import.meta.url
                ))
            },
            output: {
                entryFileNames: '[name].js',
                chunkFileNames: 'chunks/[name]-[hash].js',
                assetFileNames: '[name]-[hash][extname]'
            }
        }
    }
});
