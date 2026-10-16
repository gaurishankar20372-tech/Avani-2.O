package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.ActionHistoryScreen
import com.example.ui.screens.BuildCodeScreen
import com.example.ui.screens.CapabilitiesScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrivacySettingsScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CosmicCardSurface
import com.example.ui.theme.CosmicNavy
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.SoftAmethyst
import com.example.ui.viewmodel.AssistantViewModel

enum class MainDestination(val label: String) {
    HOME("Home"),
    CHAT("Chat"),
    BUILD("Build"),
    PROJECTS("Projects"),
    MORE("More")
}

enum class MoreSubTab(val label: String) {
    TASKS("Tasks"),
    CAPABILITIES("Capabilities"),
    HISTORY("History"),
    SETTINGS("Privacy & Settings")
}

@Composable
fun MainAppScaffold(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
    var currentMoreSubTab by remember { mutableStateOf(MoreSubTab.TASKS) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = CosmicCardSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentDestination == MainDestination.HOME,
                    onClick = { currentDestination = MainDestination.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = SoftAmethyst,
                        unselectedTextColor = SoftAmethyst,
                        indicatorColor = NeonViolet.copy(alpha = 0.25f)
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == MainDestination.CHAT,
                    onClick = { currentDestination = MainDestination.CHAT },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = SoftAmethyst,
                        unselectedTextColor = SoftAmethyst,
                        indicatorColor = NeonViolet.copy(alpha = 0.25f)
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == MainDestination.BUILD,
                    onClick = { currentDestination = MainDestination.BUILD },
                    icon = { Icon(Icons.Default.Code, contentDescription = "Build") },
                    label = { Text("Build", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = SoftAmethyst,
                        unselectedTextColor = SoftAmethyst,
                        indicatorColor = NeonViolet.copy(alpha = 0.25f)
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == MainDestination.PROJECTS,
                    onClick = { currentDestination = MainDestination.PROJECTS },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Projects") },
                    label = { Text("Projects", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = SoftAmethyst,
                        unselectedTextColor = SoftAmethyst,
                        indicatorColor = NeonViolet.copy(alpha = 0.25f)
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == MainDestination.MORE,
                    onClick = { currentDestination = MainDestination.MORE },
                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
                    label = { Text("More", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ElectricCyan,
                        selectedTextColor = ElectricCyan,
                        unselectedIconColor = SoftAmethyst,
                        unselectedTextColor = SoftAmethyst,
                        indicatorColor = NeonViolet.copy(alpha = 0.25f)
                    )
                )
            }
        },
        containerColor = CosmicNavy,
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when (currentDestination) {
                MainDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { currentDestination = MainDestination.CHAT }
                )
                MainDestination.CHAT -> ChatScreen(
                    viewModel = viewModel
                )
                MainDestination.BUILD -> BuildCodeScreen(
                    viewModel = viewModel
                )
                MainDestination.PROJECTS -> ProjectsScreen(
                    viewModel = viewModel
                )
                MainDestination.MORE -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = currentMoreSubTab.ordinal,
                            containerColor = CosmicCardSurface,
                            contentColor = Color.White,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentMoreSubTab.ordinal]),
                                    color = ElectricCyan
                                )
                            },
                            edgePadding = 12.dp
                        ) {
                            MoreSubTab.values().forEach { tab ->
                                Tab(
                                    selected = currentMoreSubTab == tab,
                                    onClick = { currentMoreSubTab = tab },
                                    text = {
                                        Text(
                                            text = tab.label,
                                            color = if (currentMoreSubTab == tab) ElectricCyan else SoftAmethyst,
                                            fontSize = 13.sp
                                        )
                                    }
                                )
                            }
                        }

                        when (currentMoreSubTab) {
                            MoreSubTab.TASKS -> TasksScreen(viewModel = viewModel)
                            MoreSubTab.CAPABILITIES -> CapabilitiesScreen(viewModel = viewModel)
                            MoreSubTab.HISTORY -> ActionHistoryScreen(viewModel = viewModel)
                            MoreSubTab.SETTINGS -> PrivacySettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            // Universal Risk Confirmation Dialog
            uiState.pendingConfirmation?.let { pending ->
                ConfirmationDialog(
                    pendingAction = pending,
                    onConfirm = { viewModel.confirmPendingAction() },
                    onDismiss = { viewModel.rejectPendingAction() }
                )
            }
        }
    }
}
