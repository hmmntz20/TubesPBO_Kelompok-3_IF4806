// Konfigurasi Metro untuk Telkom Route Finder.
// Menggabungkan dua kebutuhan:
//  1. NativeWind (Tailwind utility classes pada React Native).
//  2. Mengizinkan `require('@/assets/data/telmap.geojson')` agar
//     `services/geojson-service.ts` bisa memuat fallback offline.
//     File .geojson didaftarkan sebagai asset (bukan source), lalu di
//     runtime diakses via `expo-asset` + `fetch(localUri).json()`.

const { getDefaultConfig } = require('expo/metro-config');
const { withNativeWind } = require('nativewind/metro');

/** @type {import('expo/metro-config').MetroConfig} */
const config = getDefaultConfig(__dirname);

// Daftarkan .geojson sebagai asset agar Metro tahu cara me-require-nya.
// (Default sourceExts hanya tahu .js/.jsx/.ts/.tsx/.json/.svg/dst.)
if (!config.resolver.assetExts.includes('geojson')) {
  config.resolver.assetExts.push('geojson');
}

module.exports = withNativeWind(config, { input: './global.css' });
