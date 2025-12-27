package com.it2161.s231292a.movieviewer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.it2161.s231292a.movieviewer.Constants
import com.it2161.s231292a.movieviewer.data.entities.MovieReview
import com.it2161.s231292a.movieviewer.ui.components.AppHeader
import com.it2161.s231292a.movieviewer.ui.components.EmptyState
import com.it2161.s231292a.movieviewer.ui.components.ErrorState
import com.it2161.s231292a.movieviewer.ui.components.LoadingIndicator
import com.it2161.s231292a.movieviewer.ui.components.NetworkStatusBanner
import com.it2161.s231292a.movieviewer.ui.models.MovieReviewsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MovieReviewsScreen(
    viewModel: MovieReviewsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = if (uiState.movieTitle.isNotBlank()) "Reviews - ${uiState.movieTitle}" else "Reviews",
                canNavigateBack = true,
                onNavigateBack = onBackClick
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
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null && uiState.reviews.isEmpty() -> {
                    ErrorState(message = uiState.error!!)
                }
                uiState.reviews.isEmpty() -> {
                    EmptyState(
                        title = "No reviews yet",
                        message = "Be the first to review this movie!"
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.reviews,
                            key = { it.id }
                        ) { review ->
                            ReviewCard(review = review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: MovieReview) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    val formattedDate = try {
        val date = inputFormat.parse(review.createdAt.take(19))
        date?.let { dateFormat.format(it) } ?: review.createdAt.take(10)
    } catch (e: Exception) {
        review.createdAt.take(10)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar
                if (review.authorAvatarPath != null) {
                    val avatarUrl = if (review.authorAvatarPath.startsWith("/http")) {
                        review.authorAvatarPath.substring(1)
                    } else {
                        Constants.getPosterUrl(review.authorAvatarPath, Constants.POSTER_SIZE_W185)
                    }
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = review.author,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = review.author,
                            modifier = Modifier.padding(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.author,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${review.authorUsername}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Rating Badge
                if (review.authorRating != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rating",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", review.authorRating),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Review Content
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

