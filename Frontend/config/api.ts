import { Platform } from 'react-native';
import Constants from 'expo-constants';

const getApiUrl = () => {
  if (__DEV__) {
    // 1. Dapatkan URL Host dari Expo (Biasanya formatnya "192.168.1.5:8081")
    const hostUri = Constants.expoConfig?.hostUri;

    if (hostUri) {
      // 2. Ambil IP-nya saja (buang port :8081)
      const ipAddress = hostUri.split(':')[0];
      
      console.log(`🌍 [API Config] Terdeteksi IP Laptop: ${ipAddress}`);
      
      // 3. Kembalikan URL dengan IP tersebut dan Port Spring Boot (8080)
      return `http://${ipAddress}:8080/api`;
    } 
    
    // Fallback darurat jika hostUri tidak terdeteksi (sangat jarang terjadi)
    console.warn("⚠️ [API Config] IP gagal terdeteksi, menggunakan fallback.");
    return Platform.OS === 'android' ? 'http://10.0.2.2:8080/api' : 'http://localhost:8080/api';
    
  }
};

export const API_URL = getApiUrl();