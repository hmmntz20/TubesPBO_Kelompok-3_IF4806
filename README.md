# 🗺️ Shortest Route Finder App (based on Telkom University Map)

> **Academic Purpose:** This project is submitted to fulfill the Final Project requirement for the **Object-Oriented Programming (OOP)** course.

## 📖 About The Project

---

## 👥 Group Description

**Group 3 - Class IF-48-06**

| No | Student ID (NIM) | Name                      |
|:--:|:----------------:|:-------------------------:|
| 1  | 103012400118     | [HN]                      |
| 2  | 103012400277     | [Putri Rahayu Damayanti]  |
| 3  | 103012430055     | [Putri Ayu Lestari]       |
| 4  | 103012400305     | [Najla Tsabita Afiyah]    |

---

## 💻 Tech Stack

**Frontend (Mobile App)**
* ⚛️ **React Native (Latest)** (via **Expo**) - UI Framework
* 🎨 **NativeWind 4.2.3 (Tailwind CSS)** - Styling & UI Components

**Backend (REST API)**
* 🍃 **Spring Boot 3.5.15** - Backend Framework
* ☕ **Java 21** - Core Language
* 📦 **Maven** - Build Automation & Dependency Management

---

## ⚙️ Prerequisites

Before you begin, ensure you have the following installed on your local machine:

