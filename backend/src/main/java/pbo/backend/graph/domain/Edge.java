package pbo.backend.graph.domain;

import java.util.Objects;

/**
 * Segmen jalan berarah dari satu {@link Node} ke node lain.
 *
 * <p><strong>Encapsulation ketat:</strong> semua field {@code private final},
 * tidak ada setter publik. Konstruksi memakai {@link Builder} untuk menjaga
 * agar invariant divalidasi sekali dan tidak ada parameter constructor yang
 * urutannya rawan dipertukarkan.</p>
 *
 * <p>Edge ini bersifat <em>directed</em>. Untuk jalan dua arah, parser
 * menambahkan dua edge berlawanan arah. Properti {@code highwayType} disimpan
 * apa adanya dari OSM (mis. {@code "tertiary"}, {@code "footway"}) sebagai
 * referensi pemetaan moda transportasi.</p>
 *
 * <p><strong>Flag moda transportasi (FR-GEO-09 MVP defaults):</strong>
 * disuplai oleh parser; di luar parser ini bersifat read-only. Algoritma A*
 * di Fase 3 akan memfilter edge berdasarkan flag ini.</p>
 */
public final class Edge {

    private final long id;
    private final long fromNodeId;
    private final long toNodeId;
    private final double lengthMeters;
    private final String highwayType;
    private final boolean pedestrianAllowed;
    private final boolean motorcycleAllowed;
    private final boolean carAllowed;
    private final String featureRefId;

    private Edge(Builder b) {
        this.id = b.id;
        this.fromNodeId = b.fromNodeId;
        this.toNodeId = b.toNodeId;
        this.lengthMeters = b.lengthMeters;
        this.highwayType = Objects.requireNonNull(b.highwayType, "highwayType tidak boleh null");
        this.pedestrianAllowed = b.pedestrianAllowed;
        this.motorcycleAllowed = b.motorcycleAllowed;
        this.carAllowed = b.carAllowed;
        this.featureRefId = Objects.requireNonNull(b.featureRefId, "featureRefId tidak boleh null");
    }

    /** @return builder kosong untuk merakit {@link Edge}. */
    public static Builder builder() {
        return new Builder();
    }

    public long id()                  { return id; }
    public long fromNodeId()          { return fromNodeId; }
    public long toNodeId()            { return toNodeId; }
    public double lengthMeters()      { return lengthMeters; }
    public String highwayType()       { return highwayType; }
    public boolean pedestrianAllowed(){ return pedestrianAllowed; }
    public boolean motorcycleAllowed(){ return motorcycleAllowed; }
    public boolean carAllowed()       { return carAllowed; }
    public String featureRefId()      { return featureRefId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "Edge{id=" + id + ", " + fromNodeId + "->" + toNodeId
                + ", " + highwayType + ", " + lengthMeters + " m}";
    }

    /**
     * Builder untuk merakit {@link Edge} dengan parameter bernama.
     * <p>Pemakai wajib men-set: {@code id}, {@code fromNodeId}, {@code toNodeId},
     * {@code lengthMeters}, {@code highwayType}, {@code featureRefId}, dan
     * setidaknya satu flag transport (default: pedestrian {@code true}, lainnya
     * {@code false}).</p>
     */
    public static final class Builder {
        private long id;
        private long fromNodeId;
        private long toNodeId;
        private double lengthMeters;
        private String highwayType;
        private boolean pedestrianAllowed = true;
        private boolean motorcycleAllowed = false;
        private boolean carAllowed = false;
        private String featureRefId = "";

        public Builder id(long v)                    { this.id = v; return this; }
        public Builder fromNodeId(long v)            { this.fromNodeId = v; return this; }
        public Builder toNodeId(long v)              { this.toNodeId = v; return this; }
        public Builder lengthMeters(double v)        { this.lengthMeters = v; return this; }
        public Builder highwayType(String v)         { this.highwayType = v; return this; }
        public Builder pedestrianAllowed(boolean v)  { this.pedestrianAllowed = v; return this; }
        public Builder motorcycleAllowed(boolean v)  { this.motorcycleAllowed = v; return this; }
        public Builder carAllowed(boolean v)         { this.carAllowed = v; return this; }
        public Builder featureRefId(String v)        { this.featureRefId = v; return this; }

        /**
         * Membangun instance final dengan validasi invariant.
         *
         * @throws IllegalArgumentException jika {@code lengthMeters &lt; 0}
         *         atau {@code fromNodeId == toNodeId} (loop tidak diizinkan).
         */
        public Edge build() {
            if (lengthMeters < 0) {
                throw new IllegalArgumentException(
                        "lengthMeters harus >= 0, diterima: " + lengthMeters);
            }
            if (fromNodeId == toNodeId) {
                throw new IllegalArgumentException(
                        "Edge tidak boleh self-loop (from == to == " + fromNodeId + ")");
            }
            return new Edge(this);
        }
    }
}
