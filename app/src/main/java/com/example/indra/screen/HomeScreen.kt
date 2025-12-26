package com.example.indra.screen

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.indra.auth.AuthApi
import com.example.indra.data.AuthUser
import com.example.indra.db.DatabaseProvider
import com.example.indra.location.LocationViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.max

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    onStartAssessment: () -> Unit,
    onChatClick: () -> Unit,
    onReportClick: () -> Unit,
    onTipClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onHelpClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMyPropertiesClick: () -> Unit,
    onMyHouseClick: () -> Unit,
    onVendorClick: () -> Unit,
    locationViewModel: LocationViewModel = viewModel()
) {
    // ---------- STATE ----------
    var user by remember { mutableStateOf<AuthUser?>(null) }
    var name by remember { mutableStateOf("") }
    val locationData by locationViewModel.locationData.observeAsState()
    var feasibilityScore by remember { mutableStateOf<Double?>(null) }
    var potentialRunoff by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Animation States
    var isVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    // ---------- EFFECTS ----------
    LaunchedEffect(Unit) {
        isVisible = true // Trigger entrance animation
        user = AuthApi.currentUser()
        name = user?.displayName.orEmpty()
    }

    // Location Permission
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            locationViewModel.fetchLocation(context, fusedClient)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Run Logic
    LaunchedEffect(locationData) {
        val lat = locationData?.latitude
        val lon = locationData?.longitude
        if (lat != null && lon != null) {
            isLoading = true
            scope.launch {
                val profile = DatabaseProvider.database().getUserProfile(user?.uid ?: return@launch)
                if (profile?.onboardingCompleted == true) {
                    val repo = com.example.indra.data.AssessmentRepositoryProvider.repository()
                    repo.performAssessment(
                        com.example.indra.data.AssessmentRequest(
                            name = profile.displayName ?: "Home",
                            latitude = lat,
                            longitude = lon,
                            numDwellers = profile.numDwellers,
                            roofAreaSqm = profile.roofAreaSqm,
                            openSpaceSqm = profile.openSpaceSqm,
                            roofType = profile.roofType
                        )
                    ).onSuccess {
                        feasibilityScore = it.feasibilityScore
                        potentialRunoff = it.rwhAnalysis.potentialAnnualRunoffLiters
                    }
                }
                isLoading = false
            }
        }
    }

    // Dynamic Header Color based on Feasibility (Defaults to Primary if null/loading)
    val scoreColor = feasibilityScore?.let { getScoreColor(it) } ?: MaterialTheme.colorScheme.primary

    // ---------- UI ----------
    val headerHeight = 240.dp
    val overlapHeight = 90.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Very subtle grey for contrast
    ) {
        // 1. ANIMATED HEADER BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            // Use the score-derived color for a unified look
                            scoreColor,
                            scoreColor.darken(0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {
            // Header Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 24.dp, end = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Hello, $name",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    AnimatedVisibility(visible = true, enter = fadeIn() + expandHorizontally()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationData?.address ?: "Locating...",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Pulsing Profile Image
                val infiniteTransition = rememberInfiniteTransition(label = "profile")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                    label = "scale"
                )

                Image(
                    painter = rememberAsyncImagePainter(user?.photoUrl),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .scale(scale)
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .clickable { onProfileClick() },
                    contentScale = ContentScale.Crop
                )
            }
        }

        // 2. SCROLLABLE CONTENT
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(headerHeight - overlapHeight))

            // -- Feasibility Card with Glass Pouring Animation --
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn()
            ) {
                WaterWaveCard(
                    score = feasibilityScore,
                    loading = isLoading,
                    potentialRunoff = potentialRunoff
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // -- Search Bar --
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 100 }, animationSpec = tween(300)) + fadeIn()
            ) {
                SleekSearchBar(navController)
            }

            // -- Services Grid --
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Grid Layout
            AnimatedGridMenu(
                onStartAssessment, onChatClick, onReportClick, onTipClick,
                onCommunityClick, onSettingsClick, onHistoryClick, onHelpClick,
                onProfileClick, onMyPropertiesClick, onMyHouseClick, onVendorClick
            )
        }
    }
}

