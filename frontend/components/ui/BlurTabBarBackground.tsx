/**
 * `BlurTabBarBackground` — background bertema kaca untuk bottom tab bar
 * (FR-UI-10).
 *
 * Berbeda dari `<GlassSurface />`: tidak punya radius (full-bleed), border
 * hanya di sisi atas (hairline), dan dipasang via prop `tabBarBackground`
 * pada `<Tabs>` Expo Router. Saat reduce-transparency aktif, beralih ke
 * permukaan opaque agar konten tidak tembus pandang.
 */
import { BlurView } from 'expo-blur';
import { StyleSheet, View } from 'react-native';

import { useAppTheme } from '@/hooks/use-app-theme';
import { useReduceTransparency } from '@/hooks/use-reduce-transparency';

export function BlurTabBarBackground() {
  const { glass, colors } = useAppTheme();
  const reduceTransparency = useReduceTransparency();

  if (reduceTransparency) {
    return (
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: colors.surface,
            borderTopWidth: StyleSheet.hairlineWidth,
            borderTopColor: colors.border,
          },
        ]}
      />
    );
  }

  return (
    <View style={[StyleSheet.absoluteFill, styles.container]}>
      <BlurView
        intensity={glass.intensityRegular}
        tint={glass.blurTint}
        style={StyleSheet.absoluteFill}
      />
      <View
        style={[
          StyleSheet.absoluteFill,
          {
            backgroundColor: glass.tintRegular,
            borderTopWidth: StyleSheet.hairlineWidth,
            borderTopColor: glass.border,
          },
        ]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
  },
});
