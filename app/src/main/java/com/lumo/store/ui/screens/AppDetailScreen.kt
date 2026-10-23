package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import dev.jeziellago.compose.markdowntext.MarkdownText
import com.lumo.store.data.AppInfo

// ---------- Custom GitHub logo (Material Icons doesn't ship one) ----------
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

// Adds a "v" prefix only if the tag doesn't already start with one
private fun formatVersion(version: String): String {
    if (version.isBlank() || version == "N/A") return "N/A"
    return if (version.startsWith("v", ignoreCase = true)) version else "v$version"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    onBack: () -> Unit,
    onDownloadClick: (AppInfo) -> Unit = {}
) {
    val context = LocalContext.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    // Header entry animation
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

    LaunchedEffect(Unit) {
        startAnim = true
    }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // ---------- 1. TOP BAR ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                            .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Report Issue (only if the app has a GitHub repo)
                    if (app.githubRepo.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.08f))
                                .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                                .clickable {
                                    val issueUrl = "https://github.com/${app.githubRepo}/issues/new"
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl)))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Could not open GitHub",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BugReport,
                                contentDescription = "Report Issue",
                                tint = textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Share
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                            .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                            .clickable {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_SUBJECT, app.name)
                                    val shareText = buildString {
                                        append("Check out ${app.name}")
                                        if (app.version.isNotBlank() && app.version != "N/A") {
                                            append(" (${formatVersion(app.version)})")
                                        }
                                        append(" on Lumo Store!")
                                        if (app.repoUrl.isNotBlank()) append("\n\n${app.repoUrl}")
                                    }
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share ${app.name}"))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            tint = textColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ---------- 2. HEADER ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .scale(headerScale)
                        .alpha(headerAlpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubcomposeAsyncImage(
                        model = app.icon,
                        contentDescription = app.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, textColor.copy(alpha = 0.15f), RoundedCornerShape(22.dp)),
                        loading = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f))) },
                        error = { Box(Modifier.fillMaxSize().background(textColor.copy(alpha = 0.1f))) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            app.name,
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            app.author,
                            color = secondaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            app.description,
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Main action button (Install/Open/Update)
                    val btnText = when {
                        app.isUpdateAvailable -> "Update"
                        app.isInstalled -> "Open"
                        else -> "Install"
                    }
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (app.isInstalled && !app.isUpdateAvailable) 0.dp else 10.dp,
                                shape = RoundedCornerShape(50),
                                ambientColor = primaryColor,
                                spotColor = primaryColor
                            )
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (app.isInstalled && !app.isUpdateAvailable)
                                    SolidColor(textColor.copy(alpha = 0.1f))
                                else
                                    Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                            )
                            .clickable {
                                when {
                                    app.isInstalled && !app.isUpdateAvailable -> {
                                        val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                        if (intent != null) {
                                            context.startActivity(intent)
                                        } else {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Cannot open this app",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    app.downloadUrl.isBlank() -> {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Download not available for this app",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> showDownloadDialog = true
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            btnText,
                            color = if (app.isInstalled && !app.isUpdateAvailable) textColor else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ---------- 3. STATS ROW ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            formatVersion(app.version),
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Version", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(textColor.copy(alpha = 0.15f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            app.size.ifBlank { "N/A" },
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Size", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                    Box(modifier = Modifier.width(1.dp).height(28.dp).background(textColor.copy(alpha = 0.15f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = secondaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Verified", color = secondaryColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Safe to use", color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
            }

            // ---------- 4. SCREENSHOTS ----------
            if (app.screenshots.isNotEmpty()) {
                item {
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

            // ---------- 5. ABOUT THIS APP ----------
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
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
                                Icon(
                                    Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "About This App",
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Tag(icon = Icons.Default.Bolt, text = "Fast", iconColor = Color(0xFFFFD700))
                            Tag(icon = Icons.Default.Cloud, text = "Cloud", iconColor = secondaryColor)
                            Tag(icon = Icons.Default.Lock, text = "Secure", iconColor = Color(0xFF00FF88))
                        }
                    }
                }
            }

            // ---------- 6. SOURCE CODE & VIRUSTOTAL ----------
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // GitHub Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceColor)
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .clickable {
                                if (app.repoUrl.isNotBlank()) {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(app.repoUrl)))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Could not open link",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = GitHubIcon,
                                contentDescription = "GitHub",
                                tint = textColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Source Code", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "View and contribute on GitHub",
                                color = textColor.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // VirusTotal Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(surfaceColor)
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .clickable {
                                val urlToScan = app.downloadUrl.ifBlank { app.repoUrl }
                                if (urlToScan.isNotBlank()) {
                                    // VirusTotal expects URL-safe Base64 (no padding) in the URL path
                                    val encoded = android.util.Base64.encodeToString(
                                        urlToScan.toByteArray(Charsets.UTF_8),
                                        android.util.Base64.URL_SAFE or
                                                android.util.Base64.NO_WRAP or
                                                android.util.Base64.NO_PADDING
                                    )
                                    val vtUrl = "https://www.virustotal.com/gui/url/$encoded"
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(vtUrl)))
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Could not open VirusTotal",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "No download URL available to scan",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(secondaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = "VirusTotal",
                                tint = secondaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Scan with VirusTotal",
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (app.downloadUrl.isNotBlank())
                                    "Check this APK against 70+ antiviruses"
                                else
                                    "No download URL available",
                                color = textColor.copy(alpha = 0.5f),
                                fontSize = 12.sp
                            )
                        }
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = "Open",
                            tint = textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // ---------- 7. WHAT'S NEW (Markdown) ----------
            if (app.releaseNotes.isNotBlank() && app.releaseNotes != "No release notes available.") {
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 24.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(surfaceColor)
                            .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "What's New",
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    formatVersion(app.version),
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            MarkdownText(
                                markdown = app.releaseNotes,
                                color = textColor,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ---------- FULL SCREEN IMAGE DIALOG ----------
        if (selectedImageUrl != null) {
            Dialog(
                onDismissRequest = { selectedImageUrl = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.95f))
                        .clickable { selectedImageUrl = null },
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = selectedImageUrl,
                        contentDescription = "Full Screen",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(top = 16.dp, end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { selectedImageUrl = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ---------- DOWNLOAD CONFIRMATION DIALOG ----------
        if (showDownloadDialog) {
            Dialog(onDismissRequest = { showDownloadDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        SubcomposeAsyncImage(
                            model = app.icon,
                            contentDescription = app.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Download ${app.name}?",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${formatVersion(app.version)} · ${app.size.ifBlank { "Unknown size" }}",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(textColor.copy(alpha = 0.1f))
                                    .clickable { showDownloadDialog = false }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cancel", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                                    .clickable {
                                        showDownloadDialog = false
                                        onDownloadClick(app)
                                    }
                                    .padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Download", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Tag chip ----------
@Composable
fun Tag(icon: ImageVector, text: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(iconColor.copy(alpha = 0.1f))
            .border(1.dp, iconColor.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, color = iconColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}