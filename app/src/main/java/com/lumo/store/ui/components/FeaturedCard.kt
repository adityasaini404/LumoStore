package com.lumo.store.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.lumo.store.data.AppInfo

@Composable
fun FeaturedCard(
    app: AppInfo,
    onInstallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ⬇️ थीम के हिसाब से डायनामिक रंग
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    GlassCard(
        modifier = modifier.width(210.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = app.icon,
                contentDescription = app.name,
                contentScale = ContentScale.Crop,
                // ⬇️ डायनामिक प्लेसहोल्डर रंग
                placeholder = ColorPainter(textColor.copy(alpha = 0.15f)),
                error = ColorPainter(textColor.copy(alpha = 0.15f)),
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = app.name,
                color = textColor, // ⬅️ डायनामिक टेक्स्ट रंग
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.description,
                color = textColor.copy(alpha = 0.55f), // ⬅️ डायनामिक
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(primaryColor, secondaryColor) // ⬅️ डायनामिक ग्रेडिएंट
                        )
                    )
                    .clickable { onInstallClick() }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    "Get",
                    color = Color.White, // ग्रेडिएंट बटन पर सफेद टेक्स्ट हमेशा अच्छा लगता है
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}