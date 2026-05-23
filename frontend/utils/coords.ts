/**
 * Utilitas konversi koordinat antara dunia GeoJSON (`[lng, lat]`) dan
 * dunia React Native Maps (`{latitude, longitude}`).
 *
 * Pemisahan ini menjaga logika konversi terlokalisasi di satu titik,
 * sehingga tidak ada pembalikan silent yang tersebar di komponen lain
 * (sumber bug yang umum di aplikasi peta).
 */
import type { LatLng } from 'react-native-maps';

import type { Position } from '@/types/geojson';

/**
 * Mengonversi {@link Position} GeoJSON (`[lng, lat, ...]`) menjadi `LatLng`.
 *
 * @param position pasangan koordinat dari GeoJSON.
 * @returns objek `{latitude, longitude}` siap pakai di `react-native-maps`.
 * @throws {RangeError} jika position kurang dari 2 elemen.
 */
export function lngLatToLatLng(position: Position): LatLng {
  if (position.length < 2) {
    throw new RangeError(
      `Position GeoJSON minimal [lng, lat]; diterima: ${JSON.stringify(position)}`,
    );
  }
  const [longitude, latitude] = position;
  return { latitude, longitude };
}

/**
 * Mengonversi rangkaian `Position` GeoJSON (mis. dari LineString) menjadi
 * array `LatLng`. Membuat array baru — input tidak dimutasi.
 *
 * @param positions koleksi Position; harus iterable.
 */
export function lngLatPositionsToLatLngArray(
  positions: ReadonlyArray<Position>,
): LatLng[] {
  return positions.map(lngLatToLatLng);
}
