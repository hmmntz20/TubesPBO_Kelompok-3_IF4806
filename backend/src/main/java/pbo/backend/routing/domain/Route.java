package pbo.backend.routing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import pbo.backend.graph.domain.Coordinate;
import pbo.backend.graph.domain.Edge;

/**
 * Hasil pencarian rute terpendek antara dua titik di {@code CampusGraph}.
 *
 * <h2>Encapsulation ketat</h2>
 *
 * <p>Kelas ini {@code final} dan semua field {@code private final}; tidak ada
 * setter publik. Instance hanya dapat dibangun via {@link Route#builder()}.
 * Koleksi yang dikembalikan oleh {@link #nodeIds()}, {@link #edges()}, dan
 * {@link #coordinates()} dibungkus
 * {@link Collections#unmodifiableList unmodifiableList} sehingga client
 * tidak dapat memodifikasi state internal — sejalan dengan NFR-RT-OOP-04.</p>
 *
 * <h2>Pola desain — Builder</h2>
 *
 * <p>{@link Builder} menyediakan parameter bernama, validasi invariant, serta
 * <em>auto-derive</em> untuk:
 * <ul>
 *   <li>{@code lengthMeters} — total {@link Edge#lengthMeters()} bila tidak
 *       diset eksplisit (NFR-RT-ACC-03).</li>
 *   <li>{@code durationSeconds} — {@code lengthMeters / averageSpeedMps}
 *       bila kecepatan rata-rata diberikan via
 *       {@link Builder#averageSpeedMps(double)} (FR-RT-08).</li>
 * </ul>
 * </p>
 *
 * <p>Aturan invariant yang divalidasi di {@link Builder#build()}:</p>
 * <ul>
 *   <li>{@code mode} non-null.</li>
 *   <li>{@code nodeIds} ≥ 1 elemen (rute dengan titik yang sama valid:
 *       0 edge, 0 meter).</li>
 *   <li>{@code coordinates.size() == nodeIds.size()}.</li>
 *   <li>{@code edges.size() == nodeIds.size() - 1} (n node ⇒ n-1 edge
 *       berurutan).</li>
 *   <li>{@code lengthMeters >= 0}, {@code durationSeconds >= 0}.</li>
 *   <li>Bila edges diberikan, kontinuitas {@code edges[i].toNodeId() ==
 *       nodeIds.get(i+1)} dan {@code edges[i].fromNodeId() ==
 *       nodeIds.get(i)} divalidasi agar urutan konsisten.</li>
 * </ul>
 *
 * @see TransportMode
 * @see <a href="../../../../../../../specs/feature-3-routing-astar/requirements.md">FR-RT-07, NFR-RT-OOP-04, NFR-RT-ACC-03</a>
 */
public final class Route {

    private final TransportMode mode;
    private final double lengthMeters;
    private final long durationSeconds;
    private final List<Long> nodeIds;
    private final List<Edge> edges;
    private final List<Coordinate> coordinates;

    private Route(Builder b) {
        this.mode = b.mode;
        this.lengthMeters = b.computedLengthMeters;
        this.durationSeconds = b.computedDurationSeconds;
        this.nodeIds = List.copyOf(b.nodeIds);
        this.edges = List.copyOf(b.edges);
        this.coordinates = List.copyOf(b.coordinates);
    }

    /** @return builder kosong untuk merakit {@link Route}. */
    public static Builder builder() {
        return new Builder();
    }

    /** @return moda transportasi yang dipakai untuk menemukan rute ini. */
    public TransportMode mode() {
        return mode;
    }

    /**
     * @return total panjang rute dalam meter — eksak, sama dengan jumlah
     *         {@link Edge#lengthMeters()} dari semua edge yang dilalui.
     */
    public double lengthMeters() {
        return lengthMeters;
    }

    /**
     * @return estimasi durasi tempuh dalam detik
     *         (= {@code lengthMeters / TransportProfile.averageSpeedMps()}).
     */
    public long durationSeconds() {
        return durationSeconds;
    }

    /** @return list id node urut dari asal ke tujuan, read-only. */
    public List<Long> nodeIds() {
        return nodeIds;
    }

    /** @return list edge urut dari asal ke tujuan, read-only. */
    public List<Edge> edges() {
        return edges;
    }

    /**
     * @return list koordinat urut dari asal ke tujuan, read-only. Siap
     *         dirender sebagai polyline di frontend setelah pemetaan ke
     *         {@code [lat, lng]} di lapisan DTO.
     */
    public List<Coordinate> coordinates() {
        return coordinates;
    }

    /** @return jumlah node pada rute (selalu {@code = nodeIds.size()}). */
    public int nodeCount() {
        return nodeIds.size();
    }

    /** @return jumlah edge pada rute (selalu {@code = nodeCount() - 1}). */
    public int edgeCount() {
        return edges.size();
    }

    @Override
    public String toString() {
        return "Route{mode=" + mode
                + ", nodes=" + nodeIds.size()
                + ", edges=" + edges.size()
                + ", length=" + lengthMeters + " m"
                + ", duration=" + durationSeconds + " s}";
    }

    /**
     * Builder untuk merakit {@link Route} dengan parameter bernama,
     * auto-derivation, dan validasi invariant.
     */
    public static final class Builder {
        private TransportMode mode;
        private final List<Long> nodeIds = new ArrayList<>();
        private final List<Edge> edges = new ArrayList<>();
        private final List<Coordinate> coordinates = new ArrayList<>();
        private Double explicitLengthMeters;
        private Long explicitDurationSeconds;
        private Double averageSpeedMps;

