package pbo.backend.routing.profile;

import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Profile sepeda motor — boleh melewati jalan kendaraan dan pedestrian-only
 * (asumsi MVP).
 *
 * <p>Kecepatan rata-rata 6.94 m/s ≈ 25 km/jam zona kampus (FR-RT-08).</p>
 *
 * <p>Logika {@code canTraverse} mengikuti flag {@link Edge#motorcycleAllowed()}.</p>
 */
@Component
public final class MotorcycleProfile extends AbstractTransportProfile {

    /** Kecepatan rata-rata sepeda motor di kampus (FR-RT-08). */
    public static final double AVG_SPEED_MPS = 6.94;

    public MotorcycleProfile() {
        super(TransportMode.MOTORCYCLE, AVG_SPEED_MPS, Edge::motorcycleAllowed);
    }
}
