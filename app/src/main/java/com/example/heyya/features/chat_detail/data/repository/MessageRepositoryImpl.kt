package com.example.heyya.features.chat_detail.data.repository

import com.example.heyya.core.domain.Result // Import your custom Result
import com.example.heyya.features.chat_detail.domain.model.Message
import com.example.heyya.features.chat_detail.domain.repository.MessageRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepositoryImpl(
    private val db: FirebaseDatabase
) : MessageRepository {

    override suspend fun sendMessage(roomId: String, message: Message): Result<Boolean> {
        return try {
            db.reference.child("messages")
                .child(roomId)
                .push()
                .setValue(message)
                .await()

            val senderId = message.senderId
            val receiverId = message.receiverId

            val senderChatUpdate = mapOf(
                "uid" to receiverId,
                "name" to message.receiverName,
                "lastMessage" to message.text,
                "lastMessageTimestamp" to message.timestamp,
                "profilePic" to ""
            )

            val receiverChatUpdate = mapOf(
                "uid" to senderId,
                "name" to message.senderName, // Make sure Message model has senderName
                "lastMessage" to message.text,
                "lastMessageTimestamp" to message.timestamp,
                "profilePic" to ""
            )

            db.reference.child("recent_chats").child(senderId).child(receiverId)
                .updateChildren(senderChatUpdate)
                .await()

            db.reference.child("recent_chats").child(receiverId).child(senderId)
                .updateChildren(receiverChatUpdate)
                .await()

            Result.Success(true) // Use Capital S for your custom class
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to send message")
        }
    }

    override fun getMessages(roomId: String): Flow<List<Message>> = callbackFlow {
        val messageRef = db.reference.child("messages").child(roomId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { doc ->
                    try {
                        // Safety check: only attempt to parse if it's an object (Map)
                        if (doc.value is Map<*, *>) {
                            doc.getValue(Message::class.java)
                        } else {
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        messageRef.addValueEventListener(listener)
        awaitClose { messageRef.removeEventListener(listener) }
    }
}
