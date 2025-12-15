package com.example.indra

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.indra.auth.AuthApi
import com.example.indra.data.Report
import com.example.indra.i18n.LocaleManager
import com.example.indra.navigation.AppRoutes
import com.example.indra.db.DatabaseProvider
import com.example.indra.platform.PlatformSignIn
import com.example.indra.screen.*
import com.example.indra.ui.theme.INDRATheme
import com.example.indra.ui.theme.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val wrapped = LocaleManager.wrapContext(newBase)
        super.attachBaseContext(wrapped)
    }
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PlatformSignIn.init(this)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                PlatformSignIn.handleResult(result.data)
            } else {
                PlatformSignIn.setCallback { _, error ->
                    error?.let { println("Google Sign-In failed: $it") }
                }
            }
        }
        enableEdgeToEdge()
        setContent {
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val context = LocalContext.current
            val isDarkTheme = ThemeManager.isDarkTheme(context, isSystemInDarkTheme)

            INDRATheme(darkTheme = isDarkTheme) {
                App(
                    onGoogleSignIn = {
                        PlatformSignIn.signIn(googleSignInLauncher)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        title = {
            Text(
                "INDRA",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(onGoogleSignIn: () -> Unit) {
    MaterialTheme {
        var isAuthChecked by remember { mutableStateOf(false) }
        var isSignedIn by remember { mutableStateOf(false) }
        var playLoginAnim by remember { mutableStateOf(false) }
        var needsOnboarding by remember { mutableStateOf(false) }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        var reportData by remember { mutableStateOf<Report?>(null) }

        LaunchedEffect(Unit) {
            val user = AuthApi.currentUser()
            isSignedIn = user != null
            if (user != null) {
                try {
                    val profile = DatabaseProvider.database().getUserProfile(user.uid)
                    needsOnboarding = profile?.onboardingCompleted != true
                } catch (_: Exception) {
                    needsOnboarding = true
                }
            }
            isAuthChecked = true
            PlatformSignIn.setCallback { success, error ->
                if (success) {
                    playLoginAnim = true
                } else {
                    println("Google Sign-In Error: $error")
                }
            }
        }

        LaunchedEffect(Unit) {
            SettingsNavDispatcher.events.collectLatest {
                navController.navigate(AppRoutes.ONBOARDING)
            }
        }

        if (!isAuthChecked || !isSignedIn) {
            AuthScreen(
                onSignedIn = { playLoginAnim = true },
                onGoogleSignIn = { onGoogleSignIn() }
            )
            if (playLoginAnim) {
                WaterDropExplodeAnimation(
                    onFinished = {
                        playLoginAnim = false
                        isSignedIn = true
                        // After login, determine onboarding state then navigate
                        val scope = CoroutineScope(Dispatchers.Main)
                        scope.launch {
                            val user = AuthApi.currentUser()
                            val goOnboarding = if (user != null) {
                                try {
                                    val profile = DatabaseProvider.database().getUserProfile(user.uid)
                                    profile?.onboardingCompleted != true
                                } catch (_: Exception) { true }
                            } else false
                            needsOnboarding = goOnboarding
                            navController.navigate(if (goOnboarding) AppRoutes.ONBOARDING else AppRoutes.DASHBOARD) {
                                popUpTo(AppRoutes.DASHBOARD) { inclusive = true }
                            }
                        }
                    }
                )
            }
            return@MaterialTheme
        }

        val isDashboard = currentRoute == AppRoutes.DASHBOARD
        Scaffold(
            topBar = {
                if (isDashboard) {
                    MyTopAppBar()
                }
            },
            bottomBar = {
                if (currentRoute in listOf(
                        AppRoutes.DASHBOARD,
                        AppRoutes.HISTORY,
                        AppRoutes.LEARN_HUB,
                        AppRoutes.COMMUNITY,
                        AppRoutes.MY_PROPERTIES
                    )
                ) {
                    // Bottom bar content can go here if needed in the future
                }
            }
        ) { padding ->
            val modifier = if (isDashboard) Modifier.padding(padding) else Modifier
            NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD, modifier = modifier) {
                 composable(AppRoutes.ONBOARDING) {
                    OnboardingScreen(onCompleted = {
                        needsOnboarding = false
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        }
                    })
                }
                composable(AppRoutes.DASHBOARD) {
                    DashboardScreen(
                        navController = navController,
                        onStartAssessment = { navController.navigate(AppRoutes.ASSESS) },
                        onChatClick = { navController.navigate(AppRoutes.SERVICES) },
                        onReportClick = { navController.navigate(AppRoutes.REPORT) },
                        onTipClick = { navController.navigate(AppRoutes.LEARN_HUB) },
                        onCommunityClick = { navController.navigate(AppRoutes.COMMUNITY) },
                        onSettingsClick = { navController.navigate(AppRoutes.SETTINGS) },
                        onHistoryClick = { navController.navigate(AppRoutes.HISTORY) },
                        onHelpClick = { navController.navigate(AppRoutes.HELP) },
                        onProfileClick = { navController.navigate(AppRoutes.PROFILE) },
                        onMyPropertiesClick = { navController.navigate(AppRoutes.MY_PROPERTIES) },
                        onMyHouseClick = { navController.navigate(AppRoutes.MY_HOUSE) }
                    )
                }
                composable(AppRoutes.ASSESS) {
                    AssessmentView(
                        onBackClick = { navController.popBackStack() },
                        onAssessmentComplete = { report ->
                            reportData = report
                            navController.navigate(AppRoutes.DETAILED_REPORT)
                        }
                    )
                }
                composable(AppRoutes.REPORT) {
                    reportData?.let { report ->
                        ReportView(
                            report = report,
                            onBack = { navController.popBackStack() },
                            onViewDetailed = { navController.navigate(AppRoutes.DETAILED_REPORT) }
                        )
                    }
                }
                composable(AppRoutes.DETAILED_REPORT) {
                     reportData?.let { report ->
                        DetailedReportView(
                            report = report,
                            onBack = { navController.popBackStack() },
                            onAddToProperties = { reportToAdd ->
                                val property = com.example.indra.data.Property(
                                    id = reportToAdd.id,
                                    name = reportToAdd.name,
                                    address = reportToAdd.location,
                                    latitude = 0.0,
                                    longitude = 0.0,
                                    feasibilityScore = reportToAdd.feasibilityScore,
                                    annualHarvestingPotentialLiters = reportToAdd.annualHarvestingPotentialLiters,
                                    recommendedSolution = reportToAdd.recommendedSolution,
                                    estimatedCostInr = reportToAdd.estimatedCostInr,
                                    lastAssessmentDate = reportToAdd.timestamp,
                                    propertyType = "Residential",
                                    roofArea = reportToAdd.roofArea,
                                    openSpace = reportToAdd.openSpace,
                                    dwellers = reportToAdd.dwellers
                                )
                                val scope = CoroutineScope(Dispatchers.Main)
                                scope.launch {
                                    try {
                                        com.example.indra.data.PropertyRepositoryProvider.repository().addProperty(property)
                                        navController.navigate(AppRoutes.MY_PROPERTIES)
                                    } catch (_: Exception) {}
                                }
                            }
                        )
                    }
                }
                composable(AppRoutes.COMMUNITY) { CommunityScreen() }
                composable(AppRoutes.SERVICES) { ServicesScreen(onBackClick = { navController.popBackStack() }) }
                composable(AppRoutes.HISTORY) {
                    HistoryView(
                        onReportClick = { report ->
                            reportData = report
                            navController.navigate(AppRoutes.DETAILED_REPORT)
                        },
                        onBackClick = {navController.popBackStack()}
                    )
                }
                composable(AppRoutes.HELP) { HelpScreen(onBackClick = { navController.popBackStack() }) }
                composable(AppRoutes.LEARN_HUB) { LearnHubView(onBackClick = { navController.popBackStack() }) }
                composable(AppRoutes.PROFILE) { ProfileScreen(onSignedOut = { isSignedIn = false }) }
                composable(AppRoutes.SETTINGS) { SettingsScreen() }
                composable(AppRoutes.MY_PROPERTIES) { MyPropertiesScreen(
                    mapboxAccessToken = "pk.eyJ1IjoiYXJ5YW5iYWdsYW5lIiwiYSI6ImNtaDRpZWoxaTB4MjcyanI1c3BoZDVyY3AifQ.SWnkEA01n_kcFTSfUaW2uA"
                ) }
                composable(AppRoutes.MY_HOUSE){ MyHouseScreen() }
            }
        }
    }
}
