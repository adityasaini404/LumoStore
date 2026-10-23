package com.lumo.store.ui.components

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.lumo.store.data.AppInfo

@Composable
fun AppCard(
    app: AppInfo,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onInstallClick: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // प्रेस एनिमेशन
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.97f else 1f,
        label = "card_scale"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .clickable(
                    interactionSource = cardInteractionSource,
                    indication = null
                ) { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. App Icon
                SubcomposeAsyncImage(
                    model = app.icon,
                    contentDescription = app.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)),
                    loading = {
                        Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(app.name.take(1).uppercase(), color = textColor, fontWeight = FontWeight.Bold)
                        }
                    },
                    error = {
                        Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Text(app.name.take(1).uppercase(), color = textColor, fontWeight = FontWeight.Bold)
                        }
                    }
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 2. Center Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.name,
                            color = textColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (app.isInstalled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "✓",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${app.author} · ${app.category} · ${app.size.ifBlank { "Unknown" }}",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 3. Right Actions (Heart + Button)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ❤️ Heart Icon (Material)
                    Box(
                        modifier = Modifier.size(34.dp).clip(CircleShape).clickable { onFavoriteToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFE91E63) else textColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Get / Open / Update Button
                    val btnText = when {
                        app.isUpdateAvailable -> "Update"
                        app.isInstalled -> "Open"
                        else -> "Get"
                    }

                    val btnInteractionSource = remember { MutableInteractionSource() }
                    val isBtnPressed by btnInteractionSource.collectIsPressedAsState()
                    val btnScale by animateFloatAsState(if (isBtnPressed) 0.92f else 1f, label = "btn_scale")

                    val btnBrush = if (app.isInstalled && !app.isUpdateAvailable) {
                        SolidColor(textColor.copy(alpha = 0.1f))
                    } else {
                        Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                    }

                    Row(
                        modifier = Modifier
                            .scale(btnScale)
                            .clip(RoundedCornerShape(50))
                            .background(brush = btnBrush)
                            .clickable(
                                interactionSource = btnInteractionSource,
                                indication = null
                            ) {
                                if (app.isInstalled && !app.isUpdateAvailable) {
                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (intent != null) context.startActivity(intent)
                                } else {
                                    onInstallClick()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            btnText,
                            color = if (app.isInstalled && !app.isUpdateAvailable) textColor else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}