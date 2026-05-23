/**
 * Tab "Peta" — layar utama aplikasi.
 *
 * <p>Layar ini full-screen tanpa padding tab bar (tab bar transparan akan
 * meng-overlay di atas peta — lihat {@code BlurTabBarBackground}). Overlay
 * informasi ({@code MapLegend}) dan kontrol ({@code RecenterButton})
 * diletakkan absolute pada peta.</p>
 *
 * <h3>Integrasi routing (Fitur 3)</h3>
 * <p>Layar ini juga berperan sebagai konsumer state {@code route-store}:</p>
 * <ul>
 *   <li>Bila {@code currentRoute} non-null, polyline rute aktif dirender
 *       via prop {@code route} pada {@link CampusMap} dan
 *       {@link RouteSheet} muncul sebagai bottom sheet.</li>
 *   <li>Bila {@code picking} non-null, tap di peta diteruskan ke handler
 *       yang men-set koordinat di store dan kembali ke tab Cari (FR-UI-RT-02).</li>
 * </ul>
 */
import { router } from 'expo-router';
import { useRef } from 'react';
import { StyleSheet, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import type { LatLng } from 'react-native-maps';

import { CampusMap, type CampusMapHandle } from '@/components/map/CampusMap';
import { MapLegend } from '@/components/map/MapLegend';
import { RecenterButton } from '@/components/map/RecenterButton';
import { RouteSheet } from '@/components/route/RouteSheet';
import { ThemedText } from '@/components/themed-text';
import { GlassCard } from '@/components/ui/GlassCard';
import { Spacing } from '@/constants/theme';
import { useRouteStore } from '@/stores/route-store';

export default function MapHomeScreen() {
  const insets = useSafeAreaInsets();
  const mapHandleRef = useRef<CampusMapHandle>(null);

  // Routing state — selector spesifik per field.
  const currentRoute = useRouteStore((s) => s.currentRoute);
  const picking = useRouteStore((s) => s.picking);
  const setPendingFrom = useRouteStore((s) => s.setPendingFrom);
  const setPendingTo = useRouteStore((s) => s.setPendingTo);
  const setPicking = useRouteStore((s) => s.setPicking);
  const clear = useRouteStore((s) => s.clear);

  const handlePickPoint = (point: LatLng) => {
    if (picking === 'from') {
      setPendingFrom(point);
    } else if (picking === 'to') {
      setPendingTo(point);
    }
    setPicking(null);
    router.navigate('/(tabs)/search');
  };

  // Bottom margin dasar agar legend & sheet tidak tertutup tab bar.
  const bottomBase = insets.bottom + 96;

  return (
    <View style={styles.container}>
      <CampusMap
        ref={mapHandleRef}
        route={currentRoute}
        onPickPoint={picking ? handlePickPoint : undefined}
      />

      <RecenterButton
        onPress={() => mapHandleRef.current?.recenter()}
        style={[
          styles.recenter,
          { top: insets.top + Spacing.sm, right: Spacing.lg },
        ]}
      />

      {/* Banner instruksi saat user sedang memilih titik. */}
      {picking ? (
        <GlassCard
          tone="strong"
          style={[styles.pickingBanner, { top: insets.top + Spacing.xl + 36 }]}
          accessibilityRole="alert"
          accessibilityLabel={
            picking === 'from' ? 'Tap di peta untuk pilih asal' : 'Tap di peta untuk pilih tujuan'
          }
        >
          <ThemedText style={styles.pickingText}>
            Tap di peta untuk pilih{' '}
            <ThemedText style={styles.pickingTextBold}>
              {picking === 'from' ? 'asal' : 'tujuan'}
            </ThemedText>
          </ThemedText>
        </GlassCard>
      ) : null}

      {/* Bottom sheet ringkasan rute (FR-UI-RT-05). */}
      {currentRoute ? (
        <RouteSheet
          route={currentRoute}
          onClear={clear}
          style={[
            styles.routeSheet,
            { bottom: bottomBase, left: Spacing.lg, right: Spacing.lg },
          ]}
        />
      ) : (
        <MapLegend
          style={[
            styles.legend,
            { bottom: bottomBase, right: Spacing.lg },
          ]}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  recenter: {
    position: 'absolute',
  },
  legend: {
    position: 'absolute',
    width: 180,
  },
  routeSheet: {
    position: 'absolute',
  },
  pickingBanner: {
    position: 'absolute',
    left: Spacing.lg,
    right: Spacing.lg,
  },
  pickingText: {
    fontSize: 14,
    textAlign: 'center',
  },
  pickingTextBold: {
    fontWeight: '700',
  },
});
