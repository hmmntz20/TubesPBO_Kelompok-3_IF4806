# Frontend — Telkom Route Finder

Aplikasi mobile (Expo SDK 54 + React Native + NativeWind + Glassmorphism) untuk
**Telkom University Shortest Route Finder**.

## Persyaratan dev-build

Karena proyek memakai **Google Maps Provider** (`react-native-maps`) dan
**`expo-blur`** (glassmorphism), `Expo Go` **tidak cukup**. Anda perlu
*development build* via `npx expo run:android` / `run:ios`.

### 1. Setup environment

```bash
cp .env.example .env
# Edit .env, isi nilai EXPO_PUBLIC_GOOGLE_MAPS_*_KEY (lihat panduan di bawah).
```

### 2. Mendapatkan Google Maps API key

1. Login ke [Google Cloud Console](https://console.cloud.google.com/) → buat
   atau pilih project.
2. Aktifkan **Maps SDK for Android** dan **Maps SDK for iOS** di
   "APIs & Services > Library".
3. Buat **API key** baru di "APIs & Services > Credentials".
4. **Restrict key** untuk meminimalkan risiko bocor:
   - Android: batasi per **package name** `pbo.telkomroute` + SHA-1 cert dev/prod.
   - iOS: batasi per **bundle ID** `pbo.telkomroute`.
5. Tempel key ke `.env` (untuk Android & iOS terpisah; bisa pakai key yang
   sama jika kedua restrict berlaku).

### 3. Install dependency

```bash
npm install
```

### 4. Native build

#### Android (memerlukan Android SDK + emulator/perangkat)

```bash
npx expo prebuild --platform android   # generate /android folder (sekali)
npx expo run:android                    # build & install ke emulator/device
```

#### iOS (memerlukan macOS + Xcode)

```bash
npx expo prebuild --platform ios
npx expo run:ios
```

### 5. Iterasi cepat

Setelah build native pertama kali sukses, gunakan:

```bash
npx expo start --dev-client
```

Bundle JS akan refresh otomatis tanpa rebuild native.

## Backend lokal

Frontend membaca data dari backend Spring Boot (default
`http://10.0.2.2:8080` untuk Android emulator, `http://localhost:8080` untuk
iOS simulator). Pastikan backend berjalan:

```bash
cd ../backend
SPRING_PROFILES_ACTIVE=          # default: PostgreSQL — sediakan DB lokal
./mvnw spring-boot:run
```

Smoke check:

```bash
curl http://localhost:8080/api/v1/health      # → {"status":"UP"}
curl http://localhost:8080/api/v1/graph/meta  # → ringkasan graf
```

## Lint & test

```bash
npx tsc --noEmit
npx expo lint
```

## Struktur folder utama

```
app/                routes (expo-router)
components/         primitives (themed-text, ui/Glass*)
constants/          theme, env, mapBoundaries (TBD)
hooks/              use-app-theme, use-color-scheme, use-reduce-transparency
services/           api-client, geojson-service (TBD di MAP-03)
stores/             theme-store (Zustand)
assets/             images, data/telmap.geojson (offline fallback)
app.config.ts       Expo config dinamis (env-driven)
.env                kunci API & URL backend (TIDAK di-commit)
```
