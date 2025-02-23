package com.example.notesappwithsqlite.model

data class Folder(
    val id: Int,
    var name: String,
    val date: String,
    val userId: Int // Add userId field
)