        // Hasil derive — di-set saat build().
        private double computedLengthMeters;
        private long computedDurationSeconds;

        private Builder() {
            // package-private; instans dibuat lewat Route.builder().
        }

        /**
         * @param mode moda transportasi yang dipakai. Wajib di-set.
         * @return {@code this}.
         */
        public Builder mode(TransportMode mode) {
            this.mode = Objects.requireNonNull(mode, "mode tidak boleh null");
            return this;
        }

        /**
         * Menambah satu node id dan koordinatnya ke akhir rute.
         *
         * <p>Versi paling umum dipanggil oleh {@code Router.reconstruct(...)}
         * saat menelusuri jalur dari goal mundur ke start kemudian dibalik.</p>
         *
         * @param nodeId     id node, dipakai untuk validasi kontinuitas edge.
         * @param coordinate koordinat node, tidak boleh {@code null}.
         * @return {@code this}.
         */
        public Builder addNode(long nodeId, Coordinate coordinate) {
            Objects.requireNonNull(coordinate, "coordinate tidak boleh null");
            this.nodeIds.add(nodeId);
            this.coordinates.add(coordinate);
            return this;
        }

        /**
         * Menambah satu edge ke akhir rute.
         *
         * @param edge edge yang dilewati antar dua node terakhir, tidak boleh
         *             {@code null}.
         * @return {@code this}.
         */
        public Builder addEdge(Edge edge) {
            Objects.requireNonNull(edge, "edge tidak boleh null");
            this.edges.add(edge);
            return this;
        }

        /**
         * Set eksplisit total panjang rute. Bila tidak diset, akan
         * di-derive otomatis dari jumlah {@code edges[*].lengthMeters()}
         * (NFR-RT-ACC-03).
         *
         * @param meters panjang dalam meter, tidak boleh negatif.
         * @return {@code this}.
         */
        public Builder lengthMeters(double meters) {
            if (meters < 0) {
                throw new IllegalArgumentException(
                        "lengthMeters harus >= 0, diterima: " + meters);
            }
            this.explicitLengthMeters = meters;
            return this;
        }

        /**
         * Set eksplisit durasi tempuh.
         *
         * @param seconds durasi dalam detik, tidak boleh negatif.
         * @return {@code this}.
         */
        public Builder durationSeconds(long seconds) {
            if (seconds < 0) {
                throw new IllegalArgumentException(
                        "durationSeconds harus >= 0, diterima: " + seconds);
            }
            this.explicitDurationSeconds = seconds;
            return this;
        }

        /**
         * Set kecepatan rata-rata untuk auto-derive {@link #durationSeconds(long)}
         * pada {@link #build()}. Diabaikan bila durasi sudah diset eksplisit.
         *
         * @param mps kecepatan dalam meter per detik, harus {@code > 0}.
         * @return {@code this}.
         */
        public Builder averageSpeedMps(double mps) {
            if (mps <= 0) {
                throw new IllegalArgumentException(
                        "averageSpeedMps harus > 0, diterima: " + mps);
            }
            this.averageSpeedMps = mps;
            return this;
        }

        /**
         * Membangun {@link Route} immutable setelah validasi invariant.
         *
         * @return {@link Route} read-only.
         * @throws IllegalStateException bila salah satu invariant di kelas
         *         {@link Route} tidak terpenuhi.
         */
        public Route build() {
            if (mode == null) {
                throw new IllegalStateException("mode wajib di-set sebelum build()");
            }
            if (nodeIds.isEmpty()) {
                throw new IllegalStateException(
                        "Route memerlukan minimal 1 node (asal == tujuan diperbolehkan).");
            }
            if (coordinates.size() != nodeIds.size()) {
                throw new IllegalStateException(
                        "Jumlah coordinates (" + coordinates.size()
                                + ") harus sama dengan nodeIds (" + nodeIds.size() + ").");
            }
            if (edges.size() != Math.max(0, nodeIds.size() - 1)) {
                throw new IllegalStateException(
                        "Jumlah edges (" + edges.size()
                                + ") harus = nodeIds.size() - 1 (" + (nodeIds.size() - 1) + ").");
            }
            // Validasi kontinuitas.
            for (int i = 0; i < edges.size(); i++) {
                Edge e = edges.get(i);
                long expectedFrom = nodeIds.get(i);
                long expectedTo = nodeIds.get(i + 1);
                if (e.fromNodeId() != expectedFrom || e.toNodeId() != expectedTo) {
                    throw new IllegalStateException(
                            "Edge ke-" + i + " (" + e.fromNodeId() + "->" + e.toNodeId()
                                    + ") tidak nyambung dengan urutan nodeIds ("
                                    + expectedFrom + "->" + expectedTo + ").");
                }
            }

            // Auto-derive length bila tidak eksplisit.
            if (explicitLengthMeters != null) {
                this.computedLengthMeters = explicitLengthMeters;
            } else {
                double sum = 0.0;
                for (Edge e : edges) sum += e.lengthMeters();
                this.computedLengthMeters = sum;
            }

            // Auto-derive duration bila tidak eksplisit.
            if (explicitDurationSeconds != null) {
                this.computedDurationSeconds = explicitDurationSeconds;
            } else if (averageSpeedMps != null) {
                this.computedDurationSeconds = Math.round(this.computedLengthMeters / averageSpeedMps);
            } else {
                // Tidak ada info kecepatan; tetap valid (mis. saat caller
                // hanya butuh path geometri). RouteService akan menset
                // durasi via averageSpeedMps() di pemanggilan nyata.
                this.computedDurationSeconds = 0L;
            }

            return new Route(this);
        }
    }
}
