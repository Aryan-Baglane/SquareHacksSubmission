package com.example.indra.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.indra.service.indra.PropertyService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onPropertyAdded: () -> Unit
) {
    var propertyName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var roofArea by remember { mutableStateOf("") }
    var openSpace by remember { mutableStateOf("") }
    var dwellers by remember { mutableStateOf("4") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Property") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = propertyName,
                    onValueChange = { propertyName = it },
                    label = { Text("Property Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = roofArea,
                        onValueChange = { roofArea = it },
                        label = { Text("Roof Area (sq m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = openSpace,
                        onValueChange = { openSpace = it },
                        label = { Text("Open Space (sq m)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = dwellers,
                    onValueChange = { dwellers = it },
                    label = { Text("Number of Dwellers") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null

                        try {
                            val lat = latitude.toDoubleOrNull()
                            val lng = longitude.toDoubleOrNull()
                            val roof = roofArea.toDoubleOrNull()
                            val space = openSpace.toDoubleOrNull()
                            val dwellersCount = dwellers.toIntOrNull()

                            if (lat == null || lng == null || roof == null || space == null || dwellersCount == null) {
                                error = "Please enter valid numbers for all fields"
                                isLoading = false
                                return@launch
                            }

                            if (propertyName.isBlank() || address.isBlank()) {
                                error = "Please enter property name and address"
                                isLoading = false
                                return@launch
                            }

                            PropertyService.addPropertyFromAssessment(
                                name = propertyName,
                                address = address,
                                latitude = lat,
                                longitude = lng,
                                roofArea = roof,
                                openSpace = space,
                                numberOfDwellers = dwellersCount
                            )

                            // reset fields after success
                            propertyName = ""
                            address = ""
                            latitude = ""
                            longitude = ""
                            roofArea = ""
                            openSpace = ""
                            dwellers = "4"

                            onPropertyAdded()
                            onDismiss()
                        } catch (e: Exception) {
                            error = e.message ?: "Failed to add property"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add Property")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
