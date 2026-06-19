import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView, // SafeAreaView dihapus dari sini
  Image,
  Alert,
  ActivityIndicator // <-- Tambahkan untuk animasi loading
} from "react-native";
import { SafeAreaView } from 'react-native-safe-area-context'; // <-- Import SafeAreaView yang benar
import TelU from "@/assets/images/telu.png";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import tw from "twrnc";
import { API_URL } from "@/config/api";

const MAROON = "#8b0000";

export default function RegisterScreen() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  
  // <-- State untuk loading
  const [isLoading, setIsLoading] = useState(false); 

  const handleRegister = async () => {
    if (!username || !email || !password) {
      Alert.alert("Perhatian", "Mohon isi semua kolom!");
      return;
    }

    setIsLoading(true); // <-- Aktifkan loading saat tombol ditekan

    try {
      const response = await fetch(`${API_URL}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          username: username,
          email: email,
          password: password,
          loginProvider: "LOCAL"
        }),
      });

      const responseText = await response.text();

      if (response.ok) {
        Alert.alert("Sukses", "Akun berhasil dibuat! Silakan masuk.");
        router.replace("/login");
      } else {
        Alert.alert("Gagal", responseText);
      }
    } catch (error) {
      Alert.alert("Error", "Gagal menghubungi server backend.");
      console.error(error);
    } finally {
      setIsLoading(false); // <-- Matikan loading apapun yang terjadi (sukses/gagal)
    }
  };

  return (
    <SafeAreaView style={tw`flex-1 bg-white`}>
      
      {/* OVERLAY LOADING */}
      {isLoading && (
        <View style={[tw`absolute inset-0 justify-center items-center`, { backgroundColor: 'rgba(255, 255, 255, 0.7)', zIndex: 50 }]}>
          <ActivityIndicator size="large" color={MAROON} />
          <Text style={[tw`mt-4 font-bold`, { color: MAROON }]}>Mendaftarkan akun...</Text>
        </View>
      )}

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={tw`flex-grow justify-center px-8 py-10`}
      >
        {/* LOGO */}
        <View style={tw`items-center mb-16`}>
          <View>
            <Image
                source={TelU}
                style={{
                    width: 60,
                    height: 60,
                    resizeMode: "contain",
                }}
            />
          </View>
        </View>

        {/* HEADER */}
        <View style={tw`mb-10`}>
          <Text style={[tw`text-4xl font-bold`, { color: "#111827" }]}>
            Create Account
          </Text>
          <Text style={tw`text-gray-500 mt-2 text-base`}>
            Join and start exploring
          </Text>
        </View>

        {/* USERNAME */}
        <View style={tw`mb-5`}>
          <Text style={[tw`mb-2 font-medium`, { color: "#374151" }]}>
            Username
          </Text>
          <TextInput
            value={username}
            onChangeText={setUsername}
            placeholder="Your username"
            placeholderTextColor="#9ca3af"
            autoCapitalize="none"
            editable={!isLoading} // <-- Kunci input saat loading
            style={[
              tw`h-14 px-5 rounded-2xl border`,
              { borderColor: "#e5e7eb", backgroundColor: "#fafafa" },
            ]}
          />
        </View>

        {/* EMAIL */}
        <View style={tw`mb-5`}>
          <Text style={[tw`mb-2 font-medium`, { color: "#374151" }]}>
            Email
          </Text>
          <TextInput
            value={email}
            onChangeText={setEmail}
            placeholder="your@email.com"
            placeholderTextColor="#9ca3af"
            keyboardType="email-address"
            autoCapitalize="none"
            editable={!isLoading} // <-- Kunci input saat loading
            style={[
              tw`h-14 px-5 rounded-2xl border`,
              { borderColor: "#e5e7eb", backgroundColor: "#fafafa" },
            ]}
          />
        </View>

        {/* PASSWORD */}
        <View style={tw`mb-6`}>
          <Text style={[tw`mb-2 font-medium`, { color: "#374151" }]}>
            Password
          </Text>
          <View
            style={[
              tw`h-14 rounded-2xl border flex-row items-center px-4`,
              { borderColor: "#e5e7eb", backgroundColor: "#fafafa" },
            ]}
          >
            <TextInput
              value={password}
              onChangeText={setPassword}
              placeholder="••••••••"
              placeholderTextColor="#9ca3af"
              secureTextEntry={!showPassword}
              editable={!isLoading} // <-- Kunci input saat loading
              style={tw`flex-1`}
            />
            <TouchableOpacity 
              onPress={() => setShowPassword(!showPassword)}
              disabled={isLoading} // <-- Kunci tombol mata saat loading
            >
              <Ionicons
                name={showPassword ? "eye-off-outline" : "eye-outline"}
                size={22}
                color="#6b7280"
              />
            </TouchableOpacity>
          </View>
        </View>

        {/* REGISTER BUTTON */}
        <TouchableOpacity
          activeOpacity={0.85}
          onPress={handleRegister}
          disabled={isLoading} // <-- Cegah klik dobel saat memproses
          style={[
            tw`h-14 rounded-2xl items-center justify-center`,
            { backgroundColor: MAROON },
          ]}
        >
          <Text style={tw`text-white font-bold text-base`}>
            Create Account
          </Text>
        </TouchableOpacity>

        {/* LOGIN LINK */}
        <View style={tw`flex-row justify-center mt-10`}>
          <Text style={tw`text-gray-500`}>
            Already have an account?
          </Text>
          <TouchableOpacity 
            onPress={() => router.replace("/login")}
            disabled={isLoading} // <-- Cegah pindah halaman saat loading
          >
            <Text style={[tw`ml-2 font-bold`, { color: MAROON }]}>
              Sign In
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}