package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.ui.states.MovieDetailUiState
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MovieDetailViewModel(
    private val movieId: Int,
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
        observeFavoriteStatus()
        loadMovieDetail()
        loadReviews()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesRepository.isFavorite(movieId).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val isOnline = networkMonitor.isCurrentlyConnected()
            when (val result = movieRepository.getMovieDetail(movieId, isOnline)) {
                is NetworkResource.Success -> {
                    _uiState.update {
                        it.copy(
                            movieDetail = result.data,
                            isLoading = false,
                            error = null
                        )
                    }
                }

                is NetworkResource.Error -> {
                    _uiState.update {
                        it.copy(
                            movieDetail = result.data,
                            isLoading = false,
                            error = if (result.data == null) result.message else null
                        )
                    }
                }

                is NetworkResource.Loading -> {}
            }
        }
    }

    fun loadReviews() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReviews = true) }

            val isOnline = networkMonitor.isCurrentlyConnected()
            when (val result = movieRepository.getMovieReviews(movieId, isOnline)) {
                is NetworkResource.Success -> {
                    _uiState.update {
                        it.copy(
                            reviews = result.data ?: emptyList(),
                            isLoadingReviews = false
                        )
                    }
                }

                is NetworkResource.Error -> {
                    _uiState.update {
                        it.copy(
                            reviews = result.data ?: emptyList(),
                            isLoadingReviews = false
                        )
                    }
                }

                is NetworkResource.Loading -> {}
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            withContext(NonCancellable) {
                favoritesRepository.toggleFavorite(movieId)
            }
        }
    }

    companion object {
        fun provideFactory(
            movieId: Int,
            movieRepository: MovieRepository,
            favoritesRepository: FavoritesRepository,
            networkMonitor: NetworkMonitor
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MovieDetailViewModel(
                        movieId,
                        movieRepository,
                        favoritesRepository,
                        networkMonitor
                    ) as T
                }
            }
        }
    }
}
