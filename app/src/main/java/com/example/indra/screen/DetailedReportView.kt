package com.example.indra.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Report
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetailedReportView(
    report: Report,
    onBack: () -> Unit,
    onAddToProperties: (Report) -> Unit
) {
    // The parent composable (App.kt) already provides a Scaffold with a TopAppBar.
    // We only need to provide the main content.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color.White)
                )
            )
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main title (not in TopAppBar, but a part of the screen body)
        Text(
            stringResource(id = com.example.indra.R.string.assessment_results),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        val resp = report.assessmentResponse
        if (resp != null) {
            // Feasibility Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        resp.feasibilityScore >= 70.0 -> Color(0xFF1B5E20)
                        resp.feasibilityScore >= 50.0 -> Color(0xFFEF6C00)
                        else -> Color(0xFFB71C1C)
                    }
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = com.example.indra.R.string.feasibility_score),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${String.format("%.1f", resp.feasibilityScore)}%",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = resp.feasibilityInsights,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Information
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = com.example.indra.R.string.location_information), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(id = com.example.indra.R.string.annual_rainfall), "${formatNumber(resp.locationInfo.avgAnnualRainfallMm)} mm")
                    DetailRow(stringResource(id = com.example.indra.R.string.soil_type), resp.locationInfo.soilType)
                    DetailRow(stringResource(id = com.example.indra.R.string.soil_permeability), resp.locationInfo.soilPermeability)
                    DetailRow(stringResource(id = com.example.indra.R.string.principal_aquifer), resp.locationInfo.principalAquifer)
                    DetailRow(stringResource(id = com.example.indra.R.string.groundwater_depth), "${formatNumber(resp.locationInfo.predictedGroundwaterDepthMbgl)} m")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rainwater Harvesting
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = com.example.indra.R.string.rainwater_harvesting), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(id = com.example.indra.R.string.annual_runoff_potential), "${formatInt(resp.rwhAnalysis.potentialAnnualRunoffLiters)} L")
                    DetailRow(stringResource(id = com.example.indra.R.string.recommended_tank_size), "${resp.rwhAnalysis.recommendedTankSizeLiters} L")
                    if (resp.rwhAnalysis.notes.isNotBlank()) SupportingText(resp.rwhAnalysis.notes)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Artificial Recharge
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = com.example.indra.R.string.artificial_recharge), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(id = com.example.indra.R.string.feasible), if (resp.arAnalysis.isFeasible) stringResource(id = com.example.indra.R.string.yes) else stringResource(id = com.example.indra.R.string.no))
                    DetailRow(stringResource(id = com.example.indra.R.string.recommended_structure), resp.arAnalysis.recommendedStructureType)
                    resp.arAnalysis.structureDimensions.forEach { (k, v) -> DetailRow(k, v) }
                    if (resp.arAnalysis.notes.isNotBlank()) SupportingText(resp.arAnalysis.notes)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cost Benefit
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(id = com.example.indra.R.string.cost_benefit), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(stringResource(id = com.example.indra.R.string.initial_investment), "₹${formatCurrency(resp.costBenefitAnalysis.estimatedInitialInvestment)}")
                    DetailRow(stringResource(id = com.example.indra.R.string.annual_maintenance), "₹${formatCurrency(resp.costBenefitAnalysis.annualOperatingMaintenanceCost)}")
                    DetailRow(stringResource(id = com.example.indra.R.string.annual_water_savings), "${formatInt(resp.costBenefitAnalysis.annualWaterSavingsLiters)} L")
                    DetailRow(stringResource(id = com.example.indra.R.string.annual_monetary_savings), "₹${formatCurrency(resp.costBenefitAnalysis.annualMonetarySavings)}")
                    DetailRow(stringResource(id = com.example.indra.R.string.payback_period), "${String.format("%.1f", resp.costBenefitAnalysis.paybackPeriodYears)} years")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        } else {
            // Fallback to basic summary if detailed response not available
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Feasibility Score", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("${report.feasibilityScore.toInt()}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow("Property Name", report.name)
                    DetailRow("Location", report.location)
                    DetailRow("Dwellers", report.dwellers.toString())
                    DetailRow("Roof Area", "${report.roofArea} sq. m")
                    DetailRow("Open Space", "${report.openSpace} sq. m")
                    DetailRow("Date", formatDate(report.timestamp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Button(
            onClick = { onAddToProperties(report) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Add to My Properties",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun RecommendationItem(title: String, value: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatNumber(x: Double): String = String.format(Locale.getDefault(), "%.2f", x)
private fun formatCurrency(x: Double): String = String.format(Locale.getDefault(), "%,.0f", x)
private fun formatInt(x: Double): String = String.format(Locale.getDefault(), "%,.0f", x)

@Composable
private fun SupportingText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}