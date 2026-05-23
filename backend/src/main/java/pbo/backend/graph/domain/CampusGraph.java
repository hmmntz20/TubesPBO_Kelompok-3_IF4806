package pbo.backend.graph.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Agregat graf kampus: kumpulan {@link Node} dan {@link Edge} yang sudah
 * ter-dedup dan siap dipakai algoritma routing.
 *
 * <p><strong>Read-only aggregate:</strong> kelas ini {@code final} dan setelah
 * {@link Builder#build()} dipanggil, struktur internal dibungkus
 * {@link Collections#unmodifiableMap unmodifiableMap} /
 * {@link Collections#unmodifiableList unmodifiableList}. Pemakai
 * <strong>tidak</strong> dapat menambah, menghapus, atau mengganti elemen.</p>
 *
 * <p>Konsumer hanya berinteraksi melalui method publik berbasis
 * {@link Optional} (no-null) dan koleksi read-only.</p>
 *
 * @see Builder
 */
public final class CampusGraph {

    private final Map<Long, Node> nodes;
    private final List<Edge> edges;
    private final Instant builtAt;

    private CampusGraph(Map<Long, Node> nodes, List<Edge> edges, Instant builtAt) {
        // Defensive copy + unmodifiable wrap.
        this.nodes = Collections.unmodifiableMap(new LinkedHashMap<>(nodes));
        this.edges = List.copyOf(edges);
        this.builtAt = builtAt;
    }

    /** @return builder baru untuk merakit graf. */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Mencari node berdasarkan id.
     *
     * @param id identitas node yang dicari.
     * @return {@link Optional#empty()} bila tidak ada — bukan {@code null}.
     */
    public Optional<Node> findNode(long id) {
        return Optional.ofNullable(nodes.get(id));
    }

    /** @return koleksi node read-only (insertion-order). */
    public Collection<Node> nodes() {
        return nodes.values();
    }

    /** @return list edge read-only (insertion-order). */
    public List<Edge> edges() {
        return edges;
    }

    /** @return jumlah node. */
    public int nodeCount() {
        return nodes.size();
    }

    /** @return jumlah edge (sudah memperhitungkan duplikasi dua-arah). */
    public int edgeCount() {
        return edges.size();
    }

    /** @return waktu graf selesai dibangun. */
    public Instant builtAt() {
        return builtAt;
    }

    /**
     * Bounding box yang menutup seluruh node graf.
     *
     * @return bounding box, atau {@link Optional#empty()} jika graf tidak
     *         memiliki node sama sekali.
     */
    public Optional<BoundingBox> boundingBox() {
        if (nodes.isEmpty()) return Optional.empty();
        double minLat = Double.POSITIVE_INFINITY, minLng = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY, maxLng = Double.NEGATIVE_INFINITY;
        for (Node n : nodes.values()) {
            Coordinate c = n.coordinate();
            if (c.latitude()  < minLat) minLat = c.latitude();
            if (c.latitude()  > maxLat) maxLat = c.latitude();
            if (c.longitude() < minLng) minLng = c.longitude();
            if (c.longitude() > maxLng) maxLng = c.longitude();
        }
        return Optional.of(new BoundingBox(
                new Coordinate(minLat, minLng),
                new Coordinate(maxLat, maxLng)));
    }

    /**
     * Bounding box geografis (south-west & north-east). Immutable.
     */
    public record BoundingBox(Coordinate southWest, Coordinate northEast) {
        public BoundingBox {
            Objects.requireNonNull(southWest);
            Objects.requireNonNull(northEast);
        }
    }

    /**
     * Builder untuk merakit {@link CampusGraph}. Dirancang agar parser
     * (lihat {@link pbo.backend.graph.parser.MapDataParser}) bisa menambah
     * node/edge secara inkremental tanpa membocorkan struktur internal graf.
     *
     * <p>Penambahan {@link Node} dengan {@code id} yang sama dianggap
     * <em>idempoten</em> (sudah pernah dibuat → dilewati). Ini krusial untuk
     * dedup persimpangan OSM yang mungkin di-encode di banyak {@code LineString}
     * berbeda.</p>
     */
    public static final class Builder {
        private final Map<Long, Node> nodes = new LinkedHashMap<>();
        private final java.util.List<Edge> edges = new java.util.ArrayList<>();

        /**
         * Menambah node bila belum ada.
         *
         * @param node node yang akan ditambah, tidak boleh {@code null}.
         * @return {@code this} (chain-able).
         */
        public Builder addNode(Node node) {
            Objects.requireNonNull(node, "node tidak boleh null");
            // Idempoten — dedup persimpangan via id.
            nodes.putIfAbsent(node.id(), node);
            return this;
        }

        /**
         * Menambah edge.
         *
         * @param edge edge yang akan ditambah, tidak boleh {@code null}.
         * @return {@code this}.
         * @throws IllegalStateException jika node referensi belum ditambahkan.
         */
        public Builder addEdge(Edge edge) {
            Objects.requireNonNull(edge, "edge tidak boleh null");
            if (!nodes.containsKey(edge.fromNodeId())) {
                throw new IllegalStateException(
                        "Edge merefer fromNodeId yang belum terdaftar: " + edge.fromNodeId());
            }
            if (!nodes.containsKey(edge.toNodeId())) {
                throw new IllegalStateException(
                        "Edge merefer toNodeId yang belum terdaftar: " + edge.toNodeId());
            }
            edges.add(edge);
            return this;
        }

        /** @return apakah node dengan id tersebut sudah pernah ditambah. */
        public boolean hasNode(long id) {
            return nodes.containsKey(id);
        }

        /** @return jumlah node sementara di builder. */
        public int currentNodeCount() {
            return nodes.size();
        }

        /** @return graf yang sudah dibekukan (read-only). */
        public CampusGraph build() {
            return new CampusGraph(new HashMap<>(nodes), List.copyOf(edges), Instant.now());
        }
    }
}
