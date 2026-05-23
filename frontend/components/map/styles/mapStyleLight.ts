/**
 * Custom map style untuk Light Mode (Google Maps Style JSON).
 *
 * Tujuan: meredam POI yang ramai sehingga jaringan jalan & polyline rute
 * mudah dibaca. Tetap menampilkan label jalan untuk orientasi.
 *
 * Spesifikasi: https://developers.google.com/maps/documentation/style-reference
 */
const mapStyleLight = [
  // Sembunyikan POI bisnis/komersial — menghindari clutter di kampus.
  {
    featureType: 'poi.business',
    stylers: [{ visibility: 'off' }],
  },
  {
    featureType: 'poi.attraction',
    stylers: [{ visibility: 'off' }],
  },
  {
    featureType: 'poi.medical',
    stylers: [{ visibility: 'off' }],
  },
  // Sembunyikan transit (tidak relevan untuk kampus).
  {
    featureType: 'transit',
    stylers: [{ visibility: 'off' }],
  },
  // Pertegas jalan utama.
  {
    featureType: 'road',
    elementType: 'geometry',
    stylers: [{ saturation: -10 }, { lightness: 5 }],
  },
  {
    featureType: 'road.highway',
    elementType: 'geometry',
    stylers: [{ color: '#E1E3E6' }],
  },
  // Lanskap sedikit dingin agar polyline maroon menonjol.
  {
    featureType: 'landscape.natural',
    elementType: 'geometry',
    stylers: [{ color: '#F4F1EE' }],
  },
];

export default mapStyleLight;
