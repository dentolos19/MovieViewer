package com.it2161.s231292a.movieviewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.it2161.s231292a.movieviewer.Constants
import com.it2161.s231292a.movieviewer.R
import com.it2161.s231292a.movieviewer.data.dto.MovieGenreDto
import com.it2161.s231292a.movieviewer.ui.components.*
import com.it2161.s231292a.movieviewer.ui.models.MovieDetailViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel,
    onBackClick: () -> Unit,
    onViewReviewsClick: (Int) -> Unit,
    onFavoritesClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BackHandler {
        onBackClick()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            AppHeader(
                title = uiState.movieDetail?.title ?: "Movie Details",
                canNavigateBack = true,
                onNavigateBack = onBackClick,
                actions = {
                    if (uiState.movieDetail != null) {
                        IconButton(onClick = {
                            viewModel.toggleFavorite()
                            scope.launch {
                                val message =
                                    if (!uiState.isFavorite) "Added to favorites" else "Removed from favorites"
                                val result = snackbarHostState.showSnackbar(
                                    message = message,
                                    actionLabel = if (!uiState.isFavorite) "View" else null,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    onFavoritesClick()
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                                tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
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

            when {
                !uiState.isOnline -> {
                    OfflineState()
                }

                uiState.isLoading && uiState.movieDetail == null -> {
                    LoadingIndicator()
                }

                uiState.error != null && uiState.movieDetail == null -> {
                    ErrorState(message = uiState.error!!)
                }

                uiState.movieDetail != null -> {
                    val movie = uiState.movieDetail!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Backdrop Image
                        if (movie.backdropPath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Constants.getBackdropUrl(movie.backdropPath))
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.placeholder),
                                error = painterResource(R.drawable.placeholder),
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback if backdropPath is null
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(null)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.placeholder),
                                error = painterResource(R.drawable.placeholder),
                                contentDescription = movie.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Title and Rating
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (movie.tagline != null && movie.tagline.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${movie.tagline}\"",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                // Rating Badge
                                ElevatedCard(
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Rating",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = String.format("%.1f", movie.voteAverage),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Genres
                            val genres = try {
                                val type = object : TypeToken<List<MovieGenreDto>>() {}.type
                                Gson().fromJson<List<MovieGenreDto>>(movie.genres, type)
                            } catch (e: Exception) {
                                emptyList()
                            }

                            if (genres.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    genres.forEach { genre ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(genre.name) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Info Grid
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                elevation = CardDefaults.elevatedCardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    InfoRow(
                                        "Release Date",
                                        if (movie.releaseDate.isNotEmpty()) movie.releaseDate else "Unknown"
                                    )
                                    InfoRow("Original Language", movie.originalLanguage.uppercase())
                                    movie.runtime?.let { InfoRow("Runtime", "${it} minutes") }
                                    InfoRow("Vote Count", movie.voteCount.toString())
                                    if (movie.revenue > 0) {
                                        InfoRow("Revenue", currencyFormat.format(movie.revenue))
                                    }
                                    InfoRow("Adult Content", if (movie.adult) "Yes" else "No")
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Overview
                            Text(
                                text = "Overview",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (movie.overview.isNotEmpty()) movie.overview else "No description available",
                                style = MaterialTheme.typography.bodyLarge,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Reviews Section Preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reviews",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                if (uiState.reviews.isNotEmpty()) {
                                    TextButton(onClick = { onViewReviewsClick(movie.id) }) {
                                        Text(
                                            "View All (${uiState.reviews.size})",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (uiState.isLoadingReviews) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                )
                            } else if (uiState.reviews.isEmpty()) {
                                Text(
                                    text = "No reviews yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                // Show first 2 reviews preview
                                uiState.reviews.take(2).forEach { review ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = review.author,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (review.authorRating != null) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            ),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Star,
                                                                contentDescription = "Rating",
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = String.format("%.1f", review.authorRating),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = review.content.take(150) + if (review.content.length > 150) "..." else "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 3
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}
