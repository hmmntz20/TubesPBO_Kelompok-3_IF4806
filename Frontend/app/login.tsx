import React, { useState } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  Image,
  Alert
} from "react-native";
import TelU from '@/assets/images/telu.png'
import { Ionicons } from "@expo/vector-icons";
import tw from "twrnc";
import { router } from "expo-router";
import { API_URL } from "@/config/api";
import AsyncStorage from '@react-native-async-storage/async-storage';

const MAROON = "#8b0000";

export default function LoginScreen() {
  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert("Perhatian", "Email dan password tidak boleh kosong!");
      return;
    }

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
        
        Alert.alert("Sukses", "Login Berhasil!");
        router.replace("/(tabs)");
      } else {
        const errorText = await response.text();
        Alert.alert("Gagal", errorText);
      }
    } catch (error) {
      Alert.alert("Error", "Gagal menghubungi server.");
      console.error(error);
    }
  };

  const [showPassword, setShowPassword] =
    useState(false);

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  return (
    <SafeAreaView
      style={tw`flex-1 bg-white`}
    >
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
            Welcome Back
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
              style={tw`flex-1`}
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
          activeOpacity={0.85}
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