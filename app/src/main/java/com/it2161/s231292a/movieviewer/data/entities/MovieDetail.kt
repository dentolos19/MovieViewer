package com.it2161.s231292a.movieviewer.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movie_details")
data class MovieDetail(
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
    val genres: String, // JSON string of genres
    val runtime: Int?,
    val revenue: Long,
    val budget: Long,
    val status: String,
    val tagline: String?,
    val homepage: String?,
    val imdbId: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
