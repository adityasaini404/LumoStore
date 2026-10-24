package com.lumo.store

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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
import com.lumo.store.ui.components.AppCard
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
                    scaleIn(initialScale = 0.85f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    scaleOut(targetScale = 0.85f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350))
                },
                popEnterTransition = {
                    scaleIn(initialScale = 0.85f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    scaleOut(targetScale = 0.85f, animationSpec = tween(350, easing = FastOutSlowInEasing)) + fadeOut(animationSpec = tween(350))
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
                            onDownloadClick = { downloadViewModel.startDownload(context, app) }
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
                onDismiss = { downloadViewModel.dismiss() }
            )

            AppRateLimitDialog(
                onOpenSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                    }
                }
            )
        }
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

    // Profile avatar shown in the header
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

    // Local refresh state so the pull-to-refresh spinner stays visible long enough
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val categories = remember(apps) {
        val baseCategories = mutableSetOf("All", "Updates", "Installed", "Favorites")
        apps.forEach { app -> if (app.category.isNotBlank()) baseCategories.add(app.category) }
        baseCategories.toList()
    }

    val updateCount = apps.count { it.isUpdateAvailable }

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

        val matchesSearch = searchQuery.isBlank() || app.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val filteredApps = when (sortBy) {
        "A-Z" -> baseFiltered.sortedBy { it.name.lowercase() }
        "Z-A" -> baseFiltered.sortedByDescending { it.name.lowercase() }
        else -> baseFiltered.reversed()   // Newest: reversed apps.json order
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            // ---------- 1. COMPACT HEADER ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App logo from drawable
                Image(
                    painter = painterResource(id = R.drawable.ic_lumo_logo),
                    contentDescription = "Lumo Store",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.width(10.dp))

                // App title with a simple slogan
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Lumo Store",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        "Discover · Install · Enjoy",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }

                // Submit button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(primaryColor, secondaryColor)))
                        .clickable { onSubmitClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Submit App",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // User avatar from Settings
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, primaryColor.copy(alpha = 0.35f), CircleShape)
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

            // ---------- 2. SEARCH BAR ----------
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(surfaceColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = textColor.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(color = textColor, fontSize = 15.sp),
                    cursorBrush = SolidColor(primaryColor),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search apps, games, tools...", color = textColor.copy(alpha = 0.4f), fontSize = 15.sp)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(textColor.copy(alpha = 0.1f))
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = textColor, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- 3. CATEGORIES ----------
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { catName ->
                    val sel = catName == selectedCategory
                    val catIcon = categoryIconFor(catName)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (sel) Brush.horizontalGradient(listOf(primaryColor, secondaryColor)) else Brush.horizontalGradient(listOf(surfaceColor, surfaceColor)))
                            .border(
                                width = if (sel) 0.dp else 1.dp,
                                color = if (sel) Color.Transparent else textColor.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { selectedCategory = catName }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = if (sel) Color.White else textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = catName,
                            color = if (sel) Color.White else textColor.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ---------- 4. MAIN CONTENT ----------
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.loadApps()
                    // Ensure the refresh spinner is visible for at least 1.2 seconds
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
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFFFF3CD))
                                    .border(1.dp, Color(0xFFFFEBAA), RoundedCornerShape(16.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Warning, contentDescription = "Warning", tint = Color(0xFF856404), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "GitHub API Limit: $rateLimitMessage",
                                    color = Color(0xFF856404),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Featured of Day carousel (Home + no search only)
                    if (selectedCategory == "All" && searchQuery.isBlank() && filteredApps.size >= 2) {
                        item {
                            Column(modifier = Modifier.padding(bottom = 20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Featured of Day", color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredApps.take(6)) { app ->
                                        FeaturedAppCard(
                                            app = app,
                                            primaryColor = primaryColor,
                                            secondaryColor = secondaryColor,
                                            textColor = textColor,
                                            onClick = { onAppClick(app) },
                                            onInstallClick = { onDownloadClick(app) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // All Apps header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedCategory == "All") "Total Apps" else selectedCategory,
                                color = textColor,
                                fontSize = 17.sp
                            )

// Small pill showing how many apps are in the current view
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(textColor.copy(alpha = 0.06f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filteredApps.size}",
                                    color = textColor.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(surfaceColor)
                                    .clickable { showSortSheet = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Sort, contentDescription = "Sort", tint = primaryColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(sortBy, color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // App list
                    if (loading && apps.isEmpty()) {
                        items(4) {
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)) {
                                ShimmerAppCard()
                            }
                        }
                    } else if (filteredApps.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.SearchOff, contentDescription = null, tint = primaryColor.copy(alpha = 0.6f), modifier = Modifier.size(32.dp))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No apps found",
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Try a different search or category",
                                    color = textColor.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        items(filteredApps) { app ->
                            Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)) {
                                AppCard(
                                    app = app,
                                    isFavorite = favorites.contains(app.id),
                                    onFavoriteToggle = {
                                        FavoritesManager.toggleFavorite(context, app.id)
                                        favorites = FavoritesManager.getFavorites(context)
                                    },
                                    onInstallClick = { onDownloadClick(app) },
                                    onClick = { onAppClick(app) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sort bottom sheet
        if (showSortSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSortSheet = false },
                containerColor = surfaceColor,
                contentColor = textColor
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Sort Apps By", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                    listOf("Newest", "A-Z", "Z-A").forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { sortBy = option; showSortSheet = false }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = sortBy == option,
                                onClick = { sortBy = option; showSortSheet = false },
                                colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(option, fontSize = 16.sp, fontWeight = if (sortBy == option) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

// ---------- FEATURED APP CARD (smaller) ----------
@Composable
fun FeaturedAppCard(
    app: AppInfo,
    primaryColor: Color,
    secondaryColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    onInstallClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.10f),
                        secondaryColor.copy(alpha = 0.22f)
                    )
                )
            )
            .border(1.dp, primaryColor.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = app.icon,
                contentDescription = app.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                app.name,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                app.category,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 10.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            if (app.version != "N/A" && app.version.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(primaryColor.copy(alpha = 0.10f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        app.version,
                        color = primaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(9.dp))
                    .background(Brush.horizontalGradient(listOf(primaryColor, secondaryColor)))
                    .clickable { onInstallClick() }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Install", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- MAP A CATEGORY NAME TO AN ICON ----------
private fun categoryIconFor(name: String): ImageVector = when (name.lowercase()) {
    "all" -> Icons.Default.Apps
    "installed" -> Icons.Default.Download
    "updates" -> Icons.Default.SystemUpdate
    "favorites" -> Icons.Default.Favorite
    "educational", "education", "learning" -> Icons.Default.School
    "games", "game" -> Icons.Default.SportsEsports
    "tools", "utility", "utilities" -> Icons.Default.Build
    "social" -> Icons.Default.People
    "media", "music", "video" -> Icons.Default.PlayArrow
    else -> Icons.Default.Category
}