package com.example.indra.screen.IndraGraminScreen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indra.auth.AuthApi
import com.example.indra.location.LocationViewModel
import com.example.indra.marketModel.MarketRecord
import com.example.indra.marketModel.MarketViewModel
import com.example.indra.screen.IndraGraminScreen.viewmodel.DashboardState
import com.example.indra.screen.IndraGraminScreen.viewmodel.GraminDashboardViewModel
import com.example.indra.weather.WeatherStats

/* ---------- MODERN COLOR PALETTE ---------- */
private val GreenDark = Color(0xFF1B5E20)
private val GreenMedium = Color(0xFF4CAF50)
private val GreenLight = Color(0xFFE8F5E9)
private val ScreenBg = Color(0xFFFBFDFA)
private val SoftText = Color(0xFF5C635E)
private val AccentGold = Color(0xFFFFB300)
private val DeveloperIndicator = Color(0xFF9C27B0) // Purple dot
private val SkeletonColor = Color.LightGray.copy(alpha = 0.3f)

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndraGraminHomeScreen(
    uid: String,
    onSettingsClick: () -> Unit,
    onMarketClick: () -> Unit,
    onCropSuggestionClick: () -> Unit,
    onWaterManagementClick: () -> Unit,
    onCommunityClick: () -> Unit,
    onVendorsClick: () -> Unit,
    viewModel: GraminDashboardViewModel = viewModel(),
    marketViewModel: MarketViewModel = viewModel()
) {
    val state by viewModel.dashboardState.observeAsState(DashboardState.Loading)
    val marketRecords by marketViewModel.records.collectAsState()
    val isMarketLoading by marketViewModel.isLoading.collectAsState()

    val locationVM = viewModel<LocationViewModel>()
    val locationData by locationVM.locationData.observeAsState()
    val locationAddress = locationData?.address ?: "Locating..."

    var fullName by remember { mutableStateOf("Farmer") }
    var showAiInsights by remember { mutableStateOf(false) }

    // User Selection State (Can be fetched from Profile)
    var selectedState by remember { mutableStateOf("Maharashtra") }
    var selectedDistrict by remember { mutableStateOf("Nagpur") }
    var selectedCrop by remember { mutableStateOf("Cotton") }

    LaunchedEffect(uid) {
        val user = AuthApi.currentUser()
        fullName = user?.displayName ?: "Farmer"
        viewModel.loadDashboardData(uid, locationData?.latitude, locationData?.longitude, "Hindi")
        // Initial Market Data Fetch
        marketViewModel.searchMarketData(selectedState, selectedDistrict, selectedCrop, null, null)
    }

    Scaffold(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
            HeaderBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item { HeaderBranding(fullName, locationAddress, onSettingsClick) }

                item {
                    WeatherFeasibilityCard(
                        weatherStats = (state as? DashboardState.Success)?.weatherStats,
                        isLoading = state is DashboardState.Loading,
                        onAiClick = { showAiInsights = !showAiInsights },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // --- MARKET PRICE SECTION ---
                item {
                    val actualRecord = marketRecords.firstOrNull()
                    val isDummy = actualRecord == null && isMarketLoading

                    PinnedMarketCard(
                        record = actualRecord ?: getDummyMarketRecord(selectedCrop, selectedDistrict),
                        isLoading = isMarketLoading,
                        isDummyData = isDummy,
                        onEditClick = onMarketClick, // Navigate to full market screen to change details
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                item {
                    AnimatedVisibility(visible = showAiInsights) {
                        AiInsightCard(
                            summary = (state as? DashboardState.Success)?.aiWeatherSummary,
                            isLoading = state is DashboardState.Loading
                        )
                    }
                }

                item {
                    QuickActionSection(onMarketClick, onCropSuggestionClick, onWaterManagementClick, onVendorsClick)
                }
            }
        }
    }
}

@Composable
fun PinnedMarketCard(
    record: MarketRecord,
    isLoading: Boolean,
    isDummyData: Boolean,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Market Price", style = MaterialTheme.typography.labelLarge, color = SoftText)
                    if (isDummyData) {
                        Text("Offline Mode", color = DeveloperIndicator, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = GreenMedium)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(GreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TrendingUp, null, tint = GreenDark)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(record.commodity, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("${record.market}, ${record.district}", fontSize = 12.sp, color = SoftText)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${record.modalPrice}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = GreenDark)
                            Text("per Quintal", fontSize = 10.sp, color = SoftText)
                        }
                    }
                }

                Divider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.4f))

                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Change Crop Details", fontSize = 12.sp, color = GreenMedium)
                    Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = GreenMedium)
                }
            }
        }

        // Developer Purple Dot Indicator
        if (isDummyData) {
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .size(8.dp)
                    .background(DeveloperIndicator, CircleShape)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

// --- DUMMY DATA HELPER ---
fun getDummyMarketRecord(crop: String, district: String) = MarketRecord(
    state = "N/A",
    district = district,
    market = "Local Mandi",
    commodity = crop,
    variety = "Standard",
    minPrice = "0",
    maxPrice = "0",
    modalPrice = "4500", // Default dummy price
    date = "2024-01-01"
)

@Composable
fun HeaderBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(
                Brush.verticalGradient(listOf(GreenDark, GreenMedium))
            )
    )
}

@Composable
fun HeaderBranding(name: String, location: String, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Welcome back,", color = Color.White.copy(0.7f), fontSize = 14.sp)
            Text(name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(14.dp))
                Text(location, color = Color.White.copy(0.7f), fontSize = 12.sp)
            }
        }
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.background(Color.White.copy(0.15f), CircleShape)
        ) {
            Icon(Icons.Default.Settings, null, tint = Color.White)
        }
    }
}

@Composable
fun WeatherFeasibilityCard(
    weatherStats: WeatherStats?,
    isLoading: Boolean,
    onAiClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Today's Weather", color = SoftText, fontSize = 13.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            if (isLoading) "--" else "${weatherStats?.temp?.toInt() ?: 0}°",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.Black
                        )
                        Text("C", fontSize = 18.sp, color = SoftText, modifier = Modifier.padding(bottom = 8.dp))
                    }
                }

                Surface(
                    onClick = onAiClick,
                    shape = RoundedCornerShape(12.dp),
                    color = GreenLight
                ) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = GreenDark, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("AI Insights", color = GreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Divider(Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(0.5f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeatherMiniStat(Icons.Default.WaterDrop, "Rain", "${weatherStats?.rain ?: 0}mm", isLoading)
                WeatherMiniStat(Icons.Default.Opacity, "Humidity", "25%", isLoading)
                WeatherMiniStat(Icons.Default.Air, "Wind", "${weatherStats?.windSpeed ?: 0}km/h", isLoading)
            }
        }
    }
}

@Composable
fun WeatherMiniStat(icon: ImageVector, label: String, value: String, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = GreenMedium, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        if (isLoading) SkeletonBox(30.dp, 12.dp)
        else Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, fontSize = 11.sp, color = SoftText)
    }
}

@Composable
fun AiInsightCard(summary: String?, isLoading: Boolean) {
    Card(
        modifier = Modifier.padding(20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, GreenMedium.copy(0.2f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = GreenDark)
                Spacer(Modifier.width(8.dp))
                Text("Smart Advisory", fontWeight = FontWeight.Bold, color = GreenDark)
            }
            Spacer(Modifier.height(8.dp))
            if (isLoading) SkeletonBox(200.dp, 40.dp)
            else Text(summary ?: "Analyzing conditions for your crops...", fontSize = 14.sp, color = Color.DarkGray, lineHeight = 20.sp)
        }
    }
}

@Composable
fun QuickActionSection(
    onMarket: () -> Unit,
    onCrop: () -> Unit,
    onWater: () -> Unit,
    onVendors: () -> Unit
) {
    Column(Modifier.padding(20.dp)) {
        Text("Farm Services", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceCard("Market", Icons.Default.Storefront, GreenDark, Modifier.weight(1f), onMarket)
            ServiceCard("Crops", Icons.Default.Agriculture, AccentGold, Modifier.weight(1f), onCrop)
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ServiceCard("Water", Icons.Default.Waves, Color(0xFF0288D1), Modifier.weight(1f), onWater)
            ServiceCard("Vendors", Icons.Default.Engineering, Color(0xFF7B1FA2), Modifier.weight(1f), onVendors)
        }
    }
}

@Composable
fun ServiceCard(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SkeletonBox(width: Dp, height: Dp) {
    Box(modifier = Modifier.size(width, height).clip(RoundedCornerShape(4.dp)).background(SkeletonColor))
}