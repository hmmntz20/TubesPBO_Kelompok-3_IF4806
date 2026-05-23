/**
 * Mendeteksi flag *reduce-transparency* dari sistem operasi.
 *
 * <p>iOS: `Settings > Accessibility > Display & Text Size > Reduce Transparency`.
 * Android & Web: tidak punya API native setara → selalu mengembalikan
 * `false` (transparency aktif). Komponen `<GlassSurface />` akan beralih
 * ke permukaan opaque saat hook ini bernilai `true` (FR-UI-12).</p>
 */
import { useEffect, useState } from 'react';
import { AccessibilityInfo, Platform } from 'react-native';

export function useReduceTransparency(): boolean {
  const [enabled, setEnabled] = useState(false);

  useEffect(() => {
    if (Platform.OS !== 'ios') {
      // Android & Web tidak menyediakan API ini.
      return;
    }

    let mounted = true;

    AccessibilityInfo.isReduceTransparencyEnabled?.()
      .then((value) => {
        if (mounted) setEnabled(Boolean(value));
      })
      .catch(() => {
        // Ignore — diam-diam fallback ke false.
      });

    const sub = AccessibilityInfo.addEventListener(
      'reduceTransparencyChanged',
      (value) => setEnabled(Boolean(value)),
    );

    return () => {
      mounted = false;
      sub.remove();
    };
  }, []);

  return enabled;
}
