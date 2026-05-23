package pbo.backend.geojson.dto;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * GeoJSON {@code FeatureCollection} (RFC 7946 §3.3): wadah {@link FeatureDTO}.
 *
 * <p>Dibuat sebagai kelas terpisah (bukan record) agar tetap fleksibel
 * menambah field tambahan di masa depan (mis. {@code generator}, {@code copyright},
 * {@code timestamp} dari Overpass) tanpa breaking change.</p>
 */
public final class FeatureCollectionDTO {

    private final String type;
    private final List<FeatureDTO> features;

    /**
     * @param type     harus {@code "FeatureCollection"} sesuai RFC 7946.
     * @param features list fitur, tidak boleh {@code null} (boleh kosong).
     * @throws NullPointerException jika {@code features} {@code null}.
     */
    @JsonCreator
    public FeatureCollectionDTO(
            @JsonProperty("type") String type,
            @JsonProperty("features") List<FeatureDTO> features) {
        this.type = type;
        this.features = List.copyOf(Objects.requireNonNull(features, "features tidak boleh null"));
    }

    public String type() {
        return type;
    }

    /** @return list read-only fitur. */
    public List<FeatureDTO> features() {
        return features;
    }

    /** @return jumlah fitur. */
    public int size() {
        return features.size();
    }
}
