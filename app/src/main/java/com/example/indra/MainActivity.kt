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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.indra.auth.AuthApi
import com.example.indra.data.Report
import com.example.indra.i18n.LocaleManager
import com.example.indra.navigation.AppDestination
import com.example.indra.navigation.AppRoutes
import com.example.indra.platform.PlatformSignIn
import com.example.indra.screen.*
import com.example.indra.ui.theme.INDRATheme
import com.example.indra.ui.theme.ThemeManager
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
fun App(onGoogleSignIn: () -> Unit) {
    MaterialTheme {
        var isAuthChecked by remember { mutableStateOf(false) }
        var isSignedIn by remember { mutableStateOf(false) }
        var playLoginAnim by remember { mutableStateOf(false) }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // State to hold the report data for navigation
        var reportData by remember { mutableStateOf<Report?>(null) }

        LaunchedEffect(Unit) {
            isSignedIn = AuthApi.currentUser() != null
            isAuthChecked = true
            PlatformSignIn.setCallback { success, error ->
                if (success) {
                    playLoginAnim = true
                } else {
                    println("Google Sign-In Error: $error")
                }
            }
        }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

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
                        navController.navigate(AppRoutes.DASHBOARD) {
                            popUpTo(AppRoutes.DASHBOARD) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            return@MaterialTheme
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp)
                ) {
                    // Drawer Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "I",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Jal Sanchay Mitra",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Water Conservation Partner",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        DrawerItem(
                            icon = Icons.Default.Person,
                            label = "My Profile",
                            isSelected = currentRoute == AppRoutes.PROFILE,
                            onClick = {
                                navController.navigate(AppRoutes.PROFILE)
                                scope.launch { drawerState.close() }
                            }
                        )
                        DrawerItem(
                            icon = Icons.Default.HomeWork,
                            label = "My Properties",
                            isSelected = currentRoute == AppRoutes.MY_PROPERTIES,
                            onClick = {
                                navController.navigate(AppRoutes.MY_PROPERTIES)
                                scope.launch { drawerState.close() }
                            }
                        )
                        DrawerItem(
                            icon = Icons.Default.Settings,
                            label = "Settings",
                            isSelected = currentRoute == AppRoutes.SETTINGS,
                            onClick = {
                                navController.navigate(AppRoutes.SETTINGS)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Version 1.0.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "© 2024 Jal Sanchay Mitra",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    val isDashboard = currentRoute == AppRoutes.DASHBOARD
                    val isAssess = currentRoute == AppRoutes.ASSESS
                    val isDetailedReport = currentRoute == AppRoutes.DETAILED_REPORT
                    val isCommunity = currentRoute == AppRoutes.COMMUNITY

                    if (!isCommunity) {
                        if (isDashboard) {
                            MyTopAppBar(onMenuClick = { scope.launch { drawerState.open() } })
                        } else if (isAssess || isDetailedReport) {
                            TopAppBar(
                                title = { Text("Indra") },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        } else {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (currentRoute) {
                                            AppRoutes.HISTORY -> "History"
                                            AppRoutes.LEARN_HUB -> "Learn Hub"
                                            AppRoutes.PROFILE -> "Profile"
                                            AppRoutes.SETTINGS -> "Settings"
                                            AppRoutes.MY_PROPERTIES -> "My Properties"
                                            AppRoutes.DETAILED_REPORT -> "Assessment Report"
                                            else -> ""
                                        }
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                },
                floatingActionButton = {
                    if (currentRoute == AppRoutes.DASHBOARD) {
                        FloatingActionButton(
                            onClick = { navController.navigate(AppRoutes.SERVICES) },
                            containerColor = Color(0xFF8fc7f0)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Services"
                            )
                        }
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
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                            CapsuleBottomBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD) {
                        composable(AppRoutes.DASHBOARD) {
                            DashboardScreen(onStartAssessment = {
                                navController.navigate(AppRoutes.ASSESS)
                            })
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
                                }
                            )
                        }
                        composable(AppRoutes.LEARN_HUB) { LearnHubView() }
                        composable(AppRoutes.PROFILE) { ProfileScreen(onSignedOut = { isSignedIn = false }) }
                        composable(AppRoutes.SETTINGS) { SettingsScreen() }
                        composable(AppRoutes.MY_PROPERTIES) { MyPropertiesScreen() }
                        composable(AppRoutes.MY_HOUSE){MyHouseScreen()}
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CapsuleBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val destinations = listOf(
        AppDestination.Dashboard,
        AppDestination.History,
        AppDestination.MyHouse,
        AppDestination.LearnHub,
        AppDestination.Community
    )

    // Capsule container
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(bottom = 20.dp)
            .height(70.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route

                // Animate background highlight
                val bgColor by animateColorAsState(
                    targetValue = if (selected) Color(0xFFFFE082) else Color.Transparent,
                    animationSpec = tween(500),
                    label = "bgAnim"
                )

                // Animate icon tint
                val iconTint by animateColorAsState(
                    targetValue = if (selected) Color.Black else Color.Gray,
                    animationSpec = tween(500),
                    label = "tintAnim"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable { onNavigate(destination.route) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
