package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.NetworkResource
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.ui.states.SearchUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val movieRepository: MovieRepository,
    private val favoritesRepository: FavoritesRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

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
            favoritesRepository.favoriteMovieIds.collect { favoriteIds ->
                _uiState.update { it.copy(favoriteMovieIds = favoriteIds) }
            }
        }
    }

    fun updateQuery(query: String) {
        _uiState.update { it.copy(query = query, page = 1, canLoadMore = true) }

        // Debounce search
        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(500) // Debounce delay
                search()
            }
        } else {
            _uiState.update { it.copy(results = emptyList(), hasSearched = false) }
        }
    }

    fun search() {
        val query = _uiState.value.query
        if (query.isBlank()) return

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val isOnline = networkMonitor.isCurrentlyConnected()
            if (!isOnline) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Search requires internet connection",
                        hasSearched = true
                    )
                }
                return@launch
            }

            val page = _uiState.value.page

            when (val result = movieRepository.searchMovies(query, isOnline, page)) {
                is NetworkResource.Success -> {
                    val newMovies = result.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            results = if (page == 1) newMovies else it.results + newMovies,
                            isLoading = false,
                            error = null,
                            hasSearched = true,
                            canLoadMore = newMovies.isNotEmpty()
                        )
                    }
                }

                is NetworkResource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            hasSearched = true,
                            canLoadMore = false
                        )
                    }
                }

                is NetworkResource.Loading -> {}
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || !_uiState.value.canLoadMore) return
        _uiState.update { it.copy(page = it.page + 1) }
        search()
    }

    fun clearSearch() {
        _uiState.update {
            SearchUiState(isOnline = it.isOnline)
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
                    return SearchViewModel(movieRepository, favoritesRepository, networkMonitor) as T
                }
            }
        }
    }
}
