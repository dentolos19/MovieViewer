package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import com.it2161.s231292a.movieviewer.ui.states.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
        observeFavorites()
        loadMovies()
    }

    private fun observeNetworkStatus() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOnline = isOnline) }
                // Refresh if we come back online and had an error
                if (isOnline && _uiState.value.error != null) {
                    loadMovies()
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.favoriteMovieIds.collect { favoriteIds ->
                _uiState.update { it.copy(favoriteMovieIds = favoriteIds) }
            }
        }
    }

    fun selectCategory(category: MovieCategory) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.update { it.copy(selectedCategory = category, movies = emptyList()) }
            loadMovies()
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()

            // Artificial delay to show skeleton loading
            val startTime = System.currentTimeMillis()

            // First, observe cached data
            // We'll collect this in a separate job so we can cancel it if needed or just let it update
            // However, for the purpose of "clearing everything and showing skeleton", we might want to wait
            // for the network refresh if we want to enforce the skeleton.
            // But typically we show cache if available.
            // The user requirement says "clear everything and show a skeleton loading screen".
            // So we should probably NOT show cache immediately if we want to show skeleton.
            // But showing cache is better UX.
            // Let's follow the requirement: "clear everything".
            // So I already cleared movies in selectCategory.

            // Now we fetch from network.
            if (isOnline) {
                when (val result = movieRepository.refreshMovies(category)) {
                    is NetworkResource.Success -> {
                        // Calculate how much time passed
                        val elapsedTime = System.currentTimeMillis() - startTime
                        if (elapsedTime < 1000) {
                            delay(1000 - elapsedTime)
                        }

                        _uiState.update {
                            it.copy(
                                movies = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is NetworkResource.Error -> {
                        // Even on error, we might want to show cached data if available
                        // But for now let's just show the error or cached data if we have it from repository
                        // The repository refreshMovies returns data from DB on success.
                        // On error, we might need to fetch from DB manually if we want to show offline data.

                        val cachedMovies = movieRepository.getMoviesByCategory(category)

                        _uiState.update {
                            it.copy(
                                movies = cachedMovies,
                                isLoading = false,
                                error = if (cachedMovies.isEmpty()) result.message else null
                            )
                        }
                    }

                    is NetworkResource.Loading -> {
                        // Already handled
                    }
                }
            } else {
                // Offline
                val cachedMovies = movieRepository.getMoviesByCategory(category)
                _uiState.update {
                    it.copy(
                        movies = cachedMovies,
                        isLoading = false,
                        error = if (cachedMovies.isEmpty()) "No internet connection" else null
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }

            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()

            if (isOnline) {
                when (val result = movieRepository.refreshMovies(category)) {
                    is NetworkResource.Success -> {
                        _uiState.update {
                            it.copy(
                                movies = result.data ?: emptyList(),
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

                    is NetworkResource.Error -> {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.message
                            )
                        }
                    }

                    is NetworkResource.Loading -> {}
                }
            } else {
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = "No internet connection"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
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
                    return HomeViewModel(movieRepository, favoritesRepository, networkMonitor) as T
                }
            }
        }
    }
}
