@echo off
echo 🚀 Membaca Environment Variables dari file .env...

if not exist .env (
    echo ❌ Error: File .env tidak ditemukan di folder frontend!
    pause
    exit /b 1
)

:: Membaca file .env dan memisahkan Key dan Value berdasarkan tanda sama dengan (=)
for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    echo %%A | findstr /r "^#" >nul
    if errorlevel 1 (
        if not "%%A"=="" (
            set "%%A=%%B"
        )
    )
)

echo ✅ Kredensial berhasil dimuat! Mulai proses unggah ke EAS (Windows)...

call eas env:create --environment development --name EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY --value "%EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY%" --visibility secret
call eas env:create --environment development --name EXPO_PUBLIC_API_BASE_URL --value "%EXPO_PUBLIC_API_BASE_URL%" --visibility plaintext
call eas env:create --environment development --name EXPO_PUBLIC_SUPABASE_URL --value "%EXPO_PUBLIC_SUPABASE_URL%" --visibility plaintext
call eas env:create --environment development --name EXPO_PUBLIC_SUPABASE_ANON_KEY --value "%EXPO_PUBLIC_SUPABASE_ANON_KEY%" --visibility sensitive

echo ✅ Semua variabel berhasil diunggah ke brankas cloud Expo!
echo 🛠️ Mulai melakukan build APK...

call eas build --profile development --platform android