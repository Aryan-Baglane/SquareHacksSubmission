package com.example.indra.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.indra.auth.AuthApi
import com.example.indra.data.AuthUser
import com.example.indra.data.UserProfile
import com.example.indra.db.DatabaseProvider
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onSignedOut: () -> Unit = {}) {
    var user by remember { mutableStateOf<AuthUser?>(null) }
    var loading by remember { mutableStateOf(true) }
    var editing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Logic Section ---
    LaunchedEffect(Unit) {
        user = AuthApi.currentUser()
        loading = false
        name = user?.displayName.orEmpty()
        user?.uid?.let { uid ->
            val existing = DatabaseProvider.database().getUserProfile(uid)
            if (existing != null) {
                name = existing.displayName ?: name
            }
            val authPhoto = user?.photoUrl
            if (!authPhoto.isNullOrBlank() && (existing == null || existing.photoUrl != authPhoto)) {
                DatabaseProvider.database().setUserProfile(
                    UserProfile(uid = uid, displayName = name.ifBlank { user?.displayName }, photoUrl = authPhoto)
                )
            }
        }
    }

    // --- UI Section ---
    val headerHeight = 220.dp
    val contentOverlap = 100.dp

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // 1. Animated Header Background
        AnimatedHeaderBackground(headerHeight)

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (user == null) {
            // Placeholder for your auth screen logic if needed
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please Sign In")
            }
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Transparent // Transparent to show header background
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Push content down to overlap the header
                    Spacer(modifier = Modifier.height(headerHeight - contentOverlap))

                    if (!editing) {
                        // Modern Profile Header (Solid Card)
                        ModernProfileHeader(user = user, onEditClick = { editing = true })

                        Spacer(modifier = Modifier.height(30.dp))
                        Text(
                            "Preferences",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
                        )

                        // Modern Menu Options (Solid Cards)
                        ModernOptionCard(
                            icon = Icons.Default.HomeWork,
                            title = "My Properties",
                            subtitle = "Manage Saved Locations"
                        ) {}

                        ModernOptionCard(
                            icon = Icons.Outlined.Lock,
                            title = "Security",
                            subtitle = "Password & Auth"
                        ) {}

                        ModernOptionCard(
                            icon = Icons.Outlined.Settings,
                            title = "Settings",
                            subtitle = "App Configuration"
                        ) {}

                        Spacer(modifier = Modifier.height(30.dp))

                        // Modern Sign Out Button
                        ModernButton(
                            text = "Sign Out",
                            icon = Icons.Default.Logout,
                            isDestructive = true,
                            onClick = {
                                scope.launch {
                                    AuthApi.signOut()
                                    user = null
                                    onSignedOut()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                    } else {
                        // Edit Mode (Solid Card)
                        ModernEditProfileView(
                            name = name,
                            onNameChange = { name = it },
                            onSave = {
                                scope.launch {
                                    AuthApi.updateProfile(displayName = name, photoUrl = null)
                                    user = AuthApi.currentUser()
                                    user?.uid?.let { uid ->
                                        DatabaseProvider.database().setUserProfile(UserProfile(uid = uid, displayName = name))
                                    }
                                    editing = false
                                    snackbarHostState.showSnackbar("Profile saved!")
                                }
                            },
                            onCancel = { editing = false }
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// UI COMPONENTS - MODERN CLEAN STYLE WITH ANIMATED BACKGROUND
// -----------------------------------------------------------------------------

@Composable
fun AnimatedHeaderBackground(height: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        Color(0xFF4FC3F7) // Lighter blue at bottom for depth
                    )
                ),
                shape = RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
            )
    ) {
        // Decorative Circles in Background for animation effect
        Box(
            modifier = Modifier
                .offset(x = 200.dp, y = (-50).dp)
                .size(300.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        )
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = 80.dp)
                .size(180.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
        )
    }
}

@Composable
fun ModernProfileHeader(user: AuthUser?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Avatar with Gradient Border
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2196F3), Color(0xFF00BCD4))
                        )
                    )
                    .padding(4.dp) // Border thickness
                    .clip(CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = user?.photoUrl),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFE0E0E0))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.displayName ?: "User Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E) // Deep Blue Text
            )

            Text(
                text = user?.email ?: "email@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Edit Button (Pill shape)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
                    .clickable { onEditClick() }
                    .padding(horizontal = 32.dp, vertical = 10.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ModernOptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black.copy(alpha=0.8f))
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun ModernButton(text: String, icon: ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    val containerColor = if (isDestructive) Color(0xFFFFEBEE) else Color.White
    val contentColor = if (isDestructive) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
    val borderColor = if (isDestructive) Color(0xFFFFCDD2) else Color.LightGray

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun ModernEditProfileView(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("Update Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            // Custom TextField Styling
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Cancel Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Gray.copy(alpha=0.1f))
                        .clickable { onCancel() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                // Save Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                        .clickable { onSave() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}