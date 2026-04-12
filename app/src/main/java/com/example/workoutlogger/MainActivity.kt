package com.example.workoutlogger

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workoutlogger.auth.GoogleAuthUiClient
import com.example.workoutlogger.data.FirestoreManager
import com.example.workoutlogger.data.WorkoutDatabase
import com.example.workoutlogger.data.preferences.SettingsManager
import com.example.workoutlogger.ui.theme.WorkoutLoggerTheme
import com.example.workoutlogger.util.NotificationHelper
import com.example.workoutlogger.ux.screen.SignInScreen
import com.example.workoutlogger.ux.screen.WorkoutScreen
import com.example.workoutlogger.viewmodel.SettingsViewModel
import com.example.workoutlogger.viewmodel.SignInViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            onTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private val settingsManager by lazy { SettingsManager(applicationContext) }
    private val notificationHelper by lazy { NotificationHelper(applicationContext) }
    private val database by lazy {
        androidx.room.Room.databaseBuilder(
            applicationContext,
            WorkoutDatabase::class.java,
            "workout_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    private val firestoreManager by lazy { FirestoreManager(database.exerciseDao()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SettingsViewModel(settingsManager, firestoreManager, database.exerciseDao()) as T
                    }
                }
            )
            val darkMode by settingsViewModel.darkMode.collectAsStateWithLifecycle()
            val keepScreenOn by settingsViewModel.keepScreenOn.collectAsStateWithLifecycle()
            val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                settingsViewModel.checkAndResetStreak()
            }

            // Request Notification Permission for Android 13+
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    if (!isGranted) {
                        settingsViewModel.setNotificationsEnabled(false)
                    }
                }
            )

            LaunchedEffect(notificationsEnabled) {
                if (notificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            LaunchedEffect(keepScreenOn) {
                if (keepScreenOn) {
                    window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            WorkoutLoggerTheme(darkTheme = darkMode) {
                val viewModel = viewModel<SignInViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit) {
                    if(googleAuthUiClient.getSignedInUser() != null) {
                        // User already signed in
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if(result.resultCode == RESULT_OK) {
                            lifecycleScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                viewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )

                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if(state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Sign in successful",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val signedInUser = googleAuthUiClient.getSignedInUser()
                    if(signedInUser != null || state.isSignInSuccessful) {
                        WorkoutScreen(
                            userData = signedInUser,
                            onSignOut = {
                                lifecycleScope.launch {
                                    googleAuthUiClient.signOut()
                                    viewModel.resetState()
                                    Toast.makeText(
                                        applicationContext,
                                        "Signed out",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            settingsViewModel = settingsViewModel,
                            notificationHelper = notificationHelper,
                            database = database
                        )
                    } else {
                        SignInScreen(
                            onSignInClick = {
                                lifecycleScope.launch {
                                    val signInIntentSender = googleAuthUiClient.signIn()
                                    launcher.launch(
                                        IntentSenderRequest.Builder(
                                            signInIntentSender ?: return@launch
                                        ).build()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
