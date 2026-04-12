# 🏋️ Workout Logger - Premium Fitness Tracker

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack-Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Auth%20%26%20Firestore-orange.svg)](https://firebase.google.com/)

A high-fidelity, professional-grade Android workout tracker designed for serious lifters. **Workout Logger** combines a premium dark aesthetic with powerful data-driven features to help you crush your fitness goals.

---

## ✨ Key Features

### 👤 **Premium Identity**
*   **Google Sign-In**: Secure and seamless authentication.
*   **Customizable Profile**: Personalized display names and permanent profile picture storage.
*   **Real-time Stats**: Track your total workouts, lifetime volume, and PR count at a glance.

### 📈 **Advanced Analytics**
*   **Progress Graphs**: Beautiful, interactive line charts (powered by Vico) tracking your Max Weight and Volume over time.
*   **Smart PR Detection**: Automated trophies and notifications when you hit new Personal Records.
*   **Estimated 1RM**: Real-time 1-Rep Max calculations using professional formulas.

### ⚡ **Engagement & Consistency**
*   **Functional Streaks**: An automated system that tracks and celebrates your daily consistency.
*   **Energetic Reminders**: Daily, precisely scheduled workout alerts with randomized motivational messages.
*   **Integrated Rest Timer**: A themed modal timer that triggers automatically after logging sets.

### ☁️ **Cloud Infrastructure**
*   **Firestore Backup**: Sync your entire local database to the cloud with one tap.
*   **One-Click Restore**: Seamlessly download your data to a new device.
*   **Data Integrity**: Full support for decimal weight values (e.g., 1.5kg) and instant unit conversion (KG ↔ LBS).

---

## 🎨 Design System

The app follows a **Deep Dark Premium** aesthetic:
- **Background**: `#0D1526` (Deep Navy)
- **Surfaces**: `#1E2A4A` (Card Slate)
- **Primary Accent**: `#3B82F6` (Electric Blue)
- **Achievements**: `#F59E0B` (Gold)
- **Success**: `#10B981` (Vibrant Green)

---

## 🚀 Tech Stack

*   **UI**: Jetpack Compose (100% Declarative)
*   **Database**: Room (Local Persistence)
*   **Backend**: Firebase Auth & Firestore (Cloud Storage)
*   **Image Loading**: Coil
*   **Charts**: Vico Charts
*   **Storage**: DataStore (Persistent Settings)
*   **Architecture**: MVVM (Model-View-ViewModel)

---

## 🛠️ Setup Instructions

### 1. Prerequisites
*   Android Studio Ladybug or newer.
*   A Firebase Project.

### 2. Firebase Configuration
1.  Add your Android app to Firebase using package name `com.example.workoutlogger`.
2.  Run `./gradlew signingReport` to get your SHA-1 key and add it to Firebase.
3.  Download `google-services.json` and place it in the `app/` directory.
4.  Enable **Google Sign-In** and **Firestore** in the Firebase Console.
5.  Update the **Web Client ID** in `GoogleAuthUiClient.kt`.

### 3. Build & Run
```bash
git clone https://github.com/DeathGod-dot/Workout-Logger.git
cd Workout-Logger
./gradlew assembleDebug
```

---

## 📸 Preview

| Home Dashboard | Progress Insights | Settings & Profile |
| :---: | :---: | :---: |
| 🏋️‍♂️ | 📈 | ⚙️ |
| *(Add your screenshots here)* | *(Add your screenshots here)* | *(Add your screenshots here)* |

---

## 🤝 Support & Contribution

Enjoying the app? Leave a ⭐ on the repository!  
Found a bug? Use the **Report a Bug** feature directly in the app settings to reach out.

**Developed with ❤️ by [Shubham](https://github.com/DeathGod-dot)**
