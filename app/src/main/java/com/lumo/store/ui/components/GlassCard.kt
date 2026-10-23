package com.lumo.store.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val theme = MaterialTheme.colorScheme

    // ⬇️ पहचानो कि थीम Light है या Dark
    val isLightTheme = theme.background.luminance() > 0.5f

    // 1. शैडो (Shadow) - अब एकदम हल्की और साफ (जैसी iOS में होती है)
    val shadowColor = if (isLightTheme) {
        Color.Black.copy(alpha = 0.08f) // Light थीम में बहुत हल्की काली शैडो
    } else {
        Color.Black.copy(alpha = 0.25f) // Dark थीम में भी काली शैडो (नीली/बैंगनी नहीं)
    }

    // 2. ग्लास बैकग्राउंड (Frosted Effect)
    val glassBackground = if (isLightTheme) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.7f), // ऊपर से ज़्यादा सफेद
                Color.White.copy(alpha = 0.3f)  // नीचे से ट्रांसपेरेंट
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                theme.surface.copy(alpha = 0.4f),
                theme.surface.copy(alpha = 0.1f)
            )
        )
    }

    // 3. ग्लास का किनारा (Border) - ऊपर से चमकीला, नीचे से हल्का
    val glassBorder = if (isLightTheme) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),
                Color.Black.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isLightTheme) 8.dp else 4.dp, // शैडो को कम किया
                shape = shape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
            .background(brush = glassBackground)
            .border(
                width = 1.dp,
                brush = glassBorder,
                shape = shape
            ),
        content = content
    )
}