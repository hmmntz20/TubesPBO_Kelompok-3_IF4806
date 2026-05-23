package pbo.backend.graph.domain;

/**
 * Pasangan koordinat geografis dalam derajat desimal (WGS84).
 *
 * <p>Tipe data <em>value-typed</em> dan <strong>immutable</strong>: tidak
 * menyimpan referensi keluar dan tidak punya setter. Validasi <em>invariant</em>
 * dilakukan di compact constructor sehingga sebuah instance {@code Coordinate}
 * yang tercipta dijamin valid secara geometrik.</p>
 *
 * <p><strong>Konvensi proyek:</strong> domain memakai urutan
 * {@code (latitude, longitude)} — kebalikan dari format GeoJSON RFC 7946
 * yang menyimpan {@code [longitude, latitude]}. Pembalikan ini terjadi sekali
 * di lapisan parser ({@link pbo.backend.graph.parser.MapDataParser}).</p>
 *
 * @param latitude  derajat lintang, dibatasi {@code [-90, 90]}.
 * @param longitude derajat bujur, dibatasi {@code [-180, 180]}.
 *
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1">RFC 7946 §3.1.1</a>
 */
public record Coordinate(double latitude, double longitude) {

    /** Radius rata-rata bumi dalam meter, untuk haversine. */
    private static final double EARTH_RADIUS_METERS = 6_371_008.8;

    /**
     * Compact constructor: memvalidasi invariant koordinat.
     *
     * @throws IllegalArgumentException jika {@code latitude} atau {@code longitude}
     *                                  berada di luar rentang yang valid.
     */
    public Coordinate {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException(
                    "latitude harus berada di [-90, 90], diterima: " + latitude);
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException(
                    "longitude harus berada di [-180, 180], diterima: " + longitude);
        }
    }

    /**
     * Menghitung jarak <em>great-circle</em> ke {@code other} memakai formula
     * haversine. Cukup akurat untuk skala kampus (kurang dari 1 m error pada
     * jarak ratusan meter).
     *
     * @param other koordinat lain, tidak boleh {@code null}.
     * @return jarak dalam meter, selalu {@code >= 0}.
     * @throws NullPointerException jika {@code other} {@code null}.
     */
    public double distanceMetersTo(Coordinate other) {
        if (other == null) {
            throw new NullPointerException("other Coordinate tidak boleh null");
        }
        double phi1 = Math.toRadians(this.latitude);
        double phi2 = Math.toRadians(other.latitude);
        double deltaPhi = Math.toRadians(other.latitude - this.latitude);
        double deltaLambda = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
