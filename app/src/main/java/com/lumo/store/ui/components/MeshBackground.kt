package com.lumo.store.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MeshBackground() {
    // ⬇️ थीम के हिसाब से रंग उठाओ
    val bgColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // 1. ऊपर-बाईं तरफ (Top-Left) - प्राइमरी कलर का ग्लो
        Box(
            modifier = Modifier
                .size(450.dp)
                .offset(x = (-150).dp, y = (-150).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f), // थोड़ा और ट्रांसपेरेंट
                            Color.Transparent
                        )
                    )
                )
        )

        // 2. नीचे-दाईं तरफ (Bottom-Right) - सेकेंडरी कलर का ग्लो
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 150.dp, y = 150.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 3. बीच में हल्का सा (Center Blob) - एक तीसरा मिक्स कलर
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(x = 80.dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}