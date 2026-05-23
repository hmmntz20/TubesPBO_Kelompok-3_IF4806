/**
 * Auth store — sumber kebenaran identitas pengguna di seluruh UI.
 *
 * <h3>Karakter store</h3>
 *
 * <p>Pakai pattern yang sama dengan {@code theme-store} (Zustand +
 * hydrate) tetapi <strong>tidak persisten manual</strong> — Supabase JS
 * client sudah menangani persistensi sesi via AsyncStorage. Store ini
 * hanya cermin reactive: berlangganan
 * {@code supabase.auth.onAuthStateChange} dan men-update {@code user}
 * sesuai event.</p>
 *
 * <h3>Lifecycle</h3>
 *
 * <ol>
 *   <li>{@code _layout.tsx} memanggil {@link useAuthStore.hydrate} sekali
 *       di startup setelah theme-store hydrate.</li>
 *   <li>{@code hydrate()} mengambil sesi yang sudah ada (kalau ada) +
 *       memasang listener perubahan untuk seluruh siklus app.</li>
 *   <li>UI memanggil {@link useAuthStore.signIn} / {@code signOut} —
 *       store yang men-trigger update {@code user} via listener,
 *       tidak set manual.</li>
 * </ol>
 */
import { create } from 'zustand';

import * as authService from '@/services/auth-service';
import { supabase } from '@/services/supabase-client';
import { AuthCancelledError, AuthError, type AuthUser } from '@/types/auth';

interface AuthState {
  /** True setelah hydrate pertama selesai (sesi awal sudah dibaca). */
  hydrated: boolean;
  /** True saat signIn/signOut sedang berjalan. */
  loading: boolean;
  /** User aktif, atau null bila belum login. */
  user: AuthUser | null;
  /** Pesan error terakhir untuk ditampilkan UI; null bila tidak ada. */
  error: string | null;

  hydrate: () => Promise<void>;
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
  /** Bersihkan pesan error (mis. saat user dismiss banner). */
  clearError: () => void;
}

let listenerAttached = false;

export const useAuthStore = create<AuthState>((set, get) => ({
  hydrated: false,
  loading: false,
  user: null,
  error: null,

  hydrate: async () => {
    if (get().hydrated) return;

    // 1) Bacalah sesi awal yang sudah ada (kalau pernah login sebelumnya).
    try {
      const user = await authService.getCurrentUser();
      set({ user, hydrated: true });
    } catch {
      set({ hydrated: true });
    }

    // 2) Pasang listener sekali per JVM/JS context.
    if (!listenerAttached) {
      supabase.auth.onAuthStateChange((_event, session) => {
        const u = session?.user;
        if (u) {
          set({ user: { id: u.id, email: u.email ?? '' }, error: null });
        } else {
          set({ user: null });
        }
      });
      listenerAttached = true;
    }
  },

  signIn: async () => {
    set({ loading: true, error: null });
    try {
      await authService.signInWithGoogle();
      // user akan di-update via onAuthStateChange listener.
      set({ loading: false });
    } catch (err) {
      set({ loading: false, error: friendlyMessage(err) });
      // Tidak re-throw — UI cukup membaca state.
    }
  },

  signOut: async () => {
    set({ loading: true, error: null });
    try {
      await authService.signOut();
      set({ loading: false });
    } catch (err) {
      set({ loading: false, error: friendlyMessage(err) });
    }
  },

  clearError: () => set({ error: null }),
}));

/** Memetakan error apapun ke pesan ramah Bahasa Indonesia. */
function friendlyMessage(err: unknown): string {
  if (err instanceof AuthCancelledError) return 'Login dibatalkan.';
  if (err instanceof AuthError) return err.message;
  if (err instanceof Error) return err.message;
  return 'Terjadi kesalahan tidak terduga.';
}
