/**
 * Layanan klien untuk endpoint pencarian rute backend
 * `POST /api/v1/route`.
 *
 * <p>Tanggung jawab:</p>
 * <ul>
 *   <li>Memanggil {@code apiClient.post} dengan body {@link RouteRequest}.</li>
 *   <li>Memetakan HTTP status ke <strong>error class spesifik</strong>
 *       (lihat {@code types/route.ts}) sehingga UI dapat menampilkan pesan
 *       yang sesuai tanpa parsing string ad-hoc.</li>
 *   <li>Memvalidasi struktur response sebelum diteruskan ke pemanggil.</li>
 * </ul>
 *
 * <p>Bukan tanggung jawab service ini: caching, retry otomatis, atau
 * navigasi. Semua dipisah ke layer di atasnya (UI / store).</p>
 */
import { apiClient, ApiError } from '@/services/api-client';
import {
  isTransportMode,
  RouteNetworkError,
  RouteNotFoundError,
  RouteUnavailableError,
  RouteValidationError,
  RoutingError,
  type Route,
  type RouteErrorBody,
  type RouteRequest,
} from '@/types/route';

const ENDPOINT = '/api/v1/route';

/**
 * Mencari rute terpendek antara dua titik untuk moda yang dipilih.
 *
 * @param request body request — koordinat asal/tujuan + moda.
 * @returns       {@link Route} immutable hasil A* dari backend.
 *
 * @throws {RouteValidationError} HTTP 400 (lat/lng di luar range, mode salah).
 * @throws {RouteNotFoundError}   HTTP 404 (tidak ada rute untuk moda ini).
 * @throws {RouteUnavailableError} HTTP 503 (graf belum siap).
 * @throws {RouteNetworkError}    network/timeout/abort.
 * @throws {RoutingError}         status lain yang tak terduga.
 */
export async function findRoute(request: RouteRequest): Promise<Route> {
  const body = serializeRequest(request);

  let res: Response;
  try {
    res = await apiClient.post(ENDPOINT, body);
  } catch (cause) {
    if (cause instanceof ApiError) {
      throw new RouteNetworkError(
        'Tidak dapat terhubung ke server. Periksa koneksi internet.',
        cause,
      );
    }
    throw cause;
  }

  if (res.ok) {
    const json = (await res.json()) as unknown;
    return parseRoute(json);
  }

  // Body error — best effort baca, kalau gagal pakai pesan default.
  const errorMessage = await readErrorMessage(res);

  switch (res.status) {
    case 400:
      throw new RouteValidationError(errorMessage);
    case 404:
      throw new RouteNotFoundError(errorMessage);
    case 503:
      throw new RouteUnavailableError(errorMessage);
    default:
      throw new RoutingError(`Server merespons status ${res.status}: ${errorMessage}`);
  }
}

// ── helpers ─────────────────────────────────────────────────────────────

/** Konversi {@link RouteRequest} ke bentuk JSON yang diharapkan backend. */
function serializeRequest(req: RouteRequest): unknown {
  return {
    from: { latitude: req.from.latitude, longitude: req.from.longitude },
    to:   { latitude: req.to.latitude,   longitude: req.to.longitude   },
    mode: req.mode,
  };
}

/**
 * Validasi runtime untuk JSON tak terpercaya. Mencegah crash di komponen
 * pemakai bila backend berubah tanpa diketahui.
 */
function parseRoute(value: unknown): Route {
  if (value == null || typeof value !== 'object') {
    throw new RoutingError('Response server tidak valid (bukan objek).');
  }
  const obj = value as Record<string, unknown>;
  const from = parseLatLng(obj.from, 'from');
  const to = parseLatLng(obj.to, 'to');
  if (!isTransportMode(obj.mode)) {
    throw new RoutingError(`Response server: mode tidak dikenal (${String(obj.mode)}).`);
  }
  if (typeof obj.lengthMeters !== 'number' || typeof obj.durationSeconds !== 'number') {
    throw new RoutingError('Response server: lengthMeters/durationSeconds invalid.');
  }
  if (!Array.isArray(obj.coordinates) || !Array.isArray(obj.nodeIds)) {
    throw new RoutingError('Response server: coordinates/nodeIds invalid.');
  }
  const coordinates = (obj.coordinates as unknown[]).map((c, i) => {
    if (!Array.isArray(c) || c.length < 2 || typeof c[0] !== 'number' || typeof c[1] !== 'number') {
      throw new RoutingError(`Response server: coordinates[${i}] bukan [lat, lng].`);
    }
    return [c[0], c[1]] as readonly [number, number];
  });
  const nodeIds = (obj.nodeIds as unknown[]).map((n, i) => {
    if (typeof n !== 'number') {
      throw new RoutingError(`Response server: nodeIds[${i}] bukan number.`);
    }
    return n;
  });

  return {
    from,
    to,
    mode: obj.mode,
    lengthMeters: obj.lengthMeters,
    durationSeconds: obj.durationSeconds,
    coordinates,
    nodeIds,
  };
}

function parseLatLng(value: unknown, field: string): { latitude: number; longitude: number } {
  if (value == null || typeof value !== 'object') {
    throw new RoutingError(`Response server: ${field} bukan objek.`);
  }
  const obj = value as Record<string, unknown>;
  if (typeof obj.latitude !== 'number' || typeof obj.longitude !== 'number') {
    throw new RoutingError(`Response server: ${field}.latitude/longitude invalid.`);
  }
  return { latitude: obj.latitude, longitude: obj.longitude };
}

async function readErrorMessage(res: Response): Promise<string> {
  try {
    const json = (await res.json()) as RouteErrorBody;
    if (json && typeof json.error === 'string' && json.error.length > 0) {
      return json.error;
    }
  } catch {
    /* body bukan JSON / kosong */
  }
  return `HTTP ${res.status}`;
}
