import React, { useEffect, useState } from "react";
import { View, ActivityIndicator } from "react-native";
import { router } from "expo-router";
import AsyncStorage from "@react-native-async-storage/async-storage";

export default function Index() {
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // Fungsi untuk mengecek memori HP
    const checkAuthStatus = async () => {
      try {
        // Ambil userId dari AsyncStorage
        const userId = await AsyncStorage.getItem("userId");

        if (userId !== null) {
          // JIKA ADA: Berarti sudah login, langsung ke halaman utama
          router.replace("/(tabs)");
        } else {
          // JIKA KOSONG: Berarti belum login, arahkan ke halaman login
          router.replace("/login");
        }
      } catch (error) {
        console.error("Gagal mengecek status login:", error);
        router.replace("/login");
      } finally {
        setIsChecking(false);
      }
    };

    checkAuthStatus();
  }, []);

  // Tampilkan loading saat aplikasi sedang mencari data di memori
  if (isChecking) {
    return (
      <View style={{ flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: "white" }}>
        <ActivityIndicator size="large" color="#8b0000" />
      </View>
    );
  }

  return null;
}