import { Ionicons } from "@expo/vector-icons";
import * as Location from "expo-location";
import React, { useCallback, useEffect, useRef, useState } from "react";
import {
  Animated,
  Dimensions,
  Easing,
  Modal,
  Platform,
  Text,
  TouchableOpacity,
  View,
  Alert
} from "react-native";
import tw from "twrnc";
import TelkomMapView from "@/components/MapView";
import NavigationOverlay from "@/components/NavigationOverlay";
import { Colors } from "@/constants/Colors";
import { useTabBar } from "./_layout";
import NavigationBar from "@/components/NavigationBar";
import RouteDetailsSheet from "@/components/RouteDetailSheet";
import FloatingSearchPanel from "@/components/FloatingSearchPanel";
import LocationPickerModal from "@/components/LocationPickerModal";
import GlassmorphismCard from "@/components/GlassmorphismCard";
import { API_URL } from "../../config/api";
import AsyncStorage from '@react-native-async-storage/async-storage';

const { height } = Dimensions.get("window");

// ✅ Helper: mapping mode frontend → format yang diterima backend
const getModeForAPI = (mode: TravelMode): string => {
  switch (mode) {
    case "walk":       return "PEDESTRIAN";
    case "motorcycle": return "MOTORCYCLE";
    case "car":        return "CAR";
    default:           return "PEDESTRIAN";
  }
};

export default function HomeScreen() {
  const { setIsTabBarVisible } = useTabBar();
  const [fromNode, setFromNode] = useState<GeoNode | null>(null);
  const [toNode, setToNode] = useState<GeoNode | null>(null);
  const [selectedMode, setSelectedMode] = useState<TravelMode>("pedestrian");
  const [requireParking, setRequireParking] = useState(false);
  const [routeResult, setRouteResult] = useState<PathResult | null>(null);
  const [isNavigating, setIsNavigating] = useState(false);
  const [showFromPicker, setShowFromPicker] = useState(false);
  const [showToPicker, setShowToPicker] = useState(false);
  const [nodes, setNodes] = useState<GeoNode[]>([]);
  const [exactStartCoord, setExactStartCoord] = useState<[number, number] | null>(null);
  const [isLocating, setIsLocating] = useState(false);
  const [mapType, setMapType] = useState<"standard" | "satellite" | "hybrid" | "terrain">("standard");
  const [recenterSignal, setRecenterSignal] = useState(0);
  // ✅ State loading untuk feedback saat menghitung rute
  const [isCalculatingRoute, setIsCalculatingRoute] = useState(false);

  const [customAlert, setCustomAlert] = useState({
    visible: false,
    title: "",
    message: "",
    icon: "information-circle" as keyof typeof Ionicons.glyphMap,
  });

  const slideAnim = useRef(new Animated.Value(400)).current;
  const fadeAnim = useRef(new Animated.Value(0)).current;

  // ==========================================
  // TOGGLE MAP TYPE & RECENTER
  // ==========================================
  const toggleMapType = () => {
    setMapType((prev) => {
      if (prev === "standard") return "hybrid";
      if (prev === "hybrid") return "terrain";
      if (prev === "terrain") return "satellite";
      return "standard";
    });
  };

  const triggerRecenter = () => setRecenterSignal((prev) => prev + 1);

  // ==========================================
  // ✅ KALKULASI RUTE — memanggil backend API
  // ==========================================
  const handleCalculateRoute = useCallback(async (
    mode: TravelMode,
    parking: boolean
  ) => {
    // Pastikan dua titik sudah dipilih
    if (!fromNode || !toNode) return;

    setIsCalculatingRoute(true);
    try {
      const apiMode = getModeForAPI(mode);
      const url =
        `${API_URL}/routes/find` +
        `?startNodeId=${encodeURIComponent(fromNode.id)}` +
        `&targetNodeId=${encodeURIComponent(toNode.id)}` +
        `&mode=${apiMode}` +
        `&requireParking=${parking}`;

      const response = await fetch(url);

      if (!response.ok) {
        // Server merespons tapi rute tidak ditemukan (404) atau error
        const errorText = await response.text();
        console.warn("⚠️ Backend error:", response.status, errorText);
        setRouteResult(null);
        return;
      }

      const data: PathResult = await response.json();

      // Cek apakah rute kendaraan bermotor terlalu dekat (parkir sama)
      if (mode !== "walk" && data.edges?.length === 0) {
        setCustomAlert({
          visible: true,
          title: "Terlalu Dekat!",
          message:
            "Titik parkir asal dan tujuan Anda berada di area yang sama. " +
            "Sistem merekomendasikan untuk berjalan kaki langsung ke tujuan.",
          icon: "walk-outline",
        });
      }

      setRouteResult(data);
    } catch (error) {
      console.error("❌ Gagal menghitung rute:", error);
      setCustomAlert({
        visible: true,
        title: "Gagal Terhubung",
        message: "Tidak dapat menghitung rute. Periksa koneksi Anda dan coba lagi.",
        icon: "wifi-outline",
      });
      setRouteResult(null);
    } finally {
      setIsCalculatingRoute(false);
    }
  }, [fromNode, toNode]);

  // ==========================================
  // ✅ AUTO-HITUNG saat fromNode / toNode berubah
  // ==========================================
  useEffect(() => {
    if (fromNode && toNode) {
      handleCalculateRoute(selectedMode, requireParking);
    } else {
      // Salah satu titik dihapus → reset rute
      setRouteResult(null);
    }
  }, [fromNode, toNode]);

  const handleLocateMe = async () => {
    setIsLocating(true);
    try {
      // 1. Minta Izin GPS Dulu!
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== "granted") {
        setCustomAlert({
          visible: true,
          title: "Izin Ditolak",
          message: "Aplikasi butuh akses lokasi untuk menemukan posisi Anda.",
          icon: "warning",
        });
        return; // Hentikan fungsi jika ditolak
      }

      // 2. Baru ambil lokasi
      const location = await Location.getCurrentPositionAsync({});
      const { latitude, longitude } = location.coords;
      setExactStartCoord([longitude, latitude]);

      const response = await fetch(
        `${API_URL}/nodes/nearest?lat=${latitude}&lon=${longitude}&mode=${selectedMode}`
      );
      const nearestNode = await response.json();
      if (nearestNode) setFromNode(nearestNode);
    } catch (error) {
      console.error("❌ Gagal mendapatkan lokasi:", error);
    } finally {
      setIsLocating(false);
    }
  };

  // ==========================================
  // LOAD SEMUA NODES untuk picker
  // ==========================================
  useEffect(() => {
    const loadNodes = async () => {
      try {
        const response = await fetch(`${API_URL}/map/locations`);
        const data = await response.json();
        setNodes(data);
      } catch (err) {
        console.error("❌ Gagal memuat lokasi dari server:", err);
      }
    };
    loadNodes();
  }, []);

  // ==========================================
  // ANIMASI BOTTOM SHEET
  // ==========================================
  useEffect(() => {
    const hasRoute = routeResult !== null && routeResult !== undefined;

    if (hasRoute && !isNavigating) {
      setIsTabBarVisible(false);
      Animated.parallel([
        Animated.timing(slideAnim, {
          toValue: 0,
          duration: 400,
          easing: Easing.out(Easing.cubic),
          useNativeDriver: true,
        }),
        Animated.timing(fadeAnim, {
          toValue: 1,
          duration: 300,
          useNativeDriver: true,
        }),
      ]).start();
    } else {
      if (!isNavigating) setIsTabBarVisible(true);
      Animated.parallel([
        Animated.timing(slideAnim, {
          toValue: 400,
          duration: 300,
          easing: Easing.in(Easing.cubic),
          useNativeDriver: true,
        }),
        Animated.timing(fadeAnim, {
          toValue: 0,
          duration: 200,
          useNativeDriver: true,
        }),
      ]).start();
    }
  }, [routeResult, isNavigating, setIsTabBarVisible]);

  // ==========================================
  // CALLBACK DARI TelkomMapView (jika map juga menghitung)
  // ==========================================
  const handleRouteCalculated = useCallback((result: PathResult | null) => {
    // Hanya terima hasil dari TelkomMapView jika kita belum punya hasil sendiri
    // (Ini mencegah konflik jika TelkomMapView juga melakukan kalkulasi internal)
    if (result !== null) {
      setRouteResult(result);
    }
  }, []);

  // ==========================================
  // NAVIGASI
  // ==========================================
  const handleStartNavigation = useCallback(() => {
    if (!routeResult) return;
    setIsNavigating(true);
    setIsTabBarVisible(false);
    Animated.parallel([
      Animated.timing(slideAnim, {
        toValue: 400,
        duration: 300,
        easing: Easing.in(Easing.cubic),
        useNativeDriver: true,
      }),
      Animated.timing(fadeAnim, {
        toValue: 0,
        duration: 200,
        useNativeDriver: true,
      }),
    ]).start();
  }, [routeResult, setIsTabBarVisible]);

  // ==========================================
  // NAVIGASI & SIMPAN HISTORY
  // ==========================================
  const handleStopNavigation = useCallback(async () => {
    // 1. Simpan riwayat ke Backend terlebih dahulu
    if (routeResult && fromNode && toNode) {
      try {
        const userId = await AsyncStorage.getItem('userId'); 
        
        if (!userId) {
            console.error("User ID tidak ditemukan, apakah Anda sudah login?");
            return;
        }
        
        const payload = {
          user: { userId: userId }, // Spring Boot History mengharapkan objek user
          startPointName: fromNode.name || "Lokasi Awal",
          endPointName: toNode.name || "Tujuan",
          distance: routeResult.totalDistance,
          duration: routeResult.totalDuration,
          transportMode: getModeForAPI(selectedMode), // "PEDESTRIAN", "CAR", "MOTORCYCLE"
        };

        const response = await fetch(`${API_URL}/history/save`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
        });

        if (response.ok) {
          console.log("✅ Riwayat perjalanan berhasil disimpan!");
        } else {
          console.warn("⚠️ Gagal menyimpan riwayat:", await response.text());
        }
      } catch (error) {
        console.error("❌ Error saat menyimpan history:", error);
      }
    }

    // 2. Hentikan navigasi dan bersihkan rute
    setIsNavigating(false);
    setIsTabBarVisible(true);
    handleClearRoute();
  }, [routeResult, fromNode, toNode, selectedMode, setIsTabBarVisible, handleClearRoute]);

  const handleArrive = useCallback(() => {
    Alert.alert(
      "🎉 Sampai Tujuan",
      "Selamat! Anda telah tiba di lokasi tujuan.",
      [
        { 
          text: "Selesai", 
          onPress: () => handleStopNavigation() // Otomatis mengakhiri rute dan simpan history
        }
      ]
    );
  }, [handleStopNavigation]);

  const handleClearRoute = useCallback(() => {
    setFromNode(null);
    setToNode(null);
    setExactStartCoord(null);
    setRouteResult(null);
    setIsNavigating(false);
  }, []);

  return (
    <View style={tw`flex-1`}>
      {/* --- PETA UTAMA --- */}
      <TelkomMapView
        selectedMode={selectedMode}
        fromNodeId={fromNode?.id || null}
        toNodeId={toNode?.id || null}
        exactStartCoord={exactStartCoord}
        isNavigating={isNavigating}
        mapType={mapType}
        recenterSignal={recenterSignal}
        routeResult={routeResult} 
        nodes={nodes}

      />

      {/* --- OVERLAY NAVIGASI --- */}
      <NavigationOverlay
        isVisible={isNavigating}
        pathResult={routeResult}
        mode={selectedMode}
        onStopNavigation={handleStopNavigation}
        onArrive={handleArrive}
      />

      {/* --- PANEL PENCARIAN MELAYANG --- */}
      {!isNavigating && (
        <FloatingSearchPanel
          fromNode={fromNode}
          toNode={toNode}
          onPressFrom={() => setShowFromPicker(true)}
          onPressTo={() => setShowToPicker(true)}
          mapType={mapType}
          onToggleMapType={toggleMapType}
          onRecenter={triggerRecenter}
        />
      )}

      {/* --- MODAL PILIH LOKASI AWAL --- */}
      <LocationPickerModal
        visible={showFromPicker}
        onSelect={(node) => {
          setFromNode(node);
          setExactStartCoord(null);
        }}
        onClose={() => setShowFromPicker(false)}
        title="Pilih Lokasi Awal"
        isStartLocation={true}
        nodes={nodes}
        isLocating={isLocating}
        handleLocateMe={handleLocateMe}
      />

      {/* --- MODAL PILIH TUJUAN --- */}
      <LocationPickerModal
        visible={showToPicker}
        onSelect={setToNode}
        onClose={() => setShowToPicker(false)}
        title="Pilih Tujuan"
        isStartLocation={false}
        nodes={nodes}
        isLocating={isLocating}
        handleLocateMe={handleLocateMe}
      />

      {/* --- BOTTOM SHEET INFO RUTE --- */}
      <RouteDetailsSheet
        slideAnim={slideAnim}
        fadeAnim={fadeAnim}
        routeResult={routeResult}
        isCalculatingRoute={isCalculatingRoute}
        selectedMode={selectedMode}
        setSelectedMode={setSelectedMode}
        requireParking={requireParking}
        setRequireParking={setRequireParking}
        fromNode={fromNode}
        toNode={toNode}
        onStartNavigation={handleStartNavigation}
        onClearRoute={handleClearRoute}
        // ✅ Sekarang prop ini tersedia — dipanggil saat mode/parkir berubah
        onCalculateRoute={handleCalculateRoute}
      />

      {/* --- CUSTOM ALERT MODAL --- */}
      <Modal visible={customAlert.visible} transparent animationType="fade">
        <View style={tw`flex-1 bg-black/65 justify-center items-center p-6`}>
          <View style={tw`w-full max-w-[340px]`}>
            <GlassmorphismCard dark style={tw`p-6 items-center rounded-[28px]`}>
              <View
                style={[
                  tw`w-16 h-16 rounded-full bg-white/90 justify-center items-center mb-4`,
                  Platform.select({
                    ios: {
                      shadowColor: "#000",
                      shadowOffset: { width: 0, height: 4 },
                      shadowOpacity: 0.2,
                      shadowRadius: 8,
                    },
                    android: { elevation: 6 },
                  }),
                ]}
              >
                <Ionicons
                  name={customAlert.icon}
                  size={36}
                  color={Colors.dark.maroon.primary}
                />
              </View>
              <Text style={tw`text-xl font-extrabold text-white mb-2 text-center`}>
                {customAlert.title}
              </Text>
              <Text style={tw`text-sm text-white/75 text-center mb-6 leading-5`}>
                {customAlert.message}
              </Text>
              <TouchableOpacity
                style={tw`bg-[#7B1113] py-3.5 px-8 rounded-2xl w-full items-center`}
                activeOpacity={0.8}
                onPress={() => setCustomAlert((prev) => ({ ...prev, visible: false }))}
              >
                <Text style={tw`text-white text-[15px] font-bold tracking-wide`}>
                  Tutup
                </Text>
              </TouchableOpacity>
            </GlassmorphismCard>
          </View>
        </View>
      </Modal>

      {/* --- NAVIGATION BAR BAWAH --- */}
      {!routeResult && !isNavigating && <NavigationBar />}
    </View>
  );
}