package com.example.heyya.features.chat_detail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.heyya.core.data.FirebaseModules
import com.example.heyya.features.chat_detail.domain.model.Message
import com.example.heyya.features.chat_detail.domain.useCases.GetMessagesUseCase
import com.example.heyya.features.chat_detail.domain.useCases.SendMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.heyya.core.domain.Result
import com.example.heyya.features.chat_detail.data.repository.MessageRepositoryImpl

class MessageViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getMessagesUseCase: GetMessagesUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _sendResult = MutableStateFlow<Result<Boolean>?>(null)
    val sendResult: StateFlow<Result<Boolean>?> = _sendResult.asStateFlow()

    fun listenForMessages(roomId: String) {
        viewModelScope.launch {
            getMessagesUseCase(roomId).collect { messageList ->
                _messages.value = messageList
            }
        }
    }

    fun sendMessage(roomId: String, message: Message, senderId: String, senderName: String) {
        val message = Message(
            senderId = senderId,
            senderName = senderName,
            text = message.text,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            val result = sendMessageUseCase(roomId, message)
            _sendResult.value = result
        }
    }
    fun resetSendStatus() {
        _sendResult.value = null
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = MessageRepositoryImpl(FirebaseModules.database)
                MessageViewModel(
                    sendMessageUseCase = SendMessageUseCase(repository),
                    getMessagesUseCase = GetMessagesUseCase(repository)
                )
            }
        }
    }
}