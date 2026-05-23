/**
 * Tipe TypeScript untuk GeoJSON RFC 7946 + ekstensi properti khas
 * dump Overpass / OpenStreetMap kampus Telkom.
 *
 * Catatan: koordinat di GeoJSON adalah `[longitude, latitude]`. Untuk
 * konversi ke format `react-native-maps` (`{latitude, longitude}`)
 * gunakan `utils/coords.ts`.
 */

/** Pasangan posisi `[longitude, latitude]` dengan opsional ketinggian. */
export type Position = readonly [longitude: number, latitude: number, ...rest: number[]];

export interface LineStringGeometry {
  readonly type: 'LineString';
  readonly coordinates: ReadonlyArray<Position>;
}

export interface PointGeometry {
  readonly type: 'Point';
  readonly coordinates: Position;
}

export interface PolygonGeometry {
  readonly type: 'Polygon';
  readonly coordinates: ReadonlyArray<ReadonlyArray<Position>>;
}

export type Geometry = LineStringGeometry | PointGeometry | PolygonGeometry;

/**
 * Properti fitur — terbuka karena dump Overpass mungkin memuat tag arbitrer.
 * Field yang sering dipakai dideklarasikan eksplisit untuk type-safe access.
 */
export interface TelmapFeatureProperties {
  '@id'?: string;
  highway?: string;
  oneway?: string;
  surface?: string;
  width?: string;
  name?: string;
  building?: string;
  /** Properti kustom Telkom yang akan datang (bisa_motor, id_node, dst.). */
  [k: string]: unknown;
}

export interface TelmapFeature {
  readonly type: 'Feature';
  readonly properties: TelmapFeatureProperties;
  readonly geometry: Geometry;
}

export interface TelmapFeatureCollection {
  readonly type: 'FeatureCollection';
  readonly features: ReadonlyArray<TelmapFeature>;
}

/**
 * Type guard sederhana untuk memvalidasi runtime JSON tak terpercaya.
 *
 * @param data nilai yang akan dicek.
 * @returns `true` jika `data` cocok dengan struktur dasar
 *          `FeatureCollection` (type & features array).
 */
export function isTelmapFeatureCollection(
  data: unknown,
): data is TelmapFeatureCollection {
  if (data == null || typeof data !== 'object') return false;
  const fc = data as Record<string, unknown>;
  return fc.type === 'FeatureCollection' && Array.isArray(fc.features);
}

/**
 * Set tag `highway` yang dianggap pedestrian-only (jalan tikus). Konsisten
 * dengan logika backend `OsmGeoJsonParser.PEDESTRIAN_ONLY_HIGHWAYS`.
 */
export const PEDESTRIAN_ONLY_HIGHWAYS = new Set<string>([
  'footway',
  'path',
  'pedestrian',
  'steps',
  'cycleway',
  'track',
]);

/** True jika fitur memiliki tag highway pedestrian-only. */
export function isPedestrianOnly(feature: TelmapFeature): boolean {
  const h = String(feature.properties?.highway ?? '');
  return PEDESTRIAN_ONLY_HIGHWAYS.has(h);
}
