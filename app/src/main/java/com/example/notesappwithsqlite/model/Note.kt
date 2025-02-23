package com.example.notesappwithsqlite.model

data class Note(
    var folderID : Int,
    val id: Int,
    var subject: String,
    var date: String,
    val title: String,
    val content: String,
    val priority: String
)