/**
 * Singleton {@code SupabaseClient} untuk frontend.
 *
 * <p>Dikonfigurasi sekali pada level modul: token disimpan di
 * AsyncStorage agar sesi bertahan antar restart aplikasi, autoRefresh
 * aktif agar `access_token` tetap valid tanpa intervensi UI, dan
 * `detectSessionInUrl` dimatikan karena platform ini React Native
 * (deep-link callback ditangani manual via {@code expo-web-browser}
 * di {@link auth-service}).</p>
 *
 * <p>Nilai {@code env.supabaseUrl} / {@code supabaseAnonKey} dibaca dari
 * env var yang di-set di {@code .env}. Bila kosong (mis. developer baru
 * yang belum mengisi), `createClient` tetap dipanggil agar tipe terjamin,
 * tetapi semua call akan gagal di runtime — UI memanggil
 * {@link isSupabaseConfigured} dulu untuk menampilkan pesan ramah.</p>
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import { createClient, type SupabaseClient } from '@supabase/supabase-js';

import { env } from '@/constants/env';

export const supabase: SupabaseClient = createClient(
  // Pakai default dummy bila kosong agar createClient tidak melempar
  // (validasi shape URL terjadi di sini). UI tetap perlu cek
  // isSupabaseConfigured() sebelum memicu auth flow.
  env.supabaseUrl || 'https://placeholder.supabase.co',
  env.supabaseAnonKey || 'placeholder-anon-key',
  {
    auth: {
      storage: AsyncStorage,
      autoRefreshToken: true,
      persistSession: true,
      detectSessionInUrl: false,
    },
  },
);
