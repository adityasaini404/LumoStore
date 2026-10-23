package com.lumo.store.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lumo.store.data.FavoritesManager
import com.lumo.store.data.TokenManager
import com.lumo.store.data.UserProfileManager
import com.lumo.store.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var showProfileEdit by remember { mutableStateOf(false) }

    var userToken by remember { mutableStateOf(TokenManager.getToken(context) ?: "") }
    val isTokenActive = userToken.isNotBlank()

    // Hoisted state so the profile card updates immediately after edit
    var displayName by remember { mutableStateOf(UserProfileManager.getName(context)) }
    var avatarIndex by remember { mutableStateOf(UserProfileManager.getAvatarIndex(context)) }

    val bgColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val greenActive = Color(0xFF10B981)
    val amberFree = Color(0xFFF59E0B)

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp)
        ) {
            // ---------- 1. TOP BAR ----------
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.08f))
                            .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Settings", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 2. DYNAMIC PROFILE CARD ----------
            item {
                val favoritesCount = remember { FavoritesManager.getFavorites(context).size }
                UserProfileCard(
                    displayName = displayName,
                    avatarIndex = avatarIndex,
                    isTokenActive = isTokenActive,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textColor = textColor,
                    favoritesCount = favoritesCount,
                    onEditClick = { showProfileEdit = true }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 3. APP PREFERENCES ----------
            item {
                SettingsGroupTitle("App Preferences", textColor)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsItem(
                            icon = Icons.Outlined.ColorLens,
                            title = "App Theme",
                            subtitle = "Current: $currentTheme",
                            textColor = textColor,
                            onClick = { showThemeSheet = true }
                        )
                        HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Outlined.Star,
                            title = "Saved Apps",
                            subtitle = "View your favorites",
                            textColor = textColor,
                            onClick = { Toast.makeText(context, "Open 'Favorites' tab on Home", Toast.LENGTH_SHORT).show() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 4. DEVELOPER SETTINGS (TOKEN) ----------
            item {
                SettingsGroupTitle("Developer Settings", textColor)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Key, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "GitHub Personal Access Token",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isTokenActive) greenActive.copy(alpha = 0.15f)
                                        else amberFree.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isTokenActive) "5,000/hr" else "60/hr Free",
                                    color = if (isTokenActive) greenActive else amberFree,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTokenActive)
                                "Your token is active. Limit: 5,000 requests/hour."
                            else
                                "Default free credits (60 requests/hour). Add your token to increase the limit.",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userToken,
                            onValueChange = { userToken = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("ghp_xxxxxxxxxxxx", color = textColor.copy(alpha = 0.3f), fontSize = 14.sp) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                focusedContainerColor = bgColor,
                                unfocusedContainerColor = bgColor
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "GitHub → Settings → Developer settings → Personal access tokens → Generate new token (classic) → scope: public_repo",
                            color = textColor.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isTokenActive) primaryColor else primaryColor.copy(alpha = 0.4f))
                                    .clickable(enabled = userToken.isNotBlank()) {
                                        TokenManager.saveToken(context, userToken.trim())
                                        Toast.makeText(context, "Token saved. 5,000 requests/hour active.", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Save Token", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }

                            if (isTokenActive) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(textColor.copy(alpha = 0.1f))
                                        .clickable {
                                            userToken = ""
                                            TokenManager.clearToken(context)
                                            Toast.makeText(context, "Token removed. Back to free credits (60/hour).", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Remove", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- 5. ABOUT & SUPPORT ----------
            item {
                SettingsGroupTitle("About & Support", textColor)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        SettingsItem(
                            icon = Icons.Outlined.BugReport,
                            title = "Report a Bug",
                            subtitle = "Open GitHub Issue",
                            textColor = textColor,
                            onClick = {
                                val url = "https://github.com/adityasaini404/lumo-store-apps/issues/new"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            }
                        )
                        HorizontalDivider(color = textColor.copy(alpha = 0.05f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsItem(
                            icon = Icons.Outlined.Info,
                            title = "About Lumo Store",
                            subtitle = "Version 1.0.0",
                            textColor = textColor,
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }

        // ---------- PROFILE EDIT BOTTOM SHEET ----------
        if (showProfileEdit) {
            ModalBottomSheet(
                onDismissRequest = { showProfileEdit = false },
                containerColor = surfaceColor,
                contentColor = textColor
            ) {
                ProfileEditSheet(
                    initialName = displayName,
                    initialAvatar = avatarIndex,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    textColor = textColor,
                    onDismiss = { showProfileEdit = false },
                    onSave = { newName, newAvatar ->
                        displayName = newName
                        avatarIndex = newAvatar
                        UserProfileManager.setName(context, newName)
                        UserProfileManager.setAvatarIndex(context, newAvatar)
                        showProfileEdit = false
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ---------- THEME SELECTION BOTTOM SHEET ----------
        if (showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showThemeSheet = false },
                containerColor = surfaceColor,
                contentColor = textColor
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                    Text("Choose Theme", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))

                    val themes = listOf(
                        "Dark", "Light", "Amoled", "Ocean", "Cyberpunk", "Forest", "Sunset", "Sakura",
                        "Dracula", "Nord", "Coffee", "iPhone Light", "iPhone Dark"
                    )

                    themes.forEach { theme ->
                        val isSelected = currentTheme == theme
                        val colorPreview = when (theme) {
                            "Dark" -> listOf(Color(0xFF7C5CFF), Color(0xFF4DD9FF))
                            "Light" -> listOf(Color(0xFF7C5CFF), Color(0xFFEEEEEE))
                            "Amoled" -> listOf(Color(0xFFBB86FC), Color(0xFF000000))
                            "Ocean" -> listOf(Color(0xFF00B4D8), Color(0xFF03045E))
                            "Cyberpunk" -> listOf(Color(0xFFFF007F), Color(0xFF00F0FF))
                            "Forest" -> listOf(Color(0xFF4CAF50), Color(0xFF111A12))
                            "Sunset" -> listOf(Color(0xFFFF5722), Color(0xFFE91E63))
                            "Sakura" -> listOf(Color(0xFFFF8DA1), Color(0xFF1F1315))
                            "Dracula" -> listOf(Color(0xFFBD93F9), Color(0xFF282A36))
                            "Nord" -> listOf(Color(0xFF88C0D0), Color(0xFF2E3440))
                            "Coffee" -> listOf(Color(0xFFD4A373), Color(0xFF1E1612))
                            "iPhone Light" -> listOf(Color(0xFF007AFF), Color(0xFFFFFFFF))
                            "iPhone Dark" -> listOf(Color(0xFF0A84FF), Color(0xFF1C1C1E))
                            else -> listOf(Color.Gray, Color.DarkGray)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { onThemeChange(theme); showThemeSheet = false }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colorPreview))
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                theme,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) Text("✓", color = primaryColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        // ---------- ABOUT DIALOG ----------
        if (showAboutDialog) {
            Dialog(onDismissRequest = { showAboutDialog = false }) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Lumo Store", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text("Version 1.0.0", color = primaryColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Lumo Store is a free, open-source app store for Android. Discover, download, and update apps directly from GitHub.",
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                                .clickable { showAboutDialog = false }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ---------- PROFILE EDIT BOTTOM SHEET CONTENT ----------
@Composable
fun ProfileEditSheet(
    initialName: String,
    initialAvatar: Int,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedAvatar by remember { mutableStateOf(initialAvatar) }
    val bgColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
    ) {
        Text(
            "Edit Profile",
            color = textColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Name field
        Text(
            "Your name",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(24) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your name", color = textColor.copy(alpha = 0.3f), fontSize = 14.sp) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = primaryColor,
                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                focusedContainerColor = bgColor,
                unfocusedContainerColor = bgColor
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Avatar picker
        Text(
            "Choose avatar",
            color = textColor.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..3) {
                val isSelected = selectedAvatar == i
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) primaryColor else textColor.copy(alpha = 0.1f),
                            shape = CircleShape
                        )
                        .clickable { selectedAvatar = i }
                        .padding(4.dp)
                ) {
                    UserAvatar(
                        avatarIndex = i,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save / Cancel buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(textColor.copy(alpha = 0.1f))
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor)
                    .clickable { onSave(name, selectedAvatar) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Save", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- DYNAMIC PROFILE CARD ----------
@Composable
fun UserProfileCard(
    displayName: String,
    avatarIndex: Int,
    isTokenActive: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    favoritesCount: Int,
    onEditClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val accentColor = if (isTokenActive) Color(0xFF10B981) else Color(0xFFF59E0B)
    val effectiveName = displayName.ifBlank { if (isTokenActive) "Verified User" else "Guest User" }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header: avatar + name + edit
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Avatar with pulsing glow ring and status badge
                Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = pulseAlpha))
                    )
                    UserAvatar(
                        avatarIndex = avatarIndex,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.size(72.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-2).dp, y = (-2).dp)
                            .clip(CircleShape)
                            .background(accentColor)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTokenActive) "✓" else "!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = effectiveName,
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTokenActive) "Active · 5,000 req/hr" else "Free tier · 60 req/hr",
                            color = textColor.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Edit button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.06f))
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit profile",
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = textColor.copy(alpha = 0.06f))
            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileStat(
                    value = favoritesCount.toString(),
                    label = "Favorites",
                    accent = secondaryColor,
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    value = if (isTokenActive) "Pro" else "Free",
                    label = "Tier",
                    accent = if (isTokenActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                ProfileStat(
                    value = if (isTokenActive) "5K" else "60",
                    label = "Req / hr",
                    accent = primaryColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ProfileStat(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.08f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ---------- CANVAS-DRAWN AVATARS (no image assets needed) ----------
@Composable
fun UserAvatar(
    avatarIndex: Int,
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2

        // Gradient background circle
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(primaryColor, secondaryColor),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            ),
            radius = radius,
            center = center
        )

        // Draw the selected character on top
        when (avatarIndex.coerceIn(0, 3)) {
            0 -> drawRobotAvatar(center, radius)
            1 -> drawCatAvatar(center, radius)
            2 -> drawAstronautAvatar(center, radius)
            3 -> drawAlienAvatar(center, radius)
        }
    }
}

private fun DrawScope.drawRobotAvatar(center: Offset, radius: Float) {
    val white = Color.White
    val dark = Color(0xFF1F2937)

    // Antenna
    drawLine(
        color = white,
        start = Offset(center.x, center.y - radius * 0.78f),
        end = Offset(center.x, center.y - radius * 0.5f),
        strokeWidth = radius * 0.05f
    )
    drawCircle(color = white, radius = radius * 0.08f, center = Offset(center.x, center.y - radius * 0.82f))

    // Head
    val headW = radius * 0.9f
    val headH = radius * 0.72f
    drawRoundRect(
        color = white,
        topLeft = Offset(center.x - headW / 2, center.y - radius * 0.5f),
        size = Size(headW, headH),
        cornerRadius = CornerRadius(headW * 0.22f)
    )

    // Eyes
    drawCircle(dark, radius * 0.11f, Offset(center.x - headW * 0.22f, center.y - radius * 0.15f))
    drawCircle(dark, radius * 0.11f, Offset(center.x + headW * 0.22f, center.y - radius * 0.15f))

    // Mouth
    drawLine(
        color = dark,
        start = Offset(center.x - headW * 0.16f, center.y + radius * 0.08f),
        end = Offset(center.x + headW * 0.16f, center.y + radius * 0.08f),
        strokeWidth = radius * 0.035f
    )
}

private fun DrawScope.drawCatAvatar(center: Offset, radius: Float) {
    val white = Color.White
    val dark = Color(0xFF1F2937)
    val pink = Color(0xFFFFB4C8)

    // Ears (triangles)
    val leftEar = Path().apply {
        moveTo(center.x - radius * 0.55f, center.y - radius * 0.15f)
        lineTo(center.x - radius * 0.55f, center.y - radius * 0.75f)
        lineTo(center.x - radius * 0.15f, center.y - radius * 0.45f)
        close()
    }
    drawPath(leftEar, white)
    val rightEar = Path().apply {
        moveTo(center.x + radius * 0.55f, center.y - radius * 0.15f)
        lineTo(center.x + radius * 0.55f, center.y - radius * 0.75f)
        lineTo(center.x + radius * 0.15f, center.y - radius * 0.45f)
        close()
    }
    drawPath(rightEar, white)

    // Face
    drawCircle(white, radius * 0.6f, Offset(center.x, center.y + radius * 0.05f))

    // Eyes
    drawCircle(dark, radius * 0.09f, Offset(center.x - radius * 0.22f, center.y - radius * 0.05f))
    drawCircle(dark, radius * 0.09f, Offset(center.x + radius * 0.22f, center.y - radius * 0.05f))

    // Nose
    val nose = Path().apply {
        moveTo(center.x - radius * 0.07f, center.y + radius * 0.1f)
        lineTo(center.x + radius * 0.07f, center.y + radius * 0.1f)
        lineTo(center.x, center.y + radius * 0.18f)
        close()
    }
    drawPath(nose, pink)

    // Whiskers
    val whiskerY = center.y + radius * 0.15f
    drawLine(dark, Offset(center.x - radius * 0.15f, whiskerY), Offset(center.x - radius * 0.55f, whiskerY - radius * 0.08f), radius * 0.025f)
    drawLine(dark, Offset(center.x - radius * 0.15f, whiskerY + radius * 0.05f), Offset(center.x - radius * 0.55f, whiskerY + radius * 0.1f), radius * 0.025f)
    drawLine(dark, Offset(center.x + radius * 0.15f, whiskerY), Offset(center.x + radius * 0.55f, whiskerY - radius * 0.08f), radius * 0.025f)
    drawLine(dark, Offset(center.x + radius * 0.15f, whiskerY + radius * 0.05f), Offset(center.x + radius * 0.55f, whiskerY + radius * 0.1f), radius * 0.025f)
}

private fun DrawScope.drawAstronautAvatar(center: Offset, radius: Float) {
    val white = Color.White
    val visor = Color(0xFF0F172A)
    val shine = Color(0xFF38BDF8)

    // Helmet
    drawCircle(white, radius * 0.65f, Offset(center.x, center.y - radius * 0.05f))

    // Visor
    drawCircle(visor, radius * 0.45f, Offset(center.x, center.y - radius * 0.05f))

    // Visor shine
    drawCircle(shine, radius * 0.12f, Offset(center.x - radius * 0.15f, center.y - radius * 0.25f))
    drawCircle(shine.copy(alpha = 0.6f), radius * 0.07f, Offset(center.x + radius * 0.15f, center.y + radius * 0.05f))

    // Suit collar at bottom
    val collar = Path().apply {
        moveTo(center.x - radius * 0.55f, center.y + radius * 0.55f)
        quadraticBezierTo(center.x, center.y + radius * 0.45f, center.x + radius * 0.55f, center.y + radius * 0.55f)
        lineTo(center.x + radius * 0.55f, center.y + radius * 0.7f)
        lineTo(center.x - radius * 0.55f, center.y + radius * 0.7f)
        close()
    }
    drawPath(collar, white)
}

private fun DrawScope.drawAlienAvatar(center: Offset, radius: Float) {
    val white = Color.White
    val dark = Color(0xFF1F2937)

    // Head (oval)
    val headPath = Path().apply {
        moveTo(center.x, center.y - radius * 0.7f)
        quadraticBezierTo(center.x + radius * 0.7f, center.y - radius * 0.45f, center.x + radius * 0.55f, center.y + radius * 0.25f)
        quadraticBezierTo(center.x, center.y + radius * 0.65f, center.x - radius * 0.55f, center.y + radius * 0.25f)
        quadraticBezierTo(center.x - radius * 0.7f, center.y - radius * 0.45f, center.x, center.y - radius * 0.7f)
        close()
    }
    drawPath(headPath, white)

    // Big alien eyes
    drawOval(
        color = dark,
        topLeft = Offset(center.x - radius * 0.42f, center.y - radius * 0.15f),
        size = Size(radius * 0.28f, radius * 0.42f)
    )
    drawOval(
        color = dark,
        topLeft = Offset(center.x + radius * 0.14f, center.y - radius * 0.15f),
        size = Size(radius * 0.28f, radius * 0.42f)
    )

    // Eye highlights
    drawCircle(white, radius * 0.045f, Offset(center.x - radius * 0.32f, center.y - radius * 0.02f))
    drawCircle(white, radius * 0.045f, Offset(center.x + radius * 0.24f, center.y - radius * 0.02f))

    // Small mouth
    drawLine(
        color = dark,
        start = Offset(center.x - radius * 0.12f, center.y + radius * 0.35f),
        end = Offset(center.x + radius * 0.12f, center.y + radius * 0.35f),
        strokeWidth = radius * 0.035f
    )

    // Antennae
    drawLine(white, Offset(center.x - radius * 0.2f, center.y - radius * 0.65f), Offset(center.x - radius * 0.3f, center.y - radius * 0.85f), radius * 0.04f)
    drawCircle(white, radius * 0.06f, Offset(center.x - radius * 0.3f, center.y - radius * 0.88f))
    drawLine(white, Offset(center.x + radius * 0.2f, center.y - radius * 0.65f), Offset(center.x + radius * 0.3f, center.y - radius * 0.85f), radius * 0.04f)
    drawCircle(white, radius * 0.06f, Offset(center.x + radius * 0.3f, center.y - radius * 0.88f))
}

// Section title above each group of settings
@Composable
fun SettingsGroupTitle(title: String, textColor: Color) {
    Text(
        text = title,
        color = textColor.copy(alpha = 0.6f),
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

// Single settings row with icon, title, subtitle and arrow
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
        }
        Text("→", color = textColor.copy(alpha = 0.4f), fontSize = 18.sp)
    }
}