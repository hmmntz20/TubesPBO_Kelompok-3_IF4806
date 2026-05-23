package pbo.backend.graph.api.dto;

import java.time.Instant;

/**
 * Respons untuk {@code GET /api/v1/graph/meta} — ringkasan graf kampus
 * yang sedang di-cache di backend.
 *
 * <p>Bentuk JSON:</p>
 * <pre>
 * {
 *   "nodeCount": 4321,
 *   "edgeCount": 9876,
 *   "bbox": {
 *     "ne": [-6.965, 107.639],
 *     "sw": [-6.979, 107.625]
 *   },
 *   "loadedAt": "2026-05-22T06:30:00Z",
 *   "parser": "osm-overpass-v1"
 * }
 * </pre>
 *
 * @param nodeCount jumlah node setelah dedup persimpangan.
 * @param edgeCount jumlah edge (sudah memperhitungkan dua-arah).
 * @param bbox      bounding box minimal yang menutup seluruh node.
 * @param loadedAt  waktu graf selesai dibangun (ISO-8601 UTC).
 * @param parser    identifier parser strategy yang dipakai.
 */
public record GraphMetaResponse(
        int nodeCount,
        int edgeCount,
        BBox bbox,
        Instant loadedAt,
        String parser) {

    /**
     * Bounding box geografis: {@code ne} = north-east (max lat & lng);
     * {@code sw} = south-west (min lat & lng). Format koordinat
     * {@code [latitude, longitude]} — konsisten dengan domain (bukan dengan
     * RFC 7946 GeoJSON).
     *
     * @param ne pasangan {@code [lat, lng]} pojok kanan-atas.
     * @param sw pasangan {@code [lat, lng]} pojok kiri-bawah.
     */
    public record BBox(double[] ne, double[] sw) { }
}
