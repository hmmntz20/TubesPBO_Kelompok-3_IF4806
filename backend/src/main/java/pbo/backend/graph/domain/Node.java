package pbo.backend.graph.domain;

import java.util.Objects;

/**
 * Titik (vertex) di graf kampus.
 *
 * <p><strong>Encapsulation ketat:</strong> semua field {@code private final}.
 * Tidak ada setter publik; nilai hanya bisa di-set sekali via constructor.
 * Identitas berbasis {@code id}, sehingga koleksi seperti {@link java.util.HashMap}
 * dan {@link java.util.HashSet} dapat memakai node sebagai key tanpa risiko
 * dua node berbeda tetapi <em>equal</em>.</p>
 *
 * <p>{@code id} biasanya dihasilkan deterministik oleh parser dari koordinat
 * yang dibulatkan, sehingga dua LineString OSM yang berbagi titik
 * persimpangan menghasilkan {@code id} yang sama → otomatis ter-dedup.</p>
 *
 * @see Coordinate
 * @see CampusGraph
 */
public final class Node {

    private final long id;
    private final Coordinate coordinate;

    /**
     * Membuat node baru.
     *
     * @param id         identitas unik dalam satu {@link CampusGraph}.
     * @param coordinate koordinat geografis, tidak boleh {@code null}.
     * @throws NullPointerException jika {@code coordinate} {@code null}.
     */
    public Node(long id, Coordinate coordinate) {
        this.id = id;
        this.coordinate = Objects.requireNonNull(coordinate, "coordinate tidak boleh null");
    }

    /** @return identitas unik node. */
    public long id() {
        return id;
    }

    /** @return koordinat geografis node (immutable). */
    public Coordinate coordinate() {
        return coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Node{id=" + id + ", " + coordinate + "}";
    }
}
