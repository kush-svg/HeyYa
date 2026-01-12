package com.example.heyya.features.chats.domain.repository

import com.example.heyya.features.chats.domain.model.ChatUser
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllUsers(): Flow<List<ChatUser>>
}