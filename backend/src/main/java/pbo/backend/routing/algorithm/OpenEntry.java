package pbo.backend.routing.algorithm;

/**
 * Entry priority queue di {@link AStarSearch}: pasangan {@code (nodeId, fScore)}
 * yang immutable.
 *
 * <p>Memakai {@code record} (Java 16+) memberi kita {@code equals/hashCode/toString}
 * gratis dan menjamin tidak ada state mutable yang bocor antar iterasi.
 * Tie-break deterministik (FR-RT-10) dilakukan di {@link AStarSearch} via
 * {@link java.util.Comparator}.</p>
 *
 * @param nodeId id node yang berada di frontier.
 * @param fScore {@code g(n) + h(n)} — semakin kecil, semakin prioritas.
 */
record OpenEntry(long nodeId, double fScore) {
    // Sengaja package-private; tidak perlu dibocorkan ke luar paket algorithm.
}
