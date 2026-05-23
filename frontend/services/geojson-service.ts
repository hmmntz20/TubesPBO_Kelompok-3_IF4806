/**
 * Layanan pengambilan GeoJSON peta kampus — 3-tier loader (FR-GEO-02).
 *
 * Urutan prioritas:
 *  1. **Network** — `GET /api/v1/graph/geojson`. Jika cache memuat ETag
 *     dari fetch sebelumnya, ETag itu dikirim sebagai `If-None-Match`
 *     untuk revalidasi murah; respons 304 berarti cache masih valid.
 *  2. **AsyncStorage cache** — payload terakhir yang berhasil diambil.
 *  3. **Bundled fallback** — `assets/data/telmap.geojson` di-load via
 *     `expo-asset` + `fetch(localUri)`. Dipakai saat first-run tanpa
 *     jaringan, atau jika cache rusak.
 *
 * Validasi struktur dilakukan di setiap tier sebelum payload dipakai.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Asset } from 'expo-asset';

import { apiClient, ApiError } from '@/services/api-client';
import {
  isTelmapFeatureCollection,
  type TelmapFeatureCollection,
} from '@/types/geojson';

/**
 * Kunci cache di AsyncStorage. Suffix `:v1` di-bump saat ada
 * <em>schema-breaking change</em> (lihat design.md §10).
 */
const CACHE_KEY = 'cache:graph-geojson:v1';

/** Path endpoint backend. */
const ENDPOINT = '/api/v1/graph/geojson';

/** Modul ID asset bundled fallback. Di-resolve oleh Metro saat bundle. */
const BUNDLED_GEOJSON_MODULE = require('@/assets/data/telmap.geojson');

/** Bentuk payload yang disimpan di AsyncStorage. */
interface CachedPayload {
  /** ETag dari respons backend terakhir, lengkap dengan tanda kutip. */
  etag?: string;
  /** ISO-8601. */
  fetchedAt: string;
  /** Payload GeoJSON (sudah ter-validasi struktur). */
  data: TelmapFeatureCollection;
}

/** Sumber data tier dari mana hasil terakhir berasal — berguna untuk telemetry. */
export type GeoJsonSource = 'network' | 'cache' | 'bundle';

export interface FetchResult {
  data: TelmapFeatureCollection;
  source: GeoJsonSource;
  /** True jika respons dari network adalah 304 (memakai cache). */
  revalidated: boolean;
}

/**
 * Mengambil GeoJSON kampus dengan strategi 3-tier.
 *
 * @returns hasil dengan {@link TelmapFeatureCollection} ter-validasi.
 * @throws  Error jika ketiga tier gagal atau payload invalid di seluruh tier.
 */
export async function fetchCampusGeoJSON(): Promise<FetchResult> {
  const cached = await safeReadCache();

  // Tier 1: Network (dengan revalidasi ETag jika punya).
  try {
    const headers: Record<string, string> = {};
    if (cached?.etag) {
      headers['If-None-Match'] = cached.etag;
    }
    const res = await apiClient.get(ENDPOINT, { headers });

    if (res.status === 304 && cached) {
      return { data: cached.data, source: 'cache', revalidated: true };
    }

    if (res.ok) {
      const json = (await res.json()) as unknown;
      if (!isTelmapFeatureCollection(json)) {
        throw new GeoJsonValidationError('Respons backend bukan FeatureCollection valid');
      }
      const etag = res.headers.get('etag') ?? undefined;
      await safeWriteCache({
        etag: etag ?? undefined,
        fetchedAt: new Date().toISOString(),
        data: json,
      });
      return { data: json, source: 'network', revalidated: false };
    }

    // Status non-OK & non-304 → fallback chain.
    if (cached) {
      return { data: cached.data, source: 'cache', revalidated: false };
    }
    return await loadBundleOrThrow(`HTTP ${res.status}`);
  } catch (err) {
    // Tier 2/3: fallback saat network bermasalah.
    if (err instanceof GeoJsonValidationError) {
      // Validasi gagal di network → tidak boleh men-cache; tetap coba fallback.
      if (cached) return { data: cached.data, source: 'cache', revalidated: false };
      return await loadBundleOrThrow(err.message);
    }
    if (err instanceof ApiError) {
      if (cached) return { data: cached.data, source: 'cache', revalidated: false };
      return await loadBundleOrThrow(err.message);
    }
    // Re-throw kesalahan tak dikenal — bukan urusan loader untuk menelan.
    throw err;
  }
}

/**
 * Hapus cache GeoJSON (untuk testing manual atau saat user ingin force refresh).
 */
export async function clearCache(): Promise<void> {
  await AsyncStorage.removeItem(CACHE_KEY);
}

// ── helpers ─────────────────────────────────────────────────────────────

async function safeReadCache(): Promise<CachedPayload | null> {
  try {
    const raw = await AsyncStorage.getItem(CACHE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as CachedPayload;
    if (!parsed?.data || !isTelmapFeatureCollection(parsed.data)) {
      // Cache rusak/lama — kosongkan agar tidak dipakai lagi.
      await AsyncStorage.removeItem(CACHE_KEY);
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
}

async function safeWriteCache(payload: CachedPayload): Promise<void> {
  try {
    await AsyncStorage.setItem(CACHE_KEY, JSON.stringify(payload));
  } catch {
    // Storage penuh / error — abaikan; loader tetap berfungsi.
  }
}

async function loadBundleOrThrow(reason: string): Promise<FetchResult> {
  try {
    const data = await loadBundledGeoJSON();
    return { data, source: 'bundle', revalidated: false };
  } catch (e) {
    throw new GeoJsonValidationError(
      `Gagal memuat GeoJSON dari semua tier: network gagal (${reason}), bundle gagal (${(e as Error).message})`,
    );
  }
}

async function loadBundledGeoJSON(): Promise<TelmapFeatureCollection> {
  const asset = Asset.fromModule(BUNDLED_GEOJSON_MODULE);
  await asset.downloadAsync();
  const uri = asset.localUri ?? asset.uri;
  const response = await fetch(uri);
  const json = (await response.json()) as unknown;
  if (!isTelmapFeatureCollection(json)) {
    throw new GeoJsonValidationError('Bundle GeoJSON tidak valid');
  }
  return json;
}

export class GeoJsonValidationError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'GeoJsonValidationError';
  }
}

// Helper test-only — diekspos untuk unit test memakai struktur cache yang sama.
export const __testing = {
  CACHE_KEY,
  safeReadCache,
  safeWriteCache,
};
