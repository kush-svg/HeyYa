package com.example.heyya.features.chat_detail.domain.repository

import com.example.heyya.features.chat_detail.domain.model.Message
import com.example.heyya.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun sendMessage(roomId: String, message: Message): Result<Boolean>
    fun getMessages(roomId: String): Flow<List<Message>>
}