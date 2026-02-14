package com.it2161.s231292a.movieviewer.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.it2161.s231292a.movieviewer.data.NetworkMonitor
import com.it2161.s231292a.movieviewer.data.entities.Movie
import com.it2161.s231292a.movieviewer.data.repositories.FavoritesRepository
import com.it2161.s231292a.movieviewer.data.repositories.MovieRepository
import com.it2161.s231292a.movieviewer.data.toMovie
import com.it2161.s231292a.movieviewer.ui.states.FavoritesUiState
import com.it2161.s231292a.movieviewer.ui.states.SortOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
                            movies = sortMovies(movies.distinctBy { movie -> movie.id }, it.sortOption),
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

    fun addToFavorites(movieId: Int) {
        viewModelScope.launch {
            favoritesRepository.addToFavorites(movieId)
        }
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.update {
            it.copy(
                sortOption = sortOption,
                movies = sortMovies(it.movies, sortOption)
            )
        }
    }

    fun saveScrollPosition(index: Int, offset: Int) {
        _uiState.update { it.copy(listStateIndex = index, listStateOffset = offset) }
    }

    private fun sortMovies(movies: List<Movie>, sortOption: SortOption): List<Movie> {
        return when (sortOption) {
            SortOption.TITLE_ASC -> movies.sortedBy { it.title }
            SortOption.TITLE_DESC -> movies.sortedByDescending { it.title }
            SortOption.RATING_ASC -> movies.sortedBy { it.voteAverage }
            SortOption.RATING_DESC -> movies.sortedByDescending { it.voteAverage }
            SortOption.RELEASE_DATE_ASC -> movies.sortedBy { it.releaseDate }
            SortOption.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
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
