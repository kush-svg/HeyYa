package com.example.heyya.features.auth.domain.model

data class User(
    val uid: String,
    val name: String?,
    val email: String?,
    val profilePic: String?
)