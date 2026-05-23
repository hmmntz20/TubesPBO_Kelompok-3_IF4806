package pbo.backend.routing.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import pbo.backend.graph.domain.Coordinate;

/**
 * DTO koordinat untuk request/response routing.
 *
 * <p>Validasi range latitude/longitude dilakukan pada level Bean Validation
 * (FR-RT-03) sebelum mencapai constructor domain {@link Coordinate} —
 * memberi pesan error yang lebih ramah daripada {@link IllegalArgumentException}
 * dari domain.</p>
 *
 * @param latitude  derajat lintang [-90, 90].
 * @param longitude derajat bujur [-180, 180].
 */
public record CoordinateDTO(
        @NotNull
        @DecimalMin(value = "-90", message = "latitude harus >= -90")
        @DecimalMax(value = "90",  message = "latitude harus <= 90")
        Double latitude,

        @NotNull
        @DecimalMin(value = "-180", message = "longitude harus >= -180")
        @DecimalMax(value = "180",  message = "longitude harus <= 180")
        Double longitude) {

    /** @return objek {@link Coordinate} domain yang setara. */
    public Coordinate toDomain() {
        return new Coordinate(latitude, longitude);
    }

    /** @return DTO baru yang setara dengan {@code coord} domain. */
    public static CoordinateDTO from(Coordinate coord) {
        return new CoordinateDTO(coord.latitude(), coord.longitude());
    }
}
