import { Ionicons } from "@expo/vector-icons";
import React, { useState } from "react";
import { LayoutAnimation, Text, TouchableOpacity, View } from "react-native";
import tw from "twrnc";
import { GeoNode } from "@/config/types"; // Sesuaikan jika path types.ts Anda berbeda

interface FloatingSearchPanelProps {
  fromNode: GeoNode | null;
  toNode: GeoNode | null;
  onPressFrom: () => void;
  onPressTo: () => void;
  mapType: "standard" | "satellite" | "hybrid" | "terrain";
  onToggleMapType: () => void;
  onRecenter: () => void;
}

export default function FloatingSearchPanel({
  fromNode,
  toNode,
  onPressFrom,
  onPressTo,
  mapType,
  onToggleMapType,
  onRecenter,
}: FloatingSearchPanelProps) {
  const [isMinimized, setIsMinimized] = useState(false);

  const toggleMinimize = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setIsMinimized(!isMinimized);
  };

  return (
    <View style={tw`absolute top-[50px] left-4 right-4 z-10`}>
      {/* --- KOTAK PENCARIAN UTAMA --- */}
      <View 
        style={[
          tw`bg-white rounded-3xl overflow-hidden border border-gray-100`,
          { shadowColor: "#000", shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.1, shadowRadius: 8, elevation: 5 }
        ]}
      >
        {!isMinimized ? (
          <View style={tw`p-4 relative`}>
            {/* Tombol Minimize */}
            <TouchableOpacity
              style={tw`absolute top-4 right-4 w-8 h-8 bg-gray-100 rounded-full justify-center items-center z-20`}
              onPress={toggleMinimize}
              activeOpacity={0.7}
            >
              <Ionicons name="chevron-up" size={18} color="#6B7280" />
            </TouchableOpacity>

            <View style={tw`flex-row mt-1`}>
              {/* Kolom Ikon & Garis Penghubung */}
              <View style={tw`items-center mr-4 py-2`}>
                <Ionicons name="location-outline" size={24} color="#888" />
                <View style={tw`w-[2px] flex-1 bg-gray-200 my-1 rounded-full`} />
                <Ionicons name="location" size={24} color="#7B1113" />
              </View>

              {/* Kolom Teks Input */}
              <View style={tw`flex-1 pr-8 justify-between`}>
                {/* Lokasi Asal */}
                <TouchableOpacity style={tw`py-1`} onPress={onPressFrom} activeOpacity={0.6}>
                  <Text style={tw`text-xs text-gray-400 font-bold tracking-wider mb-1`}>
                    Your location
                  </Text>
                  <Text style={tw`text-base ${fromNode ? "font-semibold text-gray-900" : "font-normal text-gray-400"}`} numberOfLines={1}>
                    {/* SUDAH DIPERBAIKI */}
                    {fromNode ? fromNode.name : "Select starting location..."}
                  </Text>
                </TouchableOpacity>

                {/* Garis Pemisah Horizontal */}
                <View style={tw`h-[1px] bg-gray-100 my-3`} />

                {/* Lokasi Tujuan */}
                <TouchableOpacity style={tw`py-1`} onPress={onPressTo} activeOpacity={0.6}>
                  <Text style={tw`text-xs text-[#7B1113] font-bold tracking-wider mb-1`}>
                    Routing to
                  </Text>
                  <Text style={tw`text-base ${toNode ? "font-semibold text-gray-900" : "font-normal text-gray-400"}`} numberOfLines={1}>
                    {/* SUDAH DIPERBAIKI */}
                    {toNode ? toNode.name : "Select destination location..."}
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        ) : (
          /* Mode Minimized (Pil / Kapsul) */
          <TouchableOpacity style={tw`flex-row items-center p-3 px-4`} onPress={toggleMinimize} activeOpacity={0.7}>
            <View style={tw`mr-3`}>
              <Ionicons name="search" size={20} color="#7B1113" />
            </View>
            <View style={tw`flex-1 flex-row items-center pr-2`}>
              <Text style={tw`text-sm font-medium text-gray-600 flex-1 text-right`} numberOfLines={1}>
                {/* SUDAH DIPERBAIKI */}
                {fromNode ? fromNode.name : "Start"}
              </Text>
              <Ionicons name="arrow-forward" size={16} color="#9CA3AF" style={tw`mx-2`} />
              <Text style={tw`text-sm font-bold text-[#7B1113] flex-1`} numberOfLines={1}>
                {/* SUDAH DIPERBAIKI */}
                {toNode ? toNode.name : "Destination"}
              </Text>
            </View>
            <View style={tw`w-8 h-8 bg-gray-100 rounded-full justify-center items-center`}>
              <Ionicons name="chevron-down" size={18} color="#6B7280" />
            </View>
          </TouchableOpacity>
        )}
      </View>

      {/* --- TOMBOL KONTROL PETA --- */}
      <View style={tw`items-end mt-4 gap-3 pr-1`}>
        <TouchableOpacity
          style={[
            tw`w-[46px] h-[46px] bg-white rounded-full justify-center items-center border border-gray-100`,
            { shadowColor: "#000", shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4, elevation: 3 }
          ]}
          onPress={onToggleMapType}
          activeOpacity={0.7}
        >
          <Ionicons
            name={
              mapType === "standard" ? "map" :
              mapType === "hybrid" ? "layers" :
              mapType === "terrain" ? "trail-sign" : "earth"
            }
            size={20}
            color="#7B1113"
          />
        </TouchableOpacity>

        <TouchableOpacity
          style={[
            tw`w-[46px] h-[46px] bg-white rounded-full justify-center items-center border border-gray-100`,
            { shadowColor: "#000", shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.1, shadowRadius: 4, elevation: 3 }
          ]}
          onPress={onRecenter}
          activeOpacity={0.7}
        >
          <Ionicons name="locate" size={22} color="#7B1113" />
        </TouchableOpacity>
      </View>
    </View>
  );
}