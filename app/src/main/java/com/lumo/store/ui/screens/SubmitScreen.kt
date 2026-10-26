package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.SubcomposeAsyncImage
import com.lumo.store.data.GitHubApi
import com.lumo.store.data.TokenManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---------- FORM STATE ----------
    var repoUrl by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var githubRepo by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    var newScreenshotUrl by remember { mutableStateOf("") }
    val screenshotsList = remember { mutableStateListOf<String>() }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // ---------- AUTO-FETCH STATE ----------
    var isFetching by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var fetchSuccess by remember { mutableStateOf(false) }
    var lastFetchedUrl by remember { mutableStateOf("") }
    var fetchJob by remember { mutableStateOf<Job?>(null) }

    val categoriesList = listOf(
        "Tools", "Media", "Art", "Games", "Productivity",
        "Educational", "Utility", "Social", "Developer"
    )

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    // ---------- PROGRESS CALCULATION (EXPLICIT) ----------
    val missingFields = buildList {
        if (name.isBlank()) add("App Name")
        if (description.isBlank()) add("Description")
        if (author.isBlank()) add("Developer Name")
        if (githubRepo.isBlank()) add("GitHub Repo")
        if (category.isBlank()) add("Category")
        if (iconUrl.isBlank()) add("Icon URL")
    }

    val requiredScreenshots = 3
    val screenshotCount = screenshotsList.size
    val screenshotShortfall = (requiredScreenshots - screenshotCount).coerceAtLeast(0)

    val totalItems = 6 + requiredScreenshots
    val filledItems = (6 - missingFields.size) + minOf(screenshotCount, requiredScreenshots)
    val progress = filledItems.toFloat() / totalItems

    val isReadyToSubmit = missingFields.isEmpty() && screenshotCount >= requiredScreenshots

    // ---------- AUTO-FETCH LOGIC ----------
    LaunchedEffect(repoUrl) {
        val trimmed = repoUrl.trim()
        if (trimmed.length < 10) return@LaunchedEffect
        if (!trimmed.contains("github.com") && !trimmed.contains("/")) return@LaunchedEffect
        if (trimmed == lastFetchedUrl) return@LaunchedEffect

        fetchJob?.cancel()
        fetchJob = scope.launch {
            delay(800)
            isFetching = true
            fetchError = null
            fetchSuccess = false

            try {
                val token = TokenManager.getToken(context)
                val info = GitHubApi.fetchRepoInfo(trimmed, token)

                if (info == null) {
                    fetchError = "Repository not found — check the URL"
                } else {
                    // ✅ Fill fields with fallbacks
                    if (name.isBlank()) {
                        name = info.repoName.ifBlank { "Unknown App" }
                    }
                    if (description.isBlank()) {
                        description = info.description.ifBlank {
                            "${info.repoName} is an open-source Android app."
                        }
                    }
                    if (author.isBlank()) {
                        author = info.authorName.ifBlank { info.owner }
                    }
                    if (githubRepo.isBlank()) {
                        githubRepo = info.githubRepo
                    }
                    if (packageName.isBlank() && info.packageName.isNotBlank()) {
                        packageName = info.packageName
                    }
                    if (category.isBlank() && info.suggestedCategory.isNotBlank()) {
                        category = info.suggestedCategory
                    }
                    // ❌ Icon auto-fill REMOVED — user must fill manually

                    lastFetchedUrl = trimmed
                    fetchSuccess = true

                    android.util.Log.d(
                        "LumoStore",
                        "SUBMIT AUTO-FILL: name=${name.isNotBlank()}, desc=${description.isNotBlank()}, " +
                                "author=${author.isNotBlank()}, repo=${githubRepo.isNotBlank()}, " +
                                "cat=${category.isNotBlank()}, icon=${iconUrl.isNotBlank()}"
                    )
                }
            } catch (e: Exception) {
                fetchError = "Network error: ${e.message}"
            } finally {
                isFetching = false
            }
        }
    }

    // Debug log
    LaunchedEffect(missingFields, screenshotCount) {
        android.util.Log.d(
            "LumoStore",
            "FORM: filled=${6 - missingFields.size}/6, missing=${missingFields.joinToString()}, " +
                    "screenshots=$screenshotCount/$requiredScreenshots"
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ========================================
            // TOP BAR
            // ========================================
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape)
                        .background(textColor.copy(alpha = 0.08f))
                        .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Back",
                        tint = textColor, modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Publish Your App",
                        color = textColor, fontSize = 22.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Share your creation with Lumo Store",
                        color = textColor.copy(alpha = 0.5f), fontSize = 12.sp
                    )
                }
                // Progress pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isReadyToSubmit)
                                Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF34D399)))
                            else
                                Brush.horizontalGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.15f),
                                        secondaryColor.copy(alpha = 0.15f)
                                    )
                                )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = if (isReadyToSubmit) Color.White else primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Progress line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(textColor.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // ========================================
                // LIVE PREVIEW CARD
                // ========================================
                item {
                    LivePreviewCard(
                        name = name.ifBlank { "Your App Name" },
                        author = author.ifBlank { "Author Name" },
                        category = category.ifBlank { "Category" },
                        iconUrl = iconUrl,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        textColor = textColor
                    )
                }

                // ========================================
                // AUTO-FETCH CARD
                // ========================================
                item {
                    Column {
                        SectionLabel(
                            icon = Icons.Default.Bolt,
                            text = "Quick Fill from GitHub",
                            color = primaryColor,
                            textColor = textColor,
                            trailing = "Recommended"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            primaryColor.copy(alpha = 0.10f),
                                            secondaryColor.copy(alpha = 0.10f)
                                        )
                                    )
                                )
                                .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome, null,
                                            tint = Color.White, modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Paste any GitHub repo URL",
                                            color = textColor, fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "We'll auto-fill name, description, author & more",
                                            color = textColor.copy(alpha = 0.6f), fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = repoUrl,
                                    onValueChange = { repoUrl = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = {
                                        Text(
                                            "https://github.com/user/repo",
                                            color = textColor.copy(alpha = 0.35f),
                                            fontSize = 13.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Link, null,
                                            tint = if (isFetching) primaryColor else textColor.copy(alpha = 0.5f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        when {
                                            isFetching -> CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = primaryColor,
                                                strokeWidth = 2.dp
                                            )
                                            fetchSuccess -> Icon(
                                                Icons.Default.CheckCircle, "Fetched",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            fetchError != null -> Icon(
                                                Icons.Default.Error, "Error",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            else -> Icon(
                                                Icons.Default.ArrowForward, "Enter",
                                                tint = textColor.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = textColor.copy(alpha = 0.15f),
                                        focusedContainerColor = bgColor,
                                        unfocusedContainerColor = bgColor
                                    )
                                )

                                AnimatedVisibility(
                                    visible = fetchError != null || fetchSuccess,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (fetchSuccess)
                                                    Color(0xFF10B981).copy(alpha = 0.12f)
                                                else
                                                    Color(0xFFEF4444).copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (fetchSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            null,
                                            tint = if (fetchSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            if (fetchSuccess) "Auto-filled! Review and edit below."
                                            else fetchError ?: "",
                                            color = if (fetchSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ========================================
                // APP DETAILS
                // ========================================
                item {
                    Column {
                        SectionLabel(
                            icon = Icons.Default.Apps,
                            text = "App Details",
                            color = primaryColor,
                            textColor = textColor
                        )
                        Spacer(Modifier.height(10.dp))

                        FormCard(surfaceColor, textColor) {
                            LumoInput(
                                label = "App Name *",
                                value = name,
                                placeholder = "e.g. Lumo Notes",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Apps
                            ) { name = it }

                            LumoInput(
                                label = "Description *",
                                value = description,
                                placeholder = "What does your app do? (1-2 sentences)",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Description,
                                multiLine = true
                            ) { description = it }

                            LumoInput(
                                label = "Developer Name *",
                                value = author,
                                placeholder = "e.g. Aditya Saini",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Person
                            ) { author = it }

                            // Category dropdown
                            Column {
                                FieldLabel("Category *", textColor)
                                ExposedDropdownMenuBox(
                                    expanded = categoryExpanded,
                                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = category,
                                        onValueChange = {},
                                        readOnly = true,
                                        placeholder = {
                                            Text(
                                                "Pick a category",
                                                color = textColor.copy(alpha = 0.4f),
                                                fontSize = 13.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Category, null,
                                                tint = primaryColor, modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                                        },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor,
                                            focusedBorderColor = primaryColor,
                                            unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                            focusedContainerColor = surfaceColor,
                                            unfocusedContainerColor = surfaceColor
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryExpanded,
                                        onDismissRequest = { categoryExpanded = false },
                                        modifier = Modifier.background(surfaceColor)
                                    ) {
                                        categoriesList.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat, color = textColor) },
                                                onClick = {
                                                    category = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ========================================
                // REPOSITORY
                // ========================================
                item {
                    Column {
                        SectionLabel(
                            icon = Icons.Default.Code,
                            text = "Repository & Package",
                            color = primaryColor,
                            textColor = textColor
                        )
                        Spacer(Modifier.height(10.dp))

                        FormCard(surfaceColor, textColor) {
                            LumoInput(
                                label = "GitHub Repo (owner/repo) *",
                                value = githubRepo,
                                placeholder = "e.g. justshivv/Opentune",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Code
                            ) { githubRepo = it }

                            LumoInput(
                                label = "Package Name",
                                value = packageName,
                                placeholder = "e.g. com.opentune",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Android
                            ) { packageName = it }

                            if (packageName.isBlank()) {
                                InfoHint(
                                    icon = Icons.Outlined.Info,
                                    color = Color(0xFFF59E0B),
                                    text = "Not auto-detected. Find it in the app's build.gradle (applicationId).",
                                    textColor = textColor
                                )
                            } else {
                                InfoHint(
                                    icon = Icons.Default.CheckCircle,
                                    color = Color(0xFF10B981),
                                    text = "Package name detected — used to check installed status.",
                                    textColor = textColor
                                )
                            }
                        }
                    }
                }

                // ========================================
                // APP ICON (MANUAL ONLY)
                // ========================================
                item {
                    Column {
                        SectionLabel(
                            icon = Icons.Default.Image,
                            text = "App Icon",
                            color = primaryColor,
                            textColor = textColor,
                            trailing = "Manual"
                        )
                        Spacer(Modifier.height(10.dp))

                        FormCard(surfaceColor, textColor) {
                            LumoInput(
                                label = "Icon Image URL *",
                                value = iconUrl,
                                placeholder = "https://raw.githubusercontent.com/.../icon.png",
                                textColor = textColor,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                surfaceColor = surfaceColor,
                                icon = Icons.Default.Image
                            ) { iconUrl = it }

                            InfoHint(
                                icon = Icons.Outlined.Lightbulb,
                                color = Color(0xFFF59E0B),
                                text = "Paste the actual app icon URL. Use raw.githubusercontent.com link (not github.com/blob). Example: https://raw.githubusercontent.com/user/repo/main/icon.png",
                                textColor = textColor
                            )

                            AnimatedVisibility(
                                visible = iconUrl.isNotBlank(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PreviewImageBox(iconUrl, 72.dp, 16.dp, textColor)
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Visibility, null,
                                                tint = primaryColor, modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Preview", color = primaryColor,
                                                fontSize = 12.sp, fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            GitHubApi.normalizeImageUrl(iconUrl),
                                            color = textColor.copy(alpha = 0.6f),
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ========================================
                // SCREENSHOTS (MIN 3 REQUIRED)
                // ========================================
                item {
                    Column {
                        SectionLabel(
                            icon = Icons.Default.PhotoLibrary,
                            text = "Screenshots",
                            color = if (screenshotCount >= requiredScreenshots)
                                Color(0xFF10B981) else primaryColor,
                            textColor = textColor,
                            trailing = if (screenshotCount >= requiredScreenshots)
                                "${screenshotCount} added ✓"
                            else
                                "${screenshotCount}/$requiredScreenshots required"
                        )
                        Spacer(Modifier.height(10.dp))

                        FormCard(surfaceColor, textColor) {
                            // Status banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (screenshotCount >= requiredScreenshots)
                                            Color(0xFF10B981).copy(alpha = 0.1f)
                                        else
                                            Color(0xFFF59E0B).copy(alpha = 0.1f)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (screenshotCount >= requiredScreenshots)
                                        Icons.Default.CheckCircle
                                    else
                                        Icons.Outlined.Lightbulb,
                                    null,
                                    tint = if (screenshotCount >= requiredScreenshots)
                                        Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (screenshotCount >= requiredScreenshots)
                                        "✓ Minimum screenshots met ($screenshotCount added)"
                                    else
                                        "At least $requiredScreenshots screenshots required. ${requiredScreenshots - screenshotCount} more to go.",
                                    color = if (screenshotCount >= requiredScreenshots)
                                        Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 15.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = newScreenshotUrl,
                                        onValueChange = { newScreenshotUrl = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = {
                                            Text(
                                                "https://raw.githubusercontent.com/.../screen.png",
                                                color = textColor.copy(alpha = 0.3f),
                                                fontSize = 12.sp
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.PhotoCamera, null,
                                                tint = primaryColor, modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = textColor,
                                            unfocusedTextColor = textColor,
                                            focusedBorderColor = primaryColor,
                                            unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                            focusedContainerColor = surfaceColor,
                                            unfocusedContainerColor = surfaceColor
                                        )
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (newScreenshotUrl.isNotBlank())
                                                Brush.linearGradient(listOf(primaryColor, secondaryColor))
                                            else
                                                SolidColor(textColor.copy(alpha = 0.15f))
                                        )
                                        .clickable(enabled = newScreenshotUrl.isNotBlank()) {
                                            screenshotsList.add(newScreenshotUrl.trim())
                                            newScreenshotUrl = ""
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add, "Add",
                                        tint = Color.White, modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = newScreenshotUrl.isNotBlank(),
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Row(
                                    modifier = Modifier.padding(top = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PreviewImageBox(newScreenshotUrl, 48.dp, 10.dp, textColor)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "Preview — tap + to add",
                                        color = textColor.copy(alpha = 0.5f), fontSize = 11.sp
                                    )
                                }
                            }

                            if (screenshotsList.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(screenshotsList) { url ->
                                        Box(modifier = Modifier.width(90.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 90.dp, height = 160.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(textColor.copy(alpha = 0.05f))
                                            ) {
                                                SubcomposeAsyncImage(
                                                    model = GitHubApi.normalizeImageUrl(url),
                                                    contentDescription = "Screenshot",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize(),
                                                    loading = {
                                                        Box(
                                                            Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(20.dp),
                                                                color = primaryColor,
                                                                strokeWidth = 2.dp
                                                            )
                                                        }
                                                    },
                                                    error = {
                                                        Box(
                                                            Modifier.fillMaxSize(),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Outlined.BrokenImage, "Error",
                                                                tint = Color.Red.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(4.dp)
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Black.copy(alpha = 0.6f))
                                                        .clickable { screenshotsList.remove(url) },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Close, "Remove",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(12.dp)
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

                // ========================================
                // SUBMIT BUTTON
                // ========================================
                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isReadyToSubmit)
                                    Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                                else
                                    SolidColor(textColor.copy(alpha = 0.15f))
                            )
                            .clickable(enabled = isReadyToSubmit) {
                                showSuccessDialog = true
                            }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isReadyToSubmit)
                                        Icons.AutoMirrored.Filled.Send
                                    else
                                        Icons.Default.Lock,
                                    null,
                                    tint = if (isReadyToSubmit)
                                        Color.White else textColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    if (isReadyToSubmit)
                                        "Submit for Review"
                                    else
                                        "Complete required fields",
                                    color = if (isReadyToSubmit)
                                        Color.White else textColor.copy(alpha = 0.5f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (!isReadyToSubmit) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = buildString {
                                        if (missingFields.isNotEmpty()) {
                                            append("Missing: ")
                                            append(missingFields.joinToString(", "))
                                        }
                                        if (screenshotShortfall > 0) {
                                            if (missingFields.isNotEmpty()) append(" · ")
                                            append("$screenshotShortfall more screenshot${if (screenshotShortfall > 1) "s" else ""}")
                                        }
                                    },
                                    color = textColor.copy(alpha = 0.45f),
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // ========================================
        // SUCCESS DIALOG
        // ========================================
        if (showSuccessDialog) {
            Dialog(onDismissRequest = { showSuccessDialog = false }) {
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
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, "Success",
                                tint = primaryColor, modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Ready to Publish! 🎉",
                            color = textColor, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Click below to open a GitHub Issue. Our team will review and add your app within 24-48 hours.",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(textColor.copy(alpha = 0.05f))
                                .padding(12.dp)
                        ) {
                            Column {
                                SummaryRow("Name", name)
                                SummaryRow("Author", author)
                                SummaryRow("Category", category)
                                if (screenshotsList.isNotEmpty()) {
                                    SummaryRow("Screenshots", "${screenshotsList.size} added")
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        val cleanRepo = GitHubApi.normalizeGithubRepo(githubRepo)
                        val cleanIcon = GitHubApi.normalizeImageUrl(iconUrl)
                        val cleanScreenshots = screenshotsList.map { GitHubApi.normalizeImageUrl(it) }
                        val screenshotsJson = cleanScreenshots.joinToString(",") { "\"$it\"" }
                        val json = """
                        [
                          {
                            "name": "$name",
                            "id": "${name.lowercase().replace(" ", "")}",
                            "description": "$description",
                            "icon": "$cleanIcon",
                            "repoUrl": "https://github.com/$cleanRepo",
                            "githubRepo": "$cleanRepo",
                            "category": "$category",
                            "author": "$author",
                            "packageName": "$packageName",
                            "screenshots": [$screenshotsJson]
                          }
                        ]
                        """.trimIndent()

                        val issueUrl = "https://github.com/adityasaini404/lumo-store-apps/issues/new" +
                                "?title=" + Uri.encode("App Submission: $name") +
                                "&body=" + Uri.encode(
                            "Hi, please add my app to Lumo Store!\n\n" +
                                    "### App Details\n" +
                                    "- **Name:** $name\n" +
                                    "- **Developer:** $author\n" +
                                    "- **Repo:** $cleanRepo\n" +
                                    "- **Package:** $packageName\n\n" +
                                    "### App JSON\n```json\n$json\n```"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(primaryColor, secondaryColor))
                                )
                                .clickable {
                                    showSuccessDialog = false
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl))
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context, "Could not open browser",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.OpenInNew, null,
                                    tint = Color.White, modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Open GitHub Issue",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Cancel",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { showSuccessDialog = false }
                                .padding(8.dp)
                        )
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
private fun LivePreviewCard(
    name: String,
    author: String,
    category: String,
    iconUrl: String,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(primaryColor.copy(alpha = 0.15f), secondaryColor.copy(alpha = 0.15f))
                )
            )
            .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(textColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (iconUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = GitHubApi.normalizeImageUrl(iconUrl),
                        contentDescription = "App Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Image, null,
                                    tint = textColor.copy(alpha = 0.4f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    name.take(1).uppercase(),
                                    color = primaryColor,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                } else {
                    Text(
                        name.take(1).uppercase(),
                        color = primaryColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Visibility, null,
                        tint = primaryColor, modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "LIVE PREVIEW",
                        color = primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    name,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "$author · $category",
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(
    icon: ImageVector,
    text: String,
    color: Color,
    textColor: Color,
    trailing: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        if (trailing != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(trailing, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FormCard(
    surfaceColor: Color,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
private fun InfoHint(
    icon: ImageVector,
    color: Color,
    text: String,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = textColor.copy(alpha = 0.55f), fontSize = 10.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val textColor = MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = textColor.copy(alpha = 0.5f), fontSize = 11.sp)
        Text(
            value,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp).widthIn(max = 180.dp)
        )
    }
}

@Composable
private fun FieldLabel(label: String, textColor: Color) {
    Text(
        label,
        color = textColor.copy(alpha = 0.5f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun LumoInput(
    label: String,
    value: String,
    placeholder: String,
    textColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    icon: ImageVector? = null,
    multiLine: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column {
        if (label.isNotBlank()) FieldLabel(label, textColor)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(placeholder, color = textColor.copy(alpha = 0.3f), fontSize = 13.sp)
            },
            leadingIcon = if (icon != null) {
                {
                    Icon(icon, null, tint = primaryColor, modifier = Modifier.size(18.dp))
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            minLines = if (multiLine) 3 else 1,
            maxLines = if (multiLine) 8 else 1,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                cursorColor = secondaryColor
            )
        )
    }
}

@Composable
private fun PreviewImageBox(
    url: String,
    size: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(textColor.copy(alpha = 0.05f))
            .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(cornerRadius))
    ) {
        SubcomposeAsyncImage(
            model = GitHubApi.normalizeImageUrl(url),
            contentDescription = "Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = textColor.copy(alpha = 0.4f)
                    )
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.BrokenImage, "Invalid URL",
                        tint = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )
    }
}