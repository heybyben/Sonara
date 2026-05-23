package com.byben.sonara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.byben.sonara.ui.theme.OnSurface
import com.byben.sonara.ui.theme.OnSurfaceMuted
import com.byben.sonara.ui.theme.PinkAccent
import com.byben.sonara.ui.theme.PurpleDark
import com.byben.sonara.ui.theme.PurpleDeep
import com.byben.sonara.ui.theme.PurpleAccent
import com.byben.sonara.ui.theme.SurfaceCard

@Composable
fun SettingsScreen(
    playbackSpeed: Float,
    sleepTimerMinutes: Int,
    sleepTimerActive: Boolean,
    onPlaybackSpeedChange: (Float) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDarkMode by remember { mutableStateOf(true) }
    var bass by remember { mutableStateOf(0.45f) }
    var mid by remember { mutableStateOf(0.55f) }
    var treble by remember { mutableStateOf(0.4f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleDeep, PurpleDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sesuaikan pengalaman audio dan playback.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Atur bass, mid, dan treble untuk suara yang lebih hidup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    EqualizerSlider(label = "Bass", value = bass, onValueChange = { bass = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    EqualizerSlider(label = "Mid", value = mid, onValueChange = { mid = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    EqualizerSlider(label = "Treble", value = treble, onValueChange = { treble = it })
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Playback",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kontrol kecepatan pemutaran dan timer tidur.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = PurpleAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Playback speed",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                            Text(
                                text = "${"%.1fx".format(playbackSpeed)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                    }
                    Slider(
                        value = playbackSpeed,
                        onValueChange = onPlaybackSpeedChange,
                        valueRange = 0.75f..1.5f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            activeTrackColor = PurpleAccent,
                            inactiveTrackColor = OnSurfaceMuted.copy(alpha = 0.25f),
                            thumbColor = PinkAccent
                        )
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sleep timer",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface
                            )
                            Text(
                                text = if (sleepTimerActive) "Berhenti dalam $sleepTimerMinutes menit" else "Mati setelah beberapa menit",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceMuted
                            )
                        }
                        if (sleepTimerActive) {
                            Button(onClick = onCancelSleepTimer) {
                                Text(text = "Batal")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SmallTimerButton("5m", modifier = Modifier.weight(1f)) { onSetSleepTimer(5) }
                        SmallTimerButton("10m", modifier = Modifier.weight(1f)) { onSetSleepTimer(10) }
                        SmallTimerButton("15m", modifier = Modifier.weight(1f)) { onSetSleepTimer(15) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Audio output",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pilih kualitas playback untuk perangkatmu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SmallTimerButton("Auto") { }
                        SmallTimerButton("High") { }
                        SmallTimerButton("Balanced") { }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                color = SurfaceCard.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode gelap",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aktifkan tampilan gelap untuk suasana musik malam.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceMuted
                        )
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PurpleAccent,
                            uncheckedThumbColor = OnSurfaceMuted,
                            checkedTrackColor = PinkAccent.copy(alpha = 0.35f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Tentang Aplikasi",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sonara Player • Versi 1.0 • Musik keren setiap hari",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SmallTimerButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

@Composable
private fun EqualizerSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = androidx.compose.material3.SliderDefaults.colors(
                activeTrackColor = PurpleAccent,
                inactiveTrackColor = OnSurfaceMuted.copy(alpha = 0.25f),
                thumbColor = PinkAccent
            )
        )
    }
}
