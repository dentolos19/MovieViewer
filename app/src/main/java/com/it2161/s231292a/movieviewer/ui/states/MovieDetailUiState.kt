package com.it2161.s231292a.movieviewer.ui.states

import com.it2161.s231292a.movieviewer.data.entities.MovieDetail
import com.it2161.s231292a.movieviewer.data.entities.MovieReview

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val isLoadingReviews: Boolean = false,
    val isOnline: Boolean = true,
    val isFavorite: Boolean = false,
    val error: String? = null,
    val movieDetail: MovieDetail? = null,
    val reviews: List<MovieReview> = emptyList(),
)

