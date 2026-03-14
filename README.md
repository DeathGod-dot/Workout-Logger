# 🏋️ WorkoutLogger

WorkoutLogger is a simple and efficient Android application designed to help you track your fitness progress. Built with modern Android development practices, it provides a clean interface to log your exercises and view your workout performance at a glance.

## ✨ Features

- **Exercise Tracking**: Easily log your exercises including name, sets, reps, and weight.
- **Workout Summary**: Real-time calculation of total exercises, total sets, and total volume (kg).
- **Manage Exercises**: Edit or remove exercises as needed during your workout.
- **Modern UI**: Clean and intuitive interface built with Jetpack Compose and Material 3.
- **Efficient Input**: Convenient bottom sheet form for adding and updating exercises.

## 🚀 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Design System**: [Material 3](https://m3.material.io/)
- **Architecture**: Clean architecture with state management using Compose `remember` and `mutableStateListOf`.

## 📸 Screenshots

| Workout Overview | Add Exercise |
| :---: | :---: |
| ![Workout Screen](https://via.placeholder.com/300x600?text=Workout+Screen) | ![Add Exercise](https://via.placeholder.com/300x600?text=Add+Exercise) |

## 🛠️ Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/yourusername/WorkoutLogger.git
    ```
2.  **Open in Android Studio:**
    Import the project and wait for Gradle sync to complete.
3.  **Run the app:**
    Select an emulator or physical device and click the **Run** button.

## 🏗️ Project Structure

- `data/`: Contains the `Exercise` data class.
- `ux/screen/`: Includes the main `WorkoutScreen`, `AddExerciseForm`, and `WorkoutStatsCard`.
- `ux/component/`: Reusable UI components like `ExerciseItem`.
- `ui/theme/`: App styling and theme definitions.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
