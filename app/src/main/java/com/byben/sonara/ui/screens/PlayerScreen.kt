package com.byben.sonara.ui.screens

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.byben.sonara.data.model.Song
import com.byben.sonara.data.model.toFormattedTime
import com.byben.sonara.ui.theme.*
import com.byben.sonara.viewmodel.PlayerState

@Composable
fun PlayerScreen(
    state: PlayerState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit,
    onFavorite: (Song) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Background glow blobs
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Top header
            PlayerHeader(onSettingsClick = onSettingsClick)

            Spacer(modifier = Modifier.height(36.dp))

            // Album art
            AlbumArtSection(
                song = state.currentSong,
                isPlaying = state.isPlaying
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Song info + favorite
            SongInfoSection(
                song = state.currentSong,
                currentIndex = state.currentIndex,
                totalSongs = state.songs.size,
                onFavorite = { state.currentSong?.let { onFavorite(it) } }
            )

            Spacer(modifier = Modifier.height(18.dp))

            PlaybackStatusSection(
                playbackSpeed = state.playbackSpeed,
                sleepTimerMinutes = state.sleepTimerMinutes,
                sleepTimerActive = state.sleepTimerActive
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Progress bar
            ProgressSection(
                position = state.currentPosition,
                duration = state.duration,
                onSeek = onSeek
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Controls
            ControlsSection(
                isPlaying = state.isPlaying,
                shuffleEnabled = state.shuffleEnabled,
                repeatMode = state.repeatMode,
                onPlayPause = onPlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious,
                onShuffle = onShuffle,
                onRepeat = onRepeat
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GlowBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top left glow
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = (-60).dp, y = 80.dp)
                .blur(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.24f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        // Right accent glow
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 120.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.24f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
    }
}

@Composable
private fun PlayerHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Now Playing",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mainkan musik favoritmu dengan gaya modern.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AlbumArtSection(song: Song?, isPlaying: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "albumScale"
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Shadow/glow behind album art
        Box(
            modifier = Modifier
                .size(250.dp)
                .blur(40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        // Album art card
        Box(
            modifier = Modifier
                .size(260.dp)
                .shadow(32.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (song?.albumArtUri != null) {
                AsyncImage(
                    model = Uri.parse(song.albumArtUri),
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder with gradient and music icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SongInfoSection(
    song: Song?,
    currentIndex: Int,
    totalSongs: Int,
    onFavorite: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song?.title ?: "No song playing",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (song != null) "${song.artist} • ${song.album}" else "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (song != null) "${currentIndex + 1} of $totalSongs" else "No queue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Favorite button with bounce animation
        val heartScale = remember { Animatable(1f) }
        val isFav = song?.isFavorite == true

        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(heartScale.value)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            onFavorite()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFav) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun ProgressSection(
    position: Long,
    duration: Long,
    onSeek: (Long) -> Unit
) {
    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = progress,
            onValueChange = { ratio ->
                if (duration > 0) onSeek((ratio * duration).toLong())
            },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = position.toFormattedTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = duration.toFormattedTime(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ControlsSection(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onShuffle: () -> Unit,
    onRepeat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle
        ControlIconButton(
            icon = Icons.Default.Shuffle,
            tint = if (shuffleEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            size = 26.dp,
            onClick = onShuffle
        )

        // Skip previous
        ControlIconButton(
            icon = Icons.Default.SkipPrevious,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 32.dp,
            onClick = onSkipPrevious
        )

        // Play / Pause — big center button
        PlayPauseButton(isPlaying = isPlaying, onClick = onPlayPause)

        // Skip next
        ControlIconButton(
            icon = Icons.Default.SkipNext,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 32.dp,
            onClick = onSkipNext
        )

        // Repeat
        val repeatIcon = when (repeatMode) {
            Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
            else -> Icons.Default.Repeat
        }
        ControlIconButton(
            icon = repeatIcon,
            tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            size = 26.dp,
            onClick = onRepeat
        )
    }
}

@Composable
private fun PlaybackStatusSection(
    playbackSpeed: Float,
    sleepTimerMinutes: Int,
    sleepTimerActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Playback speed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Playback speed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${"%.1fx".format(playbackSpeed)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = "Sleep timer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Sleep timer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (sleepTimerActive) "$sleepTimerMinutes min" else "Off",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .size(68.dp)
            .shadow(
                elevation = 20.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                spotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.9f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f))
                )
            )
            .scale(scale.value)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClick() })
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(34.dp)
        )
    }
}

@Composable
private fun ControlIconButton(
    icon: ImageVector,
    tint: Color,
    size: Dp,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
