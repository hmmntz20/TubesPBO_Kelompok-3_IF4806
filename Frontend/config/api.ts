import { Platform } from 'react-native';

const getApiUrl = () => {
  if (__DEV__) {
    // Karena pakai HP Fisik, langsung tulis IP Laptop Anda secara manual:
    // (Pastikan IP ini sama dengan IP IPv4 laptop Anda saat ini)
    return 'http://192.168.18.12:8080/api'; 
  }
  
  // Untuk Production nanti
  return 'https://backend-tubespbo-anda.com/api'; 
};

export const API_URL = getApiUrl();