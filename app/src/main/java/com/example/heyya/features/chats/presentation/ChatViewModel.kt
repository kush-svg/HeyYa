package com.example.heyya.features.chats.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.heyya.core.data.FirebaseModules
import com.example.heyya.features.chats.data.repository.ChatRepositoryImpl
import com.example.heyya.features.chats.domain.model.ChatUser
import com.example.heyya.features.chats.domain.useCases.GetChatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getChatsUseCase: GetChatsUseCase
) : ViewModel() {

    private val _users = MutableStateFlow<List<ChatUser>>(emptyList())
    val users: StateFlow<List<ChatUser>> = _users

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            getChatsUseCase().collect {
                _users.value = it
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repository = ChatRepositoryImpl(FirebaseModules.database)
                ChatViewModel(getChatsUseCase = GetChatsUseCase(repository))
            }
        }
    }
}