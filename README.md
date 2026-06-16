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
```

---

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





