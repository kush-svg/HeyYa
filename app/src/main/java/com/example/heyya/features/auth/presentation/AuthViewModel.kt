package com.example.heyya.features.auth.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heyya.core.data.FirebaseModules
import com.example.heyya.core.domain.Result
import com.example.heyya.features.auth.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.heyya.features.auth.domain.useCases.GetCurrentUserUseCase



class AuthViewModel : ViewModel() {

    private val repository = AuthRepositoryImpl(
        auth = FirebaseModules.auth,
        database = FirebaseModules.database
    )

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val currentUser = GetCurrentUserUseCase(repository).invoke()
        _isAuthenticated.value = currentUser != null
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginWithGoogle(context: Context) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            when (val result = repository.loginWithGoogle(context)) {
                is Result.Success -> {
                    _isAuthenticated.value = true
                    _loginState.value = LoginState.Success
                }
                is Result.Error -> {
                    _loginState.value = LoginState.Error(result.message ?: "Unknown error")
                }
            }
        }
    }

    fun getCurrentUser() = repository.getCurrentUser()
}
