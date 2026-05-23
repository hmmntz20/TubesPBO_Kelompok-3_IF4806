/**
 * `RouteOverlay` — overlay peta untuk rute aktif.
 *
 * <p>Komponen ini me-render dua jenis annotation pada {@code MapView}:</p>
 *
 * <ul>
 *   <li><strong>Polyline</strong> berwarna {@code colors.brandMaroon} dengan
 *       {@code strokeWidth: 6} dan {@code zIndex: 10} agar berada di atas
 *       polyline jalan dasar (FR-UI-RT-04).</li>
 *   <li>Dua <strong>Marker</strong> di titik asal & tujuan dengan label
 *       Bahasa Indonesia (FR-UI-RT-04, NFR-RT-A11Y-02).</li>
 * </ul>
 *
 * <h3>Pemakaian</h3>
 *
 * <p>Komponen <strong>WAJIB</strong> di-mount sebagai child {@code MapView};
 * {@code Polyline} & {@code Marker} react-native-maps butuh konteks parent
 * map. Lihat {@code CampusMap.tsx}.</p>
 */
import { Marker, Polyline } from 'react-native-maps';

import { useAppTheme } from '@/hooks/use-app-theme';
import type { Route } from '@/types/route';

export interface RouteOverlayProps {
  route: Route;
}

/** Z-index polyline rute aktif — di atas polyline jalan dasar. */
const ACTIVE_ROUTE_Z_INDEX = 10;

export function RouteOverlay({ route }: RouteOverlayProps) {
  const { colors } = useAppTheme();

  // Konversi [lat, lng][] → LatLng[] yang dibutuhkan Polyline.
  const polylineCoords = route.coordinates.map(([latitude, longitude]) => ({
    latitude,
    longitude,
  }));

  if (polylineCoords.length < 2) {
    // Rute degenerate (1 node) — hanya marker, tanpa polyline.
    return (
      <Marker
        coordinate={route.from}
        title="Asal & Tujuan"
        description="Asal sama dengan tujuan"
      />
    );
  }

  return (
    <>
      <Polyline
        coordinates={polylineCoords}
        strokeColor={colors.brandMaroon}
        strokeWidth={6}
        zIndex={ACTIVE_ROUTE_Z_INDEX}
        tappable={false}
      />
      <Marker
        coordinate={route.from}
        title="Asal"
        pinColor={colors.brandMaroon}
      />
      <Marker
        coordinate={route.to}
        title="Tujuan"
        pinColor={colors.brandGold}
      />
    </>
  );
}
