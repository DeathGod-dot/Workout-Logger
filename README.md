# 🏋️ Workout Logger

A modern, data-driven Android application for tracking workouts, monitoring progress, and maintaining consistency. Built with a clean architecture and a focus on performance, usability, and scalability.

---

## 📌 Overview

**Workout Logger** is designed to help users efficiently log workouts, analyze performance trends, and stay consistent with their fitness goals. The application combines a minimal, dark-themed UI with powerful tracking and analytics features.

---

## ✨ Features

### 🔐 Authentication & User Profile

* Secure authentication via Google Sign-In
* Customizable user profile (display name & profile image)
* Persistent user data with cloud synchronization

### 📊 Workout Tracking & Analytics

* Log workouts with detailed set and weight tracking
* Automatic Personal Record (PR) detection
* Real-time statistics (total workouts, volume, PRs)
* Estimated One-Rep Max (1RM) calculations

### 📈 Progress Visualization

* Interactive charts for:

    * Maximum weight progression
    * Training volume over time
* Built using Vico charting library

### ⏱️ Consistency & Engagement

* Daily workout streak tracking
* Built-in rest timer between sets
* Scheduled reminders with motivational prompts

### ☁️ Data Management

* Local storage using Room database
* Cloud backup and restore via Firebase Firestore
* Seamless multi-device data synchronization

---

## 🎨 UI & Design

The application follows a **dark, minimal design system** focused on readability and usability:

| Element        | Color Code |
| -------------- | ---------- |
| Background     | `#0D1526`  |
| Surface        | `#1E2A4A`  |
| Primary Accent | `#3B82F6`  |
| Success        | `#10B981`  |
| Highlight      | `#F59E0B`  |

---

## 🏗️ Architecture

The project follows **MVVM (Model-View-ViewModel)** architecture for clear separation of concerns and scalability.

**Key principles:**

* Unidirectional data flow
* State-driven UI with Jetpack Compose
* Repository pattern for data handling

---

## 🧰 Tech Stack

| Layer        | Technology                |
| ------------ | ------------------------- |
| Language     | Kotlin                    |
| UI           | Jetpack Compose           |
| Architecture | MVVM                      |
| Local DB     | Room                      |
| Cloud        | Firebase Auth & Firestore |
| Charts       | Vico                      |
| Image Loader | Coil                      |
| Preferences  | DataStore                 |

---

## 🚀 Getting Started

### Prerequisites

* Android Studio (latest stable version recommended)
* Firebase project

### Installation

```bash
git clone https://github.com/DeathGod-dot/Workout-Logger.git
cd Workout-Logger
```

### Firebase Setup

1. Create a Firebase project
2. Add an Android app with package name:

   ```
   com.example.workoutlogger
   ```
3. Generate SHA-1:

   ```bash
   ./gradlew signingReport
   ```
4. Add SHA-1 to Firebase
5. Download `google-services.json` and place it in:

   ```
   app/google-services.json
   ```
6. Enable:

    * Google Sign-In
    * Firestore Database
7. Update Web Client ID in:

   ```
   GoogleAuthUiClient.kt
   ```

### Build

```bash
./gradlew assembleDebug
```

---

## 📷 Screenshots

| Dashboard          | Progress           | Profile            |
| ------------------ | ------------------ | ------------------ |
| *(Add screenshot)* | *(Add screenshot)* | *(Add screenshot)* |

---

## 🤝 Contributing

Contributions are welcome. If you'd like to improve the project:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---

## 🐛 Reporting Issues

If you encounter any issues:

* Open a GitHub issue
* Or use the in-app reporting feature

---

## 📄 License

This project is licensed under the MIT License. *(Add LICENSE file if not present)*

---

## 👨‍💻 Author

**Shubham**
GitHub: [https://github.com/DeathGod-dot](https://github.com/DeathGod-dot)

---