package com.example.heyya.core.data


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object FirebaseModules {
    val auth: FirebaseAuth get() = FirebaseAuth.getInstance()
    val database: FirebaseDatabase get() = FirebaseDatabase.getInstance("https://heyya-44603-default-rtdb.firebaseio.com/")
}
