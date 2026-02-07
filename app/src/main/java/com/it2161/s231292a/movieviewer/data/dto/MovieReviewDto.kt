package com.it2161.s231292a.movieviewer.data.dto

import com.google.gson.annotations.SerializedName

data class MovieReviewDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("author_details")
    val authorDetails: MovieReviewAuthorDto,
    @SerializedName("content")
    val content: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("url")
    val url: String
)

