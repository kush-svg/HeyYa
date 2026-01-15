package com.example.heyya.features.chats.data.repository

import com.example.heyya.features.chats.domain.model.ChatUser
import com.example.heyya.features.chats.domain.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatRepositoryImpl(
    private val db: FirebaseDatabase
) : ChatRepository {

    override fun getAllUsers(): Flow<List<ChatUser>> = callbackFlow {
        val currentUserid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val recentChatsRef = db.reference.child("recent_chats").child(currentUserid)


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { doc ->
                    try {
                        if(doc.value is Map<*, *>) {
                            doc.getValue(ChatUser::class.java)
                        } else {
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }.sortedByDescending { it.lastMessageTimestamp }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        recentChatsRef.addValueEventListener(listener)

        awaitClose { recentChatsRef.removeEventListener(listener) }
    }
}
