/**
 * `CampusMap` — komponen peta utama (FR-MAP-01..05, FR-GEO-04..05, FR-UI-06,
 * FR-UI-RT-04, FR-UI-RT-06).
 *
 * Tanggung jawab:
 *  - Render `MapView` Google Maps di Android & iOS (provider konsisten).
 *  - Mengunci pan ke BBox kampus via `setMapBoundaries(NE, SW)`.
 *  - Memuat GeoJSON via 3-tier loader (`services/geojson-service`).
 *  - Render polyline jalan dengan styling beda untuk kendaraan vs jalan tikus.
 *  - Mengaplikasikan `customMapStyle` sesuai mode tema (light/dark).
 *  - Mengekspos `recenter()` & `fitToRoute()` via {@link CampusMapHandle}.
 *  - **Routing**: bila `route` prop diisi, render {@link RouteOverlay} di atas
 *    polyline jalan dasar dan auto-fit kamera (FR-UI-RT-06).
 *  - **Pin di peta**: bila `onPickPoint` diisi, tap pada peta meneruskan
 *    {@code LatLng} hasil tap ke caller (FR-UI-RT-02).
 */
import { forwardRef, useEffect, useImperativeHandle, useMemo, useRef, useState } from 'react';
import { ActivityIndicator, StyleSheet, View } from 'react-native';
import MapView, {
  PROVIDER_GOOGLE,
  Polyline,
  type LatLng,
  type MapPressEvent,
} from 'react-native-maps';

import { RouteOverlay } from '@/components/route/RouteOverlay';
import { ThemedText } from '@/components/themed-text';
import {
  CAMPUS_BBOX,
  CAMPUS_INITIAL_REGION,
  CAMPUS_MAX_ZOOM,
  CAMPUS_MIN_ZOOM,
} from '@/constants/mapBoundaries';
import { Spacing } from '@/constants/theme';
import { useAppTheme } from '@/hooks/use-app-theme';
import { fetchCampusGeoJSON } from '@/services/geojson-service';
import type { Route } from '@/types/route';
import {
  isPedestrianOnly,
  type TelmapFeature,
  type TelmapFeatureCollection,
} from '@/types/geojson';
import { lngLatPositionsToLatLngArray } from '@/utils/coords';

import mapStyleDark from './styles/mapStyleDark';
import mapStyleLight from './styles/mapStyleLight';

type LoadState =
  | { status: 'loading' }
  | { status: 'ready'; data: TelmapFeatureCollection }
  | { status: 'error'; message: string };

export interface CampusMapHandle {
  /** Animasi kembali ke region awal (CAMPUS_INITIAL_REGION). */
  recenter: () => void;
  /** Fit kamera agar semua titik di {@code coords} terlihat. */
  fitToCoordinates: (coords: readonly LatLng[]) => void;
}

export interface CampusMapProps {
  /** Style override untuk container map. */
  style?: import('react-native').StyleProp<import('react-native').ViewStyle>;
  /**
   * Rute aktif — bila non-null, polyline maroon dirender via
   * {@link RouteOverlay} dan kamera otomatis mem-fit ke seluruh jalur
   * (FR-UI-RT-06).
   */
  route?: Route | null;
  /**
   * Callback bila pengguna men-tap peta. Disediakan untuk mendukung flow
   * "pin di peta" (FR-UI-RT-02). Bila tidak disetel, tap di peta tidak
   * dianggap sebagai aksi pemilihan.
   */
  onPickPoint?: (point: LatLng) => void;
}

export const CampusMap = forwardRef<CampusMapHandle, CampusMapProps>(
  function CampusMap({ style, route, onPickPoint }, ref) {
    const mapRef = useRef<MapView>(null);
    const { mode, colors } = useAppTheme();
    const [load, setLoad] = useState<LoadState>({ status: 'loading' });

    // Eksposur method imperatif tanpa membocorkan MapView ref.
    useImperativeHandle(ref, () => ({
      recenter: () => {
        mapRef.current?.animateToRegion(CAMPUS_INITIAL_REGION, 500);
      },
      fitToCoordinates: (coords) => {
        if (coords.length === 0) return;
        mapRef.current?.fitToCoordinates(coords as LatLng[], {
          edgePadding: { top: 80, right: 60, bottom: 200, left: 60 },
          animated: true,
        });
      },
    }), []);

    // Muat GeoJSON sekali saat mount.
    useEffect(() => {
      let cancelled = false;
      fetchCampusGeoJSON()
        .then(({ data }) => {
          if (!cancelled) setLoad({ status: 'ready', data });
        })
        .catch((e: Error) => {
          if (!cancelled) setLoad({ status: 'error', message: e.message });
        });
      return () => {
        cancelled = true;
      };
    }, []);

    // Auto-fit kamera saat route berubah (FR-UI-RT-06).
    useEffect(() => {
      if (!route || route.coordinates.length === 0) return;
      const coords = route.coordinates.map(([latitude, longitude]) => ({
        latitude,
        longitude,
      }));
      // Sedikit delay agar polyline sudah ter-mount sebelum fit.
      const timer = setTimeout(() => {
        mapRef.current?.fitToCoordinates(coords, {
          edgePadding: { top: 80, right: 60, bottom: 200, left: 60 },
          animated: true,
        });
      }, 250);
      return () => clearTimeout(timer);
    }, [route]);

    const features: readonly TelmapFeature[] = useMemo(
      () => (load.status === 'ready' ? load.data.features : []),
      [load],
    );

    const polylines = useMemo(() => buildPolylineData(features), [features]);

    const customMapStyle = mode === 'dark' ? mapStyleDark : mapStyleLight;

    return (
      <View style={[styles.container, style]}>
        <MapView
          ref={mapRef}
          provider={PROVIDER_GOOGLE}
          style={StyleSheet.absoluteFill}
          initialRegion={CAMPUS_INITIAL_REGION}
          minZoomLevel={CAMPUS_MIN_ZOOM}
          maxZoomLevel={CAMPUS_MAX_ZOOM}
          customMapStyle={customMapStyle}
          showsCompass
          showsUserLocation={false}
          toolbarEnabled={false}
          onMapReady={() => {
            mapRef.current?.setMapBoundaries?.(
              CAMPUS_BBOX.northEast,
              CAMPUS_BBOX.southWest,
            );
          }}
          onPress={(e: MapPressEvent) => {
            if (onPickPoint) {
              const { coordinate } = e.nativeEvent;
              onPickPoint({
                latitude: coordinate.latitude,
                longitude: coordinate.longitude,
              });
            }
          }}
        >
          {polylines.map((p) => (
            <Polyline
              key={p.key}
              coordinates={p.coordinates}
              strokeColor={p.pedestrian ? colors.roadPedestrian : colors.roadVehicle}
              strokeWidth={p.pedestrian ? 2 : 4}
              lineDashPattern={p.pedestrian ? [6, 4] : undefined}
              tappable={false}
            />
          ))}
          {route ? <RouteOverlay route={route} /> : null}
        </MapView>

        {load.status === 'loading' ? (
          <View
            style={[styles.statusOverlay, { backgroundColor: colors.surface + 'CC' }]}
            accessibilityRole="progressbar"
            accessibilityLabel="Memuat peta"
          >
            <ActivityIndicator color={colors.brandMaroon} />
            <ThemedText style={styles.statusText}>Memuat peta…</ThemedText>
          </View>
        ) : null}

        {load.status === 'error' ? (
          <View
            style={[styles.statusOverlay, { backgroundColor: colors.surface + 'CC' }]}
            accessibilityRole="alert"
          >
            <ThemedText style={[styles.statusText, { color: colors.danger }]}>
              Tidak dapat memuat peta
            </ThemedText>
            <ThemedText style={[styles.statusSubText, { color: colors.textSecondary }]}>
              {load.message}
            </ThemedText>
          </View>
        ) : null}
      </View>
    );
  },
);

interface PolylineDatum {
  key: string;
  coordinates: LatLng[];
  pedestrian: boolean;
}

function buildPolylineData(features: readonly TelmapFeature[]): PolylineDatum[] {
  const out: PolylineDatum[] = [];
  features.forEach((f, i) => {
    if (f.geometry.type !== 'LineString') return;
    const coords = lngLatPositionsToLatLngArray(f.geometry.coordinates);
    out.push({
      key: String(f.properties?.['@id'] ?? `feat-${i}`),
      coordinates: coords,
      pedestrian: isPedestrianOnly(f),
    });
  });
  return out;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  statusOverlay: {
    position: 'absolute',
    bottom: Spacing.xl,
    alignSelf: 'center',
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.md,
    borderRadius: 16,
    alignItems: 'center',
    gap: Spacing.xs,
    minWidth: 200,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
  },
  statusSubText: {
    fontSize: 12,
    textAlign: 'center',
  },
});
