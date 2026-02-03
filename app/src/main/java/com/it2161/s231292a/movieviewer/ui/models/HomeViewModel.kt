package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import com.it2161.s231292a.movieviewer.ui.states.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val movieRepository: MovieRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeNetworkStatus()
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

    fun selectCategory(category: MovieCategory) {
        if (_uiState.value.selectedCategory != category) {
            _uiState.update { it.copy(selectedCategory = category) }
            loadMovies()
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()

            // First, observe cached data
            movieRepository.getMoviesByCategoryFlow(category).collect { cachedMovies ->
                if (cachedMovies.isNotEmpty()) {
                    _uiState.update { it.copy(movies = cachedMovies) }
                }
            }
        }

        // Then try to refresh from network
        viewModelScope.launch {
            val category = _uiState.value.selectedCategory
            val isOnline = networkMonitor.isCurrentlyConnected()

            if (isOnline) {
                when (val result = movieRepository.refreshMovies(category)) {
                    is NetworkResource.Success -> {
                        _uiState.update {
                            it.copy(
                                movies = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is NetworkResource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = if (it.movies.isEmpty()) result.message else null
                            )
                        }
                    }

                    is NetworkResource.Loading -> {
                        // Already handled
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (it.movies.isEmpty()) "No internet connection" else null
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
            networkMonitor: NetworkMonitor
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(movieRepository, networkMonitor) as T
                }
            }
        }
    }
}
