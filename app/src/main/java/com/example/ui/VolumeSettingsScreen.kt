package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetConfig
import com.example.data.PresetSetting
import com.example.ui.theme.BorderDark
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VolumeSettingsScreen(
    presetConfig: PresetConfig,
    onUpdateConfig: (PresetConfig) -> Unit,
    onResetConfig: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Konfigurasi Profil",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Laraskan nilai asas untuk 3 butang pantas",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = onResetConfig,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderDark, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Nilai Asal",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Kad Mod Senyap
        item {
            PresetConfigCard(
                title = "Mod Senyap",
                description = "Media senyap, saluran lain kekal bersedia",
                icon = Icons.Default.NotificationsOff,
                accentColor = RoseGlow,
                setting = presetConfig.silent,
                onSettingChanged = { updated ->
                    onUpdateConfig(presetConfig.copy(silent = updated))
                }
            )
        }

        // Kad Mod Sederhana
        item {
            PresetConfigCard(
                title = "Mod Sederhana",
                description = "Keseimbangan selesa untuk pejabat dan rumah",
                icon = Icons.Default.VolumeDown,
                accentColor = IndigoGlow,
                setting = presetConfig.medium,
                onSettingChanged = { updated ->
                    onUpdateConfig(presetConfig.copy(medium = updated))
                }
            )
        }

        // Kad Mod Nyaring
        item {
            PresetConfigCard(
                title = "Mod Nyaring",
                description = "Paras tinggi untuk persekitaran bising atau luar rumah",
                icon = Icons.Default.VolumeUp,
                accentColor = NeonCyan,
                setting = presetConfig.loud,
                onSettingChanged = { updated ->
                    onUpdateConfig(presetConfig.copy(loud = updated))
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PresetConfigCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    setting: PresetSetting,
    onSettingChanged: (PresetSetting) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(18.dp)),
        color = SurfaceCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider Media
            ConfigSliderRow(
                label = "Bunyi Media",
                percent = setting.mediaPercent,
                accentColor = accentColor,
                onValueChange = { onSettingChanged(setting.copy(mediaPercent = it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slider Saluran Lain
            ConfigSliderRow(
                label = "Saluran Lain (Dering/Notif/Alarm)",
                percent = setting.otherPercent,
                accentColor = accentColor,
                onValueChange = { onSettingChanged(setting.copy(otherPercent = it)) }
            )
        }
    }
}

@Composable
private fun ConfigSliderRow(
    label: String,
    percent: Int,
    accentColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }
        Slider(
            value = percent.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 19, // Selang 5% setiap step
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF1E293B)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
