import React, { useState, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { FontAwesome5, Ionicons } from "@expo/vector-icons";
import tw from 'twrnc';
import { useFocusEffect } from 'expo-router';
import { API_URL } from '../../config/api';
import AsyncStorage from '@react-native-async-storage/async-storage';

// --- DATA STRUCTURE ---
interface HistoryItem {
  id: string;
  dateObj: Date;
  origin: string;
  destination: string;
  status: 'Arrived' | 'Cancelled';
  rawDistance: number; 
  rawDuration: number; 
  mode: string; 
}

const FILTERS = ['All', 'Today', 'Yesterday', 'Last Week', 'Last Month'];

export default function HistoryScreen() {
  const [activeFilter, setActiveFilter] = useState('All');
  const [historyData, setHistoryData] = useState<HistoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // --- FETCH DATA DARI BACKEND ---
  const fetchHistory = async () => {
    setIsLoading(true);
    try {
      // 1. Ambil userId dari AsyncStorage
      const userId = await AsyncStorage.getItem('userId'); 
      
      if (!userId) {
          console.warn("Belum login, tidak bisa mengambil history.");
          setIsLoading(false);
          return;
      }

      // 2. Fetch data menggunakan userId dinamis
      const response = await fetch(`${API_URL}/history/${userId}`);
      
      if (response.ok) {
        const data = await response.json();
        
        // 3. Mapping data dari Spring Boot ke format UI React Native
        const formattedData: HistoryItem[] = data.map((item: any) => ({
          id: item.historyId,
          dateObj: new Date(item.timestamp),
          origin: item.startPointName,
          destination: item.endPointName,
          status: 'Arrived', // Secara default dianggap sampai jika masuk history
          rawDistance: item.distance,
          rawDuration: item.duration,
          // Ubah nama PEDESTRIAN kembali ke 'walk' untuk ikon UI
          mode: item.transportMode.toLowerCase() === 'pedestrian' ? 'walk' : item.transportMode.toLowerCase(),
        }));
        
        setHistoryData(formattedData);
      }
    } catch (error) {
      console.error("❌ Gagal memuat history:", error);
    } finally {
      setIsLoading(false);
    }
  };

  // Gunakan useFocusEffect agar data selalu diperbarui saat kita membuka tab ini
  useFocusEffect(
    useCallback(() => {
      fetchHistory();
    }, [])
  );

  // --- FILTER LOGIC (Berdasarkan Tanggal) ---
  const filteredData = historyData.filter((item) => {
    if (activeFilter === 'All') return true;

    const today = new Date();
    const itemDate = item.dateObj;
    
    const todayDateOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const itemDateOnly = new Date(itemDate.getFullYear(), itemDate.getMonth(), itemDate.getDate());
    
    const diffTime = todayDateOnly.getTime() - itemDateOnly.getTime();
    const diffDays = diffTime / (1000 * 60 * 60 * 24);

    if (activeFilter === 'Today') return diffDays === 0;
    if (activeFilter === 'Yesterday') return diffDays === 1;
    if (activeFilter === 'Last Week') return diffDays > 1 && diffDays <= 7;
    if (activeFilter === 'Last Month') return diffDays > 7 && diffDays <= 30;

    return false;
  });

  // --- FORMATTERS ---
  const formatDate = (date: Date) => {
    return date.toLocaleDateString('en-US', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  };

  const formatDistance = (meters: number) => {
    if (meters < 1000) return `${Math.round(meters)} m`;
    return `${(meters / 1000).toFixed(1)} km`;
  };

  const formatDuration = (seconds: number) => {
    const mins = Math.round(seconds / 60);
    return `${mins} min`;
  };

  const getModeIcon = (mode: string) => {
    if (mode === 'car') return 'car';
    if (mode === 'motorcycle') return 'motorcycle'; 
    return 'walking';
  };

  // --- RENDER CARD ITEM ---
  const renderItem = ({ item }: { item: HistoryItem }) => (
    <TouchableOpacity 
      style={tw`bg-white rounded-[20px] p-[18px] mb-4 shadow-sm elevation-4`} 
      activeOpacity={0.7}
    >
      {/* Header: Date & Status */}
      <View style={tw`flex-row justify-between items-center mb-4`}>
        <Text style={tw`text-gray-500 text-xs font-medium`}>{formatDate(item.dateObj)}</Text>
        <View style={tw`px-3 py-1 rounded-xl ${item.status === 'Arrived' ? 'bg-green-100' : 'bg-red-100'}`}>
          <Text style={tw`text-xs font-bold ${item.status === 'Arrived' ? 'text-green-800' : 'text-red-800'}`}>
            {item.status}
          </Text>
        </View>
      </View>

      {/* Middle: Route Locations */}
      <View style={tw`mb-4`}>
        <View style={tw`flex-row items-center pr-8`}>
          <Ionicons name="location-outline" size={20} color="#6b7280" />
          <Text style={tw`text-gray-700 text-[15px] font-bold ml-3`} numberOfLines={1}>{item.origin}</Text>
        </View>
        <View style={tw`w-0.5 h-[18px] bg-gray-200 ml-[9px] my-1`} />
        <View style={tw`flex-row items-center pr-8`}>
          <Ionicons name="location" size={20} color="#7B1113" />
          <Text style={tw`text-gray-700 text-[15px] font-bold ml-3`} numberOfLines={1}>{item.destination}</Text>
        </View>
      </View>

      {/* Bottom: Stats & Badges */}
      <View style={tw`flex-row justify-between items-center border-t border-gray-100 pt-3.5`}>
        <View style={tw`flex-row items-center gap-4`}>
          <View style={tw`flex-row items-center gap-1.5`}>
            <Ionicons name="analytics-outline" size={16} color="#9ca3af" />
            <Text style={tw`text-gray-600 text-sm font-semibold`}>{formatDistance(item.rawDistance)}</Text>
          </View>
          <View style={tw`flex-row items-center gap-1.5`}>
            <Ionicons name="time-outline" size={16} color="#9ca3af" />
            <Text style={tw`text-gray-600 text-sm font-semibold`}>{formatDuration(item.rawDuration)}</Text>
          </View>
        </View>

        <View style={tw`flex-row items-center gap-3`}>
          <View style={tw`bg-red-50 p-2 rounded-xl`}>
            <FontAwesome5 name={getModeIcon(item.mode)} size={16} color="#8b0000" />
          </View>
        </View>
      </View>
    </TouchableOpacity>
  );

  // --- MAIN RENDER ---
  return (
    <View style={tw`flex-1 bg-gray-50 pt-[50px]`}>
      <Text style={tw`text-[26px] font-black text-gray-800 mx-5 mb-4`}>Route History</Text>
      
      {/* Horizontal Filters */}
      <View style={tw`mb-4`}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={tw`px-5 gap-2.5`}>
          {FILTERS.map((filter) => (
            <TouchableOpacity 
              key={filter} 
              style={tw`px-4.5 py-2.5 rounded-full border shadow-sm elevation-1 ${
                activeFilter === filter ? 'bg-[#8b0000] border-[#8b0000]' : 'bg-white border-gray-200'
              }`}
              onPress={() => setActiveFilter(filter)}
            >
              <Text style={tw`text-sm ${
                activeFilter === filter ? 'text-white font-bold' : 'text-gray-500 font-semibold'
              }`}>
                {filter}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>
      
      {/* History List */}
      {isLoading ? (
        <View style={tw`flex-1 justify-center items-center mb-24`}>
          <ActivityIndicator size="large" color="#8b0000" />
          <Text style={tw`text-gray-400 mt-3 text-[15px] font-medium`}>Memuat riwayat...</Text>
        </View>
      ) : filteredData.length > 0 ? (
        <FlatList
          data={filteredData}
          keyExtractor={(item) => item.id}
          renderItem={renderItem}
          contentContainerStyle={tw`px-5 pb-[100px]`}
          showsVerticalScrollIndicator={false}
        />
      ) : (
        <View style={tw`flex-1 justify-center items-center mb-24`}>
          <Ionicons name="map-outline" size={48} color="#d1d5db" />
          <Text style={tw`text-gray-400 mt-3 text-[15px] font-medium`}>No routes found for this period.</Text>
        </View>
      )}
    </View>
  );
}