package com.example.indra.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Report
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailedReportView(
    report: Report,
    onBack: () -> Unit,
    onAddToProperties: (Report) -> Unit
) {
    val scrollState = rememberScrollState()
    var isVisible by remember { mutableStateOf(false) }

    // Trigger animation on load
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val resp = report.assessmentResponse
    val score = report.feasibilityScore

    // Dynamic Color based on score
    val themeColor = when {
        score >= 70 -> Color(0xFF43A047) // Green
        score >= 40 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFD32F2F)        // Red
    }

    // Score Animation
    val animatedScore by animateFloatAsState(
        targetValue = if (isVisible) score else 0f,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "score"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 1. Gradient Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(themeColor, Color(0xFFF8F9FA))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 2. Custom Navbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Report Details",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Hero Section (Wave Animation)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                WaveCircle(
                    progress = animatedScore / 100f,
                    color = themeColor,
                    modifier = Modifier.size(160.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedScore.toInt()}%",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall.copy(
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
            if (resp != null) {
                Text(
                    text = resp.feasibilityInsights,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // 4. Data Cards
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (resp != null) {
                    // --- Detailed Report Cards ---

                    AnimatedVisibility(visible = isVisible, enter = slideInVertically{50} + fadeIn()) {
                        DetailSectionCard("Harvesting Potential", Icons.Outlined.WaterDrop, Color(0xFF2196F3)) {
                            InfoRow("Annual Runoff", "${formatInt(resp.rwhAnalysis.potentialAnnualRunoffLiters)} L", true)
                            InfoRow("Tank Size", "${resp.rwhAnalysis.recommendedTankSizeLiters} L")
                            if(resp.rwhAnalysis.notes.isNotEmpty()) NoteText(resp.rwhAnalysis.notes)
                        }
                    }

                    AnimatedVisibility(visible = isVisible, enter = slideInVertically{100} + fadeIn()) {
                        DetailSectionCard("Technical Solution", Icons.Outlined.Construction, Color(0xFF9C27B0)) {
                            InfoRow("Structure", resp.arAnalysis.recommendedStructureType, true)
                            InfoRow("Feasible", if(resp.arAnalysis.isFeasible) "Yes" else "No")
                            if(resp.arAnalysis.structureDimensions.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Dimensions:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                resp.arAnalysis.structureDimensions.forEach { (k, v) -> InfoRow("• $k", v) }
                            }
                        }
                    }

                    AnimatedVisibility(visible = isVisible, enter = slideInVertically{150} + fadeIn()) {
                        DetailSectionCard("Location & Geology", Icons.Outlined.Landscape, Color(0xFF795548)) {
                            InfoRow("Rainfall", "${formatNumber(resp.locationInfo.avgAnnualRainfallMm)} mm")
                            InfoRow("Soil", resp.locationInfo.soilType)
                            InfoRow("Aquifer", resp.locationInfo.principalAquifer)
                            InfoRow("GW Depth", "${formatNumber(resp.locationInfo.predictedGroundwaterDepthMbgl)} m")
                        }
                    }

                    AnimatedVisibility(visible = isVisible, enter = slideInVertically{200} + fadeIn()) {
                        DetailSectionCard("Financials", Icons.Outlined.MonetizationOn, Color(0xFF4CAF50)) {
                            InfoRow("Investment", "₹${formatCurrency(resp.costBenefitAnalysis.estimatedInitialInvestment)}", true)
                            InfoRow("Maintenance/yr", "₹${formatCurrency(resp.costBenefitAnalysis.annualOperatingMaintenanceCost)}")
                            InfoRow("Savings/yr", "₹${formatCurrency(resp.costBenefitAnalysis.annualMonetarySavings)}")
                            InfoRow("Payback", "${String.format("%.1f", resp.costBenefitAnalysis.paybackPeriodYears)} Years")
                        }
                    }

                } else {
                    // --- Fallback Simple Cards ---
                    DetailSectionCard("Basic Details", Icons.Outlined.Info, themeColor) {
                        InfoRow("Property", report.name)
                        InfoRow("Location", report.location)
                        InfoRow("Roof Area", "${report.roofArea} sqm")
                        InfoRow("Date", formatDate(report.timestamp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Button
                Button(
                    onClick = { onAddToProperties(report) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Add to My Properties",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// ================== COMPONENTS ==================

@Composable
fun DetailSectionCard(title: String, icon: ImageVector, accentColor: Color, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(accentColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
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
fun InfoRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else Color.Black
        )
    }
}

@Composable
fun NoteText(text: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Note: $text",
        style = MaterialTheme.typography.bodySmall,
        color = Color.Gray.copy(alpha = 0.8f),
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
    )
}

@Composable
fun WaveCircle(progress: Float, color: Color, modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveShift by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "shift"
    )

    Canvas(modifier = modifier) {
        val waterLevelY = size.height * (1 - progress)
        val path = Path().apply {
            moveTo(0f, waterLevelY)
            for (x in 0..size.width.toInt() step 5) {
                val y = waterLevelY + 10 * sin((x * 0.03f) + waveShift)
                lineTo(x.toFloat(), y)
            }
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, brush = Brush.verticalGradient(listOf(color.copy(alpha=0.85f), color)))
    }
}

// ================== UTILS ==================

private fun formatNumber(x: Double): String = String.format(Locale.getDefault(), "%.1f", x)
private fun formatCurrency(x: Double): String = String.format(Locale.getDefault(), "%,.0f", x)
private fun formatInt(x: Double): String = String.format(Locale.getDefault(), "%,.0f", x)

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}