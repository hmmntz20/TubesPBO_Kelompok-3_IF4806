/**
 * Konstanta geografis kampus Telkom University.
 *
 * Estimasi awal (FR-MAP-03); dapat di-fine-tune setelah pengujian visual.
 * Format `(latitude, longitude)` selaras dengan domain backend & API
 * `react-native-maps` (BUKAN format GeoJSON `[lng, lat]`).
 */
import type { LatLng, Region } from 'react-native-maps';

/**
 * Bounding box kampus untuk `MapView.setMapBoundaries(NE, SW)`.
 * - `northEast` = pojok kanan-atas (max lat, max lng).
 * - `southWest` = pojok kiri-bawah (min lat, min lng).
 */
export const CAMPUS_BBOX = {
  northEast: { latitude: -6.965, longitude: 107.639 },
  southWest: { latitude: -6.979, longitude: 107.625 },
} as const satisfies { northEast: LatLng; southWest: LatLng };

/** Pusat kampus (rata-rata BBox). */
export const CAMPUS_CENTER: LatLng = {
  latitude: (CAMPUS_BBOX.northEast.latitude + CAMPUS_BBOX.southWest.latitude) / 2,
  longitude: (CAMPUS_BBOX.northEast.longitude + CAMPUS_BBOX.southWest.longitude) / 2,
};

/** Region awal MapView — cukup memuat seluruh BBox. */
export const CAMPUS_INITIAL_REGION: Region = {
  ...CAMPUS_CENTER,
  latitudeDelta:
    CAMPUS_BBOX.northEast.latitude - CAMPUS_BBOX.southWest.latitude,
  longitudeDelta:
    CAMPUS_BBOX.northEast.longitude - CAMPUS_BBOX.southWest.longitude,
};

/** Batas zoom — di luar rentang ini peta menolak gesture (FR-MAP-04). */
export const CAMPUS_MIN_ZOOM = 15;
export const CAMPUS_MAX_ZOOM = 19;
