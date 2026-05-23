/**
 * `GlassSurface` — primitif glassmorphism untuk aplikasi.
 *
 * Komposisi visual (FR-UI-08):
 *   ┌───────────────────────────┐
 *   │ shadow (luar, iOS only)   │
 *   │  ┌─────────────────────┐  │
 *   │  │ BlurView (absolute) │  │  ← native gaussian blur (expo-blur)
 *   │  │ Tint layer (abs)    │  │  ← rgba semi-transparan
 *   │  │ Border 1px          │  │
 *   │  │ {children}          │  │  ← konten anak normal flow
 *   │  └─────────────────────┘  │
 *   └───────────────────────────┘
 *
 * Blur & tint diletakkan sebagai `StyleSheet.absoluteFill` di belakang
 * `children`, sehingga layout `GlassSurface` mengikuti bentuk children
 * (content-sized) ATAU dimensi eksplisit dari prop `style` (mis. button
 * berukuran tetap). Saat *reduce-transparency* aktif (FR-UI-12) atau
 * platform tidak mendukung BlurView (web), komponen otomatis beralih ke
 * permukaan opaque tanpa mengubah layout.
 */
import { BlurView } from 'expo-blur';
import { StyleSheet, View, type ViewProps } from 'react-native';

import { Radius } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import { useReduceTransparency } from '@/hooks/use-reduce-transparency';

export type GlassTone = 'subtle' | 'regular' | 'strong';

export interface GlassSurfaceProps extends ViewProps {
  /** Intensitas glass: subtle (legend kecil) | regular (default) | strong (modal). */
  tone?: GlassTone;
  /** Radius sudut, default `Radius.xl` (= 20). */
  radius?: number;
  /** Bila `true`, gunakan border maroon (untuk kartu glass aktif). */
  highlight?: boolean;
}

export function GlassSurface({
  tone = 'regular',
  radius = Radius.xl,
  highlight = false,
  style,
  children,
  ...rest
}: GlassSurfaceProps) {
  const { glass, colors } = useAppTheme();
  const reduceTransparency = useReduceTransparency();

  // Pilihan token berdasarkan tone.
  const intensity =
    tone === 'subtle' ? glass.intensitySubtle
    : tone === 'strong' ? glass.intensityStrong
    : glass.intensityRegular;

  const tintColor =
    tone === 'subtle' ? glass.tintSubtle
    : tone === 'strong' ? glass.tintStrong
    : glass.tintRegular;

  const borderColor = highlight ? glass.borderHighlight : glass.border;

  // Fallback opaque saat reduce-transparency aktif (FR-UI-12).
  if (reduceTransparency) {
    return (
      <View
        style={[
          shadowStyle(glass.shadow),
          {
            backgroundColor: colors.surface,
            borderRadius: radius,
            borderWidth: StyleSheet.hairlineWidth,
            borderColor: highlight ? colors.brandMaroon : colors.border,
            overflow: 'hidden',
          },
          style,
        ]}
        {...rest}
      >
        {children}
      </View>
    );
  }

  return (
    <View
      style={[
        shadowStyle(glass.shadow),
        {
          borderRadius: radius,
          borderWidth: StyleSheet.hairlineWidth,
          borderColor,
          overflow: 'hidden',
        },
        style,
      ]}
      {...rest}
    >
      <BlurView
        intensity={intensity}
        tint={glass.blurTint}
        style={[StyleSheet.absoluteFill, { borderRadius: radius }]}
      />
      <View
        style={[
          StyleSheet.absoluteFill,
          { backgroundColor: tintColor, borderRadius: radius },
        ]}
      />
      {children}
    </View>
  );
}

function shadowStyle(shadowColor: string) {
  return {
    shadowColor,
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 1,
    shadowRadius: 24,
    elevation: 8,
  };
}
