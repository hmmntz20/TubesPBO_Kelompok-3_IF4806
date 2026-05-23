import AsyncStorage from '@react-native-async-storage/async-storage';

import { __testing, fetchCampusGeoJSON } from '@/services/geojson-service';
import type { TelmapFeatureCollection } from '@/types/geojson';

const validFC: TelmapFeatureCollection = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: { '@id': 'way/1', highway: 'tertiary' },
      geometry: {
        type: 'LineString',
        coordinates: [
          [107.0, -6.0],
          [107.001, -6.001],
        ],
      },
    },
  ],
};

const otherFC: TelmapFeatureCollection = {
  type: 'FeatureCollection',
  features: [
    {
      type: 'Feature',
      properties: { '@id': 'way/2', highway: 'footway' },
      geometry: {
        type: 'LineString',
        coordinates: [
          [107.5, -6.5],
          [107.501, -6.501],
        ],
      },
    },
  ],
};

beforeEach(async () => {
  await AsyncStorage.clear();
  (global as { fetch?: typeof fetch }).fetch = jest.fn();
});

function mockFetchOnce(impl: typeof fetch) {
  (global as { fetch?: typeof fetch }).fetch = jest.fn(impl);
}

function jsonResponse(body: unknown, init: ResponseInit = {}): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'Content-Type': 'application/json', ...((init.headers ?? {}) as Record<string, string>) },
    ...init,
  });
}

describe('geojson-service.fetchCampusGeoJSON', () => {
  it('skenario 1: network sukses → simpan cache, source=network', async () => {
    mockFetchOnce(async (input, init) => {
      // Pertama kali — tidak ada If-None-Match
      expect((init?.headers as Record<string, string>)?.['If-None-Match']).toBeUndefined();
      return jsonResponse(validFC, {
        status: 200,
        headers: { ETag: '"sha256:abc123"' },
      });
    });

    const result = await fetchCampusGeoJSON();

    expect(result.source).toBe('network');
    expect(result.revalidated).toBe(false);
    expect(result.data.features).toHaveLength(1);

    // Cache terisi.
    const cached = await __testing.safeReadCache();
    expect(cached?.etag).toBe('"sha256:abc123"');
    expect(cached?.data.features).toHaveLength(1);
  });

  it('skenario 2: 304 setelah cache ada → kembalikan cache, source=cache, revalidated=true', async () => {
    // Seed cache.
    await __testing.safeWriteCache({
      etag: '"sha256:cached-etag"',
      fetchedAt: new Date().toISOString(),
      data: validFC,
    });

    mockFetchOnce(async (input, init) => {
      expect((init?.headers as Record<string, string>)?.['If-None-Match']).toBe('"sha256:cached-etag"');
      return new Response(null, { status: 304 });
    });

    const result = await fetchCampusGeoJSON();

    expect(result.source).toBe('cache');
    expect(result.revalidated).toBe(true);
    expect(result.data.features).toHaveLength(1);
  });

  it('skenario 3: network error + cache ada → fallback ke cache, source=cache', async () => {
    await __testing.safeWriteCache({
      etag: '"sha256:cached-etag"',
      fetchedAt: new Date().toISOString(),
      data: otherFC,
    });

    mockFetchOnce(async () => {
      throw new TypeError('Network request failed');
    });

    const result = await fetchCampusGeoJSON();

    expect(result.source).toBe('cache');
    expect(result.revalidated).toBe(false);
    expect(result.data.features[0].properties['@id']).toBe('way/2');
  });

  it('skenario 4: network error + cache kosong → fallback ke bundled', async () => {
    // Bundled di-mock dengan fetch yang dipanggil oleh service.
    mockFetchOnce(async (input) => {
      const url = String(input);
      if (url.startsWith('mocked://')) {
        // Permintaan ke bundle URI dari Asset
        return jsonResponse(otherFC);
      }
      throw new TypeError('Network request failed');
    });

    const result = await fetchCampusGeoJSON();

    expect(result.source).toBe('bundle');
    expect(result.data.features[0].properties['@id']).toBe('way/2');
  });

  it('skenario 5: payload network invalid → cache tetap utuh, fallback ke cache jika ada', async () => {
    await __testing.safeWriteCache({
      etag: '"sha256:cached-etag"',
      fetchedAt: new Date().toISOString(),
      data: validFC,
    });

    mockFetchOnce(async () => jsonResponse({ type: 'NotAFeatureCollection' }, { status: 200 }));

    const result = await fetchCampusGeoJSON();

    expect(result.source).toBe('cache');
    expect(result.data.features).toHaveLength(1);
  });
});
