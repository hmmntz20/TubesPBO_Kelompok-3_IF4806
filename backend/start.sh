#!/bin/bash

echo "🚀 Membaca Environment Variables dari file .env..."

# Memeriksa apakah file .env benar-benar ada
if [ ! -f .env ]; then
  echo "❌ Error: File .env tidak ditemukan di folder backend!"
  echo "💡 Silakan copy dari .env.example dan isi kredensialnya terlebih dahulu."
  exit 1
fi

# Membaca dan mengekspor (export) semua isi file .env ke terminal secara otomatis
set -a
source .env
set +a

echo "✅ Kredensial berhasil dimuat!"
echo "☕ Menyalakan Server Spring Boot Backend (Telkom Route)..."

# Menjalankan server Spring Boot
./mvnw spring-boot:run