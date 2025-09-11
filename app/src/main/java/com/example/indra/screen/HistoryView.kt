package com.example.indra.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.indra.data.Report
import com.example.indra.data.ReportRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun HistoryView(
    onReportClick: (Report) -> Unit = {}
) {
    var historyReports by remember { mutableStateOf<List<Report>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val repository = ReportRepositoryProvider.repository()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                historyReports = repository.getUserReports()
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
            }
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Error loading reports: $error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                try {
                                    historyReports = repository.getUserReports()
                                    error = null
                                } catch (e: Exception) {
                                    error = e.message
                                }
                                isLoading = false
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        historyReports.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(id = com.example.indra.R.string.no_saved_reports),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(historyReports) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReportClick(report) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        ReportSummaryCard(report)
                    }
                }
            }
        }
    }
}