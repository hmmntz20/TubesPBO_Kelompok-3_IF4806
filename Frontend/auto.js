const fs = require('fs');
const path = require('path');

// Sesuaikan path ini jika lokasi file JSON kamu berbeda
const NODE_FILE = './assets/geojson/testNode2.json';
const EDGE_FILE = './assets/geojson/testEdge2.json';
const OUT_NODE = './assets/geojson/Node.json';
const OUT_EDGE = './assets/geojson/Edge.json';

// Fungsi menghitung jarak (Haversine) dalam meter
function getDistance(coord1, coord2) {
  const R = 6371e3;
  const lat1 = (coord1[1] * Math.PI) / 180;
  const lat2 = (coord2[1] * Math.PI) / 180;
  const dLat = ((coord2[1] - coord1[1]) * Math.PI) / 180;
  const dLon = ((coord2[0] - coord1[0]) * Math.PI) / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + 
            Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
  return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
}

try {
  const nodesData = JSON.parse(fs.readFileSync(NODE_FILE, 'utf8'));
  const edgesData = JSON.parse(fs.readFileSync(EDGE_FILE, 'utf8'));

  const nodes = nodesData.features;
  const edges = edgesData.features;

  let newNodesCount = 0;

  // Fungsi untuk mencari node terdekat atau membuat node simpang baru
  function findOrCreateNode(coord) {
    let nearestNode = null;
    let minDistance = 15; // Jarak toleransi (15 meter)

    // Cari apakah ada gedung/node di dekat ujung garis ini
    for (let node of nodes) {
      const dist = getDistance(coord, node.geometry.coordinates);
      if (dist < minDistance) {
        minDistance = dist;
        nearestNode = node;
      }
    }

    if (nearestNode) {
      return nearestNode.properties.id; // Ketemu gedung!
    }

    // Jika tidak ada gedung, buat titik persimpangan baru!
    newNodesCount++;
    const newNodeId = `auto_simpang_${newNodesCount}`;
    const newNode = {
      type: "Feature",
      properties: {
        id: newNodeId,
        name: `Simpang Otomatis ${newNodesCount}`,
        category: "intersection",
        is_accessible: true
      },
      geometry: {
        type: "Point",
        coordinates: coord
      }
    };
    nodes.push(newNode);
    return newNodeId;
  }

  // Proses penyambungan otomatis
  edges.forEach((edge, index) => {
    const coords = edge.geometry.coordinates;
    if (!coords || coords.length < 2) return;

    const startCoord = coords[0];
    const endCoord = coords[coords.length - 1];

    const fromNodeId = findOrCreateNode(startCoord);
    const toNodeId = findOrCreateNode(endCoord);

    // Suntikkan properti ke Edge
    edge.properties = {
      id: `road_${index + 1}`,
      name: `Jalan ${index + 1}`,
      from_node: fromNodeId,
      to_node: toNodeId,
      pedestrian: true,
      motorcycle: true,
      car: true
    };
  });

  // Simpan hasilnya langsung menimpa file testNode.json dan testEdge.json utama
  fs.writeFileSync(OUT_NODE, JSON.stringify(nodesData, null, 2));
  fs.writeFileSync(OUT_EDGE, JSON.stringify(edgesData, null, 2));

  console.log('✅ SUKSES! Pemetaan berhasil disambungkan.');
  console.log(`🔌 Berhasil membuat ${newNodesCount} node persimpangan baru.`);
  console.log('📂 File telah disimpan sebagai testNode.json dan testEdge.json');

} catch (error) {
  console.error('❌ Terjadi kesalahan:', error.message);
}