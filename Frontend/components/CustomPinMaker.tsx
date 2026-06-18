import { Ionicons } from "@expo/vector-icons";
import React, { useEffect, useState } from "react";
import { View } from "react-native";
import { Marker } from "react-native-maps";
import tw from "twrnc";
// Pastikan path import tipe datanya sesuai dengan tempat Anda menyimpan types.ts
import { GeoNode } from "@/config/types"; 

interface CustomPinMarkerProps {
  node: GeoNode | any;
}

const CustomPinMarker = React.memo(({ node }: CustomPinMarkerProps) => {
  const [track, setTrack] = useState(true);

  useEffect(() => {
    // Pin hanya "bangun" sesaat untuk merender desain, lalu tidur
    const timer = setTimeout(() => setTrack(false), 800);
    return () => clearTimeout(timer);
  }, []);

  // --- LOGIKA FILTERING (SUDAH DIPERBAIKI: Hapus .properties) ---
  const category = (node.category || "").toLowerCase();

  let iconName: any = "location";
  let iconColor = "#7B1113"; // Default: Tel-U Maroon

  if (category.includes("dorm") || category.includes("asrama")) {
    iconName = "bed";
    iconColor = "#4A90E2"; // Soft Blue
  } else if (category.includes("food") || category.includes("canteen") || category.includes("mart")) {
    iconName = "restaurant";
    iconColor = "#F5B041"; // Soft Amber
  } else if (category.includes("park") || category.includes("parking")) {
    iconName = "car";
    iconColor = "#95A5A6"; // Blueish Gray
  } else if (category.includes("sport") || category.includes("fitness")) {
    iconName = "football";
    iconColor = "#58D68D"; // Soft Green
  } else if (category.includes("worship") || category.includes("mosque")) {
    iconName = "moon";
    iconColor = "#48C9B0"; // Soft Teal
  } else if (category.includes("building") || category.includes("faculty") || category.includes("education")) {
    iconName = "business";
    iconColor = "#AF7AC5"; // Soft Purple
  } else if (category.includes("animal") || category.includes("hewan")) {
    iconName = "paw"; // Ikon Jejak Hewan
    iconColor = "#D35400"; // Orange Kecoklatan (Terakota)
  } else if (category.includes("lake") || category.includes("danau")) {
    iconName = "water"; // Ikon Tetesan Air
    iconColor = "#3498DB"; // Biru Air Bersih
  } else if (category.includes("gate") || category.includes("gerbang")) {
    iconName = "enter"; // Ikon Masuk Pintu
    iconColor = "#34495E"; // Navy / Biru Sangat Gelap
  } else if (category.includes("field") || category.includes("lapangan") || category.includes("taman")) {
    iconName = "leaf"; // Ikon Daun Alam
    iconColor = "#2ECC71"; // Hijau Daun Terang
  } else if (category.includes("medic") || category.includes("kesehatan") || category.includes("clinic")) {
    iconName = "medkit"; // Ikon Kotak P3K
    iconColor = "#E74C3C"; // Merah Medis / Crimson
  } else if (category.includes("service") || category.includes("layanan")) {
    iconName = "information-circle"; 
    iconColor = "#8E44AD"; // Ungu Pekat
  } else if (category.includes("room") || category.includes("ruang")) {
    iconName = "grid"; 
    iconColor = "#F1C40F"; // Kuning / Emas
  }

  return (
    <Marker
      coordinate={{
        latitude: node.coordinates[1],
        longitude: node.coordinates[0],
      }}
      // SUDAH DIPERBAIKI: Hapus .properties.building_name
      title={node.name || "Lokasi"} 
      description={category ? category.toUpperCase() : undefined}
      anchor={{ x: 0.37, y: 0.95 }}
      tracksViewChanges={track}
    >
      <View style={tw`items-center justify-center`}>
        {/* Balon Ikon */}
        <View
          style={[
            tw`w-6 h-6 rounded-full justify-center items-center border-[1.5px] border-white z-10`,
            { backgroundColor: iconColor },
          ]}
        >
          <Ionicons name={iconName} size={12} color="#FFFFFF" />
        </View>
        
        {/* Segitiga Ekor */}
        <View
          style={{
            width: 0,
            height: 0,
            borderLeftWidth: 5,
            borderRightWidth: 5,
            borderTopWidth: 8,
            borderLeftColor: "transparent",
            borderRightColor: "transparent",
            borderTopColor: iconColor,
            marginTop: -2,
          }}
        />
      </View>
    </Marker>
  );
});

export default CustomPinMarker;