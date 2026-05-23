package pbo.backend.routing.profile;

import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Profile mobil — <strong>tidak</strong> boleh melewati jalan pedestrian-only
 * ({@code footway}, {@code path}, {@code pedestrian}, {@code steps},
 * {@code cycleway}, {@code track}). Filtering ini sudah dikodekan di
 * {@link Edge#carAllowed()} oleh parser ({@code OsmGeoJsonParser}).
 *
 * <p>Kecepatan rata-rata 5.56 m/s ≈ 20 km/jam zona kampus dengan polisi
 * tidur (FR-RT-08).</p>
 */
@Component
public final class CarProfile extends AbstractTransportProfile {

    /** Kecepatan rata-rata mobil di kampus (FR-RT-08). */
    public static final double AVG_SPEED_MPS = 5.56;

    public CarProfile() {
        super(TransportMode.CAR, AVG_SPEED_MPS, Edge::carAllowed);
    }
}
