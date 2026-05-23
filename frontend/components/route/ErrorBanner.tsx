/**
 * `ErrorBanner` — banner error in-place (FR-UI-RT-07).
 *
 * <p>Tidak memakai global toast (default tasks.md: "in-place banner saja"
 * tanpa menambah dependency). Banner muncul/ menghilang sesuai prop
 * {@code message}; bila {@code message} kosong/null, komponen tidak
 * me-render apapun.</p>
 *
 * <h3>Aksesibilitas</h3>
 * <p>{@code accessibilityRole="alert"} agar screen reader otomatis
 * mengumumkan saat banner muncul.</p>
 */
import { Pressable, StyleSheet, View } from 'react-native';

import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';

export interface ErrorBannerProps {
  message: string | null | undefined;
  /** Bila ada, render tombol close kecil di sisi kanan. */
  onDismiss?: () => void;
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
}

export function ErrorBanner({ message, onDismiss, style }: ErrorBannerProps) {
  const { colors } = useAppTheme();
  if (!message) return null;

  return (
    <View
      accessibilityRole="alert"
      style={[
        styles.container,
        {
          backgroundColor: colors.danger + '22', // 13% opacity tint
          borderColor: colors.danger,
        },
        style,
      ]}
    >
      <ThemedText style={[styles.text, { color: colors.danger }]}>{message}</ThemedText>
      {onDismiss ? (
        <Pressable
          onPress={onDismiss}
          accessibilityRole="button"
          accessibilityLabel="Tutup pesan"
          hitSlop={8}
        >
          <ThemedText style={[styles.dismiss, { color: colors.danger }]}>×</ThemedText>
        </Pressable>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.md,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm + 2,
    borderRadius: Radius.md,
    borderWidth: StyleSheet.hairlineWidth,
  },
  text: {
    flex: 1,
    fontSize: 14,
    lineHeight: 20,
  },
  dismiss: {
    fontSize: 22,
    fontWeight: '700',
    paddingHorizontal: Spacing.xs,
  },
});
