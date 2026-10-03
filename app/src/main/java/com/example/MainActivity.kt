package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Yemen4GScreen
import com.example.ui.Yemen4GViewModel
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.InquiryScreen
import com.example.ui.screens.ModemGatewayScreen
import com.example.ui.screens.OfficialPortalScreen
import com.example.ui.screens.PackagesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Yemen4GAccentGold
import com.example.ui.theme.Yemen4GDarkNavy
import com.example.ui.theme.Yemen4GPrimaryBlue
import com.example.ui.theme.Yemen4GPrimaryLight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: Yemen4GViewModel = viewModel()
                    val state by viewModel.uiState.collectAsStateWithLifecycle()

                    // Handle back press: if on a sub-screen, return to Inquiry screen
                    if (state.currentScreen != Yemen4GScreen.INQUIRY) {
                        BackHandler {
                            viewModel.setScreen(Yemen4GScreen.INQUIRY)
                        }
                    }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Yemen4GDarkNavy),
                        containerColor = Yemen4GDarkNavy,
                        bottomBar = {
                            Yemen4GBottomBar(
                                currentScreen = state.currentScreen,
                                onSelectScreen = { viewModel.setScreen(it) }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (state.currentScreen) {
                                Yemen4GScreen.INQUIRY -> {
                                    InquiryScreen(
                                        state = state,
                                        viewModel = viewModel
                                    )
                                }
                                Yemen4GScreen.OFFICIAL_PORTAL -> {
                                    OfficialPortalScreen(
                                        currentModemNumber = state.modemNumber
                                    )
                                }
                                Yemen4GScreen.PACKAGES -> {
                                    PackagesScreen(
                                        packages = state.packages
                                    )
                                }
                                Yemen4GScreen.MODEM_GATEWAY -> {
                                    ModemGatewayScreen(
                                        routers = state.routers
                                    )
                                }
                                Yemen4GScreen.DEVELOPER_INFO -> {
                                    AboutScreen()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Yemen4GBottomBar(
    currentScreen: Yemen4GScreen,
    onSelectScreen: (Yemen4GScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier.testTag("yemen4g_bottom_nav"),
        containerColor = Color(0xFF071428),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Yemen4GScreen.INQUIRY,
            onClick = { onSelectScreen(Yemen4GScreen.INQUIRY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "الاستعلام",
                    tint = if (currentScreen == Yemen4GScreen.INQUIRY) Yemen4GAccentGold else Color(0xFF94A3B8)
                )
            },
            label = {
                Text(
                    text = "الاستعلام",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Yemen4GScreen.INQUIRY) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentScreen == Yemen4GScreen.INQUIRY) Yemen4GAccentGold else Color(0xFF94A3B8)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFF132A4A)
            )
        )

        NavigationBarItem(
            selected = currentScreen == Yemen4GScreen.OFFICIAL_PORTAL,
            onClick = { onSelectScreen(Yemen4GScreen.OFFICIAL_PORTAL) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "الموقع الرسمي",
                    tint = if (currentScreen == Yemen4GScreen.OFFICIAL_PORTAL) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            label = {
                Text(
                    text = "الموقع الرسمي",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Yemen4GScreen.OFFICIAL_PORTAL) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentScreen == Yemen4GScreen.OFFICIAL_PORTAL) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFF132A4A)
            )
        )

        NavigationBarItem(
            selected = currentScreen == Yemen4GScreen.PACKAGES,
            onClick = { onSelectScreen(Yemen4GScreen.PACKAGES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.ViewList,
                    contentDescription = "الباقات",
                    tint = if (currentScreen == Yemen4GScreen.PACKAGES) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            label = {
                Text(
                    text = "الباقات",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Yemen4GScreen.PACKAGES) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentScreen == Yemen4GScreen.PACKAGES) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFF132A4A)
            )
        )

        NavigationBarItem(
            selected = currentScreen == Yemen4GScreen.MODEM_GATEWAY,
            onClick = { onSelectScreen(Yemen4GScreen.MODEM_GATEWAY) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = "المودم",
                    tint = if (currentScreen == Yemen4GScreen.MODEM_GATEWAY) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            label = {
                Text(
                    text = "المودم",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Yemen4GScreen.MODEM_GATEWAY) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentScreen == Yemen4GScreen.MODEM_GATEWAY) Yemen4GPrimaryLight else Color(0xFF94A3B8)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFF132A4A)
            )
        )

        NavigationBarItem(
            selected = currentScreen == Yemen4GScreen.DEVELOPER_INFO,
            onClick = { onSelectScreen(Yemen4GScreen.DEVELOPER_INFO) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "المطور",
                    tint = if (currentScreen == Yemen4GScreen.DEVELOPER_INFO) Yemen4GAccentGold else Color(0xFF94A3B8)
                )
            },
            label = {
                Text(
                    text = "المطور",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Yemen4GScreen.DEVELOPER_INFO) FontWeight.Bold else FontWeight.Normal,
                    color = if (currentScreen == Yemen4GScreen.DEVELOPER_INFO) Yemen4GAccentGold else Color(0xFF94A3B8)
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color(0xFF132A4A)
            )
        )
    }
}
