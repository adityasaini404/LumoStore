package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.lumo.store.data.ApkVariant
import com.lumo.store.data.AppInfo

// Custom GitHub logo
private val GitHubIcon: ImageVector by lazy {
    val nodes = PathParser().parsePathString(
        "M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 " +
                "0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7" +
                "c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998" +
                ".108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22" +
                "-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405" +
                " 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22" +
                " 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286" +
                " 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"
    ).toNodes()
    ImageVector.Builder(
        name = "GitHub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        addPath(pathData = nodes, fill = SolidColor(Color.Black))
    }.build()
}

private fun formatVersion(version: String): String {
    if (version.isBlank() || version == "N/A") return "N/A"
    return if (version.startsWith("v", ignoreCase = true)) version else "v$version"
}

private fun formatDownloads(count: Long): String = when {
    count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> count.toString()
}

private fun archIconFor(arch: String): ImageVector = when (arch) {
    "arm64-v8a" -> Icons.Default.Memory
    "armeabi-v7a" -> Icons.Default.Memory
    "x86_64" -> Icons.Default.Computer
    "x86" -> Icons.Default.Computer
    else -> Icons.Default.Android
}

private fun archShortLabel(arch: String): String = when (arch) {
    "arm64-v8a" -> "ARM64"
    "armeabi-v7a" -> "ARMv7"
    "x86_64" -> "x86_64"
    "x86" -> "x86"
    else -> "Universal"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    onBack: () -> Unit,
    onDownloadClick: (AppInfo, ApkVariant?) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isReleaseNotesExpanded by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    var selectedVariant by remember(app.id) {
        mutableStateOf<ApkVariant?>(null)
    }

    LaunchedEffect(app.id, app.variants) {
        if (selectedVariant == null && app.variants.isNotEmpty()) {
            selectedVariant = app.variants.firstOrNull { it.arch == "arm64-v8a" }
                ?: app.variants.first()
        }
    }

    var startAnim by remember { mutableStateOf(false) }
    val headerScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.75f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "header_scale"
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(400),
        label = "header_alpha"
    )
    LaunchedEffect(Unit) { startAnim = true }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val hasMultipleVariants = app.variants.size > 1

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ---------- TOP BAR ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(surfaceColor)
                            .border(1.dp, textColor.copy(alpha = 0.09f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = textColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (app.githubRepo.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.08f))
                                .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                                .clickable {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://github.com/${app.githubRepo}/issues/new")))
                                    } catch (_: Exception) {}
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BugReport, "Report", tint = textColor, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                            .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                            .clickable {
                                val txt = buildString {
                                    append("Check out ${app.name}")
                                    if (app.version.isNotBlank() && app.version != "N/A") append(" (${formatVersion(app.version)})")
                                    append(" on Lumo Store!")
                                    if (app.repoUrl.isNotBlank()) append("\n\n${app.repoUrl}")
                                }
                                val send = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, txt)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(send, "Share ${app.name}"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Share, "Share", tint = textColor, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // ---------- PREMIUM HEADER CARD ----------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .scale(headerScale)
                        .alpha(headerAlpha)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.18f),
                                    secondaryColor.copy(alpha = 0.07f)
                                )
                            )
                        )
                        .border(1.dp, primaryColor.copy(alpha = 0.18f), RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.dp, primaryColor.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                            ) {
                                SubcomposeAsyncImage(
                                    model = app.icon,
                                    contentDescription = app.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f))) },
                                    error = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f))) }
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        app.name,
                                        color = textColor,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        "Verified",
                                        tint = secondaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    app.author,
                                    color = secondaryColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    app.category,
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Main Action Button
                        val btnText = when {
                            app.isUpdateAvailable -> "Update to ${formatVersion(app.version)}"
                            app.isInstalled -> "Open"
                            else -> "Install App"
                        }
                        val isOpen = app.isInstalled && !app.isUpdateAvailable

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    if (isOpen) 0.dp else 8.dp,
                                    RoundedCornerShape(16.dp),
                                    ambientColor = primaryColor,
                                    spotColor = primaryColor
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isOpen)
                                        SolidColor(textColor.copy(alpha = 0.10f))
                                    else
                                        Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                                )
                                .clickable {
                                    when {
                                        isOpen -> {
                                            if (app.packageName.isBlank()) {
                                                android.widget.Toast.makeText(context, "No package name", android.widget.Toast.LENGTH_SHORT).show()
                                            } else {
                                                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                if (intent != null) {
                                                    try {
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        android.widget.Toast.makeText(context, "Cannot open app", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    android.widget.Toast.makeText(context, "App installed but cannot open", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                        app.downloadUrl.isBlank() && app.variants.isEmpty() -> {
                                            android.widget.Toast.makeText(context, "Download not available", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        else -> showDownloadDialog = true
                                    }
                                }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when {
                                        app.isUpdateAvailable -> Icons.Default.SystemUpdate
                                        isOpen -> Icons.Default.OpenInNew
                                        else -> Icons.Default.Download
                                    },
                                    contentDescription = null,
                                    tint = if (isOpen) textColor else Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    btnText,
                                    color = if (isOpen) textColor else Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Download available variants info
                        if (hasMultipleVariants) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "${app.variants.size} variants available",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ---------- STATS CARD ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(22.dp))
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        icon = Icons.Default.Tag,
                        value = formatVersion(app.version),
                        label = "Version",
                        color = primaryColor,
                        textColor = textColor
                    )
                    StatVerticalDivider(textColor)
                    StatItem(
                        icon = Icons.Default.Storage,
                        value = app.size.ifBlank { "N/A" },
                        label = "Size",
                        color = secondaryColor,
                        textColor = textColor
                    )
                    StatVerticalDivider(textColor)
                    StatItem(
                        icon = Icons.Default.Download,
                        value = if (app.downloadCount > 0) formatDownloads(app.downloadCount) else "—",
                        label = "Downloads",
                        color = Color(0xFF10B981),
                        textColor = textColor
                    )
                }
            }

            // ---------- MODERN VARIANTS SECTION ----------
            if (hasMultipleVariants) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Memory,
                                    null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Available Variants",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(primaryColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    "${app.variants.size} options",
                                    color = primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            app.variants.forEach { variant ->
                                VariantCard(
                                    variant = variant,
                                    isSelected = selectedVariant?.filename == variant.filename,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    surfaceColor = surfaceColor,
                                    textColor = textColor,
                                    onClick = { selectedVariant = variant }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.10f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Info,
                                null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Choose the variant that matches your device's CPU architecture for best compatibility.",
                                color = textColor.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // ---------- SCREENSHOTS ----------
            if (app.screenshots.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(secondaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoLibrary,
                                    null,
                                    tint = secondaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                "Screenshots",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(app.screenshots) { url ->
                                SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = "Screenshot",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 216.dp, height = 384.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .border(1.dp, textColor.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
                                        .clickable { selectedImageUrl = url },
                                    loading = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.05f))) },
                                    error = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.05f))) }
                                )
                            }
                        }
                    }
                }
            }

            // ---------- ABOUT ----------
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Description, null, tint = textColor, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("About", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                if (isDescriptionExpanded) "Read Less" else "Read More",
                                color = secondaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            app.description,
                            color = textColor.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // ---------- LINKS ----------
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    LinkCard(
                        icon = GitHubIcon,
                        iconTint = textColor,
                        iconBg = textColor.copy(alpha = 0.1f),
                        title = "Source Code",
                        subtitle = "View and contribute on GitHub",
                        onClick = {
                            if (app.repoUrl.isNotBlank()) try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.repoUrl)))
                            } catch (_: Exception) {}
                        },
                        textColor = textColor,
                        surfaceColor = surfaceColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LinkCard(
                        icon = Icons.Default.Security,
                        iconTint = secondaryColor,
                        iconBg = secondaryColor.copy(alpha = 0.2f),
                        title = "VirusTotal Scan",
                        subtitle = "Check against 70+ antiviruses",
                        onClick = {
                            val urlToScan = selectedVariant?.downloadUrl ?: app.downloadUrl.ifBlank { app.repoUrl }
                            if (urlToScan.isNotBlank()) {
                                val encoded = android.util.Base64.encodeToString(
                                    urlToScan.toByteArray(Charsets.UTF_8),
                                    android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP or android.util.Base64.NO_PADDING
                                )
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://www.virustotal.com/gui/url/$encoded")))
                                } catch (_: Exception) {}
                            }
                        },
                        textColor = textColor,
                        surfaceColor = surfaceColor
                    )
                }
            }

            // ---------- WHAT'S NEW (COLLAPSIBLE) ----------
            if (app.releaseNotes.isNotBlank() && app.releaseNotes != "No release notes available.") {
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(surfaceColor)
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isReleaseNotesExpanded = !isReleaseNotesExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "What's New",
                                            color = textColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Version ${formatVersion(app.version)}",
                                            color = textColor.copy(alpha = 0.5f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.10f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isReleaseNotesExpanded)
                                            Icons.Default.ExpandLess
                                        else
                                            Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = isReleaseNotesExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(textColor.copy(alpha = 0.08f))
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    MarkdownText(
                                        markdown = app.releaseNotes,
                                        color = textColor,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            if (!isReleaseNotesExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Tap to see full changelog",
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { isReleaseNotesExpanded = true }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- FULLSCREEN IMAGE ----------
        if (selectedImageUrl != null) {
            Dialog(
                onDismissRequest = { selectedImageUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { selectedImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = selectedImageUrl,
                        contentDescription = "Full Screen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 16.dp, end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { selectedImageUrl = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ---------- REDESIGNED DOWNLOAD DIALOG ----------
        if (showDownloadDialog) {
            Dialog(onDismissRequest = { showDownloadDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.10f), RoundedCornerShape(28.dp))
                ) {
                    Column {
                        // Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            primaryColor.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(2.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                                ) {
                                    SubcomposeAsyncImage(
                                        model = app.icon,
                                        contentDescription = app.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    "Download ${app.name}",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${formatVersion(app.version)} · ${app.author}",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Variant picker (if multiple)
                        if (hasMultipleVariants) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Memory,
                                        null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Choose variant",
                                        color = textColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                app.variants.forEach { variant ->
                                    val isSel = selectedVariant?.filename == variant.filename
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSel) primaryColor.copy(alpha = 0.12f)
                                                else textColor.copy(alpha = 0.04f)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSel) primaryColor.copy(alpha = 0.4f)
                                                else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { selectedVariant = variant }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSel,
                                            onClick = { selectedVariant = variant },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = primaryColor,
                                                unselectedColor = textColor.copy(alpha = 0.4f)
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                variant.archLabel,
                                                color = textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                variant.sizeFormatted.ifBlank { "Unknown size" },
                                                color = textColor.copy(alpha = 0.55f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        } else {
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Selected info summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(textColor.copy(alpha = 0.04f))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "File size",
                                        color = textColor.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        selectedVariant?.sizeFormatted?.ifBlank { null }
                                            ?: app.size.ifBlank { "Unknown" },
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Architecture",
                                        color = textColor.copy(alpha = 0.5f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        selectedVariant?.archLabel ?: "Auto",
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BUTTONS — clearly visible, large
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Cancel button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(textColor.copy(alpha = 0.08f))
                                    .border(1.dp, textColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                    .clickable { showDownloadDialog = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Cancel",
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Download button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .shadow(10.dp, RoundedCornerShape(14.dp), ambientColor = primaryColor, spotColor = primaryColor)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                                    .clickable {
                                        showDownloadDialog = false
                                        onDownloadClick(app, selectedVariant)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Download",
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

// ========================================
// HELPER COMPOSABLES
// ========================================

@Composable
private fun VariantCard(
    variant: ApkVariant,
    isSelected: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    val bgBrush = if (isSelected)
        Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
    else
        Brush.horizontalGradient(listOf(surfaceColor, surfaceColor))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = if (isSelected) Color.Transparent else textColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 15.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Arch icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) Color.White.copy(alpha = 0.2f)
                    else primaryColor.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = archIconFor(variant.arch),
                contentDescription = null,
                tint = if (isSelected) Color.White else primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    archShortLabel(variant.arch),
                    color = if (isSelected) Color.White else textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "SELECTED",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Storage,
                    null,
                    tint = if (isSelected) Color.White.copy(alpha = 0.7f) else textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    variant.sizeFormatted.ifBlank { "Unknown" },
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.55f),
                    fontSize = 11.sp
                )
                if (variant.downloadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Download,
                        null,
                        tint = if (isSelected) Color.White.copy(alpha = 0.7f) else textColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        formatDownloads(variant.downloadCount),
                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else textColor.copy(alpha = 0.55f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Check
        if (isSelected) {
            Icon(
                Icons.Default.CheckCircle,
                "Selected",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, textColor.copy(alpha = 0.25f), CircleShape)
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(label, color = textColor.copy(alpha = 0.5f), fontSize = 10.sp)
    }
}

@Composable
private fun StatVerticalDivider(textColor: Color) {
    Box(modifier = Modifier.width(1.dp).height(36.dp).background(textColor.copy(alpha = 0.10f)))
}

@Composable
private fun LinkCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color,
    surfaceColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(surfaceColor)
            .border(1.dp, textColor.copy(alpha = 0.07f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Icon(Icons.Default.OpenInNew, null, tint = textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
    }
}