package pbo.backend.routing.profile;

import org.springframework.stereotype.Component;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Profile pejalan kaki — boleh melewati semua jenis jalan, termasuk
 * pedestrian-only ({@code footway}, {@code path}, dll.).
 *
 * <p>Logika {@code canTraverse} didelegasikan ke flag {@link Edge#pedestrianAllowed()}
 * via method reference — implementasi tidak duplikat di sini.</p>
 *
 * <p>Kecepatan rata-rata 1.39 m/s ≈ 5 km/jam (FR-RT-08).</p>
 */
@Component
public final class PedestrianProfile extends AbstractTransportProfile {

    /** Kecepatan rata-rata berjalan di kampus (FR-RT-08). */
    public static final double AVG_SPEED_MPS = 1.39;

    public PedestrianProfile() {
        super(TransportMode.WALKING, AVG_SPEED_MPS, Edge::pedestrianAllowed);
    }
}
