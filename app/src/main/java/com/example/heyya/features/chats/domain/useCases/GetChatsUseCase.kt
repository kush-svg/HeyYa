package com.example.heyya.features.chats.domain.useCases

import com.example.heyya.features.chats.data.repository.ChatRepositoryImpl
import com.example.heyya.features.chats.domain.model.ChatUser
import kotlinx.coroutines.flow.Flow

class GetChatsUseCase(
    private val repository: ChatRepositoryImpl
) {
    operator fun invoke(): Flow<List<ChatUser>> {
        return repository.getAllUsers()
    }
}