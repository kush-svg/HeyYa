package com.example.heyya.features.chats.domain.model

data class ChatUser(
    val id: String,
    val name: String,
    val profilePictureUrl: String,
    val lastMessage: String = "",
    val lastMessageTimestamp: Long? = null
)

