package com.abosmra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Factory
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abosmra.app.ui.AboSmraTheme
import com.abosmra.app.ui.Blue
import com.abosmra.app.ui.BlueSoft
import com.abosmra.app.ui.CustomerDetailScreen
import com.abosmra.app.ui.CustomersScreen
import com.abosmra.app.ui.FactoriesScreen
import com.abosmra.app.ui.FactoryDetailScreen
import com.abosmra.app.ui.HomeScreen
import com.abosmra.app.ui.InkMid
import com.abosmra.app.ui.Navy
import com.abosmra.app.ui.PageBg
import com.abosmra.app.ui.ReportsScreen
import com.abosmra.app.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            AboSmraTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot()
                }
            }
        }
    }
}

private class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "الرئيسية", Icons.Rounded.Home),
    Tab("customers", "الزبائن", Icons.Rounded.Groups),
    Tab("factories", "المعامل", Icons.Rounded.Factory),
    Tab("reports", "التقارير", Icons.Rounded.BarChart),
    Tab("settings", "الإعدادات", Icons.Rounded.Settings)
)

@Composable
fun AppRoot() {
    val vm: AppViewModel = viewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBar = tabs.any { it.route == route }

    fun goTab(r: String) {
        nav.navigate(r) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = PageBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = { goTab(t.route) },
                            icon = { Icon(t.icon, t.label) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Navy,
                                selectedTextColor = Navy,
                                indicatorColor = BlueSoft,
                                unselectedIconColor = InkMid,
                                unselectedTextColor = InkMid
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier
                .padding(padding)
                .then(if (showBar) Modifier else Modifier.navigationBarsPadding())
        ) {
            composable("home") {
                HomeScreen(
                    state, vm,
                    onOpenCustomer = { nav.navigate("customer/$it") },
                    goCustomers = { goTab("customers") },
                    goReports = { goTab("reports") }
                )
            }
            composable("customers") {
                CustomersScreen(state, vm) { nav.navigate("customer/$it") }
            }
            composable("factories") {
                FactoriesScreen(state, vm) { nav.navigate("factory/$it") }
            }
            composable("reports") { ReportsScreen(state) }
            composable("settings") { SettingsScreen(state, vm) }
            composable(
                "customer/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                CustomerDetailScreen(entry.arguments?.getLong("id") ?: 0L, state, vm) { nav.popBackStack() }
            }
            composable(
                "factory/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                FactoryDetailScreen(entry.arguments?.getLong("id") ?: 0L, state, vm) { nav.popBackStack() }
            }
        }
    }
}
