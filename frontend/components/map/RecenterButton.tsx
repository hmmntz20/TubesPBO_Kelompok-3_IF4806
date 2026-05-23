/**
 * `RecenterButton` — tombol bulat melayang yang memerintahkan peta kembali
 * ke region awal kampus (FR-MAP-05).
 *
 * Memakai `GlassSurface` agar konsisten dengan bahasa desain glassmorphism
 * tetapi ikon-nya tetap solid maroon untuk menonjol sebagai aksi.
 */
import { Pressable, StyleSheet } from 'react-native';

import { IconSymbol } from '@/components/ui/icon-symbol';
import { GlassSurface } from '@/components/ui/GlassSurface';
import { useAppTheme } from '@/hooks/use-app-theme';

const SIZE = 48;

export interface RecenterButtonProps {
  onPress: () => void;
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
  accessibilityLabel?: string;
}

export function RecenterButton({
  onPress,
  style,
  accessibilityLabel = 'Pusatkan ulang peta',
}: RecenterButtonProps) {
  const { colors } = useAppTheme();

  return (
    <Pressable
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={accessibilityLabel}
      hitSlop={8}
      style={({ pressed }) => [
        styles.pressable,
        { opacity: pressed ? 0.85 : 1 },
        style,
      ]}
    >
      <GlassSurface tone="regular" radius={SIZE / 2} style={styles.surface}>
        <IconSymbol name="location.fill" size={22} color={colors.brandMaroon} />
      </GlassSurface>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  pressable: {
    // Layout & shadow ditangani GlassSurface; pressable hanya menerima tap.
  },
  surface: {
    width: SIZE,
    height: SIZE,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
