/**
 * `GlassCard` — preset {@link GlassSurface} untuk kartu informasi:
 * - Tone `regular` (default).
 * - Radius `Radius.xl` (= 20).
 * - Padding internal `Spacing.lg`.
 *
 * Dipakai untuk legend peta, kartu hasil pencarian, sheet asal/tujuan, dst.
 */
import { StyleSheet, View } from 'react-native';

import { Radius, Spacing } from '@/constants/theme';
import { GlassSurface, type GlassSurfaceProps } from '@/components/ui/GlassSurface';

export interface GlassCardProps extends GlassSurfaceProps {
  /** Padding internal kustom. Default `Spacing.lg`. */
  padding?: number;
}

export function GlassCard({
  padding = Spacing.lg,
  tone = 'regular',
  radius = Radius.xl,
  style,
  children,
  ...rest
}: GlassCardProps) {
  return (
    <GlassSurface tone={tone} radius={radius} style={style} {...rest}>
      <View style={[styles.content, { padding }]}>
        {children}
      </View>
    </GlassSurface>
  );
}

const styles = StyleSheet.create({
  content: {
    // Konten kartu — lapisan tint sudah ada di GlassSurface, di sini hanya padding.
  },
});
