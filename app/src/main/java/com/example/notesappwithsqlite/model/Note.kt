package com.example.notesappwithsqlite.model

data class Note(
    val id: Int,
    var subject: String,
    var date: String,
    val title: String,
    val content: String
)