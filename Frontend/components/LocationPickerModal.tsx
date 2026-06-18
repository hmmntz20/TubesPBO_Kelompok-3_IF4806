import { Ionicons } from "@expo/vector-icons";
import React, { useEffect, useState, useMemo } from "react";
import {
  ActivityIndicator,
  Dimensions,
  Modal,
  SectionList,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from "react-native";
import tw from "twrnc";
import { Colors } from "../constants/Colors";
import { GeoNode } from "@/config/types"; // Sesuaikan jika path types.ts Anda berbeda

const { height } = Dimensions.get("window");

// Helper untuk mendeteksi kategori, ikon, dan warna di list
const getCategoryInfo = (categoryStr: string) => {
  const cat = (categoryStr || "").toLowerCase();
  if (cat.includes("dorm") || cat.includes("asrama")) return { label: "Dormitory", icon: "bed", color: "#4A90E2" };
  if (cat.includes("food") || cat.includes("canteen") || cat.includes("mart")) return { label: "Food & Beverage", icon: "restaurant", color: "#F5B041" };
  if (cat.includes("park") || cat.includes("parking")) return { label: "Parking Area", icon: "car", color: "#95A5A6" };
  if (cat.includes("sport") || cat.includes("fitness")) return { label: "Sport", icon: "football", color: "#58D68D" };
  if (cat.includes("worship") || cat.includes("mosque")) return { label: "Worship", icon: "moon", color: "#48C9B0" };
  if (cat.includes("building") || cat.includes("faculty") || cat.includes("education")) return { label: "Building", icon: "business", color: "#AF7AC5" };
  if (cat.includes("animal") || cat.includes("hewan")) return { label: "Animal", icon: "paw", color: "#D35400" };
  if (cat.includes("lake") || cat.includes("danau")) return { label: "Lake", icon: "water", color: "#3498DB" };
  if (cat.includes("gate") || cat.includes("gerbang")) return { label: "Gate", icon: "enter", color: "#34495E" };
  if (cat.includes("field") || cat.includes("lapangan") || cat.includes("taman")) return { label: "Field", icon: "leaf", color: "#2ECC71" };
  if (cat.includes("medic") || cat.includes("kesehatan") || cat.includes("clinic")) return { label: "Medical", icon: "medkit", color: "#E74C3C" };
  if (cat.includes("service") || cat.includes("layanan")) return { label: "Service", icon: "information-circle", color: "#8E44AD" };
  if (cat.includes("room") || cat.includes("ruang")) return { label: "Room", icon: "grid", color: "#F1C40F" };
  
  return { label: "Others", icon: "location", color: "#7B1113" };
};

interface LocationPickerModalProps {
  visible: boolean;
  onSelect: (node: GeoNode) => void;
  onClose: () => void;
  title: string;
  isStartLocation?: boolean;
  nodes: GeoNode[];
  isLocating: boolean;
  handleLocateMe: () => void;
}

export default function LocationPickerModal({
  visible,
  onSelect,
  onClose,
  title,
  isStartLocation = false,
  nodes,
  isLocating,
  handleLocateMe,
}: LocationPickerModalProps) {
  const [searchQuery, setSearchQuery] = useState("");

  // 1. Filter Node Berdasarkan Pencarian (SUDAH DIPERBAIKI)
  const filteredNodes = nodes.filter((node) => {
    const name = node.name || "";
    const category = node.category || "";
    const queryLower = searchQuery.toLowerCase();
    return (
      name.toLowerCase().includes(queryLower) ||
      category.toLowerCase().includes(queryLower)
    );
  });

  // 2. Mengelompokkan Node Menjadi Sections (SUDAH DIPERBAIKI)
  const sections = useMemo(() => {
    const groups: { [key: string]: any[] } = {};
    filteredNodes.forEach((node) => {
      const catInfo = getCategoryInfo(node.category);
      if (!groups[catInfo.label]) {
        groups[catInfo.label] = [];
      }
      groups[catInfo.label].push(node);
    });

    return Object.keys(groups)
      .sort()
      .map((key) => ({
        title: key,
        data: groups[key],
      }));
  }, [filteredNodes]);

  useEffect(() => {
    if (!visible) setSearchQuery("");
  }, [visible]);

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <View style={tw`flex-1 bg-black/50 justify-end`}>
        <View style={[tw`bg-white rounded-t-[28px] p-6 pb-10`, { maxHeight: height * 0.6 }]}>
          <View style={tw`flex-row justify-between items-center mb-4`}>
            <Text style={tw`text-xl font-bold text-[#1A1A1A]`}>{title}</Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color="#333" />
            </TouchableOpacity>
          </View>

          <View style={tw`flex-row items-center gap-3 mb-4`}>
            <View style={tw`flex-1 flex-row items-center bg-[#F5F5F5] rounded-xl px-3 h-11`}>
              <Ionicons name="search" size={20} color="#999" style={tw`mr-2`} />
              <TextInput
                style={tw`flex-1 text-[15px] text-[#333]`}
                placeholder="Search location..."
                value={searchQuery}
                onChangeText={setSearchQuery}
                placeholderTextColor="#999"
              />
              {searchQuery.length > 0 && (
                <TouchableOpacity onPress={() => setSearchQuery("")}>
                  <Ionicons name="close-circle" size={20} color="#CCC" />
                </TouchableOpacity>
              )}
            </View>

            {isStartLocation && (
              <TouchableOpacity
                style={tw`w-11 h-11 rounded-xl bg-[#7B1113]/10 justify-center items-center border border-[#7B1113]/20`}
                onPress={() => {
                  handleLocateMe();
                  onClose();
                }}
                disabled={isLocating}
              >
                {isLocating ? (
                  <ActivityIndicator size="small" color={Colors.dark.maroon.primary} />
                ) : (
                  <Ionicons name="locate" size={20} color={Colors.dark.maroon.primary} />
                )}
              </TouchableOpacity>
            )}
          </View>

          <SectionList
            sections={sections}
            keyExtractor={(item) => item.id}
            renderSectionHeader={({ section: { title } }) => (
              <View style={tw`bg-white py-2 mt-3 border-b border-[#F0F0F0]`}>
                <Text style={tw`text-[10px] font-bold text-[#999] uppercase tracking-wider`}>
                  {title}
                </Text>
              </View>
            )}
            renderItem={({ item }) => {
              // SUDAH DIPERBAIKI
              const catInfo = getCategoryInfo(item.category || "");
              return (
                <TouchableOpacity
                  style={tw`flex-row items-center py-3.5 px-2 border-b border-[#F0F0F0] gap-3`}
                  onPress={() => {
                    onSelect(item);
                    onClose();
                  }}
                >
                  <View style={[tw`w-9 h-9 rounded-full justify-center items-center`, { backgroundColor: catInfo.color + '20' }]}>
                    <Ionicons name={catInfo.icon as any} size={18} color={catInfo.color} />
                  </View>

                  <View style={tw`flex-1`}>
                    <Text style={tw`text-[15px] font-semibold text-[#1A1A1A]`}>
                      {/* SUDAH DIPERBAIKI */}
                      {item.name}
                    </Text>
                  </View>
                  <Ionicons name="chevron-forward" size={18} color="#CCC" />
                </TouchableOpacity>
              );
            }}
            showsVerticalScrollIndicator={false}
            contentContainerStyle={tw`pb-10`}
          />
        </View>
      </View>
    </Modal>
  );
}