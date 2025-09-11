package com.example.indra.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.indra.i18n.LocaleManager
import com.example.indra.ui.theme.ThemeManager

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Profile / Account ---
        item {
            SettingsOption(
                title = "Profile",
                subtitle = "Manage your account information",
                icon = Icons.Default.Person
            )
        }

        // --- Notifications ---
        item {
            SettingsOption(
                title = "Notifications",
                subtitle = "Alerts for water quality, maintenance, and updates",
                icon = Icons.Default.Notifications
            )
        }

        // --- Location Settings ---
        item {
            SettingsOption(
                title = "Location",
                subtitle = "Set your city/state for personalized water resources",
                icon = Icons.Default.LocationOn
            )
        }

        // --- Learning Hub ---
        item {
            SettingsOption(
                title = "Learning Hub",
                subtitle = "Customize article recommendations",
                icon = Icons.Default.MenuBook
            )
        }

        // --- Budget & Insurance ---
        item {
            SettingsOption(
                title = "Budget Preferences",
                subtitle = "Set budget alerts for installation/maintenance",
                icon = Icons.Default.AttachMoney
            )
        }

        // --- App Settings ---
        item {
            SettingsOption(
                title = "Theme",
                subtitle = "Light / Dark mode",
                icon = Icons.Default.DarkMode
            )
        }

        // --- Language ---
        item {
            val activity = LocalContext.current as android.app.Activity
            var expanded by remember { mutableStateOf(false) }
            val languages = listOf(
                "English" to "en",
                "हिन्दी" to "hi",
                "বাংলা" to "bn",
                "मराठी" to "mr",
                "తెలుగు" to "te",
                "தமிழ்" to "ta",
                "ગુજરાતી" to "gu",
                "ಕನ್ನಡ" to "kn"
            )
            var selected by remember { mutableStateOf(LocaleManager.getSavedLocale(activity) ?: "en") }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Language", style = MaterialTheme.typography.titleMedium)
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            readOnly = true,
                            value = languages.firstOrNull { it.second == selected }?.first ?: "English",
                            onValueChange = {},
                            label = { Text("Choose Language") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            languages.forEach { (label, tag) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = {
                                    expanded = false
                                    selected = tag
                                    LocaleManager.applyAndRestart(activity, tag)
                                })
                            }
                        }
                    }
                }
            }
        }

        // --- Theme Selection ---
        item {
            val activity = LocalContext.current as android.app.Activity
            var themeExpanded by remember { mutableStateOf(false) }
            val themes = listOf(
                "System Default" to "system",
                "Light" to "light",
                "Dark" to "dark"
            )

            var selectedTheme by remember { mutableStateOf(ThemeManager.getThemeMode(activity)) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Change the app's appearance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ExposedDropdownMenuBox(expanded = themeExpanded, onExpandedChange = { themeExpanded = it }) {
                        OutlinedTextField(
                            readOnly = true,
                            value = themes.firstOrNull { it.second == selectedTheme }?.first ?: "System Default",
                            onValueChange = {},
                            label = { Text("Theme") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                            themes.forEach { (label, themeKey) ->
                                DropdownMenuItem(text = { Text(label) }, onClick = {
                                    themeExpanded = false
                                    selectedTheme = themeKey
                                    ThemeManager.setThemeMode(activity, themeKey)
                                    activity.recreate() // Restart activity to apply new theme
                                })
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsOption(
                title = "About",
                subtitle = "Version 1.0 • Prototype",
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
fun SettingsOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}