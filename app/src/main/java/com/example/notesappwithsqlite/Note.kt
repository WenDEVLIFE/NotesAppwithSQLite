package com.example.notesappwithsqlite

import java.sql.Date

data class Note(
    val id: Int,
    var subject: String,
    var date: String,
    val title: String,
    val content: String
)


