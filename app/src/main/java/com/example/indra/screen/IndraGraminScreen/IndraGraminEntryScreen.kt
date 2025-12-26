package com.example.indra.screen.IndraGraminScreen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.indra.auth.AuthApi
import com.example.indra.db.DatabaseProvider
import com.example.indra.navigation.AppRoutes
import kotlinx.coroutines.delay


@Composable
fun IndraGraminEntryScreen(navController: NavController) {
    // 1. Hold the logic states
    var uid by remember { mutableStateOf<String?>(null) }
    var needsOnboarding by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Perform Auth & DB checks immediately without delay
        val user = AuthApi.currentUser()
        if (user != null) {
            uid = user.uid
            val profile = DatabaseProvider.database().getGraminProfile(user.uid)
            needsOnboarding = profile?.onboardingCompleted != true
            isCheckingAuth = false
        } else {
            // If no user, jump back to Dashboard immediately
            navController.navigate(AppRoutes.DASHBOARD) { popUpTo(0) }
        }
    }

    // 2. Handle Content Rendering
    if (isCheckingAuth) {
        // Show nothing or a simple Box to prevent "flash" of empty screen
        // while the DB query (which is almost instant) runs.
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
    } else {
        // Direct transition to content
        Crossfade(targetState = needsOnboarding, label = "ContentFade") { onboarding ->
            if (onboarding) {
                GraminOnboardingScreen(onCompleted = { needsOnboarding = false })
            } else if (uid != null) {
                IndraGraminHomeScreen(
                    uid = uid!!,
                    onSettingsClick = { navController.navigate(AppRoutes.GRAMIN_SETTINGS) },
                    onMarketClick = { navController.navigate(AppRoutes.MARKET) },
                    onCropSuggestionClick = { navController.navigate(AppRoutes.CROP_SUGGESTION) },
                    onWaterManagementClick = { navController.navigate(AppRoutes.WATER_MANAGEMENT) },
                    onCommunityClick = { navController.navigate(AppRoutes.COMMUNITY) },
                    onVendorsClick = { navController.navigate(AppRoutes.VENDORS) },
                    viewModel = viewModel()
                )
            }
        }
    }
}