package com.dailyrunner.drivertracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dailyrunner.drivertracker.DailyRunnerApp
import com.dailyrunner.drivertracker.ui.screens.DashboardScreen
import com.dailyrunner.drivertracker.ui.screens.HistoryScreen
import com.dailyrunner.drivertracker.ui.screens.SettingsScreen
import com.dailyrunner.drivertracker.ui.screens.WeeklyChequesScreen
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Receipt
import com.dailyrunner.drivertracker.ui.theme.DailyRunnerTheme
import com.dailyrunner.drivertracker.ui.viewmodel.DashboardViewModel
import com.dailyrunner.drivertracker.ui.viewmodel.HistoryViewModel
import com.dailyrunner.drivertracker.ui.viewmodel.SettingsViewModel
import com.dailyrunner.drivertracker.ui.viewmodel.WeeklyChequesViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Cheques : Screen("cheques", "Payments", Icons.Default.AttachMoney)
    object History : Screen("history", "History", Icons.Default.Receipt)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as DailyRunnerApp
        val repository = app.repository

        setContent {
            DailyRunnerTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                val dashboardViewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.Factory(repository)
                )
                val weeklyChequesViewModel: WeeklyChequesViewModel = viewModel(
                    factory = WeeklyChequesViewModel.Factory(repository)
                )
                val historyViewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModel.Factory(repository)
                )
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(repository)
                )

                val items = listOf(
                    Screen.Dashboard,
                    Screen.Cheques,
                    Screen.History,
                    Screen.Settings
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (currentRoute == screen.route) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable(Screen.Cheques.route) {
                            WeeklyChequesScreen(
                                viewModel = weeklyChequesViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(
                                viewModel = historyViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }
        }
    }
}
