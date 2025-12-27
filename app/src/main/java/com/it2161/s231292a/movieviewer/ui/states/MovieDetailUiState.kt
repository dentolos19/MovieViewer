package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.MovieDetail
import com.it2161.s231292a.movieviewer.data.entities.MovieReview

data class MovieDetailUiState(
    val movieDetail: MovieDetail? = null,
    val reviews: List<MovieReview> = emptyList(),
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val error: String? = null,
    val isOnline: Boolean = true
)

