package com.example.indra.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.indra.data.Article
import com.example.indra.R

// --- Category color helper ---
@Composable
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Aquifers" -> Color(0xFFB2DFDB) // Teal
        "DIY" -> Color(0xFFF0F4C3) // Lime
        "Stories" -> Color(0xFFBBDEFB) // Light Blue
        "Policies" -> Color(0xFFFFCCBC) // Orange
        "Maintenance" -> Color(0xFFD1C4E9) // Deep Purple
        "Health" -> Color(0xFFC5E1A5) // Green
        "Community" -> Color(0xFFF8BBD0) // Pink
        "Technology" -> Color(0xFFCFD8DC) // Blue-grey
        "Gardening" -> Color(0xFFE6EE9C) // Light Lime
        "Budgeting" -> Color(0xFFBCAAA4) // Brown
        else -> MaterialTheme.colorScheme.surface
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnHubView() {
    val articles = listOf(
        Article(stringResource(R.string.article_aquifers_title), stringResource(R.string.article_aquifers_desc), stringResource(R.string.category_aquifers)),
        Article(stringResource(R.string.article_diy_title), stringResource(R.string.article_diy_desc), stringResource(R.string.category_diy)),
        Article(stringResource(R.string.article_stories_title), stringResource(R.string.article_stories_desc), stringResource(R.string.category_stories)),
        Article(stringResource(R.string.article_policies_title), stringResource(R.string.article_policies_desc), stringResource(R.string.category_policies)),
        Article(stringResource(R.string.article_maintenance_title), stringResource(R.string.article_maintenance_desc), stringResource(R.string.category_maintenance)),
        Article(stringResource(R.string.article_health_title), stringResource(R.string.article_health_desc), stringResource(R.string.category_health)),
        Article(stringResource(R.string.article_community_title), stringResource(R.string.article_community_desc), stringResource(R.string.category_community)),
        Article(stringResource(R.string.article_technology_title), stringResource(R.string.article_technology_desc), stringResource(R.string.category_technology)),
        Article(stringResource(R.string.article_gardening_title), stringResource(R.string.article_gardening_desc), stringResource(R.string.category_gardening)),
        Article(stringResource(R.string.article_budgeting_title), stringResource(R.string.article_budgeting_desc), stringResource(R.string.category_budgeting))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.learn_hub_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(articles) { article ->
            ExpandableArticleCard(article = article)
        }
    }
}

@Composable
fun ExpandableArticleCard(article: Article) {
    var expanded by remember { mutableStateOf(false) }
    val cardColor = getCategoryColor(article.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor) // Card colored
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tag chip (light background for contrast)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary (always visible)
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.article_expand_details),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Chevron icon (aligned right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .rotate(if (expanded) 90f else 0f)
                )
            }
        }
    }
}