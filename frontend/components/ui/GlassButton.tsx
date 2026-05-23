/**
 * `GlassButton` — tombol aksi utama.
 *
 * Per design language Glassmorphism (design.md §3.0): aksen Maroon Telkom
 * tetap **solid** (tidak glass) agar panggilan-aksi menonjol di atas
 * permukaan kaca. Komponen ini menyediakan dua varian:
 *
 *  - `'primary'` — background maroon, teks putih (default).
 *  - `'ghost'`   — transparent, border maroon, teks maroon (untuk aksi sekunder).
 *
 * Kontras teks vs background memenuhi WCAG AA (rasio ≥ 4.5:1) di kedua mode.
 */
import {
  ActivityIndicator,
  Pressable,
  type PressableProps,
  StyleSheet,
  View,
} from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';

export type GlassButtonVariant = 'primary' | 'ghost';

export interface GlassButtonProps extends Omit<PressableProps, 'style' | 'children'> {
  label: string;
  variant?: GlassButtonVariant;
  loading?: boolean;
  /** Content opsional di kiri label (mis. ikon). */
  leadingIcon?: React.ReactNode;
  /** Style override untuk container terluar (mis. margin antar tombol). */
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

export function GlassButton({
  label,
  variant = 'primary',
  loading = false,
  leadingIcon,
  disabled,
  style,
  ...rest
}: GlassButtonProps) {
  const { colors } = useAppTheme();

  const isPrimary = variant === 'primary';
  const backgroundColor = isPrimary ? colors.brandMaroon : 'transparent';
  const borderColor = isPrimary ? colors.brandMaroon : colors.brandMaroon;
  const textColor = isPrimary ? '#FFFFFF' : colors.brandMaroon;

  const isDisabled = disabled || loading;

  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: isDisabled, busy: loading }}
      accessibilityLabel={label}
      disabled={isDisabled}
      style={({ pressed }) => [
        styles.base,
        {
          backgroundColor,
          borderColor,
          borderWidth: isPrimary ? 0 : StyleSheet.hairlineWidth,
          opacity: isDisabled ? 0.5 : pressed ? 0.85 : 1,
        },
        style,
      ]}
      {...rest}
    >
      <View style={styles.row}>
        {loading ? (
          <ActivityIndicator color={textColor} />
        ) : (
          <>
            {leadingIcon ? <View style={styles.icon}>{leadingIcon}</View> : null}
            <ThemedText style={[styles.label, { color: textColor }]}>{label}</ThemedText>
          </>
        )}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  base: {
    minHeight: 44, // target sentuh aksesibilitas
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.sm + 2,
    borderRadius: Radius.lg,
    alignItems: 'center',
    justifyContent: 'center',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  icon: {
    marginRight: 0,
  },
  label: {
    fontSize: 16,
    fontWeight: '600',
    lineHeight: 20,
  },
});
