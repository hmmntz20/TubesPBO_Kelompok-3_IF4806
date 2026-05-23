/**
 * `RouteSheet` — bottom-sheet ringkasan rute aktif (FR-UI-RT-05).
 *
 * <h3>Konten</h3>
 * <ul>
 *   <li>Ikon moda + label Bahasa Indonesia (sepatu / motor / mobil).</li>
 *   <li>Jarak total — formatter "X.X km" / "Y m" via {@link formatLength}.</li>
 *   <li>Durasi estimasi — formatter "Y menit" via {@link formatDuration}.</li>
 *   <li>Tombol "Hapus rute" — memanggil {@code onClear} (membersihkan store).</li>
 * </ul>
 *
 * <h3>Aksesibilitas</h3>
 * <p>Container memiliki {@code accessibilityRole="summary"} dan urutan
 * fokus: mode → jarak → durasi → aksi (NFR-RT-A11Y-02).</p>
 */
import { StyleSheet, View } from 'react-native';

import { GlassButton } from '@/components/ui/GlassButton';
import { GlassCard } from '@/components/ui/GlassCard';
import { ThemedText } from '@/components/themed-text';
import { Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import {
  formatDuration,
  formatLength,
  TRANSPORT_MODE_LABELS,
  type Route,
} from '@/types/route';

export interface RouteSheetProps {
  route: Route;
  onClear: () => void;
  /** Style override untuk container terluar. */
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

export function RouteSheet({ route, onClear, style }: RouteSheetProps) {
  const { colors } = useAppTheme();
  const modeLabel = TRANSPORT_MODE_LABELS[route.mode];
  const length = formatLength(route.lengthMeters);
  const duration = formatDuration(route.durationSeconds);

  // Nilai `accessibilityValue` membantu screen reader membaca ringkasan
  // sekaligus, bila pengguna fokus ke container.
  const a11yLabel = `Rute aktif untuk ${modeLabel}, jarak ${length}, perkiraan durasi ${duration}.`;

  return (
    <GlassCard
      tone="strong"
      style={[styles.container, style]}
      accessibilityRole="summary"
      accessibilityLabel={a11yLabel}
    >
      <View style={styles.row}>
        <ModeBadge label={modeLabel} />
        <View style={styles.metrics}>
          <Metric value={length} label="Jarak" color={colors.text} />
          <View style={[styles.divider, { backgroundColor: colors.border }]} />
          <Metric value={duration} label="Estimasi" color={colors.text} />
        </View>
      </View>

      <GlassButton
        label="Hapus rute"
        variant="ghost"
        onPress={onClear}
        accessibilityHint="Menghapus rute dan kembali ke peta polos"
      />
    </GlassCard>
  );
}

interface ModeBadgeProps {
  label: string;
}

function ModeBadge({ label }: ModeBadgeProps) {
  const { colors } = useAppTheme();
  return (
    <View
      style={[styles.badge, { backgroundColor: colors.brandMaroon }]}
      accessibilityElementsHidden
    >
      <ThemedText style={styles.badgeLabel}>{label}</ThemedText>
    </View>
  );
}

interface MetricProps {
  value: string;
  label: string;
  color: string;
}

function Metric({ value, label, color }: MetricProps) {
  const { colors } = useAppTheme();
  return (
    <View style={styles.metric}>
      <ThemedText style={[styles.value, { color }]}>{value}</ThemedText>
      <ThemedText style={[styles.label, { color: colors.textSecondary }]}>{label}</ThemedText>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: Spacing.md,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.lg,
  },
  badge: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.xs,
    borderRadius: 999,
  },
  badgeLabel: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '700',
  },
  metrics: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  metric: {
    flex: 1,
    alignItems: 'center',
  },
  divider: {
    width: StyleSheet.hairlineWidth,
    height: 32,
    marginHorizontal: Spacing.sm,
  },
  value: {
    fontSize: 18,
    fontWeight: '700',
  },
  label: {
    fontSize: 11,
    fontWeight: '600',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
});
