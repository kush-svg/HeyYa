package com.example.heyya.features.chats.domain.model

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class ChatUser(
    val uid: String = "",
    val name: String = "",
    val profilePic: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long? = null
)

