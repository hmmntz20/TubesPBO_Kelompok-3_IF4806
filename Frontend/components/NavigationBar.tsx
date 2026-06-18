import React from "react";
import { View, TouchableOpacity, Text, Dimensions, Platform } from "react-native";
import { Ionicons, Feather } from "@expo/vector-icons";
import AntDesign from '@expo/vector-icons/AntDesign';
import { router, usePathname } from "expo-router";
import Svg, { Path } from "react-native-svg";
import tw from "twrnc";

const { width } = Dimensions.get("window");
const MAROON = "#8b0000"; // Sesuai warna aksen di HistoryScreen

export default function NavigationBar() {
  const pathname = usePathname();
  const active = pathname === "/history" ? "history" : pathname === "/settings" ? "settings" : "home";

  const W = width - 32;
  const H = 74;
  const R = H / 2;
  const cx = W / 2;
  const dipW = 100;
  const dipH = 40;

  const pathString = `
    M ${R} 0
    L ${cx - dipW} 0
    C ${cx - 25} 0, ${cx - 30} ${dipH}, ${cx} ${dipH}
    C ${cx + 30} ${dipH}, ${cx + 25} 0, ${cx + dipW} 0
    L ${W - R} 0
    A ${R} ${R} 0 0 1 ${W - R} ${H}
    L ${R} ${H}
    A ${R} ${R} 0 0 1 ${R} 0
    Z
  `;

  return (
    <View
      pointerEvents="box-none"
      style={tw`absolute bottom-[${Platform.OS === 'ios' ? '25px' : '15px'}] w-full items-center`}
    >
      {/* --- BACKGROUND DENGAN SHADOW KONSISTEN --- */}
      <View style={tw`absolute bottom-0 shadow-lg elevation-10`}>
        <Svg width={W} height={H} viewBox={`0 0 ${W} ${H}`}>
          <Path
            d={pathString}
            fill="#ffffff"
            stroke="#e5e7eb"
            strokeWidth="1"
          />
        </Svg>
      </View>

      {/* --- FLOATING HOME BUTTON --- */}
      <TouchableOpacity
        onPress={() => router.push("/")}
        activeOpacity={0.9}
        style={tw`absolute -top-6 items-center justify-center z-10 shadow-md elevation-6`}
      >
        <View style={tw`w-[54px] h-[54px] rounded-full border border-gray-100 bg-white justify-center items-center`}>
          <Feather name="home" size={22} color={MAROON} />
        </View>
      </TouchableOpacity>

      {/* --- NAVIGATION ITEMS --- */}
      <View
        pointerEvents="box-none"
        style={tw`w-[${W}px] h-[${H}px] flex-row justify-between items-center px-2.5`}
      >
        {/* History */}
        <TouchableOpacity
          style={tw`flex-1 items-center pt-2`}
          onPress={() => router.push("/history")}
        >
          <Ionicons
            name="git-branch-outline"
            size={22}
            color={active === "history" ? MAROON : "#9ca3af"}
          />
          <Text style={tw`mt-1 text-xs font-${active === "history" ? "bold" : "semibold"} text-${active === "history" ? "[#8b0000]" : "gray-400"}`}>
            History
          </Text>
        </TouchableOpacity>

        {/* Spacer for Home Button */}
        <View style={tw`w-[100px] items-center pt-2`}>
          <Ionicons name="home" size={22} style={tw`opacity-0`} />
          <Text style={tw`mt-1 text-xs font-${active === "home" ? "bold" : "semibold"} text-${active === "home" ? "[#8b0000]" : "gray-400"}`}>
            Home
          </Text>
        </View>

        {/* Settings */}
        <TouchableOpacity
          style={tw`flex-1 items-center pt-2`}
          onPress={() => router.push("/profile")}
        >
          <AntDesign
            name="meh"
            size={22}
            color={active === "settings" ? MAROON : "#9ca3af"}
          />
          <Text style={tw`mt-1 text-xs font-${active === "settings" ? "bold" : "semibold"} text-${active === "settings" ? "[#8b0000]" : "gray-400"}`}>
            Profile
          </Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}