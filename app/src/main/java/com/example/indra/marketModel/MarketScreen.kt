package com.example.indra.marketModel

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar

// --- MODERN COLOR PALETTE ---
private val PrimaryGreen = Color(0xFF2E7D32)
private val LightBg = Color(0xFFF8FAF8)
private val SurfaceWhite = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val context = LocalContext.current
    val records by viewModel.records.collectAsState()
    val stateList by viewModel.states.collectAsState()
    val districtList by viewModel.districts.collectAsState()
    val cropList by viewModel.crops.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedState by remember { mutableStateOf("Select State") }
    var selectedDistrict by remember { mutableStateOf("Select District") }
    var selectedCrop by remember { mutableStateOf("Select Crop") }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val formattedDate = "$year-${month + 1}-$day"
                onDateSelected(formattedDate)
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mandi Prices", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = LightBg)
            )
        },
        containerColor = LightBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // --- FILTER SECTION ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterList, contentDescription = null, tint = PrimaryGreen)
                        Spacer(Modifier.width(8.dp))
                        Text("Filters", fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SimpleDropdown("State", stateList, selectedState) {
                        selectedState = it
                        selectedDistrict = "Select District"
                        selectedCrop = "Select Crop"
                        viewModel.fetchDistricts(it)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SimpleDropdown("District", districtList, selectedDistrict) {
                        selectedDistrict = it
                        selectedCrop = "Select Crop"
                        viewModel.fetchCrops(selectedState, it)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SimpleDropdown("Crop", cropList, selectedCrop) {
                        selectedCrop = it
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- MODERN DATE ROW ---
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showDatePicker { fromDate = it } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (fromDate.isEmpty()) "From" else fromDate, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showDatePicker { toDate = it } },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (toDate.isEmpty()) "To" else toDate, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.searchMarketData(
                                selectedState, selectedDistrict, selectedCrop,
                                if(fromDate.isEmpty()) null else fromDate,
                                if(toDate.isEmpty()) null else toDate
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Check Live Prices", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- RESULTS SECTION ---
            Text(
                "Market Results",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(records) { record ->
                        MarketRecordItem(record)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(label: String, options: List<String>, selectedOption: String, onSelectionChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            readOnly = true,
            value = selectedOption,
            onValueChange = { },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(SurfaceWhite)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MarketRecordItem(record: MarketRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.commodity,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
                Text(
                    text = "${record.market}, ${record.district}",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = record.date,
                    fontSize = 11.sp,
                    color = PrimaryGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${record.modalPrice}",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = PrimaryGreen
                )
                Text(
                    text = "per Quintal",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}