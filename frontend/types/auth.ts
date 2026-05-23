/**
 * Tipe TypeScript untuk Fitur 1 (Autentikasi).
 *
 * Sengaja membungkus tipe Supabase agar layer UI tidak bergantung
 * langsung pada SDK pihak ketiga (kontrak kita sendiri stabil &
 * mudah di-mock di test).
 */

/**
 * Identitas user terautentikasi — minimal sesuai cakupan MVP Fitur 1.
 */
export interface AuthUser {
  /** UUID dari Supabase (klaim {@code sub}). */
  readonly id: string;
  /** Email user (klaim {@code email}). */
  readonly email: string;
}

// ── Error hierarchy ─────────────────────────────────────────────────────
//
// Mirror gaya yang dipakai di routing-service: base + subclass spesifik
// agar UI dapat melakukan {@code instanceof}-check tanpa parsing string.

/** Base class untuk seluruh error auth-service. */
export class AuthError extends Error {
  constructor(message: string, public readonly cause?: unknown) {
    super(message);
    this.name = 'AuthError';
  }
}

/** Pengguna membatalkan / menutup browser saat OAuth flow. */
export class AuthCancelledError extends AuthError {
  constructor(message = 'Login dibatalkan') {
    super(message);
    this.name = 'AuthCancelledError';
  }
}

/** Konfigurasi Supabase belum terisi (env var kosong). */
export class AuthConfigError extends AuthError {
  constructor(message = 'Konfigurasi Supabase belum lengkap.') {
    super(message);
    this.name = 'AuthConfigError';
  }
}
