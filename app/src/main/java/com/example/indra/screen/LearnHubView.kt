package com.example.indra.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Article

// --- 1. Category Colors & Icons ---
data class CategoryStyle(val color: Color, val icon: ImageVector)

@Composable
fun getCategoryStyle(category: String): CategoryStyle {
    return when (category) {
        "Aquifers" -> CategoryStyle(Color(0xFF00838F), Icons.Outlined.Water)      // Cyan 800
        "DIY" -> CategoryStyle(Color(0xFFEF6C00), Icons.Outlined.Build)           // Orange 800
        "Stories" -> CategoryStyle(Color(0xFF4527A0), Icons.Outlined.AutoStories) // Deep Purple 800
        "Policies" -> CategoryStyle(Color(0xFF1565C0), Icons.Outlined.Policy)     // Blue 800
        "Maintenance" -> CategoryStyle(Color(0xFFAD1457), Icons.Outlined.Engineering) // Pink 800
        "Health" -> CategoryStyle(Color(0xFF2E7D32), Icons.Outlined.HealthAndSafety) // Green 800
        "Community" -> CategoryStyle(Color(0xFF6A1B9A), Icons.Outlined.Groups)    // Purple 800
        "Technology" -> CategoryStyle(Color(0xFF37474F), Icons.Outlined.Memory)   // Blue Grey 800
        "Gardening" -> CategoryStyle(Color(0xFF558B2F), Icons.Outlined.Yard)      // Light Green 800
        "Budgeting" -> CategoryStyle(Color(0xFF4E342E), Icons.Outlined.AccountBalanceWallet) // Brown 800
        else -> CategoryStyle(Color.Gray, Icons.Outlined.Article)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnHubView(
    onBackClick: () -> Unit
) {
    // Dummy Data
    val articles = listOf(
        Article("Understanding Aquifers", "Learn how groundwater is stored naturally beneath the earth.", "Aquifers"),
        Article("DIY Rain Barrel", "A step-by-step guide to building your own rainwater harvesting system.", "DIY"),
        Article("Success Stories", "How a community in Rajasthan revived their water table.", "Stories"),
        Article("Govt Policies 2025", "Subsidies available for installing RWH systems in urban areas.", "Policies"),
        Article("Filter Maintenance", "Cleaning your filters ensures clean water and system longevity.", "Maintenance"),
        Article("Water Quality & Health", "Why testing pH levels matters for drinking water safety.", "Health"),
        Article("Tech in Water", "IoT sensors that detect leakages in real-time.", "Technology"),
        Article("Urban Gardening", "Using harvested water for your terrace garden.", "Gardening"),
    )

    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // --- 2. Header Background ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Adjusted height since filters are gone
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D47A1), // Deep Blue
                            Color(0xFF1976D2), // Medium Blue
                            Color(0xFF00BCD4)  // Cyan Accent
                        )
                    ),
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // --- Navbar & Search ---
            Column(
                modifier = Modifier
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
            ) {
                // Nav Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Knowledge Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search topics...", color = Color.White.copy(alpha = 0.8f)) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.1f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            // --- Spacing after header ---
            Spacer(modifier = Modifier.height(40.dp))

            // --- Article List ---
            // Removed LazyRow (Filters) and reduced complexity
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(articles) { index, article ->
                    // Simple search filter logic
                    if (searchQuery.isEmpty() || article.title.contains(searchQuery, ignoreCase = true) || article.category.contains(searchQuery, ignoreCase = true)) {
                        EvenColorArticleCard(article, index)
                    }
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

// ================== COMPONENTS ==================

// --- 3. Even Color Article Card ---
@Composable
fun EvenColorArticleCard(article: Article, index: Int) {
    var expanded by remember { mutableStateOf(false) }
    val style = getCategoryStyle(article.category)

    // Staggered Entrance Animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { 50 } + fadeIn()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring())
                .shadow(elevation = 0.dp) // Flat look, color provides separation
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(20.dp),
            // EVEN CARD COLOR: Solid pastel tint of the category color
            colors = CardDefaults.cardColors(containerColor = style.color.copy(alpha = 0.12f)),
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    // Category Icon Box (Solid Color)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(style.color, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            tint = Color.White, // White icon on solid background
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Text Content
                    Column(modifier = Modifier.weight(1f)) {
                        // Category Label
                        Text(
                            text = article.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = style.color, // Matches icon box
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = article.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF455A64),
                            maxLines = if (expanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expandable Section
                if (expanded) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Divider(color = style.color.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Full Article Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a placeholder for the full article content. In a real application, you would load detailed steps, images, and external links here related to ${article.title}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { /* Open Full View */ },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = style.color),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Read Full Guide", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // Toggle Arrow
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = style.color.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}