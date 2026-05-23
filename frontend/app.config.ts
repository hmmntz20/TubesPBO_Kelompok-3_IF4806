/**
 * Konfigurasi Expo dinamis.
 *
 * `app.config.ts` dieksekusi saat build/start dan dapat membaca environment
 * variable. Ini wajib untuk kunci Google Maps API yang TIDAK boleh di-commit
 * ke `app.json` (lihat NFR-SEC-01 / FR-INFRA-07).
 *
 * Saat berjalan paralel dengan `app.json`, Expo otomatis menggabungkan
 * keduanya: nilai dari `app.json` masuk via parameter `config.ConfigContext`,
 * lalu file ini menambahkan / meng-override field di atasnya.
 *
 * Required env vars (lihat .env.example):
 *   - EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY
 *   - EXPO_PUBLIC_GOOGLE_MAPS_IOS_KEY
 *   - EXPO_PUBLIC_API_BASE_URL  (untuk fetch ke backend, dipakai di MAP-03)
 */
import type { ConfigContext, ExpoConfig } from 'expo/config';

/**
 * Apakah konfigurasi sedang dievaluasi di dalam pipeline build/CI?
 *
 * Kalau ya, kita TIDAK boleh fallback diam-diam ke string kosong saat env var
 * peta hilang — efeknya AndroidManifest tidak menulis meta-data
 * `com.google.android.geo.API_KEY` dan APK akan crash di runtime dengan
 * `IllegalStateException: API key not found`. Lebih baik gagalkan build di sini.
 *
 * `EAS_BUILD` di-set otomatis oleh EAS Build worker; `CI` di-set oleh GitHub
 * Actions / GitLab CI / dsb.
 */
const IS_BUILD_CONTEXT =
  process.env.EAS_BUILD === 'true' ||
  process.env.CI === 'true' ||
  process.env.CI === '1';

/** Membaca env var dengan validasi minimal di waktu build. */
function requireEnv(name: string, fallback?: string): string {
  const value = process.env[name];
  if (!value || value.length === 0) {
    if (IS_BUILD_CONTEXT) {
      throw new Error(
        `[app.config] env var wajib '${name}' tidak terisi saat build (EAS / CI). ` +
          'Daftarkan lewat:\n' +
          `    eas env:create --environment <development|preview|production> ` +
          `--name ${name} --value <nilai> --visibility secret\n` +
          'Lihat README §4.2.1 dan dokumentasi EAS Environment Variables.',
      );
    }
    if (fallback !== undefined) {
      // Saat developer pertama kali clone repo & belum punya .env, jangan crash
      // proses lokal (mis. `npx tsc`, lint) — biarkan placeholder agar mereka
      // tahu harus mengisi. Peta akan tampil sebagai grid kosong jika kunci
      // tidak valid.
      // eslint-disable-next-line no-console
      console.warn(
        `[app.config] ${name} kosong — memakai fallback. ` +
          'Salin .env.example ke .env dan isi nilainya untuk fitur peta penuh.',
      );
      return fallback;
    }
    throw new Error(`[app.config] env var wajib '${name}' tidak terisi`);
  }
  return value;
}

const GOOGLE_MAPS_ANDROID_KEY = requireEnv('EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY', '');
const GOOGLE_MAPS_IOS_KEY     = requireEnv('EXPO_PUBLIC_GOOGLE_MAPS_IOS_KEY', '');

export default ({ config }: ConfigContext): ExpoConfig => {
  const baseAndroid = (config.android ?? {}) as ExpoConfig['android'] & {
    config?: { googleMaps?: { apiKey?: string } };
  };
  const baseIos = (config.ios ?? {}) as ExpoConfig['ios'] & {
    config?: { googleMapsApiKey?: string };
  };

  return {
    ...config,
    name: config.name ?? 'Telkom Route Finder',
    slug: config.slug ?? 'frontend',

    android: {
      ...baseAndroid,
      package: baseAndroid.package ?? 'pbo.telkomroute',
      config: {
        ...baseAndroid.config,
        googleMaps: {
          apiKey: GOOGLE_MAPS_ANDROID_KEY,
        },
      },
    },

    ios: {
      ...baseIos,
      bundleIdentifier: baseIos.bundleIdentifier ?? 'pbo.telkomroute',
      config: {
        ...baseIos.config,
        googleMapsApiKey: GOOGLE_MAPS_IOS_KEY,
      },
    },

    extra: {
      ...config.extra,
      // Data yang dapat dibaca runtime via expo-constants → constants/env.ts.
      apiBaseUrl: process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://10.0.2.2:8080',
      supabaseUrl: process.env.EXPO_PUBLIC_SUPABASE_URL ?? '',
      supabaseAnonKey: process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY ?? '',
    },
  };
};
