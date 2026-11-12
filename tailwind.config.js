/** @type {import('tailwindcss').Config} */
module.exports = {
    // Le dice a Tailwind dónde buscar tus clases para compilarlas
    content: [
        "./src/main/resources/templates/**/*.html",
        "./src/main/resources/static/script/**/*.{js,vue}"
    ],
    darkMode: "class",
    theme: {
        extend: {
            colors: {
                "primary": "#f48c25",
                "background-light": "#fcfaf8",
                "background-dark": "#221910",
                "surface": "#ffffff",
                "border-color": "#f4ede7",
                "text-main": "#1c140d",
                "text-secondary": "#9c7349",
            },
            fontFamily: {
                "display": ["Inter", "sans-serif"],
                "body": ["Inter", "sans-serif"],
            },
            borderRadius: {
                "DEFAULT": "0.25rem",
                "lg": "0.5rem",
                "xl": "0.75rem",
                "full": "9999px"
            },
        },
    },
    plugins: [
        require('@tailwindcss/forms'),
        require('@tailwindcss/container-queries')
    ],
}
