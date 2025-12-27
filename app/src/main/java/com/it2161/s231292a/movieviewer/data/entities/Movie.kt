package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    val id: Int,
    val title: String,
    val originalTitle: String,
    val originalLanguage: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val adult: Boolean,
    val video: Boolean,
    val genreIds: String, // Stored as comma-separated string
    val category: String, // "popular", "top_rated", "now_playing", "upcoming"
    val cachedAt: Long = System.currentTimeMillis()
)
