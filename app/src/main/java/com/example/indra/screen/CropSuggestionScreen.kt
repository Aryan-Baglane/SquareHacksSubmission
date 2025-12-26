package com.example.indra.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.indra.cropsuggestion.CropRecommendation
import com.example.indra.cropsuggestion.CropUiState
import com.example.indra.cropsuggestion.CropViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CropSuggestionScreen(
    viewModel: CropViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Form Inputs
    var location by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("Alluvial") }
    var season by remember { mutableStateOf("Kharif") }
    var waterAvail by remember { mutableStateOf("Medium") }
    var farmSize by remember { mutableStateOf("") }

    val primaryGradient = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), Color.Transparent)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Indra AI Advisor", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(primaryGradient)) {

            // ANIMATED TRANSITION BETWEEN FORM AND RESULTS
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) + slideInVertically { it / 2 } with
                            fadeOut(animationSpec = tween(300))
                },
                label = "ScreenTransition"
            ) { state ->
                when (state) {
                    is CropUiState.Success -> {
                        ResultView(state.data.recommendations, state.data.generalAdvice) {
                            viewModel.resetState()
                        }
                    }
                    else -> {
                        FormView(
                            location = location,
                            onLocationChange = { location = it },
                            soilType = soilType,
                            onSoilChange = { soilType = it },
                            season = season,
                            onSeasonChange = { season = it },
                            waterAvail = waterAvail,
                            onWaterChange = { waterAvail = it },
                            farmSize = farmSize,
                            onFarmSizeChange = { farmSize = it },
                            isLoading = state is CropUiState.Loading,
                            errorMessage = (state as? CropUiState.Error)?.message,
                            onSubmit = {
                                viewModel.fetchSuggestions(location, soilType, season, waterAvail, farmSize.toFloatOrNull() ?: 1f, "110001")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormView(
    location: String, onLocationChange: (String) -> Unit,
    soilType: String, onSoilChange: (String) -> Unit,
    season: String, onSeasonChange: (String) -> Unit,
    waterAvail: String, onWaterChange: (String) -> Unit,
    farmSize: String, onFarmSizeChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))

        Text("Optimize Your Yield", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Fill in your farm profile for AI analysis", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("District/Location") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.LocationOn, null) }
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Simplified logic: Using TextFields here for brevity, but Dropdowns are better in production
            OutlinedTextField(value = soilType, onValueChange = onSoilChange, label = { Text("Soil") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = season, onValueChange = onSeasonChange, label = { Text("Season") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = waterAvail, onValueChange = onWaterChange, label = { Text("Water") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = farmSize, onValueChange = onFarmSizeChange, label = { Text("Acres") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !isLoading && location.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Generate AI Strategy", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ResultView(recommendations: List<CropRecommendation>, advice: String, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("AI Strategy", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(advice, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }

        itemsIndexed(recommendations) { index, crop ->
            AnimatedCropCard(crop, index)
        }

        item {
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("← Re-calculate for different parameters")
            }
        }
    }
}

@Composable
fun AnimatedCropCard(crop: CropRecommendation, index: Int) {
    // Entrance Animation
    val visible = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible.value = true }

    AnimatedVisibility(
        visible = visible.value,
        enter = slideInHorizontally(animationSpec = tween(durationMillis = 400, delayMillis = index * 100)) + fadeIn()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${crop.rank}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(crop.cropName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("₹${crop.profitEstimate.toInt()}", color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }

                Text(crop.justification, modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray, style = MaterialTheme.typography.bodySmall)

                HorizontalDivider(Modifier.height(1.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ModernMetric(Icons.Default.WaterDrop, "${(crop.waterRequirementLiters/1000).toInt()}kL")
                    ModernMetric(Icons.Default.Payments, "₹${crop.estimatedPrice}/kg")
                    ModernMetric(Icons.Default.Star, "${crop.easeScore}/10")
                }
            }
        }
    }
}

@Composable
fun ModernMetric(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}