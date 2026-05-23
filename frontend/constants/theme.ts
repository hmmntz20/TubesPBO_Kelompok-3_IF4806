/**
 * Telkom Route Finder — design tokens.
 *
 * Sumber tunggal untuk warna, surface kaca (glassmorphism), radius, spacing,
 * dan font. Komponen WAJIB membaca dari sini (langsung atau via
 * `useThemeColor` / `useAppTheme`); literal hex tidak diizinkan tersebar
 * (lihat NFR-MAINT-01).
 *
 * Aksen utama: Maroon Telkom (`#8C1D40` di Light, `#B83A5E` di Dark untuk
 * kontras). Permukaan kaca dibangun melalui primitif `GlassSurface` yang
 * mengonsumsi token dari objek `Glass`.
 */

import { Platform } from 'react-native';

export const Colors = {
  light: {
    text: '#11181C',
    textSecondary: '#4A5258',
    background: '#F4F1EE',
    gradientFrom: '#F4F1EE',
    gradientTo: '#E9DEDF',
    surface: '#F5F5F7',
    border: '#E1E3E6',
    tint: '#8C1D40',
    icon: '#4A5258',
    tabIconDefault: '#687076',
    tabIconSelected: '#8C1D40',
    brandMaroon: '#8C1D40',
    brandMaroonAlt: '#800000',
    brandGold: '#D4A24C',
    roadVehicle: '#444B52',
    roadPedestrian: '#8C1D40',
    success: '#1F7A3A',
    danger: '#B3261E',
  },
  dark: {
    text: '#ECEDEE',
    textSecondary: '#9BA1A6',
    background: '#0F1115',
    gradientFrom: '#0F1115',
    gradientTo: '#1A0E13',
    surface: '#1A1D22',
    border: '#2A2E34',
    tint: '#B83A5E',
    icon: '#9BA1A6',
    tabIconDefault: '#9BA1A6',
    tabIconSelected: '#B83A5E',
    brandMaroon: '#B83A5E',
    brandMaroonAlt: '#A12B4F',
    brandGold: '#E6BE73',
    roadVehicle: '#C7CCD1',
    roadPedestrian: '#E07A9B',
    success: '#4FBD6A',
    danger: '#F2645A',
  },
} as const;

/**
 * Token glassmorphism. Dikonsumsi oleh `<GlassSurface />` dan komponen
 * turunannya. `tint*` adalah lapisan rgba yang ditumpuk di atas `BlurView`,
 * `intensity*` adalah nilai prop `intensity` dari `expo-blur`.
 */
export const Glass = {
  light: {
    blurTint: 'light' as const,
    intensitySubtle: 30,
    intensityRegular: 50,
    intensityStrong: 70,
    tintSubtle: 'rgba(255,255,255,0.40)',
    tintRegular: 'rgba(255,255,255,0.55)',
    tintStrong: 'rgba(255,255,255,0.72)',
    border: 'rgba(255,255,255,0.70)',
    borderHighlight: 'rgba(140,29,64,0.55)',
    shadow: 'rgba(15,17,21,0.10)',
  },
  dark: {
    blurTint: 'dark' as const,
    intensitySubtle: 40,
    intensityRegular: 60,
    intensityStrong: 80,
    tintSubtle: 'rgba(20,23,28,0.40)',
    tintRegular: 'rgba(20,23,28,0.55)',
    tintStrong: 'rgba(20,23,28,0.72)',
    border: 'rgba(255,255,255,0.12)',
    borderHighlight: 'rgba(184,58,94,0.55)',
    shadow: 'rgba(0,0,0,0.45)',
  },
} as const;

export const Radius = {
  sm: 6,
  md: 10,
  lg: 16,
  xl: 20,
  '2xl': 28,
} as const;

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  '2xl': 32,
} as const;

export const Fonts = Platform.select({
  ios: {
    /** iOS `UIFontDescriptorSystemDesignDefault` */
    sans: 'system-ui',
    /** iOS `UIFontDescriptorSystemDesignSerif` */
    serif: 'ui-serif',
    /** iOS `UIFontDescriptorSystemDesignRounded` */
    rounded: 'ui-rounded',
    /** iOS `UIFontDescriptorSystemDesignMonospaced` */
    mono: 'ui-monospace',
  },
  default: {
    sans: 'normal',
    serif: 'serif',
    rounded: 'normal',
    mono: 'monospace',
  },
  web: {
    sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    serif: "Georgia, 'Times New Roman', serif",
    rounded: "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
    mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
  },
});

export type ColorScheme = keyof typeof Colors;
export type ColorToken = keyof typeof Colors.light & keyof typeof Colors.dark;
