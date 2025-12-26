package com.example.indra.screen.IndraGraminScreen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.auth.AuthApi
import com.example.indra.data.GraminProfile
import com.example.indra.db.DatabaseProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraminOnboardingScreen(onCompleted: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Colors
    val primaryGreen = Color(0xFF2E7D32)
    val darkGreen = Color(0xFF1B331D)
    val surfaceWhite = Color(0xFFF8FBF8)

    // State
    var fullName by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var farmArea by remember { mutableStateOf("") }
    var soilType by remember { mutableStateOf("Black") }
    var irrigation by remember { mutableStateOf("Rainfed") }
    var primaryCrop by remember { mutableStateOf("") }
    var experienceYears by remember { mutableStateOf("") }

    var isLoadingAuth by remember { mutableStateOf(true) }
    var isLoadingLocation by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.values.any { it }) {
            isLoadingLocation = true
            fetchLocationName(context, fusedClient) { village = it ?: ""; isLoadingLocation = false }
        }
    }

    LaunchedEffect(Unit) {
        val user = AuthApi.currentUser()
        fullName = user?.displayName ?: ""
        isLoadingAuth = false
    }

    Scaffold(containerColor = surfaceWhite) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Header Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        brush = Brush.verticalGradient(listOf(primaryGreen, darkGreen)),
                        shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                    )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                item {
                    Column {
                        Text("Profile Setup", color = Color.White.copy(0.7f), fontSize = 14.sp)
                        Text("Personalize Indra", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Header Card (Glassmorphism Effect)
                item {
                    OnboardingInfoCard(
                        title = "Location Detected",
                        value = if (village.isEmpty()) "Tap to locate" else village,
                        icon = Icons.Default.LocationOn,
                        isLoading = isLoadingLocation,
                        onClick = {
                            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        }
                    )
                }

                // Input Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OnboardingTextField(fullName, { fullName = it }, "Full Name", Icons.Outlined.Person)
                            OnboardingTextField(primaryCrop, { primaryCrop = it }, "Primary Crop", Icons.Outlined.Eco)

                            Row(Modifier.fillMaxWidth()) {
                                OnboardingTextField(farmArea, { farmArea = it.filter { c -> c.isDigit() || c == '.' } }, "Acres", Icons.Outlined.SquareFoot, Modifier.weight(1f))
                                Spacer(Modifier.width(12.dp))
                                OnboardingTextField(experienceYears, { experienceYears = it.filter { c -> c.isDigit() } }, "Experience", Icons.Outlined.Timeline, Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Soil & Irrigation Selection
                item {
                    OnboardingSelectionSection("Soil Type", listOf("Black", "Red", "Alluvial", "Clay"), soilType) { soilType = it }
                }

                item {
                    OnboardingSelectionSection("Irrigation Source", listOf("Rainfed", "Borewell", "Canal", "Drip"), irrigation) { irrigation = it }
                }
            }

            // Bottom Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(surfaceWhite.copy(0.9f))
                    .padding(20.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    enabled = !isSaving && village.isNotEmpty() && fullName.isNotEmpty(),
                    onClick = {
                        scope.launch {
                            isSaving = true
                            val currentUser = AuthApi.currentUser() ?: return@launch
                            val profile = GraminProfile(
                                uid = currentUser.uid,
                                village = village,
                                farmAreaAcres = farmArea.toDoubleOrNull() ?: 0.0,
                                soilType = soilType,
                                irrigationSource = irrigation,
                                primaryCrop = primaryCrop,
                                onboardingCompleted = true
                            )
                            DatabaseProvider.database().setGraminProfile(profile)
                            isSaving = false
                            onCompleted()
                        }
                    }
                ) {
                    if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Save and Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OnboardingInfoCard(title: String, value: String, icon: ImageVector, isLoading: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.2f))
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Icon(icon, null, tint = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = Color.White.copy(0.7f), fontSize = 12.sp)
                Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OnboardingTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF2E7D32)) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF1F8E9).copy(0.5f),
            focusedContainerColor = Color.White,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color(0xFF2E7D32)
        )
    )
}

@Composable
fun OnboardingSelectionSection(title: String, options: List<String>, selected: String, onSelected: (String) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, color = Color.Gray, modifier = Modifier.padding(start = 4.dp))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    label = { Text(option) },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchLocationName(context: Context, fusedClient: FusedLocationProviderClient, onResult: (String?) -> Unit) {
    fusedClient.lastLocation.addOnSuccessListener { loc ->
        if (loc != null) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val address = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)?.firstOrNull()
                onResult(address?.locality ?: address?.subAdminArea ?: address?.adminArea)
            } catch (e: Exception) { onResult(null) }
        } else onResult(null)
    }.addOnFailureListener { onResult(null) }
}