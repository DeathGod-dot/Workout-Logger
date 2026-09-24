package com.example.workoutlogger.auth

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

class GoogleAuthUiClient(
    private val context: Context,
    private val onTapClient: SignInClient
) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(): IntentSender? {
        val result = try {
            onTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = onTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
                ?: return SignInResult(data = null, errorMessage = "No Google ID Token found")
            val googleIdCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user = auth.signInWithCredential(googleIdCredential).await().user
            SignInResult(
                data = user?.run {
                    UserData(
                        userId = uid,
                        username = displayName,
                        profilePictureUrl = photoUrl?.toString()
                    )
                },
                errorMessage = null
            )
        } catch(e: Exception) {
            e.printStackTrace()
            SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    suspend fun signOut() {
        try {
            onTapClient.signOut().await()
            auth.signOut()
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        val serverClientId = context.getString(com.example.workoutlogger.R.string.default_web_client_id)
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
}
