/**
 * `MapLegend` — overlay kecil di atas peta yang menjelaskan styling polyline.
 *
 * Menggunakan `GlassCard` agar konsisten dengan bahasa desain glassmorphism
 * (FR-UI-09, FR-UI-11) dan tetap memungkinkan peta di belakangnya tetap
 * terlihat samar.
 */
import { StyleSheet, View } from 'react-native';

import { GlassCard } from '@/components/ui/GlassCard';
import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';

export interface MapLegendProps {
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

export function MapLegend({ style }: MapLegendProps) {
  const { colors } = useAppTheme();

  return (
    <GlassCard
      tone="regular"
      padding={Spacing.md}
      style={style}
      accessibilityRole="summary"
      accessibilityLabel="Legenda peta"
    >
      <ThemedText
        style={[styles.title, { color: colors.textSecondary }]}
        accessibilityRole="header"
      >
        Keterangan
      </ThemedText>

      <View style={styles.row}>
        <View style={[styles.swatchVehicle, { backgroundColor: colors.roadVehicle }]} />
        <ThemedText style={styles.label}>Jalan utama</ThemedText>
      </View>

      <View style={styles.row}>
        <View style={[styles.swatchPedestrian, { borderColor: colors.roadPedestrian }]} />
        <ThemedText style={styles.label}>Jalan tikus</ThemedText>
      </View>
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 11,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: Spacing.xs,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
    paddingVertical: 2,
  },
  swatchVehicle: {
    width: 24,
    height: 4,
    borderRadius: 2,
  },
  swatchPedestrian: {
    width: 24,
    height: 0,
    borderTopWidth: 2,
    borderStyle: 'dashed',
  },
  label: {
    fontSize: 13,
    fontWeight: '500',
  },
});
