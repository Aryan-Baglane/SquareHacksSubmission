package com.example.indra.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun CommunityScreen() {
    var selectedTab by remember { mutableStateOf("Feed") }
    val tabs = listOf("Feed", "Stories", "Tips", "Events")

    // Header Animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // 1. Animated Header Background
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary, Color(0xFF4FC3F7))
                        ),
                        RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp)
                    )
            ) {
                // Header Content
                Column(modifier = Modifier.padding(top = 60.dp, start = 24.dp, end = 24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Community",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row {
                            IconButton(onClick = { /* Search */ }) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                            }
                            IconButton(onClick = { /* Notif */ }) {
                                Icon(Icons.Outlined.Notifications, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Connect with water conservationists",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // 2. Floating Content Layer
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(160.dp)) // Overlap header

            // Custom Tab Selector (Pill Shape)
            LazyRow(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tabs) { tab ->
                    ModernTabChip(
                        text = tab,
                        isSelected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Tab Content
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInVertically { 50 } togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "content"
            ) { targetTab ->
                when (targetTab) {
                    "Feed" -> FeedSection()
                    "Stories" -> StoriesSection()
                    "Tips" -> TipsSection()
                    else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Coming Soon", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// ================= SECTIONS =================

@Composable
fun FeedSection() {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Horizontal Stories Preview inside Feed
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { AddStoryItem() }
                items(5) { StoryCircleItem(it) }
            }
        }

        items(5) { index ->
            PostCard(index)
        }
    }
}

@Composable
fun StoriesSection() {
    // A Grid or specialized view for Stories
    LazyColumn(contentPadding = PaddingValues(24.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(4) {
            StoryFeatureCard(it)
        }
    }
}

@Composable
fun TipsSection() {
    LazyColumn(contentPadding = PaddingValues(24.dp, bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(tipsData) { tip ->
            TipCard(tip)
        }
    }
}

// ================= COMPONENTS =================

@Composable
fun ModernTabChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        shadowElevation = if (isSelected) 8.dp else 2.dp,
        modifier = Modifier.height(40.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PostCard(index: Int) {
    var isLiked by remember { mutableStateOf(false) }
    val likeScale by animateFloatAsState(if (isLiked) 1.2f else 1f, label = "like")

    Card(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha=0.1f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter("https://i.pravatar.cc/150?img=${index + 10}"),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha=0.2f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Sarah Jenkins", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("2 hours ago • Delhi", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                "Just installed my new rainwater harvesting system! It was easier than I thought. 🌧️💧 #Sustainability #IndraApp",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post Image
            Image(
                painter = rememberAsyncImagePainter("https://picsum.photos/600/400?random=$index"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if(isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if(isLiked) Color(0xFFE91E63) else Color.Gray,
                        modifier = Modifier
                            .scale(likeScale)
                            .clickable { isLiked = !isLiked }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if(isLiked) "24" else "23", fontSize = 14.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.width(20.dp))

                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("5", fontSize = 14.sp, color = Color.Gray)
                }

                Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun AddStoryItem() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color.LightGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("My Story", fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun StoryCircleItem(index: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFFFF9800))) // Instagram-ish gradient
                )
                .padding(2.dp) // Border width
                .clip(CircleShape)
                .background(Color.White)
                .padding(2.dp) // Gap
        ) {
            Image(
                painter = rememberAsyncImagePainter("https://i.pravatar.cc/150?img=$index"),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text("User $index", fontSize = 11.sp, color = Color.Black.copy(alpha=0.7f))
    }
}

@Composable
fun StoryFeatureCard(index: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = rememberAsyncImagePainter("https://picsum.photos/600/300?random=${index + 50}"),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.7f))))
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberAsyncImagePainter("https://i.pravatar.cc/150?img=${index + 20}"),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Vlog $index", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: TipData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tip.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tip.icon, contentDescription = null, tint = tip.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(tip.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(tip.desc, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
            }
        }
    }
}

// ================= DUMMY DATA =================

data class TipData(val title: String, val desc: String, val icon: ImageVector, val color: Color)

val tipsData = listOf(
    TipData("Catch the Rain", "Install mesh filters to prevent debris clogging.", Icons.Outlined.WaterDrop, Color(0xFF2196F3)),
    TipData("Save Water", "Fix leaky faucets to save up to 20L per day.", Icons.Outlined.Build, Color(0xFF4CAF50)),
    TipData("Garden Care", "Water plants early morning to reduce evaporation.", Icons.Outlined.Grass, Color(0xFFFF9800)),
    TipData("Purification", "Use natural filtration layers like sand and charcoal.", Icons.Outlined.FilterAlt, Color(0xFF9C27B0))
)