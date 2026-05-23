@echo off
echo 🚀 Membaca Environment Variables dari file .env...

if not exist .env (
    echo ❌ Error: File .env tidak ditemukan di folder backend!
    echo 💡 Silakan copy dari .env.example dan isi kredensialnya terlebih dahulu.
    pause
    exit /b 1
)

:: Membaca file .env baris demi baris dan meng-set sebagai env var (mengabaikan komentar #)
for /f "usebackq delims=" %%a in (".env") do (
    echo %%a | findstr /r "^#" >nul
    if errorlevel 1 (
        set "%%a"
    )
)

echo ✅ Kredensial berhasil dimuat!
echo ☕ Menyalakan Server Spring Boot Backend (Telkom Route)...

:: Di Windows, menggunakan mvnw.cmd bukan ./mvnw
call mvnw.cmd spring-boot:run