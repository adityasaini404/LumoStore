package com.lumo.store.screens

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lumo.store.R
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SplashScreen(onTimeout: () -> Unit) {

    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val primary = colors.primary
    val secondary = colors.secondary
    val tertiary = colors.tertiary

    // ---------- Entry animation states ----------
    var logoVisible by remember { mutableStateOf(false) }
    var nameVisible by remember { mutableStateOf(false) }
    var taglineVisible by remember { mutableStateOf(false) }
    var progressVisible by remember { mutableStateOf(false) }
    var progressValue by remember { mutableFloatStateOf(0f) }

    val logoScale by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0.4f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(700),
        label = "logo_alpha"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (nameVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "name_alpha"
    )
    val nameOffset by animateFloatAsState(
        targetValue = if (nameVisible) 0f else 30f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "name_offset"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue = if (taglineVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "tagline_alpha"
    )
    val progressAlpha by animateFloatAsState(
        targetValue = if (progressVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "progress_alpha"
    )

    // ---------- Infinite animations ----------
    val infinite = rememberInfiniteTransition(label = "splash")

    // Pulse rings
    val ring1Scale by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_scale"
    )
    val ring1Alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1_alpha"
    )
    val ring2Scale by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing, delayMillis = 900),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_scale"
    )
    val ring2Alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing, delayMillis = 900),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2_alpha"
    )

    // Logo breathe
    val breathe by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Star ring rotation
    val starRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_rotation"
    )

    // Star twinkle
    val starTwinkle by infinite.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    // Floating orbs
    val orbShift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_shift"
    )

    // Shimmer
    val shimmerX by infinite.animateFloat(
        initialValue = -400f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // ---------- Typewriter effect for tagline ----------
    val fullTagline = "Apps, illuminated."
    var typedTagline by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }

    LaunchedEffect(taglineVisible) {
        if (taglineVisible) {
            typedTagline = ""
            for (i in fullTagline.indices) {
                typedTagline = fullTagline.substring(0, i + 1)
                delay(70)
            }
            // Blink cursor for 1 second after typing
            delay(800)
            showCursor = false
        }
    }

    // Cursor blink
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            if (showCursor) showCursor = false else showCursor = true
        }
    }

    // ---------- Progress bar animation ----------
    LaunchedEffect(progressVisible) {
        if (progressVisible) {
            val steps = 40
            for (i in 0..steps) {
                progressValue = i / steps.toFloat()
                delay(25)
            }
        }
    }

    // ---------- Star positions (fixed random) ----------
    val starSeeds = remember {
        List(8) {
            StarSeed(
                angleOffset = Random.nextFloat() * 360f,
                radius = 90f + Random.nextFloat() * 35f,
                size = 2.5f + Random.nextFloat() * 2.5f,
                twinkleOffset = Random.nextFloat() * 1000f
            )
        }
    }

    // ---------- Choreograph intro + Haptic feedback ----------
    LaunchedEffect(Unit) {
        logoVisible = true
        // Haptic feedback when logo appears
        vibrate(context, 30)
        delay(400)

        nameVisible = true
        delay(250)

        taglineVisible = true
        // Slightly stronger haptic when tagline starts
        vibrate(context, 20)
        delay(1500)

        progressVisible = true
        delay(1200)

        // Final haptic before transition
        vibrate(context, 40)
        delay(200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        // ---------- Floating background orbs ----------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val offset1 = Offset(
                x = size.width * (0.15f + orbShift * 0.05f),
                y = size.height * (0.25f - orbShift * 0.03f)
            )
            val offset2 = Offset(
                x = size.width * (0.85f - orbShift * 0.05f),
                y = size.height * (0.75f + orbShift * 0.03f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primary.copy(alpha = 0.18f), Color.Transparent),
                    center = offset1,
                    radius = size.minDimension * 0.5f
                ),
                radius = size.minDimension * 0.5f,
                center = offset1
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondary.copy(alpha = 0.18f), Color.Transparent),
                    center = offset2,
                    radius = size.minDimension * 0.5f
                ),
                radius = size.minDimension * 0.5f,
                center = offset2
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // ---------- Logo with stars and rings ----------
            Box(
                modifier = Modifier.size(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Rotating stars layer (behind everything)
                if (logoVisible) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)

                        starSeeds.forEachIndexed { index, star ->
                            val angleRad = Math.toRadians(
                                (starRotation + star.angleOffset).toDouble()
                            )
                            val x = center.x + cos(angleRad).toFloat() * star.radius
                            val y = center.y + sin(angleRad).toFloat() * star.radius

                            // Twinkle per-star with phase offset
                            val twinkle = starTwinkle * (0.6f + 0.4f * sin(
                                (starTwinkle * 6.28f) + star.twinkleOffset
                            ).toFloat()).coerceIn(0f, 1f)

                            val starColor = when (index % 3) {
                                0 -> primary
                                1 -> secondary
                                else -> tertiary
                            }

                            // Glow
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        starColor.copy(alpha = twinkle * 0.6f),
                                        starColor.copy(alpha = 0f)
                                    ),
                                    center = Offset(x, y),
                                    radius = star.size * 6f
                                ),
                                radius = star.size * 6f,
                                center = Offset(x, y)
                            )
                            // Core
                            drawCircle(
                                color = starColor.copy(alpha = twinkle),
                                radius = star.size,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Outer pulsing rings
                if (logoVisible) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(ring1Scale)
                            .graphicsLayer { alpha = ring1Alpha }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        primary.copy(alpha = 0.4f),
                                        primary.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                }
                if (logoVisible) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(ring2Scale)
                            .graphicsLayer { alpha = ring2Alpha }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        secondary.copy(alpha = 0.4f),
                                        secondary.copy(alpha = 0f)
                                    )
                                )
                            )
                    )
                }

                // Glow behind logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // App logo
                Image(
                    painter = painterResource(id = R.drawable.ic_lumo_logo),
                    contentDescription = "Lumo Store",
                    modifier = Modifier
                        .size(110.dp)
                        .scale(logoScale * breathe)
                        .alpha(logoAlpha)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ---------- App name with shimmer ----------
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = nameAlpha
                    translationY = nameOffset
                }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            translationX = shimmerX
                            alpha = nameAlpha * 0.7f
                        }
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    primary.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "Lumo Store",
                    color = colors.onBackground,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ---------- Typewriter tagline with blinking cursor ----------
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.graphicsLayer { alpha = taglineAlpha }
            ) {
                Text(
                    text = typedTagline,
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp,
                    letterSpacing = 0.3.sp
                )
                if (showCursor && typedTagline.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 1.5.dp, height = 16.dp)
                            .background(primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // ---------- Thin animated progress bar ----------
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.surfaceVariant)
                    .graphicsLayer { alpha = progressAlpha }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressValue)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(primary, secondary)
                            )
                        )
                )
            }
        }

        // ---------- Bottom hint ----------
        Text(
            text = "Open source · No trackers",
            color = colors.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .graphicsLayer { alpha = progressAlpha }
        )
    }
}

// ---------- Star data holder ----------
private data class StarSeed(
    val angleOffset: Float,
    val radius: Float,
    val size: Float,
    val twinkleOffset: Float
)

// ---------- Haptic feedback helper ----------
@RequiresPermission(Manifest.permission.VIBRATE)
private fun vibrate(context: Context, durationMs: Long) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        durationMs,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    } catch (e: Exception) {
        // Silently ignore if vibration is unavailable
    }
}