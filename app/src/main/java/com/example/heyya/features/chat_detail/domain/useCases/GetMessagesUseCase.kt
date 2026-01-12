package com.example.heyya.features.chat_detail.domain.useCases

import com.example.heyya.features.chat_detail.data.repository.MessageRepositoryImpl
import com.example.heyya.features.chat_detail.domain.model.Message
import kotlinx.coroutines.flow.Flow

class GetMessagesUseCase(
    private val repository: MessageRepositoryImpl
) {
    operator fun invoke(roomId: String): Flow<List<Message>> {
        return repository.getMessages(roomId)
    }
}