package pbo.backend.routing.service;

/**
 * Dilemparkan oleh {@link RouteService} bila {@code CampusGraph} belum siap
 * (mis. inisialisasi gagal) atau tidak punya node sama sekali.
 *
 * <p>Dipetakan ke HTTP {@code 503 Service Unavailable} oleh
 * {@code GlobalExceptionHandler} (FR-RT-04).</p>
 */
public class GraphUnavailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public GraphUnavailableException(String message) {
        super(message);
    }

    public GraphUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
