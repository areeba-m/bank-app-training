/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                burgundy: {
                    50: '#FDF2F4',
                    100: '#F9E1E5',
                    200: '#F2C4CB',
                    300: '#E499A5',
                    400: '#D26779',
                    500: '#BE3D54',
                    600: '#A3243B',
                    700: '#800020',
                    800: '#6B092B',
                    900: '#550723',
                    950: '#330314',
                },
                sand: {
                    50: '#FAF8F5',
                    100: '#F3EFEA',
                    200: '#E7E0D6',
                }
            },
            fontFamily: {
                sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
            },
            boxShadow: {
                'burgundy-glow': '0 4px 20px -2px rgba(128, 0, 32, 0.15)',
                'burgundy-lg': '0 10px 25px -5px rgba(128, 0, 32, 0.25)',
            }
        },
    },
    plugins: [],
}