package com.raul_t.myapplication.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raul_t.myapplication.navigation.Screen
import com.raul_t.myapplication.presentation.bluetoothSensorEmulator.SensorEmitterScreen
import com.raul_t.myapplication.presentation.heart.HeartRateScreen
import com.raul_t.myapplication.ui.theme.LightSuccessGreen
import com.raul_t.myapplication.ui.theme.SuccessGreen

@Composable
fun MainScreen(
    onRequestNotificationPermission: () -> Unit
) {
    val navController = rememberNavController()
    val screens = listOf(
        Screen.HeartRate,
        Screen.BluetoothEmitter
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = LightSuccessGreen
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = screen.label,
                                modifier = if (screen == Screen.HeartRate) Modifier.padding(2.dp) else Modifier
                            )
                        },
                        label = {
                            Text(
                                text = screen.label,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selected = selected,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SuccessGreen,
                            selectedTextColor = SuccessGreen,
                            indicatorColor = LightSuccessGreen.copy(alpha = 0.4f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HeartRate.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.HeartRate.route) {
                HeartRateScreen(onRequestNotificationPermission = onRequestNotificationPermission)
            }
            composable(Screen.BluetoothEmitter.route) {
                SensorEmitterScreen()
            }
        }
    }
}
