package com.example.workoutlogger.ux.screen

sealed class Screen(val route: String) {
    object Workout : Screen("home")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
}