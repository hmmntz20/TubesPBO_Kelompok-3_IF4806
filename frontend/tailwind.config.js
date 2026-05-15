/** @type {import('tailwindcss').Config} */
   module.exports = {
     // Menyesuaikan dengan struktur folder Expo Router terbaru
     content: ["./app/**/*.{js,jsx,ts,tsx}", "./components/**/*.{js,jsx,ts,tsx}"],
     presets: [require("nativewind/preset")],
     theme: {
       extend: {},
     },
     plugins: [],
   }