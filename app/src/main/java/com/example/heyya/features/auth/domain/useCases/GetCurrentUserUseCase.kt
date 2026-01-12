package com.example.heyya.features.auth.domain.useCases

import com.example.heyya.features.auth.domain.model.User
import com.example.heyya.features.auth.domain.repository.AuthRepository

class GetCurrentUserUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): User? {
        return repository.getCurrentUser()
    }
}