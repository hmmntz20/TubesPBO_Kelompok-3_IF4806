/**
 * Tipe TypeScript untuk fitur Pencarian Rute (Fitur 3).
 *
 * Cerminan kontrak DTO backend di `backend/.../routing/api/dto/`. Bila
 * backend mengubah bentuk response, file ini WAJIB di-update agar `tsc`
 * menangkap drift di sisi klien.
 */
import type { LatLng } from 'react-native-maps';

/**
 * Moda transportasi yang didukung backend (FR-RT-08). Literal union
 * type — equivalent dengan `enum TransportMode` di Java; nilainya harus
 * persis sama (case-sensitive) karena Jackson men-deserialize via name().
 */
export type TransportMode = 'WALKING' | 'MOTORCYCLE' | 'CAR';

/** Set semua nilai valid TransportMode untuk runtime validation. */
export const TRANSPORT_MODES: readonly TransportMode[] = [
  'WALKING',
  'MOTORCYCLE',
  'CAR',
] as const;

/** Type guard runtime untuk respons backend. */
export function isTransportMode(value: unknown): value is TransportMode {
  return typeof value === 'string' && (TRANSPORT_MODES as readonly string[]).includes(value);
}

/** Body request `POST /api/v1/route`. */
export interface RouteRequest {
  readonly from: LatLng;
  readonly to: LatLng;
  readonly mode: TransportMode;
}

/**
 * Bentuk response sukses dari backend. Mirror persis
 * {@code pbo.backend.routing.api.dto.RouteResponse}.
 */
export interface Route {
  /** Koordinat asal yang diminta klien (bukan node hasil snap). */
  readonly from: LatLng;
  /** Koordinat tujuan yang diminta klien. */
  readonly to: LatLng;
  /** Moda transportasi yang dipakai. */
  readonly mode: TransportMode;
  /** Total panjang rute dalam meter. */
  readonly lengthMeters: number;
  /** Estimasi durasi tempuh dalam detik. */
  readonly durationSeconds: number;
  /**
   * Polyline — array pasangan {@code [latitude, longitude]} urut dari asal
   * ke tujuan, siap dirender oleh `react-native-maps`.
   */
  readonly coordinates: ReadonlyArray<readonly [number, number]>;
  /** Diagnostik: id node yang dilewati. */
  readonly nodeIds: ReadonlyArray<number>;
}

/** Bentuk error body standar dari backend. */
export interface RouteErrorBody {
  readonly error: string;
}

// ── Error hierarchy ─────────────────────────────────────────────────────
//
// Mirror gaya Java exception agar UX layer bisa `instanceof`-check tanpa
// inspeksi string. `RoutingError` adalah base class; subclass spesifik
// menambahkan informasi yang relevan.

/** Base class semua error yang dilempar oleh `routing-service`. */
export class RoutingError extends Error {
  constructor(message: string, public readonly cause?: unknown) {
    super(message);
    this.name = 'RoutingError';
  }
}

/** 400 — request tidak valid (mis. lat/lng di luar range, mode tidak dikenal). */
export class RouteValidationError extends RoutingError {
  constructor(message: string, cause?: unknown) {
    super(message, cause);
    this.name = 'RouteValidationError';
  }
}

/** 404 — backend menjawab tidak ada rute untuk parameter request. */
export class RouteNotFoundError extends RoutingError {
  constructor(message: string, cause?: unknown) {
    super(message, cause);
    this.name = 'RouteNotFoundError';
  }
}

/** 503 — graf backend belum siap (mis. baru di-deploy / sedang restart). */
export class RouteUnavailableError extends RoutingError {
  constructor(message: string, cause?: unknown) {
    super(message, cause);
    this.name = 'RouteUnavailableError';
  }
}

/** Network / timeout / abort. */
export class RouteNetworkError extends RoutingError {
  constructor(message: string, cause?: unknown) {
    super(message, cause);
    this.name = 'RouteNetworkError';
  }
}

// ── UI helper formatters ────────────────────────────────────────────────

/**
 * Memformat panjang rute untuk display: < 1000 m → "850 m", >= 1000 m →
 * "1.2 km" dengan satu desimal (FR-UI-RT-05).
 */
export function formatLength(meters: number): string {
  if (meters < 1000) return `${Math.round(meters)} m`;
  return `${(meters / 1000).toFixed(1)} km`;
}

/**
 * Memformat durasi rute: < 60 detik → "Y detik", < 3600 → "Y menit",
 * lainnya → "Hh Mm" (mis. "1j 5m").
 */
export function formatDuration(seconds: number): string {
  if (seconds < 60) return `${Math.round(seconds)} detik`;
  if (seconds < 3600) return `${Math.round(seconds / 60)} menit`;
  const h = Math.floor(seconds / 3600);
  const m = Math.round((seconds - h * 3600) / 60);
  return `${h}j ${m}m`;
}

/**
 * Label Bahasa Indonesia untuk UI, dipisah dari enum value yang
 * case-sensitive ke backend.
 */
export const TRANSPORT_MODE_LABELS: Readonly<Record<TransportMode, string>> = {
  WALKING: 'Jalan kaki',
  MOTORCYCLE: 'Motor',
  CAR: 'Mobil',
};

/** Nama ikon SF Symbol/Material untuk masing-masing moda. */
export const TRANSPORT_MODE_ICONS: Readonly<Record<TransportMode, string>> = {
  WALKING: 'figure.walk',
  MOTORCYCLE: 'bicycle',
  CAR: 'car.fill',
};
