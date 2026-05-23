/**
 * `ModeSelector` — segmented control 3-state untuk memilih moda transportasi
 * (FR-UI-RT-02).
 *
 * <h3>Visual</h3>
 * <p>Frame {@link GlassSurface} subtle agar menyatu dengan UI glass; segment
 * aktif diberi background {@code colors.brandMaroon} solid (kontras AA);
 * teks di atasnya putih. Segment non-aktif tetap transparan dengan teks
 * {@code colors.text}.</p>
 *
 * <h3>Aksesibilitas</h3>
 * <p>Setiap segment bertindak sebagai radio button:
 * {@code accessibilityRole="radio"} + {@code accessibilityState.selected}
 * (NFR-RT-A11Y-02). Label Bahasa Indonesia diambil dari
 * {@link TRANSPORT_MODE_LABELS}.</p>
 */
import { Pressable, StyleSheet, View } from 'react-native';

import { GlassSurface } from '@/components/ui/GlassSurface';
import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import {
  TRANSPORT_MODE_LABELS,
  TRANSPORT_MODES,
  type TransportMode,
} from '@/types/route';

export interface ModeSelectorProps {
  value: TransportMode;
  onChange: (mode: TransportMode) => void;
  /** Style override untuk container terluar. */
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

export function ModeSelector({ value, onChange, style }: ModeSelectorProps) {
  return (
    <GlassSurface tone="subtle" radius={Radius.lg} style={[styles.frame, style]}>
      <View
        style={styles.row}
        accessibilityRole="radiogroup"
        accessibilityLabel="Pilih moda transportasi"
      >
        {TRANSPORT_MODES.map((mode) => (
          <Segment
            key={mode}
            mode={mode}
            active={mode === value}
            onPress={() => onChange(mode)}
          />
        ))}
      </View>
    </GlassSurface>
  );
}

interface SegmentProps {
  mode: TransportMode;
  active: boolean;
  onPress: () => void;
}

function Segment({ mode, active, onPress }: SegmentProps) {
  const { colors } = useAppTheme();
  const label = TRANSPORT_MODE_LABELS[mode];

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="radio"
      accessibilityState={{ selected: active }}
      accessibilityLabel={label}
      style={({ pressed }) => [
        styles.segment,
        active && { backgroundColor: colors.brandMaroon },
        !active && pressed && { backgroundColor: colors.border },
      ]}
    >
      <ThemedText
        style={[
          styles.label,
          { color: active ? '#FFFFFF' : colors.text },
        ]}
      >
        {label}
      </ThemedText>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  frame: {
    padding: Spacing.xs,
  },
  row: {
    flexDirection: 'row',
    gap: Spacing.xs,
  },
  segment: {
    flex: 1,
    minHeight: 40, // target sentuh aksesibilitas
    borderRadius: Radius.md,
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.sm,
    alignItems: 'center',
    justifyContent: 'center',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    textAlign: 'center',
  },
});
