import { lngLatPositionsToLatLngArray, lngLatToLatLng } from '@/utils/coords';
import type { Position } from '@/types/geojson';

describe('coords.lngLatToLatLng', () => {
  it('membalik urutan [lng, lat] menjadi {latitude, longitude}', () => {
    const pos: Position = [107.6320, -6.972];
    expect(lngLatToLatLng(pos)).toEqual({
      latitude: -6.972,
      longitude: 107.632,
    });
  });

  it('mempertahankan presisi double', () => {
    const pos: Position = [107.6340023, -6.9725432];
    const out = lngLatToLatLng(pos);
    expect(out.latitude).toBeCloseTo(-6.9725432, 7);
    expect(out.longitude).toBeCloseTo(107.6340023, 7);
  });

  it('mengabaikan elemen ke-3 (altitude) jika ada', () => {
    const pos: Position = [107.0, -6.0, 750];
    const out = lngLatToLatLng(pos);
    expect(out).toEqual({ latitude: -6.0, longitude: 107.0 });
  });

  it('melempar RangeError untuk position kurang dari 2 elemen', () => {
    expect(() => lngLatToLatLng([107.0] as unknown as Position)).toThrow(RangeError);
  });
});

describe('coords.lngLatPositionsToLatLngArray', () => {
  it('mengonversi rangkaian Position menjadi array LatLng', () => {
    const positions: Position[] = [
      [107.0, -6.0],
      [107.001, -6.001],
      [107.002, -6.002],
    ];
    const out = lngLatPositionsToLatLngArray(positions);
    expect(out).toHaveLength(3);
    expect(out[0]).toEqual({ latitude: -6.0, longitude: 107.0 });
    expect(out[2]).toEqual({ latitude: -6.002, longitude: 107.002 });
  });

  it('mengembalikan array baru — input tidak dimutasi', () => {
    const positions: Position[] = [[107.0, -6.0]];
    const out = lngLatPositionsToLatLngArray(positions);
    expect(out).not.toBe(positions);
  });
});
