// In your existing file: 'AssessmentView.kt'
package com.example.indra.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.auth.AuthApi
import com.example.indra.data.*
import com.example.indra.db.DatabaseApiProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentView(
    onBackClick: () -> Unit,
    onAssessmentComplete: (Report) -> Unit
) {
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
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val roofTypes = listOf("concrete", "tile", "metal", "asbestos", "thatch")

    // Location permission launcher
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

    // Request location permission on first load
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "New Assessment",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Location Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isLocationLoading)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = locationAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                if (!isLocationLoading && locationAddress != "Unable to detect location" && locationAddress != "Location permission denied") {
                    IconButton(
                        onClick = {
                            isLocationLoading = true
                            fetchLocation(context, fusedClient) { location ->
                                if (location != null) {
                                    latitude = location.latitude
                                    longitude = location.longitude
                                    locationAddress = location.toAddress(context) ?: "Location detected"
                                } else {
                                    locationAddress = "Unable to detect location"
                                }
                                isLocationLoading = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.MyLocation,
                            contentDescription = "Refresh Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (locationAddress == "Unable to detect location" || locationAddress == "Location permission denied") {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Please enable location permission to continue.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = numDwellers,
            onValueChange = { numDwellers = it },
            label = { Text("Number of Dwellers") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = roofArea,
            onValueChange = { roofArea = it },
            label = { Text("Roof Area (sqm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = openSpace,
            onValueChange = { openSpace = it },
            label = { Text("Open Space (sqm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Roof Type Selection
        Text(
            text = "Roof Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.selectableGroup()
        ) {
            roofTypes.forEach { roofType ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (selectedRoofType == roofType),
                            onClick = { selectedRoofType = roofType },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedRoofType == roofType),
                        onClick = null
                    )
                    Text(
                        text = roofType.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Submit Button
        Button(
            onClick = {
                if (validateInputs(name, latitude, longitude, numDwellers, roofArea, openSpace, locationAddress)) {
                    scope.launch {
                        isLoading = true
                        errorMessage = ""

                        try {
                            val request = AssessmentRequest(
                                name = name,
                                latitude = latitude,
                                longitude = longitude,
                                numDwellers = numDwellers.toInt(),
                                roofAreaSqm = roofArea.toDouble(),
                                openSpaceSqm = openSpace.toDouble(),
                                roofType = selectedRoofType
                            )

                            // This is the key change. We now use the real repository.
                            val result = AssessmentRepositoryProvider.repository().performAssessment(request)

                            result.fold(
                                onSuccess = { response ->
                                    assessmentResponse = response
                                    showResults = true
                                },
                                onFailure = { error ->
                                    errorMessage = "Assessment failed: ${error.message ?: ""}"
                                }
                            )
                        } catch (e: Exception) {
                            errorMessage = "An unexpected error occurred: ${e.message ?: ""}"
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    errorMessage = "Please fill in all fields correctly."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isLoading) "Assessing..." else "Start Assessment",
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun AssessmentResultsScreen(
    response: AssessmentResponse,
    onBackClick: () -> Unit,
    onSaveReport: (Report) -> Unit,
    // Source inputs to persist into history/properties
    inputName: String,
    inputAddress: String,
    inputLatitude: Double,
    inputLongitude: Double,
    inputDwellers: Int,
    inputRoofArea: Double,
    inputOpenSpace: Double,
    inputRoofType: String
) {
    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showSaveToPropertyDialog by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Assessment Results",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Feasibility Score Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    response.feasibilityScore >= 70 -> Color(0xFF4CAF50)
                    response.feasibilityScore >= 50 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Feasibility Score",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = "${String.format("%.1f", response.feasibilityScore)}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = response.feasibilityInsights,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Location Info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Location Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Average Annual Rainfall", "${response.locationInfo.avgAnnualRainfallMm} mm")
                InfoRow("Soil Type", response.locationInfo.soilType)
                InfoRow("Soil Permeability", response.locationInfo.soilPermeability)
                InfoRow("Principal Aquifer", response.locationInfo.principalAquifer)
                InfoRow("Groundwater Depth", "${response.locationInfo.predictedGroundwaterDepthMbgl} m")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // RWH Analysis
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rainwater Harvesting",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Annual Runoff Potential", "${String.format("%.0f", response.rwhAnalysis.potentialAnnualRunoffLiters)} liters")
                InfoRow("Recommended Tank Size", "${response.rwhAnalysis.recommendedTankSizeLiters} liters")
                Text(
                    text = response.rwhAnalysis.notes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AR Analysis
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Artificial Recharge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Feasible", if (response.arAnalysis.isFeasible) "Yes" else "No")
                InfoRow("Recommended Structure", response.arAnalysis.recommendedStructureType)
                response.arAnalysis.structureDimensions.forEach { (key, value) ->
                    InfoRow(key, value)
                }
                Text(
                    text = response.arAnalysis.notes,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cost Benefit Analysis
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Cost-Benefit Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Estimated Initial Investment", "₹${String.format("%.0f", response.costBenefitAnalysis.estimatedInitialInvestment)}")
                InfoRow("Annual Maintenance", "₹${String.format("%.0f", response.costBenefitAnalysis.annualOperatingMaintenanceCost)}")
                InfoRow("Annual Water Savings", "${String.format("%.0f", response.costBenefitAnalysis.annualWaterSavingsLiters)} liters")
                InfoRow("Annual Monetary Savings", "₹${String.format("%.0f", response.costBenefitAnalysis.annualMonetarySavings)}")
                InfoRow("Payback Period", "${String.format("%.1f", response.costBenefitAnalysis.paybackPeriodYears)} years")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Report Button
        Button(
            onClick = {
                scope.launch {
                    isSaving = true
                    try {
                        val currentUser = AuthApi.currentUser()
                        if (currentUser != null) {
                            val report = Report(
                                name = inputName,
                                timestamp = System.currentTimeMillis(),
                                feasibilityScore = response.feasibilityScore.toFloat(),
                                annualHarvestingPotentialLiters = response.rwhAnalysis.potentialAnnualRunoffLiters.toLong(),
                                recommendedSolution = response.arAnalysis.recommendedStructureType,
                                estimatedCostInr = response.costBenefitAnalysis.estimatedInitialInvestment.toInt(),
                                location = inputAddress,
                                dwellers = inputDwellers,
                                roofArea = inputRoofArea,
                                openSpace = inputOpenSpace,
                                latitude = inputLatitude,
                                longitude = inputLongitude,
                                roofType = inputRoofType,
                                assessmentResponse = response
                            )

                            DatabaseApiProvider.databaseApi().addReport(currentUser.uid, report)
                            onSaveReport(report)
                        }
                    } catch (e: Exception) {
                        // Handle error
                    } finally {
                        isSaving = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (isSaving) "Saving..." else "Save Report",
                fontSize = 16.sp
            )
        }
    }

    // Prompt to save as Property
    if (showSaveToPropertyDialog) {
        AlertDialog(
            onDismissRequest = { showSaveToPropertyDialog = false },
            title = { Text("Save as Property?") },
            text = { Text("Do you want to save this assessment as a property to track in the future?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveToPropertyDialog = false
                    // Save property using repository
                    scope.launch {
                        try {
                            val property = com.example.indra.data.Property(
                                id = java.util.UUID.randomUUID().toString(),
                                name = inputName,
                                address = inputAddress,
                                latitude = inputLatitude,
                                longitude = inputLongitude,
                                feasibilityScore = response.feasibilityScore.toFloat(),
                                annualHarvestingPotentialLiters = response.rwhAnalysis.potentialAnnualRunoffLiters.toLong(),
                                recommendedSolution = response.arAnalysis.recommendedStructureType,
                                estimatedCostInr = response.costBenefitAnalysis.estimatedInitialInvestment.toInt(),
                                lastAssessmentDate = System.currentTimeMillis(),
                                propertyType = "Residential",
                                roofArea = inputRoofArea,
                                openSpace = inputOpenSpace,
                                dwellers = inputDwellers
                            )
                            com.example.indra.data.PropertyRepositoryProvider.repository().addProperty(property)
                        } catch (_: Exception) {}
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveToPropertyDialog = false }) { Text("Not Now") }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun validateInputs(
    name: String,
    latitude: Double,
    longitude: Double,
    numDwellers: String,
    roofArea: String,
    openSpace: String,
    locationAddress: String
): Boolean {
    return name.isNotBlank() &&
            latitude != 0.0 && longitude != 0.0 &&
            locationAddress != "Unable to detect location" &&
            locationAddress != "Location permission denied" &&
            locationAddress != "Detecting location..." &&
            numDwellers.isNotBlank() && numDwellers.toIntOrNull() != null &&
            roofArea.isNotBlank() && roofArea.toDoubleOrNull() != null &&
            openSpace.isNotBlank() && openSpace.toDoubleOrNull() != null
}

@SuppressLint("MissingPermission")
private fun fetchLocation(
    context: Context,
    fusedClient: FusedLocationProviderClient,
    callback: (Location?) -> Unit
) {
    fusedClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location)
            } else {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).setMaxUpdates(1).build()

                fusedClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val loc = result.lastLocation
                            callback(loc)
                            fusedClient.removeLocationUpdates(this)
                        }
                    },
                    null
                )
            }
        }
        .addOnFailureListener {
            callback(null)
        }
}

private fun Location.toAddress(context: Context): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            val addressParts = mutableListOf<String>()

            // Build a readable address
            addr.featureName?.let { if (it != addr.locality) addressParts.add(it) }
            addr.subLocality?.let { addressParts.add(it) }
            addr.locality?.let { addressParts.add(it) }
            addr.adminArea?.let { addressParts.add(it) }
            addr.countryName?.let { addressParts.add(it) }

            addressParts.joinToString(", ")
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}