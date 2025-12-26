package com.example.indra.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.vendor.VendorResult
import com.example.indra.vendor.VendorUiState
import com.example.indra.vendor.VendorViewModel

@Composable
fun VendorsScreen(
    onBackClick: () -> Unit,
    viewModel: VendorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0) // ✅ draw behind status bar
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            /* ---------- FIXED HEADER ---------- */
            HeaderSection(
                onBackClick = onBackClick,
                searchQuery = searchQuery,
                onQueryChange = viewModel::onQueryChange,
                onSearchTriggered = {
                    focusManager.clearFocus()
                    viewModel.performSearch()
                },
                onClearSearch = viewModel::clearSearch
            )

            /* ---------- SCROLLABLE CONTENT ---------- */
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp)
            ) {

                when (val state = uiState) {

                    is VendorUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxHeight(0.7f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2E5BCC),
                                    strokeWidth = 3.dp
                                )
                            }
                        }
                    }

                    is VendorUiState.Success -> {
                        val vendors = state.vendors.values.flatten()
                        if (vendors.isEmpty()) {
                            item {
                                EmptyStateMessage(
                                    "No vendors found in ${state.location}"
                                )
                            }
                        } else {
                            items(vendors) { vendor ->
                                VendorCard(vendor)
                            }
                        }
                    }

                    is VendorUiState.Error -> {
                        item { ErrorMessage(state.message) }
                    }

                    is VendorUiState.Idle -> {
                        item {
                            EmptyStateMessage(
                                "Search for a city (e.g., Delhi, Mumbai) to find RWH experts"
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- HEADER ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
    onBackClick: () -> Unit,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSearchTriggered: () -> Unit,
    onClearSearch: () -> Unit
) {
    val animatedOffset by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "header_anim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)                 // includes status bar area
            .statusBarsPadding()            // ✅ keeps content safe
            .offset(y = animatedOffset)
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E5BCC),
                        Color(0xFF4AC2E1)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Service Vendors",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Rainwater Harvesting Experts",
                        color = Color.White.copy(0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(0.18f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            "Enter city name",
                            color = Color.White.copy(0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, null, tint = Color.White)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchTriggered() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/* ---------------- VENDOR CARD ---------------- */

@Composable
fun VendorCard(vendor: VendorResult) {
    val context = LocalContext.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically { it / 3 }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clickable {
                    vendor.website?.takeIf { it.isNotEmpty() }?.let {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(it))
                        )
                    }
                },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE0E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (vendor.category == "online")
                                Icons.Default.Public
                            else Icons.Default.Storefront,
                            null,
                            tint = Color(0xFF2E5BCC)
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(Modifier.weight(1f)) {
                        Text(
                            vendor.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            vendor.location ?: "Online Service",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    if (!vendor.website.isNullOrEmpty()) {
                        Icon(
                            Icons.Default.OpenInNew,
                            null,
                            tint = Color(0xFF2E5BCC)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (vendor.category == "online") "Online" else "Local Vendor"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Verified,
                                null,
                                tint = Color(0xFF2E5BCC),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            null,
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            " ${vendor.rating ?: "4.0"} • ${vendor.price_range ?: "₹₹"}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- STATES ---------------- */

@Composable
fun EmptyStateMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SearchOff,
            null,
            modifier = Modifier.size(90.dp),
            tint = Color.Gray.copy(0.25f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier.padding(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Error, null, tint = Color.Red)
            Spacer(Modifier.width(12.dp))
            Text(message, color = Color.Red, fontSize = 14.sp)
        }
    }
}
