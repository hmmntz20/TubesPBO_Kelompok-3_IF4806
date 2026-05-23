/**
 * Theme store — sumber kebenaran untuk preferensi tema pengguna.
 *
 * Tiga kemungkinan nilai `mode`:
 *  - `'system'` → mengikuti pengaturan sistem (default).
 *  - `'light'`  → paksa terang.
 *  - `'dark'`   → paksa gelap.
 *
 * Persistensi di `AsyncStorage` dengan kunci `pref:theme-mode:v1`.
 * Hydrasi awal harus dilakukan **sebelum** lepas splash screen agar render
 * pertama tidak nge-flicker dari default ke nilai tersimpan.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';

export type ThemeMode = 'system' | 'light' | 'dark';

const STORAGE_KEY = 'pref:theme-mode:v1';
const DEFAULT_MODE: ThemeMode = 'system';

const VALID: readonly ThemeMode[] = ['system', 'light', 'dark'] as const;
const isValid = (value: unknown): value is ThemeMode =>
  typeof value === 'string' && (VALID as readonly string[]).includes(value);

interface ThemeState {
  /** True setelah hydrasi pertama kali selesai. */
  hydrated: boolean;
  /** Preferensi pengguna saat ini. */
  mode: ThemeMode;
  /** Memuat preferensi dari AsyncStorage. Idempoten — aman dipanggil ulang. */
  hydrate: () => Promise<void>;
  /** Mengganti preferensi & menulisnya ke AsyncStorage. */
  setMode: (mode: ThemeMode) => Promise<void>;
}

export const useThemeStore = create<ThemeState>((set, get) => ({
  hydrated: false,
  mode: DEFAULT_MODE,

  hydrate: async () => {
    if (get().hydrated) return;
    try {
      const raw = await AsyncStorage.getItem(STORAGE_KEY);
      const next = isValid(raw) ? raw : DEFAULT_MODE;
      set({ mode: next, hydrated: true });
    } catch (err) {
      // Storage gagal: lanjutkan dengan default agar app tetap usable.
      console.warn('[theme-store] hydrate failed, falling back to system:', err);
      set({ hydrated: true });
    }
  },

  setMode: async (mode) => {
    set({ mode });
    try {
      await AsyncStorage.setItem(STORAGE_KEY, mode);
    } catch (err) {
      console.warn('[theme-store] persist failed:', err);
    }
  },
}));
