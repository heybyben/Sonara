package com.byben.sonara.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byben.sonara.ui.theme.OnSurface
import com.byben.sonara.ui.theme.OnSurfaceMuted
import com.byben.sonara.ui.theme.PinkAccent
import com.byben.sonara.ui.theme.PurpleDark
import com.byben.sonara.ui.theme.PurpleDeep
import com.byben.sonara.ui.theme.PurpleAccent
import com.byben.sonara.ui.theme.SurfaceCard

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Atur pengalaman musik dan lihat informasi aplikasi.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingsCard(
                icon = Icons.Default.Info,
                title = "Tentang Aplikasi",
                subtitle = "Sonara Player • Versi 1.0 • Musik keren setiap hari"
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(
                icon = Icons.Default.Equalizer,
                title = "Equalizer",
                subtitle = "Atur bass, mid, dan treble untuk suara yang lebih hidup"
            ) {
                EqualizerControls(
                    bass = bass,
                    mid = mid,
                    treble = treble,
                    onBassChange = { bass = it },
                    onMidChange = { mid = it },
                    onTrebleChange = { treble = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsCard(
                icon = Icons.Default.Settings,
                title = "Tampilan dan Tema",
                subtitle = "Mode gelap otomatis dan gaya antarmuka"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mode gelap",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
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
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable (() -> Unit)? = null
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PurpleAccent,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceMuted
                    )
                }
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = OnSurfaceMuted.copy(alpha = 0.12f))
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun EqualizerControls(
    bass: Float,
    mid: Float,
    treble: Float,
    onBassChange: (Float) -> Unit,
    onMidChange: (Float) -> Unit,
    onTrebleChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        EqualizerSlider(label = "Bass", value = bass, onValueChange = onBassChange)
        Spacer(modifier = Modifier.height(12.dp))
        EqualizerSlider(label = "Mid", value = mid, onValueChange = onMidChange)
        Spacer(modifier = Modifier.height(12.dp))
        EqualizerSlider(label = "Treble", value = treble, onValueChange = onTrebleChange)
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
