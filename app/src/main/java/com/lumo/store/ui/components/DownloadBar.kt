package com.lumo.store.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lumo.store.viewmodel.DownloadState
import com.lumo.store.viewmodel.DownloadStatus

@Composable
fun DownloadBar(
    state: DownloadState,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = state.app != null && state.status != DownloadStatus.Idle,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        val app = state.app ?: return@AnimatedVisibility
        val primary = MaterialTheme.colorScheme.primary
        val secondary = MaterialTheme.colorScheme.secondary
        val surface = MaterialTheme.colorScheme.surface
        val onSurface = MaterialTheme.colorScheme.onSurface

        val isDownloading = state.status == DownloadStatus.Downloading
        val isDone = state.status == DownloadStatus.Downloaded ||
                state.status == DownloadStatus.Installing ||
                state.status == DownloadStatus.Completed
        val isError = state.status == DownloadStatus.Error

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(surface)
                .border(1.dp, onSurface.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDownloading) {
                            val glow = rememberInfiniteTransition(label = "glow")
                            val alpha by glow.animateFloat(
                                initialValue = 0.2f,
                                targetValue = 0.6f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "glow_alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(primary.copy(alpha = alpha))
                            )
                        }
                        AsyncImage(
                            model = app.icon,
                            contentDescription = app.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            color = onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when {
                                isError -> state.errorMessage ?: "Download failed"
                                isDownloading -> {
                                    val variantLabel = state.variant?.let { " · ${it.archLabel}" } ?: ""
                                    "Downloading$variantLabel"
                                }
                                state.status == DownloadStatus.Downloaded -> "Downloaded — preparing installer..."
                                state.status == DownloadStatus.Installing -> "Opening installer..."
                                else -> "Complete"
                            },
                            color = if (isError) Color(0xFFEF4444) else onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isDownloading) {
                        Text(
                            text = "${(state.progress * 100).toInt()}%",
                            color = primary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else if (isDone) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.horizontalGradient(listOf(primary, secondary)))
                                .clickable { onInstall() }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Install",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Install",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else if (isError) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(onSurface.copy(alpha = 0.08f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (isDownloading || isDone) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(onSurface.copy(alpha = 0.08f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(state.progress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(primary, secondary)))
                        )
                        if (isDownloading) {
                            val shimmer = rememberInfiniteTransition(label = "shimmer")
                            val x by shimmer.animateFloat(
                                initialValue = -100f,
                                targetValue = 400f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1200, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "shimmer_x"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(state.progress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.5f),
                                                Color.Transparent
                                            ),
                                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                                            end = androidx.compose.ui.geometry.Offset(x + 100f, 0f)
                                        )
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatBytes(state.bytesDownloaded)} / ${formatBytes(state.totalBytes)}",
                            color = onSurface.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                        if (isDownloading && state.speedBytesPerSec > 0) {
                            Text(
                                text = "${formatBytes(state.speedBytesPerSec)}/s · ${formatEta(state.etaSeconds)} left",
                                color = onSurface.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 KB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1.0) String.format("%.1f MB", mb) else "${bytes / 1024} KB"
}

private fun formatEta(seconds: Int): String {
    if (seconds <= 0) return "—"
    return if (seconds < 60) "${seconds}s" else "${seconds / 60}m ${seconds % 60}s"
}