package pbo.backend.routing.profile;

import pbo.backend.graph.domain.Edge;
import pbo.backend.routing.domain.TransportMode;

/**
 * Strategi moda transportasi: menentukan {@link Edge} mana yang boleh dilalui
 * dan kecepatan rata-rata yang dipakai untuk menghitung durasi.
 *
 * <p><strong>Pola desain:</strong> Strategy. Konsumer (mis. {@code Router} di
 * TASK-RT-BE-04 dan {@code RouteService} di TASK-RT-BE-06) bergantung pada
 * interface ini, bukan pada implementasi konkret seperti
 * {@code PedestrianProfile}, {@code MotorcycleProfile}, atau {@code CarProfile}
 * (lihat <a href="../../../../../../../specs/feature-3-routing-astar/design.md">design §2.2</a>).
 * Pendekatan ini menjamin Open-Closed: penambahan moda baru (mis. sepeda)
 * cukup menambah konstanta di {@link TransportMode} + implementasi
 * {@code TransportProfile} baru — tidak mengubah kelas yang sudah ada.</p>
 *
 * <p><strong>Catatan eksekusi:</strong> interface ini sengaja dideklarasikan di
 * TASK-RT-BE-01 sebagai <em>compile prerequisite</em> untuk
 * {@link TransportMode#profile()}. Implementasi konkret + binding via
 * {@code TransportConfig} ditambahkan di TASK-RT-BE-03.</p>
 *
 * @see TransportMode
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-06, FR-RT-08, NFR-RT-OOP-02</a>
 */
public interface TransportProfile {

    /**
     * @return moda yang diwakili profil ini. Pasangan 1:1 dengan
     *         konstanta {@link TransportMode}.
     */
    TransportMode mode();

    /**
     * Mengevaluasi apakah moda ini diperbolehkan melewati {@code edge}.
     *
     * @param edge edge yang akan dievaluasi, tidak boleh {@code null}.
     * @return {@code true} bila moda ini boleh melewati {@code edge}.
     * @throws NullPointerException jika {@code edge} {@code null}.
     */
    boolean canTraverse(Edge edge);

    /**
     * Kecepatan rata-rata di kampus dalam meter per detik.
     *
     * <p>Dipakai oleh {@code RouteService} untuk menghitung
     * {@code durationSeconds = lengthMeters / averageSpeedMps()}
     * (lihat FR-RT-08).</p>
     *
     * @return kecepatan rata-rata, selalu {@code > 0}.
     */
    double averageSpeedMps();
}
