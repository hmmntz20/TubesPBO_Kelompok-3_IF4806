package pbo.backend.routing.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import pbo.backend.routing.domain.TransportMode;

/**
 * Body request {@code POST /api/v1/route} (FR-RT-02).
 *
 * <p>Validasi nested {@link CoordinateDTO} dipicu oleh {@code @Valid}; field
 * yang hilang menghasilkan {@code 400 Bad Request} via
 * {@code GlobalExceptionHandler} (FR-RT-03).</p>
 *
 * @param from koordinat asal, wajib.
 * @param to   koordinat tujuan, wajib.
 * @param mode moda transportasi, wajib.
 */
public record RouteRequest(
        @NotNull(message = "from wajib disertakan")
        @Valid CoordinateDTO from,

        @NotNull(message = "to wajib disertakan")
        @Valid CoordinateDTO to,

        @NotNull(message = "mode wajib salah satu dari WALKING, MOTORCYCLE, CAR")
        TransportMode mode) {
}
