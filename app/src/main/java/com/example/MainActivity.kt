package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AddEditHabitScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    HabitTrackerApp(viewModel = habitViewModel)
                }
            }
        }
    }
}

object Routes {
    const val HOME = "home"
    const val ADD_HABIT = "add_habit"
    const val EDIT_HABIT = "edit_habit/{habitId}"
    const val HABIT_DETAIL = "habit_detail/{habitId}"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"

    fun editHabitRoute(habitId: Long) = "edit_habit/$habitId"
    fun habitDetailRoute(habitId: Long) = "habit_detail/$habitId"
}

@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for celebration events
    LaunchedEffect(Unit) {
        viewModel.celebrationEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAddHabit = { navController.navigate(Routes.ADD_HABIT) },
                    onNavigateToHabitDetail = { habitId ->
                        navController.navigate(Routes.habitDetailRoute(habitId))
                    },
                    onNavigateToStats = { navController.navigate(Routes.STATISTICS) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
                )
            }

            composable(Routes.ADD_HABIT) {
                AddEditHabitScreen(
                    habitId = null,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.EDIT_HABIT,
                arguments = listOf(navArgument("habitId") { type = NavType.LongType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
                AddEditHabitScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.HABIT_DETAIL,
                arguments = listOf(navArgument("habitId") { type = NavType.LongType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getLong("habitId") ?: 0L
                HabitDetailScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Routes.editHabitRoute(id)) }
                )
            }

            composable(Routes.STATISTICS) {
                StatisticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
