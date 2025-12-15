
package com.example.indra.navigation





import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.indra.data.Report
import com.example.indra.screen.AssessmentView
import com.example.indra.screen.CommunityScreen
import com.example.indra.screen.DashboardScreen
import com.example.indra.screen.HistoryView
import com.example.indra.screen.LearnHubView
import com.example.indra.screen.MyHouseScreen
import com.example.indra.screen.MyPropertiesScreen
import com.example.indra.screen.ProfileScreen
import com.example.indra.screen.ReportCard
import com.example.indra.screen.ReportView
import com.example.indra.screen.ServicesScreen
import com.example.indra.screen.SettingsScreen


// Using a sealed class to define destinations akin to a NavGraph.




// Define routes as constants for type safety and clarity.
object AppRoutes {
    const val DASHBOARD = "dashboard"
    const val ONBOARDING = "onboarding"
    const val ASSESS = "assess"
    const val COMMUNITY = "community"
    const val SERVICES = "Jal Sanchay Mitra"
    const val HISTORY = "history"
    const val LEARN_HUB = "learn_hub"
    const val REPORT = "report"
    const val DETAILED_REPORT = "detailed_report"
    const val REPORT_CARD = "report_card"
    const val PROFILE = "profile"
    const val MY_PROPERTIES = "my_properties"
    const val SETTINGS = "settings"
    const val MY_HOUSE = "my_house"
    const val HELP = "help"

}

/**
 * A sealed class defining all screen destinations in the app.
 * This class serves as a data model for navigation items and their metadata.
 * The actual navigation logic and Composable content are handled in the NavHost.
 */
sealed class AppDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Main screens accessible from the bottom bar
    data object MyHouse : AppDestination(AppRoutes.MY_HOUSE, "Home", Icons.Default.Home)
    data object Dashboard : AppDestination(AppRoutes.DASHBOARD, "Dashboard", Icons.Default.Dashboard)
    data object History : AppDestination(AppRoutes.HISTORY, "History", Icons.Default.History)
    data object LearnHub : AppDestination(AppRoutes.LEARN_HUB, "Learn Hub", Icons.Default.MenuBook)
    data object Community : AppDestination(AppRoutes.COMMUNITY, "Community", Icons.Default.People)

    // Other destinations
    data object Assess : AppDestination(AppRoutes.ASSESS, "Assess", Icons.Default.Calculate)
    data object Services : AppDestination(AppRoutes.SERVICES, "Services", Icons.Default.Build)
    data object Report : AppDestination(AppRoutes.REPORT, "Report", Icons.Default.Receipt)
    data object ReportCardDest : AppDestination(AppRoutes.REPORT_CARD, "Report Card", Icons.Default.Assignment)
    data object Help : AppDestination(AppRoutes.HELP, "Help", Icons.Default.Help)


    // Screens accessible from the navigation drawer
    data object Profile : AppDestination(AppRoutes.PROFILE, "My Profile", Icons.Default.Person)
    data object MyProperties : AppDestination(AppRoutes.MY_PROPERTIES, "My Properties", Icons.Default.HomeWork)
    data object Settings : AppDestination(AppRoutes.SETTINGS, "Settings", Icons.Default.Settings)

}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD) {
        composable(AppRoutes.DASHBOARD) {
            DashboardScreen(
                navController = navController,
                onStartAssessment = { navController.navigate(AppRoutes.ASSESS) },
                onChatClick = { /* TODO */ },
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
            AssessmentView(onBackClick = { navController.popBackStack() }, onAssessmentComplete = { navController.navigate(AppRoutes.REPORT) })
        }
        composable(AppRoutes.COMMUNITY) {
            CommunityScreen()
        }
        composable(AppRoutes.SERVICES) {
            ServicesScreen(onBackClick = { navController.popBackStack() })
        }
        composable(AppRoutes.HISTORY) {
            HistoryView(onBackClick = { navController.popBackStack() })
        }
        composable(AppRoutes.LEARN_HUB) {
            LearnHubView(onBackClick = { navController.popBackStack() })
        }
        composable(AppRoutes.REPORT) {
            // You might need to pass a report ID here in a real app
            ReportView(report = Report(), onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.PROFILE) {
            ProfileScreen()
        }
        composable(AppRoutes.MY_PROPERTIES) {
            MyPropertiesScreen(
                mapboxAccessToken = TODO()
            )
        }
        composable(AppRoutes.SETTINGS) {
            SettingsScreen()
        }
        composable(AppRoutes.MY_HOUSE) {
            MyHouseScreen()
        }
    }
}
