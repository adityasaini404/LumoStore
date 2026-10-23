package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubmitScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var githubRepo by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }

    // Category dropdown
    var category by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    val categoriesList = listOf(
        "Tools", "Media", "Art", "Games", "Productivity",
        "Educational", "Utility", "Social", "Developer"
    )

    // Screenshots
    var newScreenshotUrl by remember { mutableStateOf("") }
    val screenshotsList = remember { mutableStateListOf<String>() }

    var showSuccessDialog by remember { mutableStateOf(false) }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ---------- TOP BAR ----------
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
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
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Publish App", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Share your creation with the world", color = textColor.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ---------- SECTION 1: BASIC DETAILS ----------
                item {
                    FormSection(title = "Basic Details", textColor = textColor, surfaceColor = surfaceColor) {
                        LumoInput(
                            label = "App Name *",
                            value = name,
                            placeholder = "e.g. Lumo Notes",
                            icon = Icons.Default.Apps,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor
                        ) { name = it }

                        LumoInput(
                            label = "Short Description *",
                            value = description,
                            placeholder = "e.g. A simple and fast notes app.",
                            icon = Icons.Default.Description,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor,
                            multiLine = true
                        ) { description = it }

                        LumoInput(
                            label = "Developer Name *",
                            value = author,
                            placeholder = "e.g. justshivv",
                            icon = Icons.Default.Person,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor
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
                                    placeholder = { Text("Select a category", color = textColor.copy(alpha = 0.4f)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Category, contentDescription = null, tint = primaryColor)
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
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

                // ---------- SECTION 2: REPOSITORY ----------
                item {
                    FormSection(title = "Repository & Links", textColor = textColor, surfaceColor = surfaceColor) {
                        LumoInput(
                            label = "GitHub Repo (owner/repo) *",
                            value = githubRepo,
                            placeholder = "e.g. justshivv/Opentune",
                            icon = Icons.Default.Code,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor
                        ) { githubRepo = it }

                        LumoInput(
                            label = "Package Name",
                            value = packageName,
                            placeholder = "e.g. com.opentune",
                            icon = Icons.Default.Android,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor
                        ) { packageName = it }
                    }
                }

                // ---------- SECTION 3: APP ICON ----------
                item {
                    FormSection(title = "App Icon", textColor = textColor, surfaceColor = surfaceColor) {
                        LumoInput(
                            label = "Icon Image URL *",
                            value = iconUrl,
                            placeholder = "https://raw.githubusercontent.com/.../icon.png",
                            icon = Icons.Default.Image,
                            textColor = textColor,
                            primaryColor = primaryColor,
                            secondaryColor = secondaryColor,
                            surfaceColor = surfaceColor
                        ) { iconUrl = it }

                        HintText(
                            "Tip: On GitHub, open the image file and click \"Raw\" to get a direct link. " +
                                    "Blob URLs are converted automatically.",
                            textColor = textColor
                        )

                        // Live preview of the icon
                        if (iconUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PreviewImageBox(
                                    url = iconUrl,
                                    size = 72.dp,
                                    cornerRadius = 16.dp,
                                    textColor = textColor
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Preview", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        "URL to be saved:",
                                        color = textColor.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        GitHubApi.normalizeImageUrl(iconUrl),
                                        color = textColor.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------- SECTION 4: SCREENSHOTS ----------
                item {
                    FormSection(title = "Screenshots", textColor = textColor, surfaceColor = surfaceColor) {
                        Text(
                            "Add 2-5 screenshots (optional, but recommended)",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                        HintText(
                            "Paste a screenshot URL and tap the + button. " +
                                    "GitHub blob URLs are converted to raw URLs automatically.",
                            textColor = textColor
                        )

                        // Add screenshot row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f)) {
                                LumoInput(
                                    label = "",
                                    value = newScreenshotUrl,
                                    placeholder = "https://raw.githubusercontent.com/.../screen.png",
                                    icon = Icons.Default.PhotoCamera,
                                    textColor = textColor,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    surfaceColor = surfaceColor
                                ) { newScreenshotUrl = it }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Screenshot",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Live preview of the URL being typed
                        if (newScreenshotUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PreviewImageBox(
                                    url = newScreenshotUrl,
                                    size = 48.dp,
                                    cornerRadius = 10.dp,
                                    textColor = textColor
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Preview",
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // List of added screenshots with previews
                        if (screenshotsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "${screenshotsList.size} screenshot(s) added",
                                color = textColor.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(screenshotsList) { url ->
                                    Box(modifier = Modifier.width(90.dp)) {
                                        Column {
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
                                                            Modifier.fillMaxSize().background(textColor.copy(alpha = 0.05f)),
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
                                                            Modifier.fillMaxSize().background(textColor.copy(alpha = 0.05f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                Icons.Outlined.BrokenImage,
                                                                contentDescription = "Error",
                                                                tint = Color.Red.copy(alpha = 0.6f),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                )
                                                // Remove button overlay
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
                                                        Icons.Default.Close,
                                                        contentDescription = "Remove",
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

                // ---------- SUBMIT BUTTON ----------
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                            .clickable {
                                if (name.isNotBlank() && githubRepo.isNotBlank() && author.isNotBlank()
                                    && category.isNotBlank() && iconUrl.isNotBlank()
                                ) {
                                    showSuccessDialog = true
                                } else {
                                    android.widget.Toast.makeText(
                                        context,
                                        "Please fill all required fields (*)",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit for Review", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        // ---------- SUCCESS DIALOG ----------
        if (showSuccessDialog) {
            Dialog(onDismissRequest = { showSuccessDialog = false }) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = primaryColor,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ready to Publish!", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Click below to open a GitHub Issue. Our team will review and add your app.",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Build a clean JSON payload for the issue
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
                                    "- **Repo:** $cleanRepo\n\n" +
                                    "### App JSON\n```json\n$json\n```"
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                                .clickable {
                                    showSuccessDialog = false
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl)))
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Open GitHub & Submit", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Cancel",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { showSuccessDialog = false }.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------- HINT TEXT ----------
@Composable
fun HintText(text: String, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Lightbulb,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text,
            color = textColor.copy(alpha = 0.55f),
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
}

// ---------- PREVIEW IMAGE BOX ----------
@Composable
fun PreviewImageBox(
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
                        Icons.Outlined.BrokenImage,
                        contentDescription = "Invalid URL",
                        tint = Color.Red.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )
    }
}

// ---------- FORM SECTION ----------
@Composable
fun FormSection(
    title: String,
    textColor: Color,
    surfaceColor: Color,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            "• $title",
            color = textColor.copy(alpha = 0.8f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surfaceColor)
                .border(1.dp, textColor.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) { content() }
        }
    }
}

// ---------- FIELD LABEL ----------
@Composable
fun FieldLabel(label: String, textColor: Color) {
    Text(
        label,
        color = textColor.copy(alpha = 0.5f),
        fontSize = 12.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

// ---------- LUMO INPUT ----------
@Composable
fun LumoInput(
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
        if (label.isNotBlank()) {
            FieldLabel(label, textColor)
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = textColor.copy(alpha = 0.3f), fontSize = 13.sp) },
            leadingIcon = if (icon != null) {
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
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