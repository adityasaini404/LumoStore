package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
    "arm64-v8a", "armeabi-v7a" -> Icons.Default.Memory
    "x86_64", "x86" -> Icons.Default.Computer
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
    var showVariantMenu by remember { mutableStateOf(false) }

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
        targetValue = if (startAnim) 1f else 0.92f,
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
    val isOpen = app.isInstalled && !app.isUpdateAvailable

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ---------- TOP BAR ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                            .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
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
                                    if (app.version.isNotBlank() && app.version != "N/A")
                                        append(" (${formatVersion(app.version)})")
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

            // ---------- HEADER CARD WITH ACTIONS ----------
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .scale(headerScale)
                        .alpha(headerAlpha)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.15f),
                                    secondaryColor.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .border(1.dp, primaryColor.copy(alpha = 0.20f), RoundedCornerShape(24.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        // App info row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .border(2.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
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

                        Spacer(modifier = Modifier.height(18.dp))

                        // ========================================
                        // TWO BUTTONS: Install + Variant Dropdown
                        // ========================================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Main Install/Open/Update button
                            val btnText = when {
                                app.isUpdateAvailable -> "Update"
                                app.isInstalled -> "Open"
                                else -> "Install"
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(
                                        if (isOpen) 0.dp else 12.dp,
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
                                                    Toast.makeText(context, "No package name", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    if (intent != null) {
                                                        try {
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Cannot open app", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "App installed but cannot open", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            app.downloadUrl.isBlank() && app.variants.isEmpty() -> {
                                                Toast.makeText(context, "Download not available", Toast.LENGTH_SHORT).show()
                                            }
                                            else -> showDownloadDialog = true
                                        }
                                    }
                                    .padding(horizontal = 20.dp),
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

                            // Variant dropdown (only if multiple variants)
                            if (hasMultipleVariants) {
                                Box {
                                    // The dropdown trigger button
                                    Row(
                                        modifier = Modifier
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(surfaceColor)
                                            .border(1.dp, primaryColor.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                            .clickable { showVariantMenu = true }
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = archIconFor(selectedVariant?.arch ?: "universal"),
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            archShortLabel(selectedVariant?.arch ?: "universal"),
                                            color = textColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Select variant",
                                            tint = textColor.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Dropdown menu
                                    DropdownMenu(
                                        expanded = showVariantMenu,
                                        onDismissRequest = { showVariantMenu = false },
                                        modifier = Modifier
                                            .background(surfaceColor)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .widthIn(min = 220.dp)
                                    ) {
                                        Text(
                                            "Choose Architecture",
                                            color = textColor.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                        HorizontalDivider(color = textColor.copy(alpha = 0.06f))

                                        app.variants.forEach { variant ->
                                            val isSelected = selectedVariant?.filename == variant.filename
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(32.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(
                                                                    if (isSelected) primaryColor.copy(alpha = 0.15f)
                                                                    else textColor.copy(alpha = 0.06f)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                archIconFor(variant.arch),
                                                                null,
                                                                tint = if (isSelected) primaryColor else textColor.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                archShortLabel(variant.arch),
                                                                color = if (isSelected) primaryColor else textColor,
                                                                fontSize = 13.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                                            )
                                                            Text(
                                                                variant.sizeFormatted.ifBlank { "Unknown size" },
                                                                color = textColor.copy(alpha = 0.55f),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                null,
                                                                tint = primaryColor,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    selectedVariant = variant
                                                    showVariantMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Selected variant info (if multiple)
                        if (hasMultipleVariants && selectedVariant != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primaryColor.copy(alpha = 0.08f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    buildString {
                                        append("Selected: ")
                                        append(archShortLabel(selectedVariant!!.arch))
                                        if (selectedVariant!!.sizeFormatted.isNotBlank()) {
                                            append(" · ")
                                            append(selectedVariant!!.sizeFormatted)
                                        }
                                    },
                                    color = textColor.copy(alpha = 0.75f),
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
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.06f), RoundedCornerShape(20.dp))
                        .padding(vertical = 16.dp),
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
                        value = (selectedVariant?.sizeFormatted?.ifBlank { null } ?: app.size).ifBlank { "N/A" },
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

            // ---------- SCREENSHOTS ----------
            if (app.screenshots.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
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
                                        .size(width = 210.dp, height = 380.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
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
                        .padding(horizontal = 20.dp, vertical = 8.dp)
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
                                    fontWeight = FontWeight.Medium
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

        // ---------- DOWNLOAD CONFIRMATION DIALOG (SIMPLIFIED) ----------
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

                        // Selected variant summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(primaryColor.copy(alpha = 0.06f))
                                .border(1.dp, primaryColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = archIconFor(selectedVariant?.arch ?: "universal"),
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        selectedVariant?.archLabel ?: "Universal APK",
                                        color = textColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        selectedVariant?.sizeFormatted?.ifBlank { null }
                                            ?: app.size.ifBlank { "Unknown size" },
                                        color = textColor.copy(alpha = 0.55f),
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    "Selected",
                                    color = primaryColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // BUTTONS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
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
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
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