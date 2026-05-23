/**
 * Wrapper `fetch` yang sederhana namun bertema-proyek.
 *
 * Tanggung jawab:
 *  - Menghasilkan URL absolut dari path relatif (`/api/v1/...`) memakai
 *    base URL dari `constants/env.ts`.
 *  - Mengaplikasikan timeout default agar request tidak menggantung
 *    pada jaringan yang lambat.
 *  - Membungkus error jaringan ke jenis yang konsisten.
 *
 * Bukan tanggung jawab:
 *  - Caching atau revalidasi ETag → itu di service-spesifik
 *    (lihat `services/geojson-service.ts`).
 *  - Auth / token — akan ditambah saat Fitur 1 (Auth) tiba.
 */
import { env } from '@/constants/env';

export interface ApiRequestOptions extends Omit<RequestInit, 'signal'> {
  /** Timeout ms (default 8000). 0 = tanpa timeout. */
  timeoutMs?: number;
}

const DEFAULT_TIMEOUT_MS = 8000;

/**
 * Melakukan HTTP GET ke `path` (relatif terhadap `env.apiBaseUrl`) dan
 * mengembalikan `Response` mentah. Pemanggil bertanggung jawab
 * mem-parse JSON / membaca header sesuai endpoint.
 *
 * @throws {ApiError} pada timeout, network failure, atau abort.
 */
export async function get(path: string, options: ApiRequestOptions = {}): Promise<Response> {
  return request('GET', path, options);
}

/**
 * Melakukan HTTP POST dengan body JSON. Body otomatis di-serialize via
 * `JSON.stringify` dan `Content-Type: application/json` di-set kecuali
 * pemanggil sudah meng-override-nya di `options.headers`.
 *
 * @param path     path relatif (mis. `/api/v1/route`).
 * @param body     payload yang akan di-serialize ke JSON. Pakai `null`
 *                 untuk mengirim body kosong.
 * @param options  opsi tambahan (timeout, header tambahan).
 * @returns        `Response` mentah; pemanggil mem-parse JSON sendiri.
 *
 * @throws {ApiError} timeout / network failure / abort.
 */
export async function post(
  path: string,
  body: unknown,
  options: ApiRequestOptions = {},
): Promise<Response> {
  const headers = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...(options.headers ?? {}),
  };
  return request('POST', path, {
    ...options,
    headers,
    body: body == null ? undefined : JSON.stringify(body),
  });
}

async function request(
  method: string,
  path: string,
  options: ApiRequestOptions,
): Promise<Response> {
  const url = buildUrl(path);
  const { timeoutMs = DEFAULT_TIMEOUT_MS, headers, ...rest } = options;

  // Attach Authorization header otomatis bila ada sesi Supabase aktif.
  // Pemanggil yang sudah men-set "Authorization" di options.headers di-respect.
  const finalHeaders = await injectAuthHeader(
    (headers ?? {}) as Record<string, string>,
  );

  const controller = new AbortController();
  const timer = timeoutMs > 0 ? setTimeout(() => controller.abort(), timeoutMs) : null;

  try {
    return await fetch(url, {
      ...rest,
      method,
      headers: finalHeaders,
      signal: controller.signal,
    });
  } catch (cause) {
    if ((cause as Error).name === 'AbortError') {
      throw new ApiError(`Request timeout setelah ${timeoutMs} ms: ${url}`, cause);
    }
    throw new ApiError(`Network error untuk ${url}: ${(cause as Error).message}`, cause);
  } finally {
    if (timer) clearTimeout(timer);
  }
}

/**
 * Bila pemanggil belum mengeset header Authorization sendiri dan ada
 * sesi Supabase aktif, lampirkan {@code Bearer <access_token>}.
 *
 * <p>Memakai dynamic import agar tidak menarik {@code auth-service} ke
 * bundle awal saat aplikasi belum login (mengurangi cold start) dan
 * agar testfile yang me-mock fetch tidak harus me-mock seluruh
 * Supabase SDK.</p>
 */
async function injectAuthHeader(
  headers: Record<string, string>,
): Promise<Record<string, string>> {
  if (headers['Authorization'] || headers['authorization']) return headers;
  try {
    const { getAccessToken } = await import('@/services/auth-service');
    const token = await getAccessToken();
    if (token) {
      return { ...headers, Authorization: `Bearer ${token}` };
    }
  } catch {
    // Auth service belum siap (mis. di test yang tidak butuh auth) — abaikan.
  }
  return headers;
}

function buildUrl(path: string): string {
  if (/^https?:\/\//i.test(path)) return path;
  const base = env.apiBaseUrl.replace(/\/$/, '');
  const pathPart = path.startsWith('/') ? path : `/${path}`;
  return `${base}${pathPart}`;
}

/** Error transport-level dari `apiClient`. */
export class ApiError extends Error {
  constructor(message: string, public readonly cause?: unknown) {
    super(message);
    this.name = 'ApiError';
  }
}

export const apiClient = {
  get,
  post,
  /** Untuk testing — eksposur internal builder URL. */
  __buildUrl: buildUrl,
};
