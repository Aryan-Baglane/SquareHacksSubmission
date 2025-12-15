package com.example.indra.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.auth.AuthApi
import com.example.indra.data.*
import com.example.indra.db.DatabaseApiProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentView(
    onBackClick: () -> Unit,
    onAssessmentComplete: (Report) -> Unit
) {
    // --- State (Unchanged) ---
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }
    var locationAddress by remember { mutableStateOf("Detecting location...") }
    var numDwellers by remember { mutableStateOf("") }
    var roofArea by remember { mutableStateOf("") }
    var openSpace by remember { mutableStateOf("") }
    var selectedRoofType by remember { mutableStateOf("concrete") }

    var isLoading by remember { mutableStateOf(false) }
    var isLocationLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }
    var assessmentResponse by remember { mutableStateOf<AssessmentResponse?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val roofTypes = listOf("concrete", "tile", "metal", "asbestos", "thatch")

    // --- Permission & Location Logic ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                fetchLocation(context, fusedClient) { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        locationAddress = location.toAddress(context) ?: "Location detected"
                        isLocationLoading = false
                    } else {
                        locationAddress = "Unable to detect location"
                        isLocationLoading = false
                    }
                }
            } else {
                locationAddress = "Location permission denied"
                isLocationLoading = false
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // --- Navigation to Results ---
    if (showResults && assessmentResponse != null) {
        AssessmentResultsScreen(
            response = assessmentResponse!!,
            onBackClick = { showResults = false },
            onSaveReport = { report ->
                onAssessmentComplete(report)
                onBackClick()
            },
            inputName = name,
            inputAddress = locationAddress,
            inputLatitude = latitude,
            inputLongitude = longitude,
            inputDwellers = numDwellers.toIntOrNull() ?: 0,
            inputRoofArea = roofArea.toDoubleOrNull() ?: 0.0,
            inputOpenSpace = openSpace.toDoubleOrNull() ?: 0.0,
            inputRoofType = selectedRoofType
        )
        return
    }

    // --- Input Form UI ---
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF007EC1))
                    ),
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            // Navbar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape).size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("New Assessment", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Container
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {

                // Location Card
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Color(0xFFE3F2FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLocationLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Location", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                            Text(text = locationAddress, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2)
                        }
                        IconButton(onClick = {
                            isLocationLoading = true
                            fetchLocation(context, fusedClient) { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    locationAddress = location.toAddress(context) ?: "Detected"
                                } else { locationAddress = "Unable to detect" }
                                isLocationLoading = false
                            }
                        }) { Icon(Icons.Default.MyLocation, "Refresh", tint = MaterialTheme.colorScheme.primary) }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ModernTextField(name, { name = it }, "Property Name", Icons.Outlined.Home)
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) { ModernTextField(numDwellers, { numDwellers = it }, "Dwellers", Icons.Outlined.People, KeyboardType.Number) }
                    Box(modifier = Modifier.weight(1f)) { ModernTextField(roofArea, { roofArea = it }, "Roof (sqm)", Icons.Outlined.SquareFoot, KeyboardType.Decimal) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ModernTextField(openSpace, { openSpace = it }, "Open Space (sqm)", Icons.Outlined.Landscape, KeyboardType.Decimal)
                Spacer(modifier = Modifier.height(24.dp))

                Text("Roof Material", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(roofTypes) { type ->
                        RoofTypeCard(type, selectedRoofType == type) { selectedRoofType = type }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (errorMessage.isNotEmpty()) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                }

                Button(
                    onClick = {
                        if (validateInputs(name, latitude, longitude, numDwellers, roofArea, openSpace, locationAddress)) {
                            scope.launch {
                                isLoading = true
                                errorMessage = ""
                                try {
                                    val request = AssessmentRequest(
                                        name = name, latitude = latitude, longitude = longitude,
                                        numDwellers = numDwellers.toInt(), roofAreaSqm = roofArea.toDouble(),
                                        openSpaceSqm = openSpace.toDouble(), roofType = selectedRoofType
                                    )
                                    AssessmentRepositoryProvider.repository().performAssessment(request).fold(
                                        onSuccess = { assessmentResponse = it; showResults = true },
                                        onFailure = { errorMessage = "Failed: ${it.message}" }
                                    )
                                } catch (e: Exception) { errorMessage = "Error: ${e.message}" } finally { isLoading = false }
                            }
                        } else { errorMessage = "Please check all fields." }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Start Analysis", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentResultsScreen(
    response: AssessmentResponse,
    onBackClick: () -> Unit,
    onSaveReport: (Report) -> Unit,
    inputName: String, inputAddress: String, inputLatitude: Double, inputLongitude: Double,
    inputDwellers: Int, inputRoofArea: Double, inputOpenSpace: Double, inputRoofType: String
) {
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showSaveToPropertyDialog by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { isVisible = true }

    // Animation for the score number and wave
    val animatedScore by animateFloatAsState(
        targetValue = if (isVisible) response.feasibilityScore.toFloat() else 0f,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "score"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            if (response.feasibilityScore > 70) Color(0xFF43A047) else if (response.feasibilityScore > 40) Color(0xFFFF9800) else Color(0xFFD32F2F),
                            Color(0xFFF8F9FA) // Fade into background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navbar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.3f), CircleShape)
                ) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Analysis Results", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- WAVE ANIMATION HERO ---
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .shadow(16.dp, CircleShape) // Standard shadow without blurRadius
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // The Water Wave
                WaveLoadingIndicator2(
                    progress = animatedScore / 100f,
                    modifier = Modifier.size(180.dp),
                    color = if (response.feasibilityScore > 70) Color(0xFF43A047) else if (response.feasibilityScore > 40) Color(0xFFFF9800) else Color(0xFFD32F2F)
                )

                // Text Overlay
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        // FIXED: Use style for text shadow instead of modifier
                        style = MaterialTheme.typography.displayMedium.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = "Feasibility",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                blurRadius = 2f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Insights Text
            Text(
                text = response.feasibilityInsights,
                modifier = Modifier.padding(horizontal = 24.dp),
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- DETAILED CARDS ---
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // 1. Rainwater Analysis
                ResultSectionCard("Harvesting Potential", Icons.Outlined.WaterDrop, Color(0xFF2196F3)) {
                    InfoRow("Annual Runoff", "${String.format("%.0f", response.rwhAnalysis.potentialAnnualRunoffLiters)} Liters", true)
                    InfoRow("Recommended Tank", "${response.rwhAnalysis.recommendedTankSizeLiters} Liters")
                    if (response.rwhAnalysis.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Note: ${response.rwhAnalysis.notes}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Artificial Recharge & Structure
                ResultSectionCard("Technical Solution", Icons.Outlined.Construction, Color(0xFF9C27B0)) {
                    InfoRow("Feasible for AR", if(response.arAnalysis.isFeasible) "Yes" else "No")
                    InfoRow("Structure Type", response.arAnalysis.recommendedStructureType, true)

                    // Show dimensions if available
                    if (response.arAnalysis.structureDimensions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Dimensions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        response.arAnalysis.structureDimensions.forEach { (k, v) ->
                            InfoRow("• $k", v)
                        }
                    }
                    if (response.arAnalysis.notes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(response.arAnalysis.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Geology & Location
                ResultSectionCard("Location & Geology", Icons.Outlined.Landscape, Color(0xFF795548)) {
                    InfoRow("Avg Rainfall", "${response.locationInfo.avgAnnualRainfallMm} mm")
                    InfoRow("Soil Type", response.locationInfo.soilType)
                    InfoRow("Permeability", response.locationInfo.soilPermeability)
                    InfoRow("Aquifer", response.locationInfo.principalAquifer)
                    InfoRow("GW Depth", "${response.locationInfo.predictedGroundwaterDepthMbgl} m")
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 4. Financial Analysis
                ResultSectionCard("Cost-Benefit Analysis", Icons.Outlined.MonetizationOn, Color(0xFF4CAF50)) {
                    InfoRow("Est. Investment", "₹${String.format("%.0f", response.costBenefitAnalysis.estimatedInitialInvestment)}", true)
                    InfoRow("Annual Maintenance", "₹${String.format("%.0f", response.costBenefitAnalysis.annualOperatingMaintenanceCost)}")
                    InfoRow("Annual Savings", "₹${String.format("%.0f", response.costBenefitAnalysis.annualMonetarySavings)}")
                    InfoRow("Water Savings", "${String.format("%.0f", response.costBenefitAnalysis.annualWaterSavingsLiters)} L")
                    InfoRow("Payback Period", "${String.format("%.1f", response.costBenefitAnalysis.paybackPeriodYears)} Years")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Save Button
                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                val currentUser = AuthApi.currentUser()
                                if (currentUser != null) {
                                    val report = Report(
                                        name = inputName, timestamp = System.currentTimeMillis(),
                                        feasibilityScore = response.feasibilityScore.toFloat(),
                                        annualHarvestingPotentialLiters = response.rwhAnalysis.potentialAnnualRunoffLiters.toLong(),
                                        recommendedSolution = response.arAnalysis.recommendedStructureType,
                                        estimatedCostInr = response.costBenefitAnalysis.estimatedInitialInvestment.toInt(),
                                        location = inputAddress, dwellers = inputDwellers,
                                        roofArea = inputRoofArea, openSpace = inputOpenSpace,
                                        latitude = inputLatitude, longitude = inputLongitude,
                                        roofType = inputRoofType, assessmentResponse = response
                                    )
                                    DatabaseApiProvider.databaseApi().addReport(currentUser.uid, report)
                                    onSaveReport(report)
                                }
                            } catch (_: Exception) {} finally { isSaving = false }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Save Report", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Save as Property Dialog
    if (showSaveToPropertyDialog) {
        AlertDialog(
            onDismissRequest = { showSaveToPropertyDialog = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Save to Properties?") },
            text = { Text("Track this assessment as a permanent property to monitor future harvesting data.") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveToPropertyDialog = false
                    scope.launch {
                        try {
                            val property = Property(
                                id = UUID.randomUUID().toString(), name = inputName, address = inputAddress,
                                latitude = inputLatitude, longitude = inputLongitude,
                                feasibilityScore = response.feasibilityScore.toFloat(),
                                annualHarvestingPotentialLiters = response.rwhAnalysis.potentialAnnualRunoffLiters.toLong(),
                                recommendedSolution = response.arAnalysis.recommendedStructureType,
                                estimatedCostInr = response.costBenefitAnalysis.estimatedInitialInvestment.toInt(),
                                lastAssessmentDate = System.currentTimeMillis(), propertyType = "Residential",
                                roofArea = inputRoofArea, openSpace = inputOpenSpace, dwellers = inputDwellers
                            )
                            PropertyRepositoryProvider.repository().addProperty(property)
                        } catch (_: Exception) {}
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveToPropertyDialog = false }) { Text("No thanks") }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ================== WAVE ANIMATION LOGIC ==================

@Composable
fun WaveLoadingIndicator2(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveShift"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        // Invert progress so 0 is bottom, 1 is top
        val waterLevelY = height * (1 - progress)

        val path = Path().apply {
            moveTo(0f, waterLevelY)
            // Draw sine wave
            for (x in 0..width.toInt() step 5) {
                // Amplitude 10f, Frequency 0.03f
                val y = waterLevelY + 10 * sin((x * 0.03f) + waveShift)
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
                colors = listOf(color.copy(alpha = 0.8f), color)
            )
        )
    }
}

// ================== HELPER COMPONENTS ==================

@Composable
fun ResultSectionCard(title: String, icon: ImageVector, accentColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray.copy(alpha = 0.1f))
            content()
        }
    }
}



@Composable
fun ModernTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
            focusedContainerColor = Color.White, unfocusedContainerColor = Color.White
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Composable
fun RoofTypeCard(type: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
            .border(1.dp, if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(type.replaceFirstChar { it.uppercase() }, color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.6f))
    }
}

// ================== LOGIC HELPERS ==================

private fun validateInputs(name: String, latitude: Double, longitude: Double, numDwellers: String, roofArea: String, openSpace: String, locationAddress: String): Boolean {
    return name.isNotBlank() && latitude != 0.0 && longitude != 0.0 &&
            locationAddress != "Unable to detect location" && locationAddress != "Location permission denied" &&
            numDwellers.toIntOrNull() != null && roofArea.toDoubleOrNull() != null && openSpace.toDoubleOrNull() != null
}

@SuppressLint("MissingPermission")
private fun fetchLocation(context: Context, fusedClient: FusedLocationProviderClient, callback: (Location?) -> Unit) {
    fusedClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) callback(location) else {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setMaxUpdates(1).build()
            fusedClient.requestLocationUpdates(request, object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) { callback(result.lastLocation); fusedClient.removeLocationUpdates(this) }
            }, null)
        }
    }.addOnFailureListener { callback(null) }
}

private fun Location.toAddress(context: Context): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val parts = mutableListOf<String>()
            addr.locality?.let { parts.add(it) }
            addr.adminArea?.let { parts.add(it) }
            parts.joinToString(", ")
        } else null
    } catch (e: Exception) { null }
}