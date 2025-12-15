package com.example.indra.screen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Sort
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Report
import com.example.indra.data.ReportRepositoryProvider
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView(
    onBackClick: () -> Unit, // Added Back Click Handler
    onReportClick: (Report) -> Unit = {}
) {
    // --- State ---
    var historyReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val repository = ReportRepositoryProvider.repository()
    val scope = rememberCoroutineScope()

    // --- Effects ---
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                historyReports = repository.getUserReports().sortedByDescending { it.timestamp }
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 1. Sleek Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Slightly taller for better spacing
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFF005691)
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, start = 16.dp, end = 24.dp)
                    .fillMaxWidth()
            ) {
                // Header Row with Back Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp), // Bigger Text
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "You have saved ${historyReports.size} assessments.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp), // Bigger Text
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // 2. Content List
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(160.dp)) // Push content below header

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                error != null -> {
                    ErrorState(error = error!!) {
                        isLoading = true
                        scope.launch {
                            try {
                                historyReports = repository.getUserReports().sortedByDescending { it.timestamp }
                                error = null
                            } catch (e: Exception) { error = e.message }
                            isLoading = false
                        }
                    }
                }
                historyReports.isEmpty() -> EmptyHistoryState()
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp), // More spacing between cards
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(historyReports) { index, report ->
                            var isVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(index * 75L)
                                isVisible = true
                            }

                            AnimatedVisibility(
                                visible = isVisible,
                                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
                            ) {
                                UltraModernReportCard(report = report, onClick = { onReportClick(report) })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ================== MODERN CARD COMPONENT ==================

@Composable
fun UltraModernReportCard(report: Report, onClick: () -> Unit) {
    // Logic for color coding
    val (statusColor, statusText) = when {
        report.feasibilityScore >= 75 -> Color(0xFF43A047) to "Excellent"
        report.feasibilityScore >= 40 -> Color(0xFFFF9800) to "Moderate"
        else -> Color(0xFFE53935) to "Poor"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.08f))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left Status Strip (Wider)
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .padding(20.dp) // More padding inside card
                    .weight(1f)
            ) {
                // Header: Name & Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = report.name.ifEmpty { "Unnamed Property" },
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), // Bigger
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp) // Bigger Icon
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = report.location,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), // Bigger
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Date Badge
                    Surface(
                        color = Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = formatDate(report.timestamp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), // Bigger
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Divider(color = Color.Gray.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(18.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Feasibility Badge
                    MetricBadge(
                        label = "Score",
                        value = "${report.feasibilityScore.toInt()}%",
                        color = statusColor,
                        icon = null
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Potential Badge
                    MetricBadge(
                        label = "Potential",
                        value = formatCompactNumber(report.annualHarvestingPotentialLiters),
                        color = Color(0xFF2196F3),
                        icon = Icons.Default.WaterDrop
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Arrow Icon
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp) // Bigger clickable arrow
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String,
    color: Color,
    icon: ImageVector?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Box(
                modifier = Modifier.size(10.dp).background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp), // Bigger Text
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ================== STATES ==================

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Color.White, CircleShape)
                .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No History Found",
            style = MaterialTheme.typography.headlineMedium, // Bigger
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your completed assessments will appear here.",
            style = MaterialTheme.typography.bodyLarge, // Bigger
            color = Color.Gray
        )
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.Search, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Oops!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(error, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}

// ================== UTILS ==================

@SuppressLint("SimpleDateFormat")
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatCompactNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM L", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.0fK L", number / 1_000.0)
        else -> "$number L"
    }
}