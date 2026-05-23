/**
 * Custom map style untuk Dark Mode (Google Maps Style JSON).
 *
 * Disusun konsisten dengan palette `Colors.dark` di `constants/theme.ts`
 * (background `#0F1115`, surface `#1A1D22`). POI ramai disembunyikan dan
 * label dibuat lebih redup agar polyline maroon tetap menonjol.
 */
const mapStyleDark = [
  // Lapisan dasar gelap.
  {
    elementType: 'geometry',
    stylers: [{ color: '#0F1115' }],
  },
  {
    elementType: 'labels.text.stroke',
    stylers: [{ color: '#0F1115' }],
  },
  {
    elementType: 'labels.text.fill',
    stylers: [{ color: '#9BA1A6' }],
  },

  // Lanskap.
  {
    featureType: 'landscape',
    elementType: 'geometry',
    stylers: [{ color: '#1A1D22' }],
  },

  // Air sedikit lebih gelap.
  {
    featureType: 'water',
    elementType: 'geometry',
    stylers: [{ color: '#0A0B0E' }],
  },

  // Jalan: gelap dengan kontras cukup untuk label.
  {
    featureType: 'road',
    elementType: 'geometry',
    stylers: [{ color: '#2A2E34' }],
  },
  {
    featureType: 'road',
    elementType: 'labels.text.fill',
    stylers: [{ color: '#C7CCD1' }],
  },
  {
    featureType: 'road.highway',
    elementType: 'geometry',
    stylers: [{ color: '#3A3F46' }],
  },

  // Sembunyikan POI ramai.
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
  {
    featureType: 'transit',
    stylers: [{ visibility: 'off' }],
  },
];

export default mapStyleDark;
