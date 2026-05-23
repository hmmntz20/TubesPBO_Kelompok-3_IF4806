package pbo.backend.routing.api;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pbo.backend.routing.api.dto.RouteRequest;
import pbo.backend.routing.api.dto.RouteResponse;
import pbo.backend.routing.domain.Route;
import pbo.backend.routing.service.RouteService;

/**
 * Endpoint REST untuk pencarian rute (FR-RT-01).
 *
 * <h2>Tanggung jawab</h2>
 *
 * <p>Lapisan <em>thin controller</em>: hanya menjadwalkan
 * {@link RouteService#findRoute}, memetakan hasil ke {@link RouteResponse},
 * dan menambahkan header HTTP yang relevan ({@code Cache-Control: no-store}
 * per FR-RT-11). Logika domain & validasi tidak hidup di sini.</p>
 *
 * @see RouteService
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/design.md">design §2.6</a>
 */
@RestController
@RequestMapping("/api/v1")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Mencari rute terpendek antara dua titik untuk moda yang dipilih.
     *
     * <p>Bean Validation (mis. range latitude, mode non-null) dieksekusi
     * sebelum body method ini dipanggil; pelanggaran menghasilkan
     * {@link org.springframework.web.bind.MethodArgumentNotValidException}
     * yang ditangani {@link GlobalExceptionHandler} → 400.</p>
     *
     * @param request body request yang sudah di-validasi.
     * @return 200 {@link RouteResponse} dengan {@code Cache-Control: no-store}.
     */
    @PostMapping(
            path = "/route",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RouteResponse> route(@Valid @RequestBody RouteRequest request) {
        Route route = routeService.findRoute(
                request.from().toDomain(),
                request.to().toDomain(),
                request.mode());

        RouteResponse body = RouteResponse.from(route, request.from(), request.to());

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(body);
    }
}
