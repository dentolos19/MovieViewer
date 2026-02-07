package com.it2161.s231292a.movieviewer.data.dto

import com.google.gson.annotations.SerializedName

data class MovieReviewListResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("results")
    val results: List<MovieReviewDto>,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_results")
    val totalResults: Int
)
