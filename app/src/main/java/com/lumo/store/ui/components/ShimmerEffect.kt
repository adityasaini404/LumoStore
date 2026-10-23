package com.lumo.store.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerAppCard() {
    // ⬇️ थीम के हिसाब से डायनामिक रंग
    val textColor = MaterialTheme.colorScheme.onSurface

    // शिमर के रंग (अब लाइट और डार्क दोनों में एकदम हल्के और सॉफ्ट दिखेंगे)
    val shimmerColors = listOf(
        textColor.copy(alpha = 0.05f),
        textColor.copy(alpha = 0.12f),
        textColor.copy(alpha = 0.05f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    // ⬇️ यहाँ GlassCard का इस्तेमाल किया है ताकि यह AppCard जैसा ही दिखे
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Skeleton
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Text Skeleton (डेवलपर नाम और साइज़ वाली लाइनें)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.width(60.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(modifier = Modifier.width(140.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                Box(modifier = Modifier.width(100.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            }

            // Button Skeleton (Get बटन जैसा)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(brush)
            )
        }
    }
}