/**
 * Layanan autentikasi — wrapper tipis di atas Supabase JS untuk
 * menjaga kontrak yang stabil di seluruh aplikasi.
 *
 * <p>Strategi flow OAuth (FR-AUTH-01):</p>
 *
 * <ol>
 *   <li>Minta URL OAuth ke Supabase dengan
 *       {@code signInWithOAuth({ skipBrowserRedirect: true })}.</li>
 *   <li>Buka URL itu di {@code WebBrowser.openAuthSessionAsync} dengan
 *       {@code redirectTo} = deep-link aplikasi
 *       ({@code Linking.createURL('/auth/callback')}).</li>
 *   <li>Setelah Google selesai, browser redirect ke deep-link aplikasi
 *       dengan fragment {@code #access_token=...&refresh_token=...}.</li>
 *   <li>Parse fragment, panggil {@code supabase.auth.setSession(...)}
 *       agar SDK menyimpan token + memicu {@code onAuthStateChange}.</li>
 * </ol>
 *
 * <p>Flow ini bekerja di iOS & Android tanpa konfigurasi platform khusus
 * di luar {@code scheme} di {@code app.json}.</p>
 */
import * as Linking from 'expo-linking';
import * as WebBrowser from 'expo-web-browser';

import { isSupabaseConfigured } from '@/constants/env';
import { supabase } from '@/services/supabase-client';
import {
  AuthCancelledError,
  AuthConfigError,
  AuthError,
  type AuthUser,
} from '@/types/auth';

const REDIRECT_PATH = '/auth/callback';

/**
 * Memulai flow Google OAuth lewat Supabase. Mengembalikan user begitu
 * sesi terbentuk.
 *
 * @throws {AuthConfigError}    bila {@code EXPO_PUBLIC_SUPABASE_*} belum terisi.
 * @throws {AuthCancelledError} bila pengguna menutup browser sebelum selesai.
 * @throws {AuthError}          untuk kegagalan lain (Supabase error, no token).
 */
export async function signInWithGoogle(): Promise<AuthUser> {
  if (!isSupabaseConfigured()) {
    throw new AuthConfigError();
  }

  const redirectTo = Linking.createURL(REDIRECT_PATH);

  const { data, error } = await supabase.auth.signInWithOAuth({
    provider: 'google',
    options: {
      redirectTo,
      skipBrowserRedirect: true,
    },
  });
  if (error) throw new AuthError(`Supabase: ${error.message}`, error);
  if (!data?.url) throw new AuthError('URL OAuth tidak diterima dari Supabase.');

  const result = await WebBrowser.openAuthSessionAsync(data.url, redirectTo);

  if (result.type === 'cancel' || result.type === 'dismiss') {
    throw new AuthCancelledError();
  }
  if (result.type !== 'success' || !result.url) {
    throw new AuthError('Login gagal: tidak ada redirect URL.');
  }

  const tokens = parseTokensFromUrl(result.url);
  if (!tokens.access_token || !tokens.refresh_token) {
    throw new AuthError('Token tidak ditemukan di URL callback.');
  }

  const { data: sessionData, error: sessionError } = await supabase.auth.setSession({
    access_token: tokens.access_token,
    refresh_token: tokens.refresh_token,
  });
  if (sessionError) {
    throw new AuthError(`Supabase: ${sessionError.message}`, sessionError);
  }

  const user = sessionData.user;
  if (!user) throw new AuthError('Sesi terbentuk tetapi tidak ada user.');
  return toAuthUser(user);
}

/** Sign out — membersihkan sesi lokal & remote. */
export async function signOut(): Promise<void> {
  const { error } = await supabase.auth.signOut();
  if (error) throw new AuthError(`Supabase: ${error.message}`, error);
}

/**
 * Identitas user dari sesi aktif, atau {@code null} jika belum login.
 * Tidak melakukan call jaringan — hanya membaca cache SDK.
 */
export async function getCurrentUser(): Promise<AuthUser | null> {
  const { data, error } = await supabase.auth.getSession();
  if (error) return null;
  const u = data.session?.user;
  if (!u) return null;
  return toAuthUser(u);
}

/**
 * Access token JWT yang dapat dilampirkan sebagai
 * {@code Authorization: Bearer ...}. {@code null} bila belum login.
 */
export async function getAccessToken(): Promise<string | null> {
  const { data, error } = await supabase.auth.getSession();
  if (error) return null;
  return data.session?.access_token ?? null;
}

// --- helpers -----------------------------------------------------------

/**
 * Kontrak minimal yang kita butuhkan dari objek user Supabase: id +
 * email. Tipe lokal agar tidak bocor ke konsumer.
 */
type SupabaseUserShape = { id: string; email?: string | null };

function toAuthUser(u: SupabaseUserShape): AuthUser {
  return {
    id: u.id,
    email: u.email ?? '',
  };
}

/**
 * Parse fragment URL OAuth callback (`#access_token=...&refresh_token=...&...`).
 * Mengembalikan map sederhana; cukup untuk MVP (Supabase tidak memakai
 * encoded slashes).
 */
function parseTokensFromUrl(url: string): {
  access_token?: string;
  refresh_token?: string;
} {
  const fragment = url.includes('#') ? url.substring(url.indexOf('#') + 1) : '';
  const params = new URLSearchParams(fragment);
  return {
    access_token: params.get('access_token') ?? undefined,
    refresh_token: params.get('refresh_token') ?? undefined,
  };
}
