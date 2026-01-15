package com.example.heyya.features.auth.data.repository

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.heyya.R
import com.example.heyya.core.domain.Result
import com.example.heyya.features.auth.domain.model.User
import com.example.heyya.features.auth.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthRepository {

    override suspend fun loginWithGoogle(context: Context): Result<Boolean> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential

            val idToken = when {
                credential is GoogleIdTokenCredential -> {
                    credential.idToken
                }
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data).idToken
                    } catch (e: Exception) {
                        android.util.Log.e("AuthRepo", "Failed to extract Google ID token", e)
                        null
                    }
                }
                else -> null
            }
            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                // 1. Sign in to Firebase Auth
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user

                // 2. Save user to Realtime Database
                firebaseUser?.let {
                    val userMap = mapOf(
                        "uid" to it.uid,
                        "name" to it.displayName,
                        "email" to it.email,
                        "profilePic" to it.photoUrl.toString()
                    )
                    database.reference.child("users").child(it.uid).setValue(userMap).await()
                }

                Result.Success(true)
            } else {
                // If idToken is still null, it means the type was truly unexpected
                Result.Error("Unexpected credential type: ${credential.type}")
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Authentication failed")
        }
    }
    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let {
            User(
                uid = it.uid,
                name = it.displayName,
                email = it.email,
                profilePic = it.photoUrl?.toString()
            )
        }
    }
}
