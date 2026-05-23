package pbo.backend.routing.service;

import pbo.backend.routing.domain.TransportMode;

/**
 * Dilemparkan oleh {@link RouteService} bila {@link pbo.backend.routing.algorithm.Router}
 * tidak menemukan jalur antara dua node untuk moda yang diminta.
 *
 * <p>Ditangkap oleh {@code GlobalExceptionHandler} dan dipetakan ke
 * HTTP {@code 404 Not Found} dengan body diagnostik (FR-RT-09).</p>
 *
 * <p>Kelas tetap {@code RuntimeException} agar tidak memaksa caller meng-catch
 * tetapi tetap eksplisit secara semantik via tipe.</p>
 */
public class RouteNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final TransportMode mode;
    private final long fromNodeId;
    private final long toNodeId;

    public RouteNotFoundException(TransportMode mode, long fromNodeId, long toNodeId) {
        super("Tidak ada rute untuk moda " + mode + " (from " + fromNodeId + " → " + toNodeId + ")");
        this.mode = mode;
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    /** @return moda transportasi yang diminta saat error. */
    public TransportMode mode() {
        return mode;
    }

    /** @return id node asal hasil snap. */
    public long fromNodeId() {
        return fromNodeId;
    }

    /** @return id node tujuan hasil snap. */
    public long toNodeId() {
        return toNodeId;
    }
}
