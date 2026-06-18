import React, { useState, useCallback } from 'react';
import { 
  View, Text, TouchableOpacity, Image, ScrollView, 
  Modal, FlatList, Alert, TextInput, KeyboardAvoidingView, Platform, ActivityIndicator 
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import tw from 'twrnc';
import { router, useFocusEffect } from "expo-router";
import AsyncStorage from '@react-native-async-storage/async-storage';
import { API_URL } from '@/config/api';

const AVATAR_OPTIONS = [
  'https://i.pravatar.cc/150?u=1',
  'https://i.pravatar.cc/150?u=2',
  'https://i.pravatar.cc/150?u=3',
  'https://i.pravatar.cc/150?u=4',
  'https://i.pravatar.cc/150?u=5',
  'https://i.pravatar.cc/150?u=6',
];

export default function Profile() {
  // --- STATE DATA USER ---
  const [username, setUsername] = useState('User');
  const [email, setEmail] = useState('user@email.com');
  const [profilePic, setProfilePic] = useState(AVATAR_OPTIONS[0]);
  
  // --- STATE MODALS ---
  const [showAvatarModal, setShowAvatarModal] = useState(false);
  const [showUserModal, setShowUserModal] = useState(false);
  const [showPassModal, setShowPassModal] = useState(false);

  // --- STATE FORM USERNAME ---
  const [newUsername, setNewUsername] = useState('');
  const [confirmUserText, setConfirmUserText] = useState('');
  const [isUpdatingUser, setIsUpdatingUser] = useState(false);

  // --- STATE FORM PASSWORD ---
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassText, setConfirmPassText] = useState('');
  const [isUpdatingPass, setIsUpdatingPass] = useState(false);

  // 1. Memuat Data dari HP (AsyncStorage) saat halaman dibuka
  useFocusEffect(
    useCallback(() => {
      const loadUserData = async () => {
        const storedName = await AsyncStorage.getItem('userName');
        const storedEmail = await AsyncStorage.getItem('userEmail');
        const storedPhoto = await AsyncStorage.getItem('userPhoto');

        if (storedName) setUsername(storedName);
        if (storedEmail) setEmail(storedEmail);
        if (storedPhoto) setProfilePic(storedPhoto);
      };
      loadUserData();
    }, [])
  );

  // 2. Handler Ubah Avatar (Langsung hit API & simpan ke HP)
  const handleUpdateAvatar = async (newPhotoUrl: string) => {
    setProfilePic(newPhotoUrl);
    setShowAvatarModal(false);
    await AsyncStorage.setItem('userPhoto', newPhotoUrl);
    
    try {
      await fetch(`${API_URL}/users/update-profile?email=${email}&newProfilePhoto=${encodeURIComponent(newPhotoUrl)}`, {
        method: 'PUT',
      });
    } catch (error) {
      console.error("Gagal sinkronisasi foto ke server:", error);
    }
  };

  // 3. Handler Ubah Username
  const handleUpdateUsername = async () => {
    if (!newUsername.trim()) return;
    setIsUpdatingUser(true);
    try {
      const response = await fetch(`${API_URL}/users/update-profile?email=${email}&newUsername=${encodeURIComponent(newUsername)}`, {
        method: 'PUT',
      });
      
      const responseText = await response.text();
      if (response.ok) {
        Alert.alert("Sukses", "Username berhasil diubah!");
        setUsername(newUsername);
        await AsyncStorage.setItem('userName', newUsername);
        
        // Reset & Tutup Modal
        setShowUserModal(false);
        setNewUsername('');
        setConfirmUserText('');
      } else {
        Alert.alert("Gagal", responseText);
      }
    } catch (error) {
      Alert.alert("Error", "Gagal menghubungi server");
    } finally {
      setIsUpdatingUser(false);
    }
  };

  // 4. Handler Ubah Password
  const handleUpdatePassword = async () => {
    if (!oldPassword || !newPassword) return;
    setIsUpdatingPass(true);
    try {
      const response = await fetch(`${API_URL}/users/update-password?email=${email}&oldPassword=${encodeURIComponent(oldPassword)}&newPassword=${encodeURIComponent(newPassword)}`, {
        method: 'PUT',
      });
      
      const responseText = await response.text();
      if (response.ok) {
        Alert.alert("Sukses", "Password berhasil diperbarui!");
        // Reset & Tutup Modal
        setShowPassModal(false);
        setOldPassword('');
        setNewPassword('');
        setConfirmPassText('');
      } else {
        Alert.alert("Gagal", responseText);
      }
    } catch (error) {
      Alert.alert("Error", "Gagal menghubungi server");
    } finally {
      setIsUpdatingPass(false);
    }
  };

  // 5. Handler Logout
  const handleLogout = () => {
    Alert.alert(
      "Sign Out",
      "Are you sure you want to log out?",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Log Out",
          style: "destructive",
          onPress: async () => {
            await AsyncStorage.clear(); // Bersihkan memori sesi
            router.replace("/login");   // Lempar ke halaman login
          },
        },
      ]
    );
  };

  return (
    <View style={tw`flex-1 bg-gray-50`}>
      <ScrollView style={tw`flex-1 pt-12 px-6`}>
        <Text style={tw`text-3xl font-black text-gray-900 mb-8`}>Profile</Text>

        {/* 1. Header Profile */}
        <View style={tw`items-center mb-10`}>
          <View style={tw`relative`}>
            <Image 
              source={{ uri: profilePic }} 
              style={tw`w-28 h-28 rounded-full border-2 border-white shadow-md bg-gray-200`}
            />
            <TouchableOpacity 
              onPress={() => setShowAvatarModal(true)}
              style={tw`absolute bottom-0 right-0 bg-[#8b0000] p-2.5 rounded-full border-[3px] border-gray-50`}
            >
              <Ionicons name="camera" size={16} color="white" />
            </TouchableOpacity>
          </View>
          <Text style={tw`mt-4 text-xl font-bold text-gray-900`}>{username}</Text>
          <Text style={tw`text-sm text-gray-400 font-medium`}>{email}</Text>
        </View>

        {/* 2. Account Section */}
        <View style={tw`mb-8`}>
          <Text style={tw`text-xs font-bold text-gray-400 uppercase tracking-widest mb-3 ml-1`}>Account Settings</Text>
          <View style={tw`bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden`}>
            <SettingItem 
              icon="person-outline" 
              label="Username" 
              value={username} 
              onPress={() => setShowUserModal(true)} 
            />
            <View style={tw`h-[1px] bg-gray-50 mx-4`} />
            <SettingItem 
              icon="lock-closed-outline" 
              label="Password" 
              value="••••••••" 
              onPress={() => setShowPassModal(true)} 
            />
          </View>
        </View>

        {/* 3. Dangerous Section */}
        <TouchableOpacity 
          onPress={handleLogout}
          style={tw`flex-row items-center justify-center bg-white p-4 rounded-2xl shadow-sm border border-red-50 mb-12`}
        >
          <Ionicons name="log-out-outline" size={20} color="#b91c1c" />
          <Text style={tw`text-red-700 font-bold ml-2 text-base`}>Log Out</Text>
        </TouchableOpacity>
      </ScrollView>

      {/* =========================================================
          MODAL: PILIH AVATAR
      ========================================================= */}
      <Modal visible={showAvatarModal} transparent animationType="slide">
        <View style={tw`flex-1 bg-black/50 justify-end`}>
          <View style={tw`bg-white rounded-t-3xl p-6 h-1/2`}>
            <Text style={tw`text-lg font-bold mb-4 text-center`}>Choose your avatar</Text>
            <FlatList
              data={AVATAR_OPTIONS}
              numColumns={3}
              keyExtractor={(item) => item}
              columnWrapperStyle={tw`justify-center`}
              renderItem={({ item }) => (
                <TouchableOpacity 
                  onPress={() => handleUpdateAvatar(item)}
                  style={tw`m-2`}
                >
                  <Image source={{ uri: item }} style={tw`w-22 h-22 rounded-2xl`} />
                </TouchableOpacity>
              )}
            />
            <TouchableOpacity onPress={() => setShowAvatarModal(false)} style={tw`mt-4 p-4 bg-gray-100 rounded-xl items-center`}>
              <Text style={tw`font-bold text-gray-700`}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* =========================================================
          MODAL: UBAH USERNAME
      ========================================================= */}
      <Modal visible={showUserModal} transparent animationType="slide">
        <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={tw`flex-1`}>
          <View style={tw`flex-1 justify-end bg-black/60`}>
            <TouchableOpacity style={tw`flex-1`} onPress={() => setShowUserModal(false)} />
            
            <View style={tw`bg-white w-full rounded-t-3xl p-6 pb-10`}>
              <View style={tw`items-center mb-6`}>
                <View style={tw`w-12 h-1.5 bg-gray-200 rounded-full mb-4`} />
                <Text style={tw`text-xl font-bold text-gray-900`}>Change Username</Text>
              </View>

              <Text style={tw`text-xs font-bold text-gray-500 uppercase ml-1 mb-2 tracking-wide`}>Current Username</Text>
              <TextInput
                style={tw`bg-gray-100 border border-gray-200 rounded-xl px-4 py-3.5 text-gray-500 mb-4`}
                value={username}
                editable={false}
              />

              <Text style={tw`text-xs font-bold text-gray-500 uppercase ml-1 mb-2 tracking-wide`}>New Username</Text>
              <TextInput
                style={tw`bg-gray-50 border border-gray-200 rounded-xl px-4 py-3.5 text-gray-900 mb-4`}
                placeholder="Enter new username"
                value={newUsername}
                onChangeText={setNewUsername}
                placeholderTextColor="#9ca3af"
              />

              <Text style={tw`text-xs font-bold text-red-400 uppercase ml-1 mb-2 tracking-wide`}>Type 'confirm' to save</Text>
              <TextInput
                style={tw`bg-red-50 border border-red-100 rounded-xl px-4 py-3.5 text-red-900 mb-6`}
                placeholder="confirm"
                value={confirmUserText}
                onChangeText={setConfirmUserText}
                autoCapitalize="none"
                placeholderTextColor="#fca5a5"
              />

              <TouchableOpacity 
                style={tw`py-4 rounded-xl items-center justify-center ${confirmUserText.toLowerCase() === 'confirm' ? 'bg-[#8b0000]' : 'bg-gray-300'}`}
                disabled={confirmUserText.toLowerCase() !== 'confirm' || isUpdatingUser}
                onPress={handleUpdateUsername}
              >
                {isUpdatingUser ? (
                  <ActivityIndicator color="#FFF" />
                ) : (
                  <Text style={tw`text-white font-bold text-base`}>Save Username</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>

      {/* =========================================================
          MODAL: UBAH PASSWORD
      ========================================================= */}
      <Modal visible={showPassModal} transparent animationType="slide">
        <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={tw`flex-1`}>
          <View style={tw`flex-1 justify-end bg-black/60`}>
            <TouchableOpacity style={tw`flex-1`} onPress={() => setShowPassModal(false)} />
            
            <View style={tw`bg-white w-full rounded-t-3xl p-6 pb-10`}>
              <View style={tw`items-center mb-6`}>
                <View style={tw`w-12 h-1.5 bg-gray-200 rounded-full mb-4`} />
                <Text style={tw`text-xl font-bold text-gray-900`}>Change Password</Text>
              </View>

              <Text style={tw`text-xs font-bold text-gray-500 uppercase ml-1 mb-2 tracking-wide`}>Old Password</Text>
              <TextInput
                style={tw`bg-gray-50 border border-gray-200 rounded-xl px-4 py-3.5 text-gray-900 mb-4`}
                placeholder="Enter old password"
                secureTextEntry
                value={oldPassword}
                onChangeText={setOldPassword}
                placeholderTextColor="#9ca3af"
              />

              <Text style={tw`text-xs font-bold text-gray-500 uppercase ml-1 mb-2 tracking-wide`}>New Password</Text>
              <TextInput
                style={tw`bg-gray-50 border border-gray-200 rounded-xl px-4 py-3.5 text-gray-900 mb-4`}
                placeholder="Enter new password"
                secureTextEntry
                value={newPassword}
                onChangeText={setNewPassword}
                placeholderTextColor="#9ca3af"
              />

              <Text style={tw`text-xs font-bold text-red-400 uppercase ml-1 mb-2 tracking-wide`}>Type 'confirm' to save</Text>
              <TextInput
                style={tw`bg-red-50 border border-red-100 rounded-xl px-4 py-3.5 text-red-900 mb-6`}
                placeholder="confirm"
                value={confirmPassText}
                onChangeText={setConfirmPassText}
                autoCapitalize="none"
                placeholderTextColor="#fca5a5"
              />

              <TouchableOpacity 
                style={tw`py-4 rounded-xl items-center justify-center ${confirmPassText.toLowerCase() === 'confirm' ? 'bg-[#8b0000]' : 'bg-gray-300'}`}
                disabled={confirmPassText.toLowerCase() !== 'confirm' || isUpdatingPass}
                onPress={handleUpdatePassword}
              >
                {isUpdatingPass ? (
                  <ActivityIndicator color="#FFF" />
                ) : (
                  <Text style={tw`text-white font-bold text-base`}>Save Password</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>

    </View>
  );
}

// Sub-komponen agar kode rapi
function SettingItem({ icon, label, value, onPress }: { icon: any, label: string, value: string, onPress: () => void }) {
  return (
    <TouchableOpacity onPress={onPress} style={tw`flex-row items-center justify-between p-5 active:bg-gray-50`}>
      <View style={tw`flex-row items-center`}>
        <View style={tw`p-2`}>
          <Ionicons name={icon} size={18} color="#61666c" />
        </View>
        <Text style={tw`text-gray-700 font-semibold ml-4 text-base`}>{label}</Text>
      </View>
      <View style={tw`flex-row items-center`}>
        <Text style={tw`text-gray-400 font-medium mr-2`} numberOfLines={1} ellipsizeMode="tail">{value}</Text>
        <Ionicons name="chevron-forward" size={16} color="#d1d5db" />
      </View>
    </TouchableOpacity>
  );
}