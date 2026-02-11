package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.ui.states.MovieReviewsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieReviewsViewModel(
    private val movieId: Int,
    private val movieRepository: MovieRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieReviewsUiState())
    val uiState: StateFlow<MovieReviewsUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
        loadMovieTitle()
        loadReviews()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    private fun loadMovieTitle() {
        viewModelScope.launch {
            val movie = movieRepository.getMovieById(movieId)
            if (movie != null) {
                _uiState.update { it.copy(movieTitle = movie.title) }
            }
        }
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val isOnline = networkMonitor.isCurrentlyConnected()
            when (val result = movieRepository.getMovieReviews(movieId, isOnline)) {
                is NetworkResource.Success -> {
                    _uiState.update {
                        it.copy(
                            reviews = (result.data ?: emptyList()).distinctBy { review -> review.id },
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is NetworkResource.Error -> {
                    _uiState.update {
                        it.copy(
                            reviews = (result.data ?: emptyList()).distinctBy { review -> review.id },
                            isLoading = false,
                            error = if (result.data.isNullOrEmpty()) result.message else null
                        )
                    }
                }

                is NetworkResource.Loading -> {}
            }
        }
    }

    companion object {
        fun provideFactory(
            movieId: Int,
            movieRepository: MovieRepository,
            networkMonitor: NetworkMonitor
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MovieReviewsViewModel(movieId, movieRepository, networkMonitor) as T
                }
            }
        }
    }
}
