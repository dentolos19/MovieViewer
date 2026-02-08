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
import kotlinx.coroutines.Job
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

    private var loadMoviesJob: Job? = null

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
            loadMoviesJob?.cancel()
            _uiState.update { it.copy(selectedCategory = category, movies = emptyList(), page = 1, canLoadMore = true, listStateIndex = 0, listStateOffset = 0, isLoading = false) }
            loadMovies()
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _uiState.update { it.copy(listStateIndex = index, listStateOffset = offset) }
    }

    fun loadMovies() {
        if (_uiState.value.isLoading) return

        loadMoviesJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()
            val page = _uiState.value.page

            // Artificial delay to show skeleton loading
            val startTime = System.currentTimeMillis()

            if (isOnline) {
                when (val result = movieRepository.refreshMovies(category, page)) {
                    is NetworkResource.Success -> {
                        // Calculate how much time passed
                        val elapsedTime = System.currentTimeMillis() - startTime
                        if (elapsedTime < 1000 && page == 1) {
                            delay(1000 - elapsedTime)
                        }

                        val newMovies = result.data ?: emptyList()
                        _uiState.update {
                            it.copy(
                                movies = if (page == 1) newMovies else it.movies + newMovies,
                                isLoading = false,
                                error = null,
                                canLoadMore = newMovies.isNotEmpty()
                            )
                        }
                    }

                    is NetworkResource.Error -> {
                        val cachedMovies = if (page == 1) {
                            movieRepository.getMoviesByCategory(category)
                        } else {
                            _uiState.value.movies
                        }

                        _uiState.update {
                            it.copy(
                                movies = cachedMovies,
                                isLoading = false,
                                error = if (cachedMovies.isEmpty()) result.message else null,
                                canLoadMore = false
                            )
                        }
                    }

                    is NetworkResource.Loading -> {
                        // Already handled
                    }
                }
            } else {
                // Offline
                if (page == 1) {
                    val cachedMovies = movieRepository.getMoviesByCategory(category)
                    _uiState.update {
                        it.copy(
                            movies = cachedMovies,
                            isLoading = false,
                            error = if (cachedMovies.isEmpty()) "No internet connection" else null,
                            canLoadMore = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            canLoadMore = false
                        )
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || !_uiState.value.canLoadMore) return
        _uiState.update { it.copy(page = it.page + 1) }
        loadMovies()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, page = 1, canLoadMore = true, listStateIndex = 0, listStateOffset = 0) }

            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()

            if (isOnline) {
                when (val result = movieRepository.refreshMovies(category, 1)) {
                    is NetworkResource.Success -> {
                        _uiState.update {
                            it.copy(
                                movies = result.data ?: emptyList(),
                                isRefreshing = false,
                                error = null,
                                canLoadMore = (result.data?.size ?: 0) > 0
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
