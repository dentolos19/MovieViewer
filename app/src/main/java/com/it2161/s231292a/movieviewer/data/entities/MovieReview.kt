package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_reviews")
data class MovieReview(
    @PrimaryKey
    val id: String,
    val movieId: Int,
    val author: String,
    val authorUsername: String,
    val authorAvatarPath: String?,
    val authorRating: Double?,
    val content: String,
    val createdAt: String,
    val updatedAt: String,
    val url: String,
    val cachedAt: Long = System.currentTimeMillis()
)
