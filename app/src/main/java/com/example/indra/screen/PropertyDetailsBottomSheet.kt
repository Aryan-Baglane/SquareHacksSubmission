package com.example.indra.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Property
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PropertyDetailsBottomSheet(
    property: Property,
    onDismiss: () -> Unit
) {
    // Determine color based on score
    val scoreColor = when {
        property.feasibilityScore >= 70 -> Color(0xFF4CAF50) // Green
        property.feasibilityScore >= 40 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFE53935) // Red
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Dark Overlay (Click to dismiss)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { onDismiss() }
        )

        // 2. Bottom Sheet Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 650.dp) // Limit height
                .clickable(enabled = false) {}, // Prevent clicks from passing through
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- Header & Close Button ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Property Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = property.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(Color(0xFFF5F5F5), CircleShape)
                            .size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Black
                        )
                    }
                }

                Divider(color = Color(0xFFF5F5F5), thickness = 2.dp)

                // --- Score Section ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Feasibility Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (property.feasibilityScore >= 70) "Excellent Potential" else "Moderate Potential",
                            style = MaterialTheme.typography.bodyMedium,
                            color = scoreColor,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Circular Progress Indicator
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(70.dp),
                            color = Color(0xFFF0F0F0),
                            strokeWidth = 6.dp,
                        )
                        CircularProgressIndicator(
                            progress = { property.feasibilityScore / 100f },
                            modifier = Modifier.size(70.dp),
                            color = scoreColor,
                            strokeWidth = 6.dp,
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = "${property.feasibilityScore.toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                    }
                }

                // --- Quick Stats Row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ModernMetricItem(
                        icon = Icons.Outlined.WaterDrop,
                        label = "Potential",
                        value = "${property.annualHarvestingPotentialLiters / 1000} kL",
                        color = MaterialTheme.colorScheme.primary
                    )
                    ModernMetricItem(
                        icon = Icons.Outlined.CurrencyRupee,
                        label = "Est. Cost",
                        value = "₹${property.estimatedCostInr/1000}k",
                        color = Color(0xFF795548)
                    )
                    ModernMetricItem(
                        icon = Icons.Outlined.Build,
                        label = "Solution",
                        value = property.recommendedSolution.take(8) + "...", // Truncate if long
                        color = Color(0xFF607D8B)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Detailed Info Card ---
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Specifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )

                        ModernDetailRow("Property Type", property.propertyType)
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
                        ModernDetailRow("Roof Area", "${property.roofArea} m²")
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
                        ModernDetailRow("Open Space", "${property.openSpace} m²")
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
                        ModernDetailRow("Occupants", "${property.dwellers} People")
                        Divider(color = Color.LightGray.copy(alpha = 0.2f))
                        ModernDetailRow("Date", formatDate(property.lastAssessmentDate))
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ModernMetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ModernDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.8f)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}