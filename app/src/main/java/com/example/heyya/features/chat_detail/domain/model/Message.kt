package com.example.heyya.features.chat_detail.domain.model

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val timestamp: Long = 0
)
