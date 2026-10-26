package com.lumo.store

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.lumo.store.data.AppInfo
import com.lumo.store.data.FavoritesManager
import com.lumo.store.data.ThemeManager
import com.lumo.store.data.UserProfileManager
import com.lumo.store.screens.AboutMeScreen
import com.lumo.store.screens.AppDetailScreen
import com.lumo.store.screens.SettingsScreen
import com.lumo.store.screens.SplashScreen
import com.lumo.store.screens.SubmitScreen
import com.lumo.store.screens.UserAvatar
import com.lumo.store.ui.components.AppRateLimitDialog
import com.lumo.store.ui.components.BottomNavBar
import com.lumo.store.ui.components.DownloadBar
import com.lumo.store.ui.components.ShimmerAppCard
import com.lumo.store.ui.theme.LumoTheme
import com.lumo.store.viewmodel.AppViewModel
import com.lumo.store.viewmodel.DownloadViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val context = LocalContext.current
            var currentTheme by remember { mutableStateOf(ThemeManager.getTheme(context)) }

            LumoTheme(themeName = currentTheme) {
                LumoNavHost(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme: String ->
                        currentTheme = newTheme
                        ThemeManager.setTheme(context, newTheme)
                    }
                )
            }
        }
    }
}

@Composable
fun LumoNavHost(
    currentTheme: String,
    onThemeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: AppViewModel = viewModel()
    val downloadViewModel: DownloadViewModel = viewModel()
    val apps by viewModel.apps.collectAsState()
    val downloadState by downloadViewModel.downloadState.collectAsState()
    val context = LocalContext.current

    var showSplash by remember { mutableStateOf(true) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    if (showSplash) {
        SplashScreen(onTimeout = { showSplash = false })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                enterTransition = {
                    scaleIn(initialScale = 0.95f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    scaleOut(targetScale = 0.95f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350))
                },
                popEnterTransition = {
                    scaleIn(initialScale = 0.95f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    scaleOut(targetScale = 0.95f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350))
                }
            ) {
                composable("home") {
                    HomeScreen(
                        viewModel = viewModel,
                        onAppClick = { app ->
                            val encodedName = Uri.encode(app.name)
                            navController.navigate("detail/$encodedName")
                        },
                        onSubmitClick = { navController.navigate("submit") },
                        onDownloadClick = { app -> downloadViewModel.startDownload(context, app) },
                        onProfileClick = { navController.navigate("settings") },
                        initialCategory = "All"
                    )
                }

                composable("installed") {
                    HomeScreen(
                        viewModel = viewModel,
                        onAppClick = { app ->
                            val encodedName = Uri.encode(app.name)
                            navController.navigate("detail/$encodedName")
                        },
                        onSubmitClick = { navController.navigate("submit") },
                        onDownloadClick = { app -> downloadViewModel.startDownload(context, app) },
                        onProfileClick = { navController.navigate("settings") },
                        initialCategory = "Installed"
                    )
                }

                composable("detail/{name}") { backStackEntry ->
                    val name = backStackEntry.arguments?.getString("name") ?: ""
                    val app = apps.find { it.name == name }
                    if (app != null) {
                        AppDetailScreen(
                            app = app,
                            onBack = { navController.popBackStack() },
                            onDownloadClick = { app, variant ->
                                downloadViewModel.startDownload(context, app, variant)
                            }
                        )
                    }
                }

                composable("submit") { SubmitScreen(onBack = { navController.popBackStack() }) }

                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange
                    )
                }

                composable("about") {
                    AboutMeScreen(onBack = { navController.popBackStack() })
                }
            }

            if (currentRoute == "home" || currentRoute == "installed" || currentRoute == "settings" || currentRoute == "about") {
                BottomNavBar(
                    currentTab = when {
                        currentRoute == "settings" -> "Settings"
                        currentRoute == "about" -> "Dev"
                        currentRoute == "installed" -> "Installed"
                        else -> "Home"
                    },
                    onTabSelected = { tab ->
                        when (tab) {
                            "Home" -> navController.navigate("home") { popUpTo("home") { inclusive = true } }
                            "Installed" -> navController.navigate("installed") { popUpTo("home") }
                            "Dev" -> navController.navigate("about") { popUpTo("home") }
                            "Settings" -> navController.navigate("settings") { popUpTo("home") }
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            DownloadBar(
                state = downloadState,
                onInstall = { downloadViewModel.installApk(context, downloadState.app?.name ?: "") },
                onDismiss = { downloadViewModel.dismiss() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )

            AppRateLimitDialog(
                onOpenSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                }
            )

            LaunchedEffect(downloadState.status, downloadState.app?.id) {
                if (downloadState.status == com.lumo.store.viewmodel.DownloadStatus.Downloaded
                    && downloadState.app != null) {
                    kotlinx.coroutines.delay(600)
                    downloadViewModel.installApk(context, downloadState.app!!.name)
                }
            }
        }
    }
}

// ========================================
// HELPER: Format Downloads Count
// ========================================
fun formatDownloads(count: Long): String {
    if (count <= 0) return ""
    return when {
        count >= 1_000_000 -> String.format("%.1fM+", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK+", count / 1_000.0)
        else -> "$count"
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel = viewModel(),
    onAppClick: (AppInfo) -> Unit = {},
    onSubmitClick: () -> Unit = {},
    onDownloadClick: (AppInfo) -> Unit = {},
    onProfileClick: () -> Unit = {},
    initialCategory: String = "All"
) {
    val apps by viewModel.apps.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val rateLimitMessage by viewModel.rateLimitMessage.collectAsState()
    val context = LocalContext.current

    val avatarIndex = UserProfileManager.getAvatarIndex(context)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val surfaceColor = MaterialTheme.colorScheme.surface

    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(FavoritesManager.getFavorites(context)) }
    var sortBy by remember { mutableStateOf("Newest") }
    var showSortSheet by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val categories = remember(apps) {
        val baseCategories = mutableSetOf("All", "Updates", "Installed", "Favorites")
        apps.forEach { app -> if (app.category.isNotBlank()) baseCategories.add(app.category) }
        baseCategories.toList()
    }

    val baseFiltered = apps.filter { app ->
        val isFavTab = selectedCategory == "Favorites"
        val isUpdatesTab = selectedCategory == "Updates"
        val isInstalledTab = selectedCategory == "Installed"

        val matchesCategory = when {
            isFavTab -> favorites.contains(app.id)
            isUpdatesTab -> app.isUpdateAvailable
            isInstalledTab -> app.isInstalled
            else -> selectedCategory == "All" || app.category.equals(selectedCategory, ignoreCase = true)
        }

        // Search Logic: Instantly filter by name
        val matchesSearch = searchQuery.isBlank() || app.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredApps = when (sortBy) {
        "A-Z" -> baseFiltered.sortedBy { it.name.lowercase() }
        "Z-A" -> baseFiltered.sortedByDescending { it.name.lowercase() }
        else -> baseFiltered.reversed() // Newest first
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ========================================
            // TOP HEADER (Logo, Title, Actions)
            // ========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo (Using your drawable)
                Image(
                    painter = painterResource(id = R.drawable.ic_lumo_logo),
                    contentDescription = "Lumo Logo",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Lumo Store",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Open Source. No Limits.",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Submit Button (+)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(surfaceColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), CircleShape)
                        .clickable { onSubmitClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Submit App", tint = textColor, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, primaryColor.copy(alpha = 0.5f), CircleShape)
                        .clickable { onProfileClick() }
                ) {
                    UserAvatar(
                        avatarIndex = avatarIndex,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // ========================================
            // SEARCH BAR
            // ========================================
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it }, // Instant filtering
                    textStyle = TextStyle(color = textColor, fontSize = 14.sp),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search apps by name...", color = textColor.copy(alpha = 0.4f), fontSize = 14.sp)
                        }
                        innerTextField()
                    }
                )

                // Filter/Sort Icon
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { showSortSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Tune, contentDescription = "Filter", tint = primaryColor, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========================================
            // MAIN SCROLLABLE CONTENT
            // ========================================
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.loadApps()
                    scope.launch {
                        delay(1200)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Rate-limit banner
                    if (rateLimitMessage != null) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFFF3CD))
                                    .border(1.dp, Color(0xFFFFEBAA), RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Warning, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "API Limit: $rateLimitMessage",
                                    color = Color(0xFF856404),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Categories (Squircle Icons)
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(categories) { catName ->
                                val sel = catName == selectedCategory
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(64.dp)
                                        .clickable { selectedCategory = catName }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (sel) primaryColor else surfaceColor)
                                            .border(1.dp, if (sel) Color.Transparent else textColor.copy(alpha = 0.05f), RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = categoryIconFor(catName),
                                            contentDescription = catName,
                                            tint = if (sel) Color.White else textColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = catName,
                                        color = if (sel) primaryColor else textColor.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Featured Apps Carousel
                    if (selectedCategory == "All" && searchQuery.isBlank() && filteredApps.size >= 2) {
                        item {
                            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Featured Apps",
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(filteredApps.take(6)) { app ->
                                        FeaturedAppCard(
                                            app = app,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            textColor = textColor,
                                            surfaceColor = surfaceColor,
                                            onClick = { onAppClick(app) },
                                            onInstallClick = { onAppClick(app) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top Free / All Apps List
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedCategory == "All") "Top Free Apps" else selectedCategory,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(surfaceColor)
                                    .clickable { showSortSheet = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sort, "Sort", tint = textColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(sortBy, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Apps list
                    if (loading && apps.isEmpty()) {
                        items(4) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)) {
                                ShimmerAppCard()
                            }
                        }
                    } else if (filteredApps.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.SearchOff, null, tint = primaryColor.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No apps found", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                if (searchQuery.isNotBlank()) {
                                    Text("Try a different name", color = textColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        itemsIndexed(filteredApps) { index, app ->
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                TopFreeAppCard(
                                    rank = index + 1,
                                    app = app,
                                    primaryColor = primaryColor,
                                    secondaryColor = secondaryColor,
                                    textColor = textColor,
                                    surfaceColor = surfaceColor,
                                    onClick = { onAppClick(app) },
                                    onInstallClick = { onAppClick(app) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ========================================
        // SORT BOTTOM SHEET
        // ========================================
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                containerColor = surfaceColor,
                contentColor = textColor
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Sort Apps By", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    listOf(
                        "Newest" to Icons.Default.NewReleases,
                        "A-Z" to Icons.Default.SortByAlpha,
                        "Z-A" to Icons.Default.SortByAlpha
                    ).forEach { (option, icon) ->
                        val isSelected = sortBy == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) primaryColor.copy(alpha = 0.10f) else Color.Transparent)
                                .clickable { sortBy = option; showSortSheet = false }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) primaryColor.copy(alpha = 0.15f) else textColor.copy(alpha = 0.06f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = if (isSelected) primaryColor else textColor.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(option, fontSize = 15.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) primaryColor else textColor, modifier = Modifier.weight(1f))
                            if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = primaryColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// ========================================
// NEW PREMIUM FEATURED CARD
// ========================================
@Composable
fun FeaturedAppCard(
    app: AppInfo,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit = {},
    onInstallClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        secondaryColor.copy(alpha = 0.05f),
                        surfaceColor
                    )
                )
            )
            .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Icon
            AsyncImage(
                model = app.icon,
                contentDescription = app.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // App Name & Category
            Text(
                app.name,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                app.category,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.weight(1f))

            // Actual Downloads Count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (app.downloadCount > 0) formatDownloads(app.downloadCount) else "New",
                    color = textColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Install Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                    .clickable { onInstallClick() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "View",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ========================================
// NEW PREMIUM VERTICAL LIST CARD (Top Free)
// ========================================
@Composable
fun TopFreeAppCard(
    rank: Int,
    app: AppInfo,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit = {},
    onInstallClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Number
        Text(
            text = "$rank",
            color = if (rank <= 3) primaryColor else textColor.copy(alpha = 0.5f),
            fontSize = if (rank <= 3) 20.sp else 18.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(10.dp))

        // App Icon
        AsyncImage(
            model = app.icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
        )

        Spacer(modifier = Modifier.width(14.dp))

        // App Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                app.name,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                app.description.ifBlank { app.category },
                color = textColor.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Actual Downloads Count
            if (app.downloadCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.FileDownload, null, tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(formatDownloads(app.downloadCount), color = textColor.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Action Button (Open/Install)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (app.isInstalled) textColor.copy(alpha = 0.1f) else primaryColor.copy(alpha = 0.15f))
                .clickable { onInstallClick() }
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (app.isInstalled) "Open" else "Get",
                color = if (app.isInstalled) textColor else primaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ========================================
// CATEGORY ICON MAPPER
// ========================================
private fun categoryIconFor(name: String): ImageVector = when (name.lowercase()) {
    "all" -> Icons.Default.GridView
    "installed" -> Icons.Default.DownloadDone
    "updates" -> Icons.Default.SystemUpdate
    "favorites" -> Icons.Default.Favorite
    "educational", "education", "learning" -> Icons.Default.School
    "games", "game" -> Icons.Default.SportsEsports
    "tools", "utility", "utilities" -> Icons.Default.Build
    "social" -> Icons.Default.People
    "media", "music", "video" -> Icons.Default.PlayArrow
    "developer" -> Icons.Default.Code
    "productivity" -> Icons.Default.CheckCircle
    "art" -> Icons.Default.Brush
    else -> Icons.Default.Category
}