import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Image,
  Alert,
  ActivityIndicator
} from "react-native";
import TelU from '@/assets/images/telu.png'
import { Ionicons } from "@expo/vector-icons";
import tw from "twrnc";
import { SafeAreaView } from 'react-native-safe-area-context';
import { router } from "expo-router";
import { API_URL } from "@/config/api";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { GoogleSignin } from '@react-native-google-signin/google-signin';

const MAROON = "#8b0000";

GoogleSignin.configure({
  webClientId: '930696209276-q62ougicnpm6fsgmngvbhgcujdehcnkv.apps.googleusercontent.com',
});

export default function LoginScreen() {

  const [isLoading, setIsLoading] = useState(false);

  const handleNativeGoogleLogin = async () => {
    setIsLoading(true); 
    try {
      await GoogleSignin.hasPlayServices();
      const response = await GoogleSignin.signIn();
      
      // 1. CEK STATUS PEMBATALAN (Pencegah "Tiba-Tiba Login")
      // Jika user menekan tombol back atau tap di luar area popup
      if (response.type === 'cancelled') {
        console.log("User membatalkan proses Google Sign-In.");
        return; // <-- HENTIKAN KODE DI SINI, JANGAN LANJUT KE BACKEND!
      }

      // Jika HP tidak memiliki akun Google sama sekali
      if (response.type === 'noSavedCredentialFound') {
        Alert.alert("Perhatian", "Tidak ada akun Google di perangkat ini.");
        return; 
      }

      let userEmail = '';
      let userName = '';
      let userPhoto = '';

      // 2. Ambil data dengan aman
      if (response.data && response.data.user) {
        userEmail = response.data.user.email;
        userName = response.data.user.name || 'Google User';
        userPhoto = response.data.user.photo || '';
      } else if (response.user) { // (Jalur cadangan)
        userEmail = response.user.email;
        userName = response.user.name || 'Google User';
        userPhoto = response.user.photo || '';
      }

      // 3. Validasi Ekstra (Gembok terakhir agar backend tidak tertipu)
      if (!userEmail || userEmail === 'undefined') {
        Alert.alert("Gagal", "Tidak dapat membaca email dari Google.");
        return;
      }

      console.log("Berhasil mendapat data dari Google:", userEmail);

      const url = `${API_URL}/users/login-google?email=${encodeURIComponent(userEmail)}&username=${encodeURIComponent(userName)}&profilePhoto=${encodeURIComponent(userPhoto)}`;

      const backendResponse = await fetch(url, {
        method: 'POST',
      });

      if (backendResponse.ok) {
        const userData = await backendResponse.json();
        
        await AsyncStorage.setItem('userId', userData.userId); 
        await AsyncStorage.setItem('userName', userData.username);
        await AsyncStorage.setItem('userEmail', userData.email);
        await AsyncStorage.setItem('loginProvider', 'GOOGLE'); // <-- Simpan tipe login
        
        Alert.alert("Sukses", "Login dengan Google Berhasil!");
        router.replace("/(tabs)");
      } else {
        const errorText = await backendResponse.text();
        Alert.alert("Gagal Login Backend", errorText);
      }

    } catch (error: any) {
      // Tangkap jika library mengeluarkan error gaya lama
      console.log('LOG Login dibatalkan atau terjadi error:', error.message || error);
    } finally {
      setIsLoading(false); // Matikan animasi loading
    }
  };

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert("Perhatian", "Email dan password tidak boleh kosong!");
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch(`${API_URL}/users/login?email=${email}&password=${password}`, {
        method: 'POST',
      });

      if (response.ok) {
        // 2. TANGKAP JSON DARI BACKEND
        const userData = await response.json(); 
        
        // 3. SIMPAN ID KE MEMORI HP
        await AsyncStorage.setItem('userId', userData.userId); 
        await AsyncStorage.setItem('userName', userData.username); // Boleh simpan nama juga
        await AsyncStorage.setItem('userEmail', email);
        await AsyncStorage.setItem('loginProvider', 'LOCAL');
        Alert.alert("Sukses", "Login Berhasil!");
        router.replace("/(tabs)");
      } else {
        const errorText = await response.text();
        Alert.alert("Gagal", errorText);
      }
    } catch (error) {
      Alert.alert("Error", "Gagal menghubungi server.");
      console.error(error);
    } finally {
      setIsLoading(false);
    }
  };

  const [showPassword, setShowPassword] =
    useState(false);

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  return (
    <SafeAreaView style={tw`flex-1 bg-white`}>
      {isLoading && (
        <View style={[tw`absolute inset-0 justify-center items-center`, { backgroundColor: 'rgba(255, 255, 255, 0.7)', zIndex: 50 }]}>
          <ActivityIndicator size="large" color={MAROON} />
          <Text style={[tw`mt-4 font-bold`, { color: MAROON }]}>Mohon tunggu...</Text>
        </View>
      )}

      <View
        style={tw`flex-1 px-8 justify-center`}
      >
        {/* LOGO */}
        <View
          style={tw`items-center mb-16`}
        >
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
        <View
          style={tw`mb-10`}
        >
          <Text
            style={[
              tw`text-4xl font-bold`,
              {
                color: "#111827",
              },
            ]}
          >
            Welcome Telyutizen!
          </Text>

          <Text
            style={tw`text-gray-500 mt-2 text-base`}
          >
            Sign in to continue
          </Text>
        </View>

        {/* EMAIL */}
        <View
          style={tw`mb-5`}
        >
          <Text
            style={[
              tw`mb-2 font-medium`,
              {
                color: "#374151",
              },
            ]}
          >
            Email
          </Text>

          <TextInput
            value={email}
            onChangeText={setEmail}
            placeholder="your@email.com"
            placeholderTextColor="#9ca3af"
            keyboardType="email-address"
            autoCapitalize="none"
            editable={!isLoading}
            style={[
              tw`h-14 px-5 rounded-2xl border`,
              {
                borderColor: "#e5e7eb",
                backgroundColor: "#fafafa",
              },
            ]}
          />
        </View>

        {/* PASSWORD */}
        <View
          style={tw`mb-6`}
        >
          <Text
            style={[
              tw`mb-2 font-medium`,
              {
                color: "#374151",
              },
            ]}
          >
            Password
          </Text>

          <View
            style={[
              tw`h-14 rounded-2xl border flex-row items-center px-4`,
              {
                borderColor: "#e5e7eb",
                backgroundColor: "#fafafa",
              },
            ]}
          >
            <TextInput
              value={password}
              onChangeText={setPassword}
              placeholder="••••••••"
              placeholderTextColor="#9ca3af"
              secureTextEntry={
                !showPassword
              }
              editable={!isLoading}
              style={[tw`flex-1 text-gray-900`, { color: '#111827' }]}
            />

            <TouchableOpacity
              onPress={() =>
                setShowPassword(
                  !showPassword
                )
              }
            >
              <Ionicons
                name={
                  showPassword
                    ? "eye-off-outline"
                    : "eye-outline"
                }
                size={22}
                color="#6b7280"
              />
            </TouchableOpacity>
          </View>
        </View>

        {/* LOGIN BUTTON */}
        <TouchableOpacity
            activeOpacity={0.85}
            onPress={handleLogin}
            disabled={isLoading}
          style={[
            tw`h-14 rounded-2xl items-center justify-center`,
            {
              backgroundColor:
                MAROON,
            },
          ]}
        >
          <Text
            style={tw`text-white font-bold text-base`}
          >
            Sign In
          </Text>
        </TouchableOpacity>

        {/* DIVIDER */}
        <View
          style={tw`flex-row items-center my-8`}
        >
          <View
            style={tw`flex-1 h-px bg-gray-200`}
          />

          <Text
            style={tw`mx-4 text-gray-400`}
          >
            OR
          </Text>

          <View
            style={tw`flex-1 h-px bg-gray-200`}
          />
        </View>

        {/* GOOGLE */}
        <TouchableOpacity
          onPress={handleNativeGoogleLogin}
          activeOpacity={0.85}
          disabled={isLoading}
          style={[
            tw`h-14 rounded-2xl border flex-row items-center justify-center`,
            {
              borderColor: "#e5e7eb",
              backgroundColor:
                "#ffffff",
            },
          ]}
        >
          <Ionicons
            name="logo-google"
            size={20}
            color="#EA4335"
          />

          <Text
            style={tw`ml-3 font-semibold text-gray-700`}
          >
            Continue with Google
          </Text>
        </TouchableOpacity>

        {/* SIGN UP */}
        <View
          style={tw`flex-row justify-center mt-10`}
        >
          <Text
            style={tw`text-gray-500`}
          >
            Don't have an account?
          </Text>

          <TouchableOpacity
            onPress={() =>
              router.push("/register")
            }
          >
            <Text
              style={[
                tw`ml-2 font-bold`,
                {
                  color: MAROON,
                },
              ]}
            >
              Sign Up
            </Text>
          </TouchableOpacity>
        </View>
      </View>
    </SafeAreaView>
  );
}