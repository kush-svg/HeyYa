package com.example.heyya.features.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.heyya.features.auth.domain.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    
    val searchResults: StateFlow<List<User>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                searchUsersFromDatabase(query)
            }
        }
        .stateIn(
            scope = viewModelScope, 
            started = SharingStarted.WhileSubscribed(5000), 
            initialValue = emptyList()
        )
    
    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun searchUsersFromDatabase(query: String): Flow<List<User>> =
        callbackFlow {
        val database =
            FirebaseDatabase.getInstance().getReference("users")

        // Search for users where name starts with the query
        val queryRef = database.orderByChild("name")
            .startAt(query)
            .endAt(query + "\uf8ff")

        val listener = queryRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val users = snapshot.children.mapNotNull { childSnapshot ->
                    try {
                        if (childSnapshot.value is Map<*, *>) {
                            childSnapshot.getValue(User::class.java)
                        } else {
                            null
                        }
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(users)
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
            awaitClose { queryRef.removeEventListener(listener) }
    }
}