/* ================= UTILITY FUNCTIONS ================= */

/**
 * Maps a score (0.0 to 100.0) to a color gradient: Red -> Yellow -> Green.
 * @param score The feasibility score (0.0 to 100.0).
 * @return The corresponding [Color].
 */
@Composable
fun getScoreColor(score: Double): Color {
    // Clamp score between 0 and 100
    val clampedScore = score.toFloat().coerceIn(0f, 100f)

    // Normalize the score to a 0.0 to 1.0 range
    val normalized = clampedScore / 100f

    // Define color stops (example: Red at 0, Yellow at 50, Green at 100)
    val red = Color(0xFFF44336) // Low Score
    val yellow = Color(0xFFFFC107) // Moderate Score
    val green = Color(0xFF4CAF50) // High Score

    return when {
        normalized <= 0.5f -> lerp(red, yellow, normalized * 2f)
        else -> lerp(yellow, green, (normalized - 0.5f) * 2f)
    }
}

/**
 * Linear interpolation between two colors.
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return androidx.compose.ui.graphics.lerp(start, stop, fraction)
}


/**
 * Darkens a color by a given factor (0.0 to 1.0).
 */
private fun Color.darken(factor: Float): Color {
    return Color(
        red = max(0f, red - factor).coerceIn(0f, 1f),
        green = max(0f, green - factor).coerceIn(0f, 1f),
        blue = max(0f, blue - factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/* ================= COMPOSABLES ================= */

@Composable
fun WaterWaveCard(
    score: Double?,
    loading: Boolean,
    potentialRunoff: Double?
) {
    // Dynamic Color based on Score
    val dynamicColor = score?.let { getScoreColor(it) } ?: MaterialTheme.colorScheme.primary
    val targetProgress = (score?.toFloat() ?: 0f) / 100f

    // The actual progress shown in the wave
    var currentProgress by remember { mutableStateOf(0f) }

    // Trigger animation
    var startPourAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(loading, score) {
        if (!loading && score != null) {
            currentProgress = 0f
            // Gives the app a moment to "breathe" before starting the show
            delay(800)
            startPourAnimation = true
        } else {
            startPourAnimation = false
        }
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .height(240.dp)
            // Use the dynamic color for the shadow spot
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = dynamicColor.copy(alpha=0.3f)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // CONTENT LAYER
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT SIDE: The Wave Gauge
                Box(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F4F8))
                    )

                    WaveLoadingIndicator(
                        progress = if (loading) 0.1f else currentProgress,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape),
                        // Use the dynamic color for the wave
                        color = dynamicColor.copy(alpha = 0.8f),
                        isMoving = true
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (loading) "--" else "${(currentProgress * 100).toInt()}%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            // Use the dynamic color for the text if the wave hasn't covered it much
                            color = if (currentProgress > 0.3f) Color.Black else dynamicColor,
                            modifier = Modifier.shadow(0.dp)
                        )
                    }
                }

                // RIGHT SIDE: Details
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .padding(end = 24.dp, top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Feasibility",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = when {
                            loading -> "Calculating..."
                            (score ?: 0.0) > 75 -> "Excellent"
                            (score ?: 0.0) > 40 -> "Moderate"
                            else -> "Low Potential"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        // Use the dynamic color for the headline text
                        color = dynamicColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (potentialRunoff != null && potentialRunoff > 0) {
                        // Use dynamic color for the badge accent
                        Surface(
                            color = dynamicColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.WaterDrop,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = dynamicColor // Dynamic icon color
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${potentialRunoff.toInt()}L / yr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicColor.darken(0.1f) // Darker dynamic color for contrast
                                )
                            }
                        }
                    }
                }
            }

            // ANIMATION LAYER (Drawn on top)
            if (startPourAnimation) {
                PouringGlassAnimation(
                    targetFill = targetProgress,
                    onFillUpdate = { progress -> currentProgress = progress },
                    onFinished = { startPourAnimation = false },
                    // Pass the dynamic color to the pouring animation
                    waterColor = dynamicColor
                )
            }
        }
    }
}

@Composable
fun PouringGlassAnimation(
    targetFill: Float,
    onFillUpdate: (Float) -> Unit,
    onFinished: () -> Unit,
    waterColor: Color // New parameter
) {
    // 0f = Offscreen Right, 1f = Pouring Position
    val positionProgress = remember { Animatable(0f) }

    val glassTilt = remember { Animatable(0f) }
    val streamHeight = remember { Animatable(0f) }
    val fillProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Enter: Slide in smoothly with a slight ease-out
        positionProgress.animateTo(
            targetValue = 0.9f,
            animationSpec = tween(1000, easing = EaseOutCubic)
        )

        // Short pause before action
        delay(100)

        // 2. Tilt Glass
        glassTilt.animateTo(
            targetValue = -45f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        )

        // 3. Pour Stream (Gravity effect: Fast down)
        streamHeight.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = EaseInExpo)
        )

        // 4. Fill the Circle & Drain the Glass
        // Slower duration for realism (2.5 seconds to fill)
        fillProgress.animateTo(
            targetValue = targetFill,
            animationSpec = tween(2500, easing = LinearEasing),
            block = { onFillUpdate(this.value) }
        )

        // 5. Retract Stream (Snap up)
        streamHeight.animateTo(
            targetValue = 0f,
            animationSpec = tween(250, easing = EaseOutExpo)
        )

        // 6. Untilt
        glassTilt.animateTo(
            targetValue = 0f,
            animationSpec = tween(600, easing = EaseOutBack)
        )

        // 7. Exit: Slide out smoothly
        positionProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(800, easing = EaseInCubic)
        )

        onFinished()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val glassWidth = 60.dp.toPx()
        val glassHeight = 80.dp.toPx()

        // --- Calculate Dynamic Position ---
        // Target: Roughly centered above the circle (approx 22.5% of width)
        val targetX = size.width * 0.225f
        // Start: Offscreen to the right (Width + buffer)
        val startX = size.width + glassWidth + 20f

        // Interpolate current X based on animation progress
        val currentX = startX + (targetX - startX) * positionProgress.value
        val currentY = 30.dp.toPx()

        // 1. Draw Stream (Behind glass effectively)
        if (streamHeight.value > 0f) {
            val streamStart = Offset(currentX - glassWidth / 2, currentY + 10f)
            val streamEndY = size.height * 0.7f

            // Make the stream slightly thinner than the opening for realism
            val streamWidth = 6.dp.toPx() * (0.8f + 0.2f * streamHeight.value)

            drawLine(
                color = waterColor, // Use dynamic color
                start = streamStart,
                end = Offset(
                    streamStart.x,
                    streamStart.y + (streamEndY - streamStart.y) * streamHeight.value
                ),
                strokeWidth = streamWidth,
                cap = StrokeCap.Round
            )
        }

        // 2. Draw Glass
        rotate(degrees = glassTilt.value, pivot = Offset(currentX - glassWidth / 2, currentY)) {
            val path = Path().apply {
                moveTo(currentX - glassWidth / 2, currentY) // Top Left
                lineTo(currentX + glassWidth / 2, currentY) // Top Right
                lineTo(currentX + glassWidth / 2 - 12f, currentY + glassHeight) // Bottom Right (more taper)
                lineTo(currentX - glassWidth / 2 + 12f, currentY + glassHeight) // Bottom Left (more taper)
                close()
            }

            // Water inside Glass
            clipPath(path) {
                // Water level calculation:
                // Starts at 0.8 (full-ish). As fillProgress goes 0->target, this goes down.
                // We exaggerate the draining so it looks like it's emptying.
                val drainFactor = (fillProgress.value / targetFill).coerceIn(0f, 1f)
                val waterLevelY = currentY + glassHeight * (0.2f + (0.8f * drainFactor))

                drawRect(
                    color = waterColor.copy(alpha = 0.6f), // Use dynamic color
                    topLeft = Offset(currentX - glassWidth, waterLevelY),
                    size = Size(glassWidth * 2, glassHeight)
                )
            }

            // Glass Outline (Thicker for cartoon realism)
            drawPath(
                path = path,
                color = Color.Gray.copy(alpha=0.6f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )

            // Add a small "glint" or highlight on the glass
            val highlightPath = Path().apply {
                moveTo(currentX - glassWidth / 2 + 8f, currentY + 10f)
                lineTo(currentX - glassWidth / 2 + 12f, currentY + glassHeight - 10f)
            }
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha=0.4f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * A beautiful animated sine-wave filling effect.
 */
@Composable
fun WaveLoadingIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
    isMoving: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing), // Slightly slower wave for calm look
            repeatMode = RepeatMode.Restart
        ),
        label = "waveShift"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val waterLevelY = height * (1 - progress)

        val path = Path().apply {
            moveTo(0f, waterLevelY)
            // Draw sine wave
            for (x in 0..width.toInt() step 5) {
                val amp = if (isMoving) 12f else 0f // Slightly smaller amplitude
                val y = waterLevelY + amp * sin((x * 0.02f) + waveShift) // 0.02 frequency for smoother wave
                lineTo(x.toFloat(), y)
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        // Draw Water
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.7f), color)
            )
        )
    }
}

// ... (Rest of the composables and helper functions remain the same)

@Composable
fun AnimatedGridMenu(
    onStartAssessment: () -> Unit,
    onChatClick: () -> Unit,
    onReportClick: () -> Unit,
    onTipClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onHelpClick: () -> Unit,
    onProfileClick: () -> Unit,
    onMyPropertiesClick: () -> Unit,
    onMyHouseClick: () -> Unit,
    onVendorClick: () -> Unit
) {
    val menuItems = listOf(
        MenuItemData("New Scan", Icons.Outlined.AddCircleOutline, Color(0xFF2196F3), onStartAssessment),
        MenuItemData("My House", Icons.Outlined.Home, Color(0xFF4CAF50), onMyHouseClick),
        MenuItemData("Chat", Icons.Outlined.ChatBubbleOutline, Color(0xFFE91E63), onChatClick),
        MenuItemData("Report", Icons.Outlined.Analytics, Color(0xFF9C27B0), onReportClick),
        MenuItemData("Properties", Icons.Outlined.HomeWork, Color(0xFF673AB7), onMyPropertiesClick),
        MenuItemData("Tips", Icons.Outlined.Lightbulb, Color(0xFFFFC107), onTipClick),
        MenuItemData("History", Icons.Outlined.History, Color(0xFF795548), onHistoryClick),
        MenuItemData("Community", Icons.Filled.MoreHoriz, Color(0xFF009688), onCommunityClick),
        MenuItemData("Profile", Icons.Outlined.Person, Color(0xFF3F51B5), onProfileClick),
        MenuItemData("Settings", Icons.Outlined.Settings, Color(0xFF607D8B), onSettingsClick),
        MenuItemData("Help", Icons.Outlined.HelpOutline, Color(0xFFF44336), onHelpClick),
        MenuItemData("Vendors", Icons.Outlined.Business, Color(0xFF03A9F4), onVendorClick)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(400.dp)
    ) {
        itemsIndexed(menuItems) { index, item ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(index * 50L)
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(animationSpec = spring(dampingRatio = 0.6f)) + fadeIn()
            ) {
                SleekGridItem(item)
            }
        }
    }
}

@Composable
fun SleekGridItem(item: MenuItemData) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.bounceClick { item.onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = item.color.copy(alpha = 0.3f))
                .background(Color.White, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                item.color.copy(alpha = 0.1f),
                                item.color.copy(alpha = 0.05f)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
            )
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = item.color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SleekSearchBar(navController: NavController) {
    var text by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha=0.1f)),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if(text.isEmpty()) {
                    Text("Try 'Rainwater Calculation'...", color = Color.Gray.copy(alpha=0.5f))
                }
                // Using OutlinedTextField for a consistent look without the default underline
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

// ================= FIXED BOUNCE CLICK =================

fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

data class MenuItemData(val title: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)
