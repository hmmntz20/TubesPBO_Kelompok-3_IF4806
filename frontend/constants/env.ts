/**
 * Akses environment variable di runtime, type-safe.
 *
 * Sumber utamanya adalah `expo.extra` yang di-set oleh `app.config.ts`.
 * Untuk env yang berpola `EXPO_PUBLIC_*`, Expo juga otomatis menyuntikkannya
 * ke `process.env` saat bundle JS — kita tetap baca dari `Constants.expoConfig?.extra`
 * agar nilai konsisten dengan yang dibaca config dinamis saat build/start.
 */
import Constants from 'expo-constants';

interface AppEnv {
  /** Base URL backend Spring Boot. */
  apiBaseUrl: string;
  /** URL project Supabase (https://<ref>.supabase.co). */
  supabaseUrl: string;
  /** Anon key (JWT publik) untuk Supabase JS client. */
  supabaseAnonKey: string;
}

const extra = (Constants.expoConfig?.extra ?? {}) as Partial<AppEnv>;

/** Singleton env terbaca. */
export const env: AppEnv = {
  apiBaseUrl:
    extra.apiBaseUrl ?? process.env.EXPO_PUBLIC_API_BASE_URL ?? 'http://10.0.2.2:8080',
  supabaseUrl:
    extra.supabaseUrl ?? process.env.EXPO_PUBLIC_SUPABASE_URL ?? '',
  supabaseAnonKey:
    extra.supabaseAnonKey ?? process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY ?? '',
};

/**
 * True bila konfigurasi Supabase sudah lengkap. Komponen UI dapat memakai
 * flag ini untuk menyembunyikan tombol login saat developer belum mengisi
 * `.env`, daripada crash di tengah jalan.
 */
export function isSupabaseConfigured(): boolean {
  return env.supabaseUrl.length > 0 && env.supabaseAnonKey.length > 0;
}
