package com.example.indra.screen.IndraGraminScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.indra.data.GraminProfile
import com.example.indra.screen.IndraGraminScreen.viewmodel.GraminSettingsViewModel

@Composable
fun GraminSettingsScreen(
    graminSettingsViewModel: GraminSettingsViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val combinedProfileState by graminSettingsViewModel.combinedProfile.observeAsState()
    val updateStatus by graminSettingsViewModel.updateStatus.observeAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on update
    LaunchedEffect(updateStatus) {
        updateStatus?.let { (success, message) ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // You can add a TopAppBar here if you wish
        }
    ) { paddingValues ->
        val combinedProfile = combinedProfileState
        if (combinedProfile == null || combinedProfile.userProfile == null || combinedProfile.graminProfile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Loading Profile...", modifier = Modifier.padding(top = 80.dp))
            }
        } else {
            SettingsContent(
                modifier = Modifier.padding(paddingValues),
                userProfile = combinedProfile.userProfile,
                graminProfile = combinedProfile.graminProfile,
                onSave = { village, farmArea, soilType, irrigation, language, primaryCrop ->
                    graminSettingsViewModel.updateGraminProfile(village, farmArea, soilType, irrigation, language, primaryCrop)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    modifier: Modifier = Modifier,
    userProfile: com.example.indra.data.UserProfile,
    graminProfile: GraminProfile,
    onSave: (String, Double, String, String, String, String) -> Unit
) {
    var village by remember(graminProfile.village) { mutableStateOf(graminProfile.village) }
    var farmArea by remember(graminProfile.farmAreaAcres) { mutableStateOf(graminProfile.farmAreaAcres.toString()) }
    var soilType by remember(graminProfile.soilType) { mutableStateOf(graminProfile.soilType) }
    var irrigation by remember(graminProfile.irrigationSource) { mutableStateOf(graminProfile.irrigationSource) }
    var language by remember(graminProfile.language) { mutableStateOf(graminProfile.language) }
    var primaryCrop by remember(graminProfile.primaryCrop) { mutableStateOf(graminProfile.primaryCrop) }
    var expandedLanguage by remember { mutableStateOf(false) }
    var expandedCrop by remember { mutableStateOf(false) }
    val languages = listOf("English", "Hindi", "Marathi", "Telugu", "Tamil")
    val crops = listOf("Rice", "Wheat", "Cotton", "Sugarcane", "Soybean")


    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userProfile.photoUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(text = userProfile.displayName ?: "N/A", style = MaterialTheme.typography.headlineSmall)
        Text(text = userProfile.onboardingCompleted.toString() ?: "N/A", style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.height(24.dp))
        Divider()
        Text(
            "Farm Details",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        OutlinedTextField(
            value = village,
            onValueChange = { village = it },
            label = { Text("Village Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = farmArea,
            onValueChange = { farmArea = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Farm Area (Acres)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text("Soil Type", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Black", "Red", "Sandy").forEach {
                FilterChip(selected = soilType == it, onClick = { soilType = it }, label = { Text(it) })
                Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Irrigation Source", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Rainfed", "Borewell", "Canal").forEach {
                FilterChip(selected = irrigation == it, onClick = { irrigation = it }, label = { Text(it) })
                Spacer(Modifier.width(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = expandedLanguage,
            onExpandedChange = { expandedLanguage = !expandedLanguage },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = language,
                onValueChange = { },
                label = { Text("Language") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLanguage) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedLanguage,
                onDismissRequest = { expandedLanguage = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                languages.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            language = selectionOption
                            expandedLanguage = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = expandedCrop,
            onExpandedChange = { expandedCrop = !expandedCrop },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = primaryCrop,
                onValueChange = { },
                label = { Text("Primary Crop") },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCrop) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedCrop,
                onDismissRequest = { expandedCrop = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                crops.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            primaryCrop = selectionOption
                            expandedCrop = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                onSave(
                    village,
                    farmArea.toDoubleOrNull() ?: 0.0,
                    soilType,
                    irrigation,
                    language,
                    primaryCrop
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Changes")
        }
    }
}
