/** @type {import('tailwindcss').Config} */
module.exports = {
  // Menyesuaikan dengan struktur folder Expo Router terbaru.
  content: ['./app/**/*.{js,jsx,ts,tsx}', './components/**/*.{js,jsx,ts,tsx}'],
  presets: [require('nativewind/preset')],
  theme: {
    extend: {
      colors: {
        brand: {
          // Mirror dari constants/theme.ts → Colors.{light,dark}.brandMaroon*.
          // Komponen yang butuh warna mode-aware harus tetap pakai useAppTheme,
          // tetapi class Tailwind di bawah berguna untuk aksen statis (CTA, badge).
          maroon: '#8C1D40',
          'maroon-dark': '#B83A5E',
          'maroon-alt': '#800000',
          gold: '#D4A24C',
        },
        glass: {
          // Hex 8-digit (RRGGBBAA) untuk class Tailwind. Tint glass aktual
          // (rgba) tetap dikonsumsi via primitif <GlassSurface /> dari token JS.
          'border-light': '#FFFFFFB3',
          'border-dark': '#FFFFFF1F',
          highlight: '#8C1D408C',
        },
      },
      borderRadius: {
        glass: '20px',
        'glass-lg': '28px',
      },
    },
  },
  plugins: [],
};
