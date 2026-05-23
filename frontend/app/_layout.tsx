import {
  DarkTheme as NavDarkTheme,
  DefaultTheme as NavDefaultTheme,
  ThemeProvider,
  type Theme,
} from '@react-navigation/native';
import { Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import 'react-native-reanimated';
import '../global.css';

import { Colors } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import { useAuthStore } from '@/stores/auth-store';
import { useThemeStore } from '@/stores/theme-store';

export const unstable_settings = {
  anchor: '(tabs)',
};

// Cegah splash auto-hide; kita kontrol sendiri setelah hydrasi tema.
SplashScreen.preventAutoHideAsync().catch(() => {
  /* race-condition saat hot-reload — abaikan */
});

/**
 * Tema React Navigation di-derive dari token Colors agar background, primary,
 * card, text, dan border selaras dengan komponen non-navigation.
 */
function buildNavTheme(mode: 'light' | 'dark'): Theme {
  const base = mode === 'dark' ? NavDarkTheme : NavDefaultTheme;
  const c = Colors[mode];
  return {
    ...base,
    dark: mode === 'dark',
    colors: {
      ...base.colors,
      primary: c.brandMaroon,
      background: c.background,
      card: c.surface,
      text: c.text,
      border: c.border,
      notification: c.brandMaroon,
    },
  };
}

export default function RootLayout() {
  const hydrate = useThemeStore((s) => s.hydrate);
  const hydrated = useThemeStore((s) => s.hydrated);
  const hydrateAuth = useAuthStore((s) => s.hydrate);
  const { mode } = useAppTheme();
  const [splashHidden, setSplashHidden] = useState(false);

  // Hydrasi preferensi tema dari AsyncStorage.
  useEffect(() => {
    hydrate();
  }, [hydrate]);

  // Hydrasi sesi Supabase + pasang listener onAuthStateChange.
  // Tidak memblokir splash — kalau gagal sekalipun, app tetap usable
  // sebagai anonymous (endpoint Fitur 3 tetap publik).
  useEffect(() => {
    hydrateAuth();
  }, [hydrateAuth]);

  // Lepas splash hanya setelah hydrasi → render pertama sudah bertema benar.
  useEffect(() => {
    if (hydrated && !splashHidden) {
      SplashScreen.hideAsync().finally(() => setSplashHidden(true));
    }
  }, [hydrated, splashHidden]);

  if (!hydrated) {
    return null;
  }

  return (
    <SafeAreaProvider>
      <ThemeProvider value={buildNavTheme(mode)}>
        <Stack>
          <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
          <Stack.Screen name="modal" options={{ presentation: 'modal', title: 'Modal' }} />
        </Stack>
        <StatusBar style={mode === 'dark' ? 'light' : 'dark'} />
      </ThemeProvider>
    </SafeAreaProvider>
  );
}
