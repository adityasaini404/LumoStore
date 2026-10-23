package com.lumo.store.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NavTab(
    val id: String,
    val icon: ImageVector,
    val label: String
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    val tabs = listOf(
        NavTab("Home", Icons.Outlined.Home, "Home"),
        NavTab("Installed", Icons.Outlined.Apps, "Installed"),
        NavTab("Dev", Icons.Outlined.Code, "Dev"),
        NavTab("Settings", Icons.Outlined.Settings, "Settings")
    )

    val safeTab = tabs.firstOrNull { it.id == currentTab }?.id ?: "Home"
    val selectedIndex = tabs.indexOfFirst { it.id == safeTab }.coerceAtLeast(0)

    val gradient = Brush.horizontalGradient(listOf(colors.primary, colors.secondary))

    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 350f),
        label = "bubble_position"
    )

    Box(
        modifier = modifier.fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val horizontalPadding = 6.dp
            val gap = 4.dp
            val tabCount = tabs.size
            // FIX 1: divide by tab count (was hardcoded 3)
            // FIX 2: subtract gaps for (tabCount - 1) gaps, not always 2
            val slotWidth = (maxWidth - horizontalPadding * 2 - gap * (tabCount - 1)) / tabCount

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.onSurface.copy(alpha = 0.08f), RoundedCornerShape(32.dp))
                    .padding(horizontal = horizontalPadding),
                contentAlignment = Alignment.CenterStart
            ) {
                // Animated gradient pill behind the selected tab
                Box(
                    modifier = Modifier
                        .offset(x = (slotWidth + gap) * animatedIndex)
                        .width(slotWidth)
                        .height(48.dp)
                        .padding(horizontal = 2.dp)
                        .background(brush = gradient, shape = CircleShape)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val selected = tab.id == safeTab

                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.16f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "scale_${tab.id}"
                        )

                        val iconColor by animateColorAsState(
                            targetValue = if (selected) Color.White
                            else colors.onSurface.copy(alpha = 0.70f),
                            animationSpec = tween(180),
                            label = "color_${tab.id}"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(CircleShape)
                                .clickable { if (!selected) onTabSelected(tab.id) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label,
                                    tint = iconColor,
                                    // Slightly smaller to make room for 4 tabs
                                    modifier = Modifier.scale(iconScale).size(20.dp)
                                )
                                if (selected) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}