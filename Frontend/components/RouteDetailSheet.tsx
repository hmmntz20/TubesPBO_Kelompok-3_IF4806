import { FontAwesome5, Ionicons } from "@expo/vector-icons";
import React, { useEffect } from "react";
import { Animated, Platform, Text, TouchableOpacity, View } from "react-native";
import tw from "twrnc";

// --- 1. DEFINISI TIPE DATA (Menyesuaikan Response JSON dari Spring Boot) ---
export type TravelMode = "pedestrian" | "motorcycle" | "car"; // 'walk' diganti 'pedestrian' agar cocok dengan Backend

export interface GeoNode {
  id: string;
  name?: string; // Di Java, MainNode punya atribut 'name'
  category?: string;
  coordinates: number[];
}

export interface PathResult {
  totalDistance: number; // Sesuai dengan properti 'totalDistance' di RouteResult.java
  totalDuration: number; // Sesuai dengan properti 'totalDuration' di RouteResult.java
}

interface RouteDetailsSheetProps {
  slideAnim: Animated.Value;
  fadeAnim: Animated.Value;
  routeResult: PathResult | null;
  selectedMode: TravelMode;
  setSelectedMode: (mode: TravelMode) => void;
  requireParking: boolean;
  setRequireParking: (req: boolean) => void;
  fromNode: GeoNode | null;
  toNode: GeoNode | null;
  onStartNavigation: () => void;
  onClearRoute: () => void;
  onCalculateRoute: (mode: TravelMode, parking: boolean) => void;
}

export default function RouteDetailsSheet({
  slideAnim,
  fadeAnim,
  routeResult,
  selectedMode,
  setSelectedMode,
  requireParking,
  setRequireParking,
  fromNode,
  toNode,
  onStartNavigation,
  onClearRoute,
  onCalculateRoute,
}: RouteDetailsSheetProps) {

  // Otomatis fetch rute ke backend kalau mode/parkir diganti
  useEffect(() => {
    if (fromNode && toNode) {
      onCalculateRoute(selectedMode, requireParking);
    }
  }, [selectedMode, requireParking]); 
  
  // Helper Formatter
  const formatDuration = (seconds: number) => {
    const mins = Math.round(seconds / 60);
    if (mins < 1) return ["<1", "min"];
    return [`${mins}`, "min"];
  };

  const formatDistance = (meters: number) => {
    if (meters < 1000) return `${Math.round(meters)} m`;
    return `${(meters / 1000).toFixed(1)} km`;
  };

  const getModeLabel = () => {
    switch (selectedMode) {
      case "pedestrian": return "By Walking";
      case "motorcycle": return "By Motorcycle";
      case "car": return "By Car";
    }
  };

  const timeData = routeResult ? formatDuration(routeResult.totalDuration) : ["--", "min"];

  return (
    <Animated.View
      style={[
        tw`absolute bottom-5 left-4 right-4 bg-white/95 rounded-[32px] p-5 border border-white/80`,
        Platform.select({
          ios: {
            shadowColor: "#000",
            shadowOffset: { width: 0, height: 10 },
            shadowOpacity: 0.15,
            shadowRadius: 20,
          },
          android: { elevation: 12 },
        }),
        { transform: [{ translateY: slideAnim }], opacity: fadeAnim },
      ]}
    >
      {/* HEADER: Waktu & Jarak + Tombol Tutup */}
      <View style={tw`flex-row justify-between items-start mb-4`}>
        <View>
          <Text style={tw`text-3xl font-extrabold text-[#1A1A1A]`}>
            {timeData[0]}
            <Text style={tw`text-lg text-[#666] font-semibold`}> {timeData[1]}</Text>
          </Text>
          <Text style={tw`text-[13px] font-medium text-[#888] mt-0.5`}>
            {routeResult ? formatDistance(routeResult.totalDistance) : "--"} • {getModeLabel()}
          </Text>
        </View>

        <TouchableOpacity
          onPress={onClearRoute}
          style={tw`w-8 h-8 bg-[#F0F0F0] rounded-full justify-center items-center`}
        >
          <Ionicons name="close" size={18} color="#666" />
        </TouchableOpacity>
      </View>

      {/* MODE KENDARAAN (SEGMENTED CONTROL) */}
      <View style={tw`flex-row bg-[#F5F5F5] rounded-2xl p-1.5 mb-3`}>
        {/* --- 2. UBAH 'walk' MENJADI 'pedestrian' --- */}
        {(["pedestrian", "motorcycle", "car"] as TravelMode[]).map((m) => (
          <TouchableOpacity
            key={m}
            style={[
              tw`flex-1 h-[42px] rounded-xl flex-row justify-center items-center gap-2`,
              selectedMode === m ? tw`bg-white shadow-sm` : null,
            ]}
            onPress={() => setSelectedMode(m)}
          >
            <FontAwesome5
              name={m === "pedestrian" ? "walking" : m}
              size={15}
              color={selectedMode === m ? "#7B1113" : "#888"}
            />
            {selectedMode === m && (
              <Text style={tw`text-[13px] font-bold text-[#7B1113] capitalize`}>
                {m === "pedestrian" ? "Walk" : m}
              </Text>
            )}
          </TouchableOpacity>
        ))}
      </View>

      {/* TOGGLE PARKIR/DROP-OFF (HANYA JIKA BUKAN PEDESTRIAN) */}
      {selectedMode !== "pedestrian" && (
        <View style={tw`mb-4`}>

          <View style={tw`flex-row gap-3`}>
            <TouchableOpacity
              style={[
                tw`flex-1 py-3 rounded-2xl border flex-row justify-center items-center gap-2`,
                !requireParking
                  ? tw`bg-[#7B1113]/5 border-[#7B1113]`
                  : tw`bg-white border-[#E5E5E5]`,
              ]}
              onPress={() => setRequireParking(false)}
              activeOpacity={0.7}
            >
              <Ionicons
                name="location"
                size={16}
                color={!requireParking ? "#7B1113" : "#999"}
              />
              <Text
                style={tw`font-bold text-[13px] ${
                  !requireParking ? "text-[#7B1113]" : "text-[#999]"
                }`}
              >
                Drop-off
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={[
                tw`flex-1 py-3 rounded-2xl border flex-row justify-center items-center gap-2`,
                requireParking
                  ? tw`bg-[#7B1113]/5 border-[#7B1113]`
                  : tw`bg-white border-[#E5E5E5]`,
              ]}
              onPress={() => setRequireParking(true)}
              activeOpacity={0.7}
            >
              <Ionicons
                name="car"
                size={16}
                color={requireParking ? "#7B1113" : "#999"}
              />
              <Text
                style={tw`font-bold text-[13px] ${
                  requireParking ? "text-[#7B1113]" : "text-[#999]"
                }`}
              >
                Search Parking
              </Text>
            </TouchableOpacity>
          </View>
        </View>
      )}

      {/* TIMELINE LOKASI VERTIKAL (LEBIH RAPI) */}
      <View style={tw`bg-[#F9F9F9] rounded-2xl p-4 mb-5 border border-[#F0F0F0]`}>
        <View style={tw`flex-row items-center`}>
          <Ionicons name="location-outline" size={14} color="#888" style={tw`mr-2 ml-[-1px]`} />
          <Text style={tw`text-[14px] text-[#333] font-medium flex-1`} numberOfLines={1}>
            {/* --- 3. AMBIL LANGSUNG DARI fromNode.name (Sesuai Java MainNode) --- */}
            {fromNode ? fromNode.name : "Lokasi Awal"}
          </Text>
        </View>
        
        <View style={tw`w-[2px] h-4 bg-[#E0E0E0] ml-[5px] my-1`} />
        
        <View style={tw`flex-row items-center`}>
          <Ionicons name="location" size={14} color="#7B1113" style={tw`mr-2 ml-[-1px]`} />
          <Text style={tw`text-[14px] text-[#333] font-medium flex-1`} numberOfLines={1}>
             {/* --- 3. AMBIL LANGSUNG DARI toNode.name (Sesuai Java MainNode) --- */}
            {toNode ? toNode.name : "Tujuan"}
          </Text>
        </View>
      </View>

      {/* TOMBOL START */}
      <TouchableOpacity
        style={[
          tw`bg-[#7B1113] rounded-2xl h-[54px] flex-row items-center justify-center gap-2.5`,
          !routeResult && tw`bg-[#CCC] opacity-70`,
        ]}
        activeOpacity={0.85}
        onPress={onStartNavigation}
        disabled={!routeResult}
      >
        <Ionicons name="navigate" size={18} color="#FFF" />
        <Text style={tw`text-white text-[15px] font-bold tracking-wide`}>
          {routeResult ? "Start Navigation" : "Pilih Rute Terlebih Dahulu"}
        </Text>
      </TouchableOpacity>
    </Animated.View>
  );
}