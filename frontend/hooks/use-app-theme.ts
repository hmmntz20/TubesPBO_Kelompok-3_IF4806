/**
 * Hook tema utama untuk komponen.
 *
 * Menggabungkan preferensi pengguna (`'system' | 'light' | 'dark'` di
 * theme-store) dengan color scheme sistem dari React Native, dan
 * mengembalikan token siap pakai dari `Colors` & `Glass`.
 *
 * Pemakaian:
 * ```tsx
 * const { mode, colors, glass } = useAppTheme();
 * <View style={{ backgroundColor: colors.background }} />
 * ```
 */
import { Colors, Glass } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import { useThemeStore, type ThemeMode } from '@/stores/theme-store';

export type EffectiveMode = 'light' | 'dark';

export interface AppTheme {
  /** Mode efektif yang sedang dirender (`'light'` | `'dark'`). */
  mode: EffectiveMode;
  /** Preferensi pengguna apa adanya (mungkin `'system'`). */
  preference: ThemeMode;
  /** Token warna untuk `mode` saat ini. */
  colors: typeof Colors.light | typeof Colors.dark;
  /** Token glassmorphism untuk `mode` saat ini. */
  glass: typeof Glass.light | typeof Glass.dark;
}

export function useAppTheme(): AppTheme {
  const system = useColorScheme() ?? 'light';
  const preference = useThemeStore((s) => s.mode);
  const mode: EffectiveMode = preference === 'system' ? system : preference;

  return {
    mode,
    preference,
    colors: Colors[mode],
    glass: Glass[mode],
  };
}
