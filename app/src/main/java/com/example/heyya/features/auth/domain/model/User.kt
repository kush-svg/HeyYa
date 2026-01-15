package com.example.heyya.features.auth.domain.model


data class User(
    val uid: String = "",
    val name: String? = "",
    val profilePic: String? = "",
    val email: String? = ""
)