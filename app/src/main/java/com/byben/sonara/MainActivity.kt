package com.byben.sonara

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.byben.sonara.ui.screens.LibraryScreen
import com.byben.sonara.ui.screens.PlayerScreen
import com.byben.sonara.ui.screens.SettingsScreen
import com.byben.sonara.ui.theme.*
import com.byben.sonara.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.refreshSongs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Draws behind status bar & nav bar
        requestAudioPermission()

        setContent {
            SonaraTheme {
                SonaraApp(viewModel = viewModel)
            }
        }
    }

    private fun requestAudioPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        }
    }
}

@Composable
fun SonaraApp(viewModel: PlayerViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PurpleDark,
        bottomBar = {
            BottomNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        // innerPadding sudah include status bar + nav bar dari Scaffold
        when (selectedTab) {
            0 -> PlayerScreen(
                state = state,
                onPlayPause = viewModel::togglePlayPause,
                onSkipNext = viewModel::skipNext,
                onSkipPrevious = viewModel::skipPrevious,
                onSeek = viewModel::seekTo,
                onShuffle = viewModel::toggleShuffle,
                onRepeat = viewModel::toggleRepeat,
                onFavorite = viewModel::toggleFavorite,
                onSettingsClick = { selectedTab = 2 },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding()
            )
            1 -> LibraryScreen(
                state = state,
                onSongClick = { song, index ->
                    viewModel.playSong(song, index)
                    selectedTab = 0
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding()
            )
            2 -> SettingsScreen(
                playbackSpeed = state.playbackSpeed,
                sleepTimerMinutes = state.sleepTimerMinutes,
                sleepTimerActive = state.sleepTimerActive,
                onPlaybackSpeedChange = viewModel::setPlaybackSpeed,
                onSetSleepTimer = viewModel::setSleepTimer,
                onCancelSleepTimer = viewModel::cancelSleepTimer,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .systemBarsPadding()
            )
        }
    }
}

@Composable
fun BottomNavBar(
    selectedTab: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceCard.copy(alpha = 0.97f),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Player",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Player", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PurpleAccent,
                selectedTextColor = PurpleAccent,
                unselectedIconColor = OnSurfaceMuted,
                unselectedTextColor = OnSurfaceMuted,
                indicatorColor = PurpleMid
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = "Library",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Library", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PurpleAccent,
                selectedTextColor = PurpleAccent,
                unselectedIconColor = OnSurfaceMuted,
                unselectedTextColor = OnSurfaceMuted,
                indicatorColor = PurpleMid
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Settings", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PurpleAccent,
                selectedTextColor = PurpleAccent,
                unselectedIconColor = OnSurfaceMuted,
                unselectedTextColor = OnSurfaceMuted,
                indicatorColor = PurpleMid
            )
        )
    }
}
