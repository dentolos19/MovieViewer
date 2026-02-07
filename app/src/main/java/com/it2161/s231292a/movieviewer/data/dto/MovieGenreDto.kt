package com.it2161.s231292a.movieviewer.data.dto

import com.google.gson.annotations.SerializedName

data class MovieGenreDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
)