* **[Git](https://git-scm.com/)** - Version control to clone the repository.
* **[Node.js](https://nodejs.org/)** (v18.x or newer) - Required for the frontend.
* **[JDK 21](https://adoptium.net/)** - Java Development Kit required for compiling the backend.
* **Expo Go App** - Installed on your physical iOS/Android device for testing, OR an **Android Emulator / iOS Simulator** configured on your PC.

---

## 📂 Directory Structure

This project follows a Monorepo architecture, separating the client and server environments:
```
shortest-route-finder/
├── backend/                  # Spring Boot Java Application
│   ├── .mvn/                 # Maven wrapper files
│   ├── src/                  # Backend source code (Controllers, Services, Models)
│   ├── mvnw                  # Maven wrapper executable (Linux/macOS)
│   ├── mvnw.cmd              # Maven wrapper executable (Windows)
│   └── pom.xml               # Backend dependencies
│
├── frontend/                 # React Native Expo Application
│   ├── app/                  # Expo Router UI components and screens
│   ├── assets/               # Images, fonts, etc.
│   ├── global.css            # Tailwind global stylesheet
│   ├── tailwind.config.js    # NativeWind configuration
│   └── package.json          # Frontend dependencies
│
├── .gitignore                # Global git ignore rules
└── README.md                 # Project documentation
=======
# Telkom University Shortest Route Finder

Aplikasi mobile pencarian rute terpendek di kampus Telkom University.
Tugas Besar mata kuliah **Object-Oriented Programming (OOP) — IF4806**.

> **Status (22 Mei 2026):** Fase **fondasi peta & visualisasi** selesai dan
> sudah diverifikasi (40 unit/integration tests pass). Empat fitur utama
> (Auth, Manajemen Graf, Pencarian A\*, Riwayat) akan dibangun di atas
> fondasi ini.

---

## 1. Ringkasan

| Aspek | Detail |
|---|---|
| **Frontend** | Expo SDK 54 · React Native 0.81.5 · expo-router 6 · NativeWind 4 · TypeScript 5.9 |
| **Backend** | Spring Boot 3.3.13 (LTS) · Java 21 · Maven · PostgreSQL (H2 hanya untuk test) |
| **Bahasa Desain** | **Glassmorphism** light/dark + aksen Maroon Telkom (`#8C1D40` / `#B83A5E`) |
| **Provider Peta** | Google Maps (`PROVIDER_GOOGLE`) di Android & iOS |
| **Sumber data peta** | `telmap.geojson` (RFC 7946, dump Overpass / OSM) — dilayani backend, di-cache di AsyncStorage, fallback bundle |
| **Algoritma rute** | A\* (akan ditambahkan di Fitur 3) dengan parser plug-in lewat `MapDataParser` interface |

---

## 2. Struktur monorepo

```
TubesPBO---Kelompok-3---IF4806/
├── backend/                  Spring Boot 3.3.13 (Java 21, Maven)
│   ├── pom.xml
│   ├── src/main/java/pbo/backend/
│   │   ├── config/           CorsConfig
│   │   ├── geojson/          DTO sealed + GeoJsonLoader + GeoJsonResource (ETag)
│   │   ├── geojson/api/      GeoJsonController (GET /api/v1/graph/geojson + 304)
│   │   ├── graph/domain/     Coordinate, Node, Edge, CampusGraph (encapsulated)
│   │   ├── graph/parser/     MapDataParser (interface) + OsmGeoJsonParser (Strategy)
│   │   ├── graph/service/    GraphService (orchestration via DI)
│   │   ├── graph/api/        GraphController (GET /api/v1/graph/meta)
│   │   └── health/           HealthController (GET /api/v1/health)
│   └── src/main/resources/
│       ├── application.properties        (default: PostgreSQL via env vars)
│       ├── application-prod.properties   (placeholder)
│       └── telmap.geojson                (348 fitur OSM, sumber kanonis)
│
├── frontend/                 Expo (React Native + NativeWind + Glass)
│   ├── app/                  routes (expo-router)
│   │   ├── (tabs)/           Peta · Eksplorasi · Pengaturan
│   │   ├── _layout.tsx       SafeAreaProvider + ThemeProvider + StatusBar
│   │   └── modal.tsx
│   ├── components/
│   │   ├── map/              CampusMap, MapLegend, RecenterButton, mapStyle*.ts
│   │   ├── ui/               GlassSurface, GlassCard, GlassButton, BlurTabBarBackground
│   │   ├── themed-text.tsx   themed-view.tsx
│   ├── constants/            theme, env, mapBoundaries
│   ├── hooks/                use-app-theme, use-color-scheme, use-reduce-transparency
│   ├── services/             api-client, geojson-service (3-tier loader)
│   ├── stores/               theme-store (Zustand + AsyncStorage)
│   ├── types/                geojson types
│   ├── utils/                coords (lng/lat ↔ lat/lng)
│   ├── assets/data/          telmap.geojson (offline fallback)
│   ├── app.config.ts         env-driven Google Maps API keys
│   └── .env.example
│
└── specs/
    ├── README.md             indeks modular SDD
    └── foundation-map/
        ├── requirements.md   (EARS — FR-MAP, FR-GEO, FR-UI, FR-INFRA, NFR-OOP)
        ├── design.md         (arsitektur, palette, parser strategy, SOLID, JavaDoc)
        └── tasks.md          (rencana eksekusi bertahap)
>>>>>>> 076dfd1 (progres yang ada aja)
```

---

<<<<<<< HEAD
## 🚀 How to Run the Application

Step 1: Clone the Repository
Open your terminal and run:
* git clone https://github.com/hmmntz20/TubesPBO_Kelompok-3_IF4806.git
* cd TubesPBO_Kelompok-3_IF4806

Step 2: Running the Backend (Spring Boot)
The backend acts as the core engine for our routing algorithm and REST API.
* cd backend
* ./mvnw clean spring-boot:run

```The server will start, and the API will be accessible at http://localhost:8080.```

Step 3: Running the Frontend (React Native)
* cd frontend
* npm install
* npm run start

```Open the Expo Go app on your phone and scan the QR code displayed in the terminal. (Note: Ensure your phone and PC are connected to the same Wi-Fi network).```

---





=======
## 3. Pola desain & OOP yang ditonjolkan (untuk presentasi OOP)

| Pola | Lokasi |
|---|---|
| **Strategy / Adapter** | `MapDataParser` interface + `OsmGeoJsonParser` implementasi konkret |
| **Template Method** | `AbstractMapDataParser.parse(...)` `final` + hook `protected abstract mapFeature(...)` |
| **Builder** | `Edge.Builder`, `CampusGraph.Builder` (mutasi terbatas, hasil `final`) |
| **Sealed hierarchy** | `GeometryDTO` (`LineStringGeometryDTO` / `PointGeometryDTO` / `PolygonGeometryDTO`) |
| **Polimorfisme + pattern matching** | `if (geom instanceof LineStringGeometryDTO line)` di `OsmGeoJsonParser` |
| **Encapsulation ketat** | Semua field domain `private final`, tanpa setter; mutasi hanya via Builder |
| **Dependency Inversion** | `GraphService` bergantung pada interface `MapDataParser`, bukan implementasi |
| **No-null API** | `findNode → Optional<Node>`, `Objects.requireNonNull` untuk parameter |

Pemetaan SOLID lengkap ada di [`specs/foundation-map/design.md` §4.3](specs/foundation-map/design.md).

---

## 4. Cara menjalankan

### 4.1 Prasyarat

- **Java 21** (untuk backend)
- **Node 20+** dan **npm 10+** (untuk frontend)
- **Database PostgreSQL** — pilih salah satu:
  - **Supabase** (rekomendasi, sudah dipakai proyek): cukup punya akun + project di [supabase.com](https://supabase.com).
  - **PostgreSQL lokal** untuk dev offline.
- **Android SDK** + emulator/perangkat (atau **Xcode** + macOS untuk iOS).
- **Google Maps API key** untuk Android & iOS (dari Google Cloud Console; di-restrict per package).
- **Supabase project** (bila ingin login Google) — auth provider Google sudah harus aktif di dashboard.

### 4.2 Setup kredensial (HARUS dilakukan sekali sebelum run)

#### 4.2.1 Frontend `.env`

```bash
cd frontend
cp .env.example .env
# Lalu edit .env (file lokal, sudah .gitignore) dan isi:
#   EXPO_PUBLIC_GOOGLE_MAPS_ANDROID_KEY=AIza...
#   EXPO_PUBLIC_GOOGLE_MAPS_IOS_KEY=AIza...
#   EXPO_PUBLIC_API_BASE_URL=http://10.0.2.2:8080  (Android emulator) atau IP LAN
#   EXPO_PUBLIC_SUPABASE_URL=https://<project-ref>.supabase.co
#   EXPO_PUBLIC_SUPABASE_ANON_KEY=eyJ...   # Supabase dashboard → Settings → API → anon key
```

#### 4.2.2 Backend env var

Salin template script lalu isi nilainya:

```bash
cp backend/scripts/set-env.example.sh backend/scripts/set-env.sh
# Edit backend/scripts/set-env.sh — isi DB_*, SUPABASE_JWT_SECRET, dst.

# Sebelum menjalankan backend:
source backend/scripts/set-env.sh
```

`set-env.sh` sudah masuk `.gitignore`. Alternatif: export env vars langsung di shell `~/.bashrc`/`~/.zshrc` Anda.

#### 4.2.3 Konfigurasi OAuth di Supabase dashboard

Supabase → Authentication → URL Configuration → **Redirect URLs**, tambahkan URL deep-link aplikasi:

```
frontend://auth/callback
```

(atau ganti `scheme` di `frontend/app.json` sesuai kebutuhan, lalu sesuaikan di Supabase).

### 4.3 Backend

```bash
cd backend

# Setelah set-env.sh sudah diisi:
source scripts/set-env.sh
./mvnw spring-boot:run
```

Smoke check (di terminal lain):

```bash
curl http://localhost:8080/api/v1/health        # → {"status":"UP"}
curl -i http://localhost:8080/api/v1/graph/meta # ringkasan graf
curl -i http://localhost:8080/api/v1/auth/me    # → 401 (perlu Bearer token)
```

Build artefak:

```bash
./mvnw verify   # menghasilkan target/backend-0.0.1-SNAPSHOT.jar
```

**Smoke E2E otomatis** (tidak butuh Postgres — pakai H2 dari Maven cache):

```bash
python3 scripts/smoke-test.py
```

Skrip itu otomatis start backend, jalankan 9 skenario routing (Fitur 3), dan stop kembali. Tidak menyentuh kredensial Anda; tidak menguji Auth (karena butuh Supabase nyata).

### 4.4 Frontend

```bash
cd frontend
npm install   # sekali

# Native build (sekali per platform) — pilih salah satu:
npx expo prebuild --platform android
npx expo run:android

# atau:
npx expo prebuild --platform ios
npx expo run:ios
```

Setelah build native pertama, iterasi cepat dengan:

```bash
npx expo start --dev-client
```

> **Catatan**: aplikasi memakai `react-native-maps` (`PROVIDER_GOOGLE`) +
> `expo-blur` (glassmorphism); **Expo Go tidak cukup**, harus dev-build.

### 4.5 Verifikasi tanpa kredensial Supabase

Jika Anda hanya ingin memastikan kode build & test:

```bash
# Backend
cd backend && ./mvnw verify          # 108 tests pass, fat jar repackaged
python3 scripts/smoke-test.py        # 9 skenario E2E pass via H2

# Frontend
cd frontend && npx tsc --noEmit && npx expo lint && npm test
# tsc clean, lint clean, 28 tests pass
```

Itu semua bisa dijalankan **sekarang juga** tanpa setup Supabase apapun.

### 4.6 Status runnability per fitur

| Fitur | Bisa dijalankan tanpa setup tambahan? | Yang dibutuhkan untuk full run |
|---|---|---|
| Fondasi peta + graf (Fitur 0) | ✅ Smoke test otomatis pass tanpa DB | Supabase / Postgres lokal hanya untuk run dev backend penuh |
| Pencarian rute A* (Fitur 3) | ✅ Smoke test otomatis pass | Sama seperti di atas + frontend `.env` lengkap untuk emulator |
| Login Google (Fitur 1) | ❌ Butuh setup penuh | Supabase project + JWT secret + anon key + redirect URL terdaftar |

```bash
npx expo start --dev-client
```

> **Catatan**: aplikasi memakai `react-native-maps` (`PROVIDER_GOOGLE`) +
> `expo-blur` (glassmorphism); **Expo Go tidak cukup**, harus dev-build.

### 4.4 Test & verifikasi

| Layer | Perintah | Hasil saat ini |
|---|---|---|
| Frontend | `npx tsc --noEmit` | ✅ |
| Frontend | `npx expo lint` | ✅ |
| Frontend | `npm test` | ✅ 19 tests (coords + geojson-service + routing-service) |
| Backend | `./mvnw verify` | ✅ 96 tests + repackage fat-jar |
| Backend (smoke E2E) | `python3 backend/scripts/smoke-test.py` | ✅ 9 skenario via HTTP |
| **Total** | | **115 tests pass, 0 failure** |

---

## 5. Endpoint backend

| Method | Path | Tujuan |
|---|---|---|
| `GET` | `/api/v1/health` | Smoke check umum |
| `GET` | `/api/v1/graph/meta` | Ringkasan graf: `nodeCount`, `edgeCount`, BBox, parser, `loadedAt` |
| `GET` | `/api/v1/graph/geojson` | GeoJSON mentah dengan `ETag` (mendukung `If-None-Match` → `304`) |
| `POST` | `/api/v1/route` | A\* shortest path: body `{from, to, mode}` → `{lengthMeters, durationSeconds, coordinates, nodeIds}` (FR-RT-01..09) |

Endpoint auth dan history akan ditambahkan di fase berikutnya — interface
`MapDataParser`, `Heuristic`, dan domain `Route` sudah dirancang
mendukung perluasan (Open-Closed) tanpa refactor.

---

## 6. Roadmap fitur

| Fase | Fokus | Status |
|---|---|---|
| **0 — Fondasi** | Peta + tema + GeoJSON loader + REST dasar | ✅ Selesai |
| **1 — Auth** | Registrasi & Login (OAuth Google via Supabase) | 📝 Spec draft |
| **2 — Manajemen Graf** | Polygon gedung & jalan tikus tambahan, parser kustom | ⏳ Belum dimulai |
| **3 — Pencarian A\*** | 3 moda transportasi (jalan kaki / motor / mobil) | ✅ Selesai |
| **4 — Riwayat** | Riwayat pencarian rute pengguna login | ⏳ Belum dimulai |

Tiap fase nanti memiliki spec modular tersendiri di `specs/<modul>/`.

---

## 7. Spesifikasi (SDD)

Dokumen Spec-Driven Development:

- 📋 [`specs/README.md`](specs/README.md) — indeks modul SDD
- 📋 [`specs/foundation-map/requirements.md`](specs/foundation-map/requirements.md) — fondasi peta (EARS)
- 📋 [`specs/foundation-map/design.md`](specs/foundation-map/design.md) — fondasi: arsitektur, palette, parser strategy
- 📋 [`specs/foundation-map/tasks.md`](specs/foundation-map/tasks.md) — fondasi: rencana implementasi
- 📋 [`specs/feature-3-routing-astar/requirements.md`](specs/feature-3-routing-astar/requirements.md) — Fitur 3 routing (EARS) ✅
- 📋 [`specs/feature-3-routing-astar/design.md`](specs/feature-3-routing-astar/design.md) — Fitur 3: Heuristic + Profile + Router A\* ✅
- 📋 [`specs/feature-3-routing-astar/tasks.md`](specs/feature-3-routing-astar/tasks.md) — Fitur 3: rencana implementasi ✅

---

## 8. Tim

Kelompok 3 — IF4806 Object-Oriented Programming, Telkom University.

## 9. Lisensi

Proyek tugas akademik. Konten OSM (telmap.geojson) tunduk pada lisensi
**ODbL** dari OpenStreetMap.
>>>>>>> 076dfd1 (progres yang ada aja)
