package com.example.heyya.features.auth.domain.repository

import android.content.Context
import com.example.heyya.core.domain.Result
import com.example.heyya.features.auth.domain.model.User

interface AuthRepository {
    suspend fun loginWithGoogle(context: Context): Result<Boolean>
    fun getCurrentUser(): User?
}
