package com.it2161.s231292a.movieviewer.data.dto

import com.google.gson.annotations.SerializedName

data class MovieDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("original_title")
    val originalTitle: String,
    @SerializedName("original_language")
    val originalLanguage: String,
    @SerializedName("overview")
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    @SerializedName("backdrop_path")
    val backdropPath: String?,
    @SerializedName("release_date")
    val releaseDate: String?,
    @SerializedName("vote_average")
    val voteAverage: Double,
    @SerializedName("vote_count")
    val voteCount: Int,
    @SerializedName("popularity")
    val popularity: Double,
    @SerializedName("adult")
    val adult: Boolean,
    @SerializedName("video")
    val video: Boolean,
    @SerializedName("genres")
    val genres: List<MovieGenreDto>,
    @SerializedName("runtime")
    val runtime: Int?,
    @SerializedName("revenue")
    val revenue: Long,
    @SerializedName("budget")
    val budget: Long,
    @SerializedName("status")
    val status: String,
    @SerializedName("tagline")
    val tagline: String?,
    @SerializedName("homepage")
    val homepage: String?,
    @SerializedName("imdb_id")
    val imdbId: String?
)


