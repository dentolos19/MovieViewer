package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.it2161.s231292a.movieviewer.ui.components.*
import com.it2161.s231292a.movieviewer.ui.models.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMovieClick: (Int) -> Unit,
    onBackClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.listStateIndex,
        initialFirstVisibleItemScrollOffset = uiState.listStateOffset
    )
    val coroutineScope = rememberCoroutineScope()
    var shouldScrollToTop by remember { mutableStateOf(false) }

    // Infinite scroll detection
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - 5)
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && uiState.canLoadMore && !uiState.isLoading) {
            viewModel.loadNextPage()
        }
    }

    LaunchedEffect(uiState.results) {
        if (shouldScrollToTop && uiState.results.isNotEmpty()) {
            listState.scrollToItem(0)
            shouldScrollToTop = false
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Search Movies",
                canNavigateBack = true,
                onNavigateBack = onBackClick,
                actions = {
                    IconButton(onClick = {
                        viewModel.saveScrollPosition(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
                        onFavoritesClick()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Favorites"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NetworkStatusBanner(isOnline = uiState.isOnline)

            // Search Bar
            OutlinedTextField(
                value = uiState.query,
                onValueChange = {
                    viewModel.updateQuery(it)
                    shouldScrollToTop = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by title...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        viewModel.search()
                        focusManager.clearFocus()
                        shouldScrollToTop = true
                    }
                )
            )

            // Results
            when {
                !uiState.isOnline && uiState.query.isNotBlank() -> {
                    OfflineState()
                }

                uiState.isLoading && uiState.results.isEmpty() -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) {
                            SkeletonMovieCard()
                        }
                    }
                }

                uiState.error != null && uiState.results.isEmpty() -> {
                    ErrorState(message = uiState.error!!)
                }

                uiState.hasSearched && uiState.results.isEmpty() -> {
                    NoResultsState(query = uiState.query)
                }

                uiState.results.isEmpty() && !uiState.hasSearched -> {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = "Search for movies",
                        message = "Enter a movie title to search"
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.results,
                            key = { it.id }
                        ) { movie ->
                            MovieCard(
                                movie = movie,
                                onClick = {
                                    viewModel.saveScrollPosition(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
                                    onMovieClick(movie.id)
                                },
                                isFavorite = uiState.favoriteMovieIds.contains(movie.id)
                            )
                        }

                        if (uiState.isLoading && uiState.results.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
