package com.example.indra.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indra.watermanagnement.WaterManagementResponse
import com.example.indra.watermanagnement.WaterUiState
import com.example.indra.watermanagnement.WaterViewModel

/* ---------- PREMIUM COLOR PALETTE ---------- */
private val DeepNavy = Color(0xFF011627)
private val WaterBlue = Color(0xFF2196F3)
private val CyberBlue = Color(0xFF00E5FF)
private val SoftWhite = Color(0xFFF8F9FA)
private val WarningOrange = Color(0xFFFF9100)
private val SuccessEmerald = Color(0xFF00C853)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterManagementScreen(viewModel: WaterViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    var location by remember { mutableStateOf("Pune") }
    var season by remember { mutableStateOf("summer") }
    var cattle by remember { mutableStateOf("5") }
    var members by remember { mutableStateOf("4") }
    var acres by remember { mutableStateOf("2.5") }

    val infiniteTransition = rememberInfiniteTransition(label = "HeaderAnim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart), label = "Wave"
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {

        /* ---------- ANIMATED GRAPHIC HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .drawBehind {
                    val brush = Brush.verticalGradient(listOf(DeepNavy, WaterBlue.copy(alpha = 0.3f)))
                    drawRect(brush)
                    drawCircle(CyberBlue.copy(0.1f), radius = 100f, center = Offset(waveOffset % size.width, 200f))
                    drawCircle(WaterBlue.copy(0.15f), radius = 150f, center = Offset((waveOffset * 0.5f) % size.width, 400f))
                }
        ) {
            Column(Modifier.padding(top = 64.dp, start = 24.dp, end = 24.dp)) {
                Text("HYDRO AI", color = CyberBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("Resource Optimization", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
            }
        }

        /* ---------- GLASS CONTENT SHEET ---------- */
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)
                },
            color = SoftWhite
        ) {
            Crossfade(targetState = uiState, animationSpec = tween(600), label = "ScreenSwitch") { state ->
                when (state) {
                    is WaterUiState.Success -> WaterResultsView(state.data) { viewModel.reset() }
                    is WaterUiState.Loading -> LoadingState()
                    else -> InputForm(
                        location, { location = it },
                        season, { season = it },
                        cattle, { cattle = it },
                        members, { members = it },
                        acres, { acres = it }
                    ) { viewModel.getWaterPlan(location, season, cattle, members, acres) }
                }
            }
        }
    }
}

@Composable
fun WaterResultsView(data: WaterManagementResponse, onReset: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Status Banner
        item { StatusBanner(data.waterStatus, data.gisSummary) }

        // 2. Data Dashboard Card
        item {
            Column {
                Text("Resource Distribution", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DeepNavy)
                Spacer(Modifier.height(12.dp))
                AnalyticsDashboard(data)
            }
        }

        // 3. AI Insights Card
        item {
            Column {
                Text("Intelligence Strategy", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DeepNavy)
                Spacer(Modifier.height(12.dp))
                PremiumInsightCard(data.aiInsights)
            }
        }

        // 4. Recommendation List Header
        item {
            Text("Action Plan", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DeepNavy)
        }

        // 5. Staggered List Items
        itemsIndexed(data.recommendations) { index, rec ->
            StaggeredEntrance(index = index) {
                RecommendationTile(rec)
            }
        }

        // 6. Primary Action Button
        item {
            Button(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(12.dp))
                Text("REFRESH ANALYTICS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun AnalyticsDashboard(data: WaterManagementResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Usage Matrix", style = MaterialTheme.typography.labelLarge, color = Color.Gray, letterSpacing = 1.sp)
            Spacer(Modifier.height(20.dp))

            DonutChart(
                irrigation = data.distribution.irrigationPct.toFloat(),
                cattle = data.distribution.cattlePct.toFloat(),
                drinking = data.distribution.drinkingPct.toFloat()
            )



            Spacer(Modifier.height(32.dp))

            // Enhanced Grid Layout for Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatColumn("Irrigation", data.distribution.irrigationBuckets, WaterBlue, Icons.Default.Agriculture)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray.copy(0.5f)))
                StatColumn("Cattle", data.distribution.cattleBuckets, WarningOrange, Icons.Default.Pets)
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.LightGray.copy(0.5f)))
                StatColumn("Domestic", data.distribution.drinkingBuckets, SuccessEmerald, Icons.Default.Home)
            }
        }
    }
}

@Composable
fun StatColumn(label: String, value: Int, color: Color, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
        Text(label.uppercase(), fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RecommendationTile(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = WaterBlue.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.Verified, null, tint = WaterBlue, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(text, fontSize = 14.sp, color = Color(0xFF454B50), lineHeight = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/* ---------- RETAINED COMPONENTS FROM PREVIOUS VERSION ---------- */

@Composable
fun StatusBanner(status: String, summary: String) {
    val isCritical = status.contains("Critical")
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "Scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if(isCritical) WarningOrange.copy(0.1f) else SuccessEmerald.copy(0.1f)),
        border = BorderStroke(1.dp, if(isCritical) WarningOrange.copy(0.3f) else SuccessEmerald.copy(0.3f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).background(if(isCritical) WarningOrange else SuccessEmerald, CircleShape), contentAlignment = Alignment.Center) {
                Icon(if(isCritical) Icons.Default.PriorityHigh else Icons.Default.DoneAll, null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(status.uppercase(), fontWeight = FontWeight.Black, color = if(isCritical) WarningOrange else SuccessEmerald, fontSize = 20.sp)
                Text(summary, color = Color.DarkGray, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun DonutChart(irrigation: Float, cattle: Float, drinking: Float) {
    val animationProgress = animateFloatAsState(targetValue = 1f, animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "Chart").value
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val stroke = 40f
            drawArc(WaterBlue, -90f, (irrigation / 100 * 360) * animationProgress, false, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(WarningOrange, (irrigation / 100 * 360) - 90f, (cattle / 100 * 360) * animationProgress, false, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(SuccessEmerald, ((irrigation + cattle) / 100 * 360) - 90f, (drinking / 100 * 360) * animationProgress, false, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("TOTAL", fontSize = 10.sp, color = Color.Gray, letterSpacing = 2.sp)
            Text("${(irrigation + cattle + drinking).toInt()}%", fontSize = 24.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StaggeredEntrance(index: Int, content: @Composable () -> Unit) {
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 100L)
        visible.value = true
    }
    AnimatedVisibility(visible = visible.value, enter = slideInHorizontally(animationSpec = tween(500)) { -it } + fadeIn(), label = "Staggered") { content() }
}

@Composable
fun InputForm(l: String, onL: (String) -> Unit, s: String, onS: (String) -> Unit, c: String, onC: (String) -> Unit, m: String, onM: (String) -> Unit, a: String, onA: (String) -> Unit, onGo: () -> Unit) {
    LazyColumn(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Parameter Input", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DeepNavy) }
        item { ModernInput(l, onL, "District", Icons.Default.LocationSearching) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("summer", "monsoon", "winter").forEach { season ->
                    val isSelected = s == season
                    Surface(
                        onClick = { onS(season) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if(isSelected) WaterBlue else Color.White,
                        border = BorderStroke(1.dp, if(isSelected) WaterBlue else Color.LightGray)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(season.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(isSelected) Color.White else Color.Gray)
                        }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { ModernInput(c, onC, "Cattle", Icons.Default.Pets) }
                Box(Modifier.weight(1f)) { ModernInput(m, onM, "Family", Icons.Default.Group) }
            }
        }
        item { ModernInput(a, onA, "Farm Area", Icons.Default.Architecture) }
        item {
            Button(onClick = onGo, modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 8.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = WaterBlue)) {
                Text("LAUNCH AI ANALYSIS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun LoadingState() {
    val infinite = rememberInfiniteTransition(label = "loading")
    val rotation by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "rot")
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.CloudSync, null, Modifier.size(60.dp).graphicsLayer { rotationZ = rotation }, tint = WaterBlue)
        Text("Syncing GIS Satellites...", Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold, color = DeepNavy)
    }
}

@Composable
fun ModernInput(v: String, onV: (String) -> Unit, label: String, icon: ImageVector) {
    OutlinedTextField(
        value = v, onValueChange = onV,
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, null, tint = WaterBlue, modifier = Modifier.size(18.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = WaterBlue, unfocusedBorderColor = Color.LightGray)
    )
}

@Composable
fun PremiumInsightCard(text: String) {
    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Brush.horizontalGradient(listOf(DeepNavy, Color(0xFF0D47A1)))).padding(24.dp)) {
        Column {
            Icon(Icons.Default.Psychology, null, tint = CyberBlue)
            Spacer(Modifier.height(8.dp))
            Text(text, color = Color.White, fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
        }
    }
}