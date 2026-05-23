import { findRoute } from '@/services/routing-service';
import {
  RouteNetworkError,
  RouteNotFoundError,
  RouteUnavailableError,
  RouteValidationError,
  RoutingError,
  type Route,
  type RouteRequest,
} from '@/types/route';

// Body request standar dipakai di hampir semua test.
const REQUEST: RouteRequest = {
  from: { latitude: -6.972, longitude: 107.635 },
  to:   { latitude: -6.978, longitude: 107.640 },
  mode: 'WALKING',
};

const VALID_RESPONSE = {
  from: { latitude: -6.972, longitude: 107.635 },
  to:   { latitude: -6.978, longitude: 107.640 },
  mode: 'WALKING',
  lengthMeters: 532.4,
  durationSeconds: 383,
  coordinates: [
    [-6.972, 107.635],
    [-6.975, 107.637],
    [-6.978, 107.640],
  ],
  nodeIds: [101, 202, 303],
};

beforeEach(() => {
  (global as { fetch?: typeof fetch }).fetch = jest.fn();
});

function mockFetchOnce(impl: typeof fetch) {
  (global as { fetch?: typeof fetch }).fetch = jest.fn(impl);
}

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('routing-service.findRoute', () => {
  it('skenario sukses (200) mengembalikan Route yang ter-validasi', async () => {
    mockFetchOnce(async (input, init) => {
      // Verifikasi: method POST + body JSON yang sesuai.
      expect(init?.method).toBe('POST');
      expect((init?.headers as Record<string, string>)?.['Content-Type']).toBe('application/json');
      expect(JSON.parse(init?.body as string)).toEqual({
        from: { latitude: -6.972, longitude: 107.635 },
        to:   { latitude: -6.978, longitude: 107.640 },
        mode: 'WALKING',
      });
      return jsonResponse(VALID_RESPONSE, 200);
    });

    const route: Route = await findRoute(REQUEST);

    expect(route.mode).toBe('WALKING');
    expect(route.lengthMeters).toBeCloseTo(532.4, 6);
    expect(route.durationSeconds).toBe(383);
    expect(route.coordinates).toHaveLength(3);
    expect(route.coordinates[0]).toEqual([-6.972, 107.635]);
    expect(route.nodeIds).toEqual([101, 202, 303]);
  });

  it('skenario 400 → RouteValidationError dengan pesan dari body', async () => {
    mockFetchOnce(async () =>
      jsonResponse({ error: 'latitude: latitude harus <= 90' }, 400),
    );

    await expect(findRoute(REQUEST)).rejects.toBeInstanceOf(RouteValidationError);
    await expect(findRoute(REQUEST)).rejects.toThrow(/latitude/);
  });

  it('skenario 404 → RouteNotFoundError', async () => {
    mockFetchOnce(async () =>
      jsonResponse(
        { error: 'Tidak ada rute untuk moda CAR. Coba moda lain atau titik berbeda.' },
        404,
      ),
    );

    await expect(findRoute({ ...REQUEST, mode: 'CAR' }))
      .rejects.toBeInstanceOf(RouteNotFoundError);
  });

  it('skenario 503 → RouteUnavailableError', async () => {
    mockFetchOnce(async () =>
      jsonResponse({ error: 'Graf belum siap. Silakan coba beberapa saat lagi.' }, 503),
    );

    await expect(findRoute(REQUEST)).rejects.toBeInstanceOf(RouteUnavailableError);
  });

  it('network failure → RouteNetworkError', async () => {
    mockFetchOnce(async () => {
      throw new TypeError('Network request failed');
    });

    await expect(findRoute(REQUEST)).rejects.toBeInstanceOf(RouteNetworkError);
  });

  it('payload invalid (missing fields) → RoutingError generic', async () => {
    mockFetchOnce(async () =>
      jsonResponse({ /* incomplete */ from: { latitude: 0, longitude: 0 } }, 200),
    );

    await expect(findRoute(REQUEST)).rejects.toBeInstanceOf(RoutingError);
  });

  it('status tak dikenal (500) → RoutingError generic', async () => {
    mockFetchOnce(async () => new Response('Internal error', { status: 500 }));

    await expect(findRoute(REQUEST)).rejects.toBeInstanceOf(RoutingError);
  });

  it('mode tidak dikenal pada response → RoutingError generic', async () => {
    mockFetchOnce(async () =>
      jsonResponse({ ...VALID_RESPONSE, mode: 'TANK' }, 200),
    );

    await expect(findRoute(REQUEST)).rejects.toThrow(/mode tidak dikenal/);
  });
});
