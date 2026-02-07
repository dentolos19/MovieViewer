package com.it2161.s231292a.movieviewer.data.dto

import com.google.gson.annotations.SerializedName

data class MovieReviewAuthorDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("avatar_path")
    val avatarPath: String?,
    @SerializedName("rating")
    val rating: Double?
)
