/**
 * Route store — sumber kebenaran state pencarian rute pada satu sesi.
 *
 * <h3>Karakter store</h3>
 * <ul>
 *   <li><strong>Tidak persisten</strong> — beda dengan {@code theme-store}.
 *       Rute hanya relevan untuk sesi saat ini; persistensi riwayat menjadi
 *       tanggung jawab Fitur 4 (history).</li>
 *   <li><strong>Encapsulasi</strong> — state hanya dapat dimutasi via
 *       <em>actions</em> di interface ini. Komponen memanggil action,
 *       bukan men-set field langsung. Pattern ini meniru "method
 *       boundary" pada kelas OOP.</li>
 *   <li><strong>Selector-friendly</strong> — komponen di-rerender hanya
 *       saat field yang dibaca berubah; pakai selector spesifik:
 *       {@code useRouteStore(s => s.mode)}.</li>
 * </ul>
 */
import type { LatLng } from 'react-native-maps';
import { create } from 'zustand';

import type { Route, RoutingError, TransportMode } from '@/types/route';

/**
 * Status flow "pin di peta": null saat tidak picking, atau target field
 * yang sedang dipilih (FR-UI-RT-02).
 */
export type Picking = 'from' | 'to' | null;

interface RouteState {
  // ── data ────────────────────────────────────────────────────────────
  mode: TransportMode;
  pendingFrom: LatLng | null;
  pendingTo: LatLng | null;
  picking: Picking;
  currentRoute: Route | null;
  loading: boolean;
  error: RoutingError | null;

  // ── actions (satu-satunya cara mutasi) ──────────────────────────────
  setMode: (mode: TransportMode) => void;
  setPendingFrom: (p: LatLng | null) => void;
  setPendingTo: (p: LatLng | null) => void;
  swapPending: () => void;
  setPicking: (p: Picking) => void;
  setLoading: (loading: boolean) => void;
  setError: (e: RoutingError | null) => void;
  setCurrentRoute: (r: Route | null) => void;
  /** Reset ke state awal — dipanggil oleh tombol "Hapus rute". */
  clear: () => void;
}

const INITIAL: Pick<
  RouteState,
  | 'mode'
  | 'pendingFrom'
  | 'pendingTo'
  | 'picking'
  | 'currentRoute'
  | 'loading'
  | 'error'
> = {
  mode: 'WALKING',
  pendingFrom: null,
  pendingTo: null,
  picking: null,
  currentRoute: null,
  loading: false,
  error: null,
};

export const useRouteStore = create<RouteState>((set, get) => ({
  ...INITIAL,

  setMode: (mode) => set({ mode }),
  setPendingFrom: (pendingFrom) => set({ pendingFrom, error: null }),
  setPendingTo: (pendingTo) => set({ pendingTo, error: null }),
  swapPending: () => {
    const { pendingFrom, pendingTo } = get();
    set({ pendingFrom: pendingTo, pendingTo: pendingFrom });
  },
  setPicking: (picking) => set({ picking }),
  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),
  setCurrentRoute: (currentRoute) => set({ currentRoute, error: null }),
  clear: () => set({ ...INITIAL }),
}));
