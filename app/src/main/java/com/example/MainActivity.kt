package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.components.PinLockDialog
import com.example.ui.navigation.ExpiryRoutes
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.detail.DocumentDetailScreen
import com.example.ui.screens.editor.DocumentEditScreen
import com.example.ui.screens.privacy.PrivacyVaultScreen
import com.example.ui.screens.renewal.RenewalHubScreen
import com.example.ui.screens.scanner.DocumentScannerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpiryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ExpiryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ExpiryApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ExpiryApp(viewModel: ExpiryViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Snackbar messages from ViewModel
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("bottom_nav_bar")
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
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
                    viewModel = viewModel,
                    uiState = uiState,
                    onNavigateToDetail = { docId ->
                        navController.navigate(ExpiryRoutes.documentDetail(docId))
                    },
                    onNavigateToEditor = { docId ->
                        navController.navigate(ExpiryRoutes.documentEditor(docId))
                    },
                    onNavigateToScanner = {
                        navController.navigate(ExpiryRoutes.DOCUMENT_SCANNER)
                    }
                )
            }

            composable(Screen.Timeline.route) {
                com.example.ui.screens.timeline.TimelineScreen(
                    uiState = uiState,
                    onNavigateToDetail = { docId ->
                        navController.navigate(ExpiryRoutes.documentDetail(docId))
                    }
                )
            }

            composable(Screen.RenewalHub.route) {
                RenewalHubScreen()
            }

            composable(Screen.VaultPrivacy.route) {
                PrivacyVaultScreen(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }

            composable(
                route = ExpiryRoutes.DOCUMENT_DETAIL,
                arguments = listOf(
                    navArgument("docId") { type = NavType.LongType }
                )
            ) { backStack ->
                val docId = backStack.arguments?.getLong("docId") ?: 0L
                DocumentDetailScreen(
                    docId = docId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(ExpiryRoutes.documentEditor(id))
                    }
                )
            }

            composable(
                route = ExpiryRoutes.DOCUMENT_EDITOR,
                arguments = listOf(
                    navArgument("docId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStack ->
                val docId = backStack.arguments?.getLong("docId") ?: 0L
                DocumentEditScreen(
                    docId = docId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToScanner = {
                        navController.navigate(ExpiryRoutes.DOCUMENT_SCANNER)
                    }
                )
            }

            composable(ExpiryRoutes.DOCUMENT_SCANNER) {
                DocumentScannerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onApplyToNewDocument = {
                        navController.navigate(ExpiryRoutes.documentEditor(0L)) {
                            popUpTo(Screen.Dashboard.route)
                        }
                    }
                )
            }
        }

        // Show PIN Lock screen overlay if vault is locked
        if (uiState.isVaultLocked) {
            PinLockDialog(
                onUnlockAttempt = { pin ->
                    viewModel.unlockVaultWithPin(pin)
                }
            )
        }
    }
}
