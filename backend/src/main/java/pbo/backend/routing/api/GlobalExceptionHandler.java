package pbo.backend.routing.api;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import pbo.backend.routing.api.dto.ErrorResponse;
import pbo.backend.routing.service.GraphUnavailableException;
import pbo.backend.routing.service.RouteNotFoundException;

/**
 * Pemetaan terpusat exception → HTTP status untuk endpoint routing
 * (FR-RT-03, FR-RT-09, FR-RT-04).
 *
 * <p>Hanya mengaktifkan dirinya untuk paket {@code pbo.backend.routing.api} —
 * tidak mempengaruhi handler controller lain (mis. {@code GraphController}
 * di paket {@code graph.api}).</p>
 */
@RestControllerAdvice(basePackages = "pbo.backend.routing.api")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Validasi Bean Validation (mis. lat/lng di luar range, mode null) → 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        if (message.isBlank()) message = "Permintaan tidak valid.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    /**
     * JSON malformed atau enum {@code mode} tidak dikenal (mis. {@code "TANK"}) → 400.
     *
     * <p>Jackson melempar {@link HttpMessageNotReadableException} untuk kasus
     * deserialisasi enum gagal. Pesan diambil ringkas agar tidak membocorkan
     * detail internal.</p>
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        String message = "Permintaan tidak valid: format JSON salah atau nilai enum tidak dikenal.";
        // Beri petunjuk lebih spesifik untuk mode enum bila terdeteksi.
        if (ex.getMessage() != null && ex.getMessage().contains("TransportMode")) {
            message = "mode harus salah satu dari WALKING, MOTORCYCLE, CAR.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(message));
    }

    /** Tidak ada rute → 404 (FR-RT-09). */
    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRouteNotFound(RouteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(
                        "Tidak ada rute untuk moda " + ex.mode()
                                + ". Coba moda lain atau titik berbeda."));
    }

    /** Graf belum siap / kosong → 503. */
    @ExceptionHandler(GraphUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleGraphUnavailable(GraphUnavailableException ex) {
        log.warn("Graf tidak tersedia: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ErrorResponse.of("Graf belum siap. Silakan coba beberapa saat lagi."));
    }

    /**
     * Argumen domain tidak valid yang lolos Bean Validation (jaring pengaman).
     * Mis. {@link IllegalArgumentException} dari constructor {@code Coordinate}
     * untuk kasus yang sangat jarang.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ex.getMessage()));
    }
}
