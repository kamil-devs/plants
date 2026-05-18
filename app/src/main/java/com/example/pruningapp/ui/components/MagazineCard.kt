package com.example.pruningapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.pruningapp.R

// MagazineCard jest niezależny od modeli biznesowych dzięki interfejsowi CardDisplayable.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineCard(
    item: CardDisplayable,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.72f,
    owned: Boolean = false,
    pinned: Boolean = false,
    syncPending: Boolean = false,
    onToggleOwned: (() -> Unit)? = null,
    onTogglePinned: (() -> Unit)? = null
) {
    val imageUrl = remember(item.imageUrl) { item.imageUrl }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = imageUrl.takeIf { !it.isNullOrBlank() },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { CardImagePlaceholder() }
            )

            // Gradient scrim for text legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.82f)
                        )
                    )
            )

            // Top row: action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (syncPending) {
                    ActionCircleButton(
                        icon = Icons.Default.Sync,
                        contentDescription = stringResource(R.string.cd_sync_pending),
                        active = false,
                        activeColor = Color.White
                    )
                }
                if (onTogglePinned != null) {
                    ActionCircleButton(
                        icon = Icons.Default.PushPin,
                        contentDescription = null,
                        active = pinned,
                        activeColor = Color(0xFFFFC107),
                        onClick = onTogglePinned
                    )
                }
                if (onToggleOwned != null) {
                    ActionCircleButton(
                        icon = Icons.Default.Check,
                        contentDescription = null,
                        active = owned,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = onToggleOwned
                    )
                }
            }

            // Category chip — glassmorphism style
            Surface(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.35f),
                contentColor = Color.White
            ) {
                Text(
                    text = item.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }

            // Title / subtitle at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CardImagePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Eco,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ActionCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    active: Boolean,
    activeColor: Color,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        modifier = Modifier.size(32.dp),
        enabled = onClick != null,
        shape = CircleShape,
        color = if (active) activeColor else Color.Black.copy(alpha = 0.3f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
