package com.example.sikeren

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.sikeren.model.*
import com.example.sikeren.network.ApiClient
import com.example.sikeren.theme.SikerenTheme
import com.example.sikeren.ui.components.BottomFloatingDock
import com.example.sikeren.ui.screens.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SikerenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SikerenApp()
                }
            }
        }
    }
}

@Composable
fun SikerenApp() {
    val coroutineScope = rememberCoroutineScope()

    var isLoggedIn by remember { mutableStateOf(false) }
    var user by remember { mutableStateOf(UserProfile()) }
    var token by remember { mutableStateOf("") }

    var currentTab by remember { mutableStateOf(0) } // 0: Home, 1: Kegiatan, 2: QR Absensi, 3: Profil
    var selectedKegiatan by remember { mutableStateOf<KegiatanItem?>(null) }

    var dailySummary by remember { mutableStateOf(DailySummary()) }
    var kegiatanStats by remember { mutableStateOf(KegiatanStats()) }
    var kegiatanList by remember { mutableStateOf<List<KegiatanItem>>(emptyList()) }

    // Load initial data in parallel
    LaunchedEffect(isLoggedIn, token) {
        if (isLoggedIn) {
            coroutineScope.launch {
                val summaryDeferred = async { ApiClient.fetchDailySummary(token) }
                val kegiatanDeferred = async { ApiClient.fetchKegiatan(token, type = "all") }

                val summary = summaryDeferred.await()
                val kegiatanResult = kegiatanDeferred.await()
                dailySummary = summary
                kegiatanStats = kegiatanResult.first
                kegiatanList = kegiatanResult.second
            }
        }
    }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { isLoggedIn = true },
            onDirectLogin = { username, password ->
                coroutineScope.launch {
                    val res = ApiClient.login(username, password)
                    res.onSuccess { (profile, userToken) ->
                        user = profile
                        token = userToken
                        isLoggedIn = true
                    }
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            // Soft animated transition between screens (crossfade without jerky jumps)
            AnimatedContent(
                targetState = if (selectedKegiatan != null) -1 else currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "screenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    -1 -> KegiatanDetailScreen(
                        item = selectedKegiatan!!,
                        onBack = { selectedKegiatan = null }
                    )
                    0 -> HomeScreen(
                        user = user,
                        dailySummary = dailySummary,
                        onNavigateToQr = { currentTab = 2 },
                        onNavigateToKegiatan = { currentTab = 1 },
                        onNavigateToProfile = { currentTab = 3 },
                        onSelectKegiatan = { selectedKegiatan = it }
                    )
                    1 -> KegiatanScreen(
                        stats = kegiatanStats,
                        items = kegiatanList,
                        onBack = { currentTab = 0 },
                        onSelectFilter = { filter ->
                            coroutineScope.launch {
                                val (stats, list) = ApiClient.fetchKegiatan(token, type = filter)
                                kegiatanStats = stats
                                kegiatanList = list
                            }
                        },
                        onSelectKegiatan = { selectedKegiatan = it }
                    )
                    2 -> QrAbsensiScreen(
                        user = user,
                        onBack = { currentTab = 0 },
                        onSubmitPresensi = { status, qrCode, ketDinas ->
                            coroutineScope.launch {
                                ApiClient.submitPresensi(token, status, qrCode, ketDinas)
                                dailySummary = ApiClient.fetchDailySummary(token)
                            }
                        }
                    )
                    3 -> ProfileScreen(
                        user = user,
                        onBack = { currentTab = 0 },
                        onLogout = {
                            isLoggedIn = false
                            currentTab = 0
                        }
                    )
                }
            }

            // Minimalist Floating Bottom Dock (hidden in detail view, visible in main tabs)
            if (selectedKegiatan == null) {
                BottomFloatingDock(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
