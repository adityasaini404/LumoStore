package com.lumo.store.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Lumo Dark (Default)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C5CFF),
    secondary = Color(0xFF4DD9FF),
    background = Color(0xFF0A0A0E),
    surface = Color(0xFF141419),
    onBackground = Color.White,
    onSurface = Color.White
)

// 2. Lumo Light
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7C5CFF),
    secondary = Color(0xFF4DD9FF),
    background = Color(0xFFF5F7FB),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F1115),
    onSurface = Color(0xFF1A1D24)
)

// 3. AMOLED Black
private val AmoledColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFF000000),
    surface = Color(0xFF121212),
    onBackground = Color.White,
    onSurface = Color.White
)

// 4. Ocean Blue
private val OceanColorScheme = darkColorScheme(
    primary = Color(0xFF00B4D8),
    secondary = Color(0xFF90E0EF),
    background = Color(0xFF03045E),
    surface = Color(0xFF023E8A),
    onBackground = Color.White,
    onSurface = Color.White
)

// 5. Cyberpunk
private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFF007F),
    secondary = Color(0xFF00F0FF),
    background = Color(0xFF0B0B0F),
    surface = Color(0xFF1A1A24),
    onBackground = Color.White,
    onSurface = Color.White
)

// 6. Forest
private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF81C784),
    background = Color(0xFF111A12),
    surface = Color(0xFF1B2A1E),
    onBackground = Color.White,
    onSurface = Color.White
)

// 7. Sunset
private val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFF5722),
    secondary = Color(0xFFE91E63),
    background = Color(0xFF1C1114),
    surface = Color(0xFF2B1B2E),
    onBackground = Color.White,
    onSurface = Color.White
)

// 8. Sakura
private val SakuraColorScheme = darkColorScheme(
    primary = Color(0xFFFF8DA1),
    secondary = Color(0xFFFFC2CD),
    background = Color(0xFF1F1315),
    surface = Color(0xFF2D1C1F),
    onBackground = Color(0xFFFFF0F2),
    onSurface = Color(0xFFFFF0F2)
)

// 9. Dracula
private val DraculaColorScheme = darkColorScheme(
    primary = Color(0xFFBD93F9),
    secondary = Color(0xFF50FA7B),
    background = Color(0xFF282A36),
    surface = Color(0xFF44475A),
    onBackground = Color(0xFFF8F8F2),
    onSurface = Color(0xFFF8F8F2)
)

// 10. Nord
private val NordColorScheme = darkColorScheme(
    primary = Color(0xFF88C0D0),
    secondary = Color(0xFF81A1C1),
    background = Color(0xFF2E3440),
    surface = Color(0xFF3B4252),
    onBackground = Color(0xFFECEFF4),
    onSurface = Color(0xFFECEFF4)
)

// 11. Coffee
private val CoffeeColorScheme = darkColorScheme(
    primary = Color(0xFFD4A373),
    secondary = Color(0xFFFAEDCD),
    background = Color(0xFF1E1612),
    surface = Color(0xFF2C211B),
    onBackground = Color(0xFFFEFAE0),
    onSurface = Color(0xFFFEFAE0)
)

// ⬇️ नई थीम 12: iPhone Light (Apple System Blue)
private val IphoneLightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF), // iOS System Blue
    secondary = Color(0xFF5AC8FA), // iOS Light Blue
    background = Color(0xFFF2F2F7), // iOS System Gray 6
    surface = Color(0xFFFFFFFF), // Pure White
    onBackground = Color(0xFF000000),
    onSurface = Color(0xFF000000)
)

// ⬇️ नई थीम 13: iPhone Dark (Apple System Blue Dark)
private val IphoneDarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF), // iOS Blue Dark
    secondary = Color(0xFF64D2FF), // iOS Light Blue Dark
    background = Color(0xFF000000), // True Black for iPhone Dark
    surface = Color(0xFF1C1C1E), // iOS System Gray 6 Dark
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF)
)

@Composable
fun LumoTheme(
    themeName: String = "Dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "Light" -> LightColorScheme
        "Amoled" -> AmoledColorScheme
        "Ocean" -> OceanColorScheme
        "Cyberpunk" -> CyberpunkColorScheme
        "Forest" -> ForestColorScheme
        "Sunset" -> SunsetColorScheme
        "Sakura" -> SakuraColorScheme
        "Dracula" -> DraculaColorScheme
        "Nord" -> NordColorScheme
        "Coffee" -> CoffeeColorScheme
        "iPhone Light" -> IphoneLightColorScheme
        "iPhone Dark" -> IphoneDarkColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LumoTypography,
        content = content
    )
}