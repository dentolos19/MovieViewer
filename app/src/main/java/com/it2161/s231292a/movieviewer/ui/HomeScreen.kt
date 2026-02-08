@file:OptIn(ExperimentalMaterial3Api::class)

package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.it2161.s231292a.movieviewer.data.types.MovieCategory
import com.it2161.s231292a.movieviewer.ui.components.*
import com.it2161.s231292a.movieviewer.ui.models.HomeViewModel
import kotlinx.coroutines.launch

private data class NavItem(
    val category: MovieCategory,
    val title: String,
    val icon: ImageVector
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMovieClick: (Int) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFavoritesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDropdownMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val navItems = listOf(
        NavItem(MovieCategory.POPULAR, "Popular", Icons.Filled.Star),
        NavItem(MovieCategory.TOP_RATED, "Top Rated", Icons.Filled.ThumbUp),
        NavItem(MovieCategory.NOW_PLAYING, "Now Playing", Icons.Filled.PlayArrow),
        NavItem(MovieCategory.UPCOMING, "Upcoming", Icons.Filled.DateRange)
    )

    val selectedNavItem = remember(uiState.selectedCategory) {
        navItems.first { it.category == uiState.selectedCategory }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null && uiState.movies.isNotEmpty()) {
            viewModel.clearError()
        }
    }

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

    Scaffold(
        topBar = {
            AppHeader(
                title = "Movie Viewer",
                navigationIcon = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showDropdownMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = {
                                    showDropdownMenu = false
                                    onProfileClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Favorites") },
                                onClick = {
                                    showDropdownMenu = false
                                    onFavoritesClick()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEach { navItem ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.title
                            )
                        },
                        label = { Text(navItem.title) },
                        selected = selectedNavItem == navItem,
                        onClick = {
                            if (selectedNavItem != navItem) {
                                viewModel.selectCategory(navItem.category)
                                coroutineScope.launch {
                                    listState.scrollToItem(0)
                                }
                            } else {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NetworkStatusBanner(isOnline = uiState.isOnline)

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading && uiState.movies.isEmpty() -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(5) {
                                SkeletonMovieCard()
                            }
                        }
                    }

                    !uiState.isOnline && uiState.movies.isEmpty() -> {
                        OfflineState()
                    }

                    uiState.error != null && uiState.movies.isEmpty() -> {
                        ErrorState(message = uiState.error!!)
                    }

                    uiState.movies.isEmpty() -> {
                        EmptyState(
                            title = "No movies found",
                            message = "Pull to refresh to load movies"
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.movies,
                                key = { it.id }
                            ) { movie ->
                                MovieCard(
                                    movie = movie,
                                    onClick = { onMovieClick(movie.id) },
                                    isFavorite = uiState.favoriteMovieIds.contains(movie.id)
                                )
                            }

                            if (uiState.isLoading && uiState.movies.isNotEmpty()) {
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
}
