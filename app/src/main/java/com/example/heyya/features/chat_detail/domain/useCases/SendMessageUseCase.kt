package com.example.heyya.features.chat_detail.domain.useCases

import com.example.heyya.core.domain.Result
import com.example.heyya.features.chat_detail.data.repository.MessageRepositoryImpl
import com.example.heyya.features.chat_detail.domain.model.Message

class SendMessageUseCase(
    private val repository: MessageRepositoryImpl
) {
    suspend operator fun invoke(roomId: String, message: Message): Result<Boolean> {
        if (message.text.isBlank()) {
            return Result.Error("Message cannot be empty")
        }
        return repository.sendMessage(roomId, message)
    }
}
