package pbo.backend.routing.api.dto;

import java.util.List;

import pbo.backend.graph.domain.Coordinate;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.domain.TransportMode;

/**
 * Body response sukses {@code POST /api/v1/route} (FR-RT-07).
 *
 * <p>Format {@code coordinates} sesuai kontrak FR-RT-07: array
 * {@code [latitude, longitude]} urut dari asal ke tujuan, siap dirender
 * sebagai polyline di frontend.</p>
 *
 * @param from           koordinat asal yang diminta klien.
 * @param to             koordinat tujuan yang diminta klien.
 * @param mode           moda transportasi yang dipakai.
 * @param lengthMeters   panjang total jalur dalam meter.
 * @param durationSeconds estimasi durasi tempuh dalam detik.
 * @param coordinates    polyline — list pasangan [lat, lng].
 * @param nodeIds        list id node yang dilewati (debug/diagnostik).
 */
public record RouteResponse(
        CoordinateDTO from,
        CoordinateDTO to,
        TransportMode mode,
        double lengthMeters,
        long durationSeconds,
        List<double[]> coordinates,
        List<Long> nodeIds) {

    /**
     * Pabrik: memetakan {@link Route} domain + koordinat awal/akhir yang
     * diminta klien menjadi {@link RouteResponse}.
     *
     * <p>Catatan: {@code from} dan {@code to} di response menggunakan koordinat
     * <strong>asli yang diminta klien</strong>, bukan koordinat node hasil
     * snap. Ini memudahkan frontend menggambar marker di lokasi yang tepat.
     * Koordinat node tetap tersedia di {@code coordinates}.</p>
     */
    public static RouteResponse from(Route route, CoordinateDTO requestedFrom, CoordinateDTO requestedTo) {
        List<double[]> latLngs = route.coordinates().stream()
                .map(c -> new double[] { c.latitude(), c.longitude() })
                .toList();

        return new RouteResponse(
                requestedFrom,
                requestedTo,
                route.mode(),
                route.lengthMeters(),
                route.durationSeconds(),
                latLngs,
                route.nodeIds());
    }

    /** Helper diagnostik: pertama node coordinate. Tidak dipakai di JSON. */
    @SuppressWarnings("unused")
    private Coordinate firstNodeCoord() {
        if (coordinates.isEmpty()) return null;
        double[] head = coordinates.get(0);
        return new Coordinate(head[0], head[1]);
    }
}
