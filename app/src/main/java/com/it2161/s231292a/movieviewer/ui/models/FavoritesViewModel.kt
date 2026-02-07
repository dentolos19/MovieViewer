package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.ui.states.FavoritesUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.it2161.s231292a.movieviewer.data.toMovie

class FavoritesViewModel(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
        observeFavorites()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            favoritesRepository.favoriteMovieIds
                .flatMapLatest { favoriteIds ->
                    if (favoriteIds.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        movieRepository.getMovieDetailsByIds(favoriteIds.toList())
                            .map { cachedDetails ->
                                val cachedMovies = cachedDetails.map { it.toMovie() }
                                val cachedIds = cachedDetails.map { it.id }.toSet()
                                val missingIds = favoriteIds - cachedIds

                                if (missingIds.isNotEmpty() && _uiState.value.isOnline) {
                                    _uiState.update { it.copy(loadingMovieIds = missingIds) }
                                    fetchMissingMovieDetails(missingIds)
                                }

                                cachedMovies
                            }
                    }
                }
                .collect { movies ->
                    _uiState.update {
                        it.copy(
                            movies = movies,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun fetchMissingMovieDetails(movieIds: Set<Int>) {
        viewModelScope.launch {
            movieIds.forEach { movieId ->
                try {
                    val result = movieRepository.getMovieDetail(movieId, _uiState.value.isOnline)
                    if (result is com.it2161.s231292a.movieviewer.data.NetworkResource.Success) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                loadingMovieIds = currentState.loadingMovieIds - movieId
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            loadingMovieIds = currentState.loadingMovieIds - movieId
                        )
                    }
                }
            }
        }
    }

    fun refreshFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            // Simulate network refresh delay since we don't have a batch update API
            delay(1500)

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun removeFromFavorites(movieId: Int) {
        viewModelScope.launch {
            favoritesRepository.removeFromFavorites(movieId)
        }
    }

    companion object {
        fun provideFactory(
            movieRepository: MovieRepository,
            favoritesRepository: FavoritesRepository,
            networkMonitor: NetworkMonitor
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FavoritesViewModel(
                        movieRepository,
                        favoritesRepository,
                        networkMonitor
                    ) as T
                }
            }
        }
    }
}
