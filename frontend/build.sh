#!/bin/bash

echo "📖 Membaca rahasia dari file .env..."

# Cek apakah file .env ada
if [ ! -f .env ]; then
  echo "❌ Error: File .env tidak ditemukan! Pastikan file .env sudah dibuat di folder frontend."
  exit 1
fi

# Memasukkan semua isi .env ke memori terminal secara otomatis
set -a
source .env
set +a

echo "🚀 Memulai proses unggah Environment Variables ke EAS..."

# Perhatikan: Bagian --value sekarang menggunakan nama variabel dengan awalan tanda dolar ($)

# 1. Key Google Maps (Secret)
eas env:create --environment development --name EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY --value "$EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY" --visibility sensitive
eas env:create --environment development --name EXPO_PUBLIC_GOOGLE_MAPS_IOS_KEY --value "$EXPO_PUBLIC_GOOGLE_MAPS_IOS_KEY" --visibility sensitive

# 2. URL Backend (Plaintext)
eas env:create --environment development --name EXPO_PUBLIC_API_BASE_URL --value "$EXPO_PUBLIC_API_BASE_URL" --visibility plaintext

# 3. URL Supabase (Plaintext)
eas env:create --environment development --name EXPO_PUBLIC_SUPABASE_URL --value "$EXPO_PUBLIC_SUPABASE_URL" --visibility plaintext

# 4. Anon Key Supabase (Sensitive)
eas env:create --environment development --name EXPO_PUBLIC_SUPABASE_ANON_KEY --value "$EXPO_PUBLIC_SUPABASE_ANON_KEY" --visibility sensitive

echo "✅ Semua variabel berhasil diunggah ke brankas cloud Expo!"
echo "🛠️  Mulai melakukan build APK..."

# 5. Langsung jalankan proses build otomatis!
eas build --profile development --platform android