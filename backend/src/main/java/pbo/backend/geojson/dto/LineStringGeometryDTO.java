package pbo.backend.geojson.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoJSON {@code LineString} — rangkaian titik 2D yang membentuk garis.
 *
 * <p>Setiap koordinat adalah array {@code [longitude, latitude]} sesuai
 * RFC 7946 §3.1.4. Lapisan parser bertanggung jawab membalik ke
 * {@code (latitude, longitude)} sebelum membentuk
 * {@link pbo.backend.graph.domain.Coordinate}.</p>
 */
public final class LineStringGeometryDTO extends GeometryDTO {

    private final List<List<Double>> coordinates;

    /**
     * @param coordinates daftar pasangan {@code [lng, lat]}; minimal 2 titik.
     * @throws NullPointerException     jika {@code coordinates} {@code null}.
     * @throws IllegalArgumentException jika ukurannya {@literal <} 2 atau ada
     *                                  pasangan dengan elemen {@literal <} 2.
     */
    @JsonCreator
    public LineStringGeometryDTO(@JsonProperty("coordinates") List<List<Double>> coordinates) {
        super("LineString");
        if (coordinates == null) {
            throw new NullPointerException("coordinates LineString tidak boleh null");
        }
        if (coordinates.size() < 2) {
            throw new IllegalArgumentException(
                    "LineString minimal 2 titik, diterima: " + coordinates.size());
        }
        for (List<Double> p : coordinates) {
            if (p == null || p.size() < 2) {
                throw new IllegalArgumentException(
                        "Setiap titik LineString minimal [lng, lat]");
            }
        }
        // Defensive copy — tetap menampilkan tipe dasar List untuk Jackson.
        this.coordinates = List.copyOf(coordinates);
    }

    /** @return koordinat read-only dalam urutan {@code [lng, lat]}. */
    public List<List<Double>> coordinates() {
        return coordinates;
    }
}
