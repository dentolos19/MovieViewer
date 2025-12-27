package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val password: String,
    val preferredName: String,
    val dateOfBirth: Int,
    val profilePicture: Boolean
)
