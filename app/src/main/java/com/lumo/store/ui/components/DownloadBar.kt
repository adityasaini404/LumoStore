package com.lumo.store.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lumo.store.viewmodel.DownloadState

@Composable
fun DownloadBar(state: DownloadState, onInstall: () -> Unit, onDismiss: () -> Unit) {
    // ⬇️ थीम के हिसाब से डायनामिक रंग
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // नीचे की तरफ एनिमेटेड बार
    AnimatedVisibility(
        visible = state.isDownloading,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = Modifier
                    .padding(bottom = 100.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = primaryColor.copy(alpha = 0.3f),
                        spotColor = primaryColor.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(surfaceColor.copy(alpha = 0.95f)) // थोड़ा ट्रांसपेरेंट ग्लास लुक
                    .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            "Downloading ${state.app?.name}...",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            "${(state.progress * 100).toInt()}%",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp) // थोड़ा मोटा किया ताकि Apple जैसा दिखे
                            .clip(RoundedCornerShape(50)),
                        color = primaryColor,
                        trackColor = textColor.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }

    // डाउनलोड पूरा होने पर आने वाला डायलॉग
    if (state.isCompleted) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = primaryColor.copy(alpha = 0.4f),
                        spotColor = primaryColor.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(surfaceColor)
                    .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅", fontSize = 56.sp) // थोड़ा बड़ा किया
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Download Complete",
                        color = textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${state.app?.name} is ready to install.",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(28.dp))

                    // Install Button (Apple Style Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                            .clickable { onInstall() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Install / Update Now",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Cancel Button (Optional - adds a nice touch)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Cancel",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onDismiss() }.padding(8.dp)
                    )
                }
            }
        }
    }
}