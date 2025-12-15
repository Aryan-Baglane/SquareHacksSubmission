package com.example.indra.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Property
import com.example.indra.ui.theme.MapView
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPropertiesScreen(
    mapboxAccessToken: String = "pk.eyJ1IjoiYXJ5YW5iYWdsYW5lIiwiYSI6ImNtaDRpZWoxaTB4MjcyanI1c3BoZDVyY3AifQ.SWnkEA01n_kcFTSfUaW2uA"
) {
    var selectedProperty by remember { mutableStateOf<Property?>(null) }
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isMapExpanded by remember { mutableStateOf(false) }

    // Animation state
    var startAnimation by remember { mutableStateOf(false) }

    // Fetch properties
    LaunchedEffect(Unit) {
        try {
            // Simulate slight delay for animation smoothness if local
            val fetchedProps = com.example.indra.data.PropertyRepositoryProvider.repository().getUserProperties()
            properties = fetchedProps
            isLoading = false
            startAnimation = true
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    val mapHeight by animateDpAsState(
        targetValue = if (isMapExpanded) 500.dp else 240.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "mapHeight"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 1. Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary, Color(0xFF4FC3F7))
                    ),
                    RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        )

        // 2. Content
        Column(modifier = Modifier.fillMaxSize()) {

            // Header Text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 24.dp, end = 24.dp, bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My Properties",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Stats Chip
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "${properties.size} Locations",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (error != null) {
                ErrorView(error = error!!) {
                    isLoading = true; error = null
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // MAP CARD (First Item)
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(mapHeight)
                                .shadow(12.dp, RoundedCornerShape(32.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha=0.3f)),
                            shape = RoundedCornerShape(32.dp),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                MapView(
                                    properties = properties,
                                    onPropertyClick = { selectedProperty = it },
                                    modifier = Modifier.fillMaxSize(),
                                    defaultZoom = if (isMapExpanded) 14.0 else 12.0,
                                    accessToken = mapboxAccessToken
                                )

                                // Expand Button
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp)
                                ) {
                                    FloatingActionButton(
                                        onClick = { isMapExpanded = !isMapExpanded },
                                        containerColor = Color.White,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isMapExpanded) Icons.Default.CloseFullscreen else Icons.Default.OpenInFull,
                                            contentDescription = "Toggle Map",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PROPERTY LIST
                    itemsIndexed(properties) { index, property ->
                        // Staggered Entry Animation
                        var isVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 100L)
                            isVisible = true
                        }

                        AnimatedVisibility(
                            visible = isVisible,
                            enter = slideInVertically { 50 } + fadeIn()
                        ) {
                            ModernPropertyCard(
                                property = property,
                                onClick = { selectedProperty = property }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }

        // 3. Property Details Bottom Sheet (Integrated Here)
        selectedProperty?.let { property ->
            PropertyDetailsBottomSheet(
                property = property,
                onDismiss = { selectedProperty = null }
            )
        }
    }
}

// ================= MODERN COMPONENTS =================

@Composable
fun ModernPropertyCard(property: Property, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha=0.05f))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    // Icon Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.HomeWork,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = property.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Score Badge
                val scoreColor = when {
                    property.feasibilityScore >= 70 -> Color(0xFF4CAF50)
                    property.feasibilityScore >= 40 -> Color(0xFFFF9800)
                    else -> Color(0xFFE53935)
                }

                Surface(
                    color = scoreColor.copy(alpha=0.1f),
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, scoreColor.copy(alpha=0.2f))
                ) {
                    Text(
                        text = "${property.feasibilityScore.toInt()}% Score",
                        color = scoreColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PropertyMetric(
                    label = "Potential",
                    value = "${property.annualHarvestingPotentialLiters / 1000} kL",
                    icon = Icons.Outlined.WaterDrop,
                    color = MaterialTheme.colorScheme.primary
                )

                PropertyMetric(
                    label = "Est. Cost",
                    value = "₹${property.estimatedCostInr/1000}k",
                    icon = Icons.Outlined.CurrencyRupee,
                    color = Color(0xFF795548)
                )

                PropertyMetric(
                    label = "Type",
                    value = property.propertyType,
                    icon = Icons.Outlined.Apartment,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PropertyMetric(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha=0.8f))
        }
    }
}

@Composable
fun ErrorView(error: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Oops!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(error, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("Try Again") }
        }
    }
}