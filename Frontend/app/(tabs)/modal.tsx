import React, { useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ScrollView } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

// --- FUNGSI HELPER UNTUK TANGGAL DINAMIS ---
const getPastDate = (daysAgo) => {
  const date = new Date();
  date.setDate(date.getDate() - daysAgo);
  return date;
};

// 1. Data Dummy (Fokus ke Jarak & Waktu)
const historyData = [
  {
    id: '1',
    dateObj: getPastDate(0), // Hari ini
    lokasiAsal: 'Gedung Rektorat Telkom',
    lokasiTujuan: 'Kosan Sukabirus',
    status: 'Tiba',
    jarak: '1.2 km',
    waktu: '5 min',
  },
  {
    id: '2',
    dateObj: getPastDate(1), // Kemarin
    lokasiAsal: 'Trans Studio Mall',
    lokasiTujuan: 'Stasiun Bandung',
    status: 'Tiba',
    jarak: '4.5 km',
    waktu: '18 min',
  },
  {
    id: '3',
    dateObj: getPastDate(5), // Minggu Lalu
    lokasiAsal: 'Alun-alun Bandung',
    lokasiTujuan: 'Gedung Sate',
    status: 'Batal',
    jarak: '2.1 km',
    waktu: '10 min',
  },
  {
    id: '4',
    dateObj: getPastDate(20), // Bulan Lalu
    lokasiAsal: 'Lembang',
    lokasiTujuan: 'Dago Atas',
    status: 'Tiba',
    jarak: '12.4 km',
    waktu: '45 min',
  },
];

const FILTERS = ['All', 'Today', 'Yesterday', 'Last Week', 'Last Month'];

export default function HistoryScreen() {
  const [activeFilter, setActiveFilter] = useState('All');

  // --- LOGIKA FILTER TANGGAL ---
  const filteredData = historyData.filter((item) => {
    if (activeFilter === 'All') return true;

    const today = new Date();
    const itemDate = item.dateObj;
    
    // Hilangkan jam agar perbandingan hari lebih akurat
    const todayDateOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const itemDateOnly = new Date(itemDate.getFullYear(), itemDate.getMonth(), itemDate.getDate());
    
    const diffTime = todayDateOnly - itemDateOnly;
    const diffDays = diffTime / (1000 * 60 * 60 * 24);

    if (activeFilter === 'Today') return diffDays === 0;
    if (activeFilter === 'Yesterday') return diffDays === 1;
    if (activeFilter === 'Last Week') return diffDays > 1 && diffDays <= 7;
    if (activeFilter === 'Last Month') return diffDays > 7 && diffDays <= 30;

    return false;
  });

  // 2. Format Tanggal UI (Cth: 15 Jun 2026)
  const formatDate = (date) => {
    const options = { day: 'numeric', month: 'short', year: 'numeric' };
    return date.toLocaleDateString('id-ID', options);
  };

  // 3. Render Card Item
  const renderItem = ({ item }) => (
    <TouchableOpacity style={styles.cardContainer} activeOpacity={0.7}>
      
      {/* Header: Tanggal & Status */}
      <View style={styles.cardHeader}>
        <Text style={styles.dateText}>{formatDate(item.dateObj)}</Text>
        <View style={[
          styles.statusBadge, 
          { backgroundColor: item.status === 'Tiba' ? 'rgba(0, 255, 0, 0.1)' : 'rgba(255, 0, 0, 0.1)' }
        ]}>
          <Text style={[
            styles.statusText, 
            { color: item.status === 'Tiba' ? '#4ade80' : '#f87171' }
          ]}>
            {item.status}
          </Text>
        </View>
      </View>

      {/* Tengah: Rute Lokasi */}
      <View style={styles.routeContainer}>
        <View style={styles.routePoint}>
          <Ionicons name="location-outline" size={20} color="#3b82f6" />
          <Text style={styles.locationText}>{item.lokasiAsal}</Text>
        </View>
        <View style={styles.routeLine} />
        <View style={styles.routePoint}>
          <Ionicons name="flag-outline" size={20} color="#facc15" />
          <Text style={styles.locationText}>{item.lokasiTujuan}</Text>
        </View>
      </View>

      {/* Bawah: Jarak & Waktu (Sesuai Shortest Route Finder) */}
      <View style={styles.cardFooter}>
        <View style={styles.statBox}>
          <Ionicons name="analytics-outline" size={16} color="#9ca3af" />
          <Text style={styles.statLabel}>Jarak: </Text>
          <Text style={styles.statValue}>{item.jarak}</Text>
        </View>
        <View style={styles.statBox}>
          <Ionicons name="time-outline" size={16} color="#9ca3af" />
          <Text style={styles.statLabel}>Waktu: </Text>
          <Text style={styles.statValue}>{item.waktu}</Text>
        </View>
      </View>

    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <Text style={styles.pageTitle}>History Rute</Text>
      
      {/* UI Filter Horizontal */}
      <View style={styles.filterWrapper}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterScroll}>
          {FILTERS.map((filter) => (
            <TouchableOpacity 
              key={filter} 
              style={[styles.filterBtn, activeFilter === filter && styles.filterBtnActive]}
              onPress={() => setActiveFilter(filter)}
            >
              <Text style={[styles.filterText, activeFilter === filter && styles.filterTextActive]}>
                {filter}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>
      
      {/* List Riwayat */}
      {filteredData.length > 0 ? (
        <FlatList
          data={filteredData}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.listContainer}
          showsVerticalScrollIndicator={false}
        />
      ) : (
        <View style={styles.emptyState}>
          <Ionicons name="map-outline" size={48} color="#4b5563" />
          <Text style={styles.emptyText}>Tidak ada rute di periode ini.</Text>
        </View>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0a0a0a',
    paddingTop: 50,
  },
  pageTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#ffffff',
    marginHorizontal: 20,
    marginBottom: 15,
  },
  filterWrapper: {
    marginBottom: 15,
  },
  filterScroll: {
    paddingHorizontal: 20,
    gap: 10,
  },
  filterBtn: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
  },
  filterBtnActive: {
    backgroundColor: '#3b82f6',
    borderColor: '#3b82f6',
  },
  filterText: {
    color: '#9ca3af',
    fontSize: 14,
    fontWeight: '500',
  },
  filterTextActive: {
    color: '#ffffff',
    fontWeight: 'bold',
  },
  listContainer: {
    paddingHorizontal: 20,
    paddingBottom: 30,
  },
  cardContainer: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderColor: 'rgba(255, 255, 255, 0.1)',
    borderWidth: 1,
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  dateText: {
    color: '#9ca3af',
    fontSize: 12,
  },
  statusBadge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
  },
  statusText: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  routeContainer: {
    marginBottom: 16,
  },
  routePoint: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  locationText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
    marginLeft: 12,
  },
  routeLine: {
    width: 2,
    height: 16,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    marginLeft: 9,
    marginVertical: 4,
  },
  cardFooter: {
    flexDirection: 'row',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.1)',
    paddingTop: 12,
    gap: 20,
  },
  statBox: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statLabel: {
    color: '#9ca3af',
    fontSize: 14,
    marginLeft: 6,
  },
  statValue: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: 'bold',
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 50,
  },
  emptyText: {
    color: '#9ca3af',
    marginTop: 12,
    fontSize: 14,
  },
});