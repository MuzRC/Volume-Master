package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetType
import com.example.data.RingerModeType
import com.example.data.StreamCategory
import com.example.data.StreamVolumeState
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VolumeMainScreen(
    viewModel: VolumeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(imageVector = Icons.Default.Equalizer, contentDescription = "Saluran Audio")
                    },
                    label = { Text("Saluran Audio", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = "Profil Bunyi")
                    },
                    label = { Text("Profil Bunyi", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(16.dp)) { data ->
                Snackbar(
                    containerColor = SurfaceCard,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(data.visuals.message, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.activeTab == 0) {
                // TAB 0: ZERO-SCROLL DASHBOARD
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Header Ringkas + Penunjuk Mod Deringan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Volume Master",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        // Status Mod Deringan (Boleh ketuk untuk pusing mod)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                                .clickable {
                                    val nextMode = when (uiState.currentRingerMode) {
                                        RingerModeType.NORMAL -> RingerModeType.VIBRATE
                                        RingerModeType.VIBRATE -> RingerModeType.SILENT
                                        RingerModeType.SILENT -> RingerModeType.NORMAL
                                    }
                                    viewModel.setRingerMode(nextMode)
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (icon, color) = when (uiState.currentRingerMode) {
                                    RingerModeType.NORMAL -> Icons.Default.NotificationsActive to EmeraldGlow
                                    RingerModeType.VIBRATE -> Icons.Default.Vibration to IndigoGlow
                                    RingerModeType.SILENT -> Icons.Default.NotificationsOff to RoseGlow
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.currentRingerMode.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                            }
                        }
                    }

                    // 2. Barisan 3 Butang 1-Tap Preset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetQuickButton(
                            title = "Senyap",
                            icon = Icons.Default.NotificationsOff,
                            accentColor = RoseGlow,
                            isSelected = uiState.activePresetType == PresetType.SILENT,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.applyPreset(PresetType.SILENT) }
                        )
                        PresetQuickButton(
                            title = "Sederhana",
                            icon = Icons.Default.VolumeDown,
                            accentColor = IndigoGlow,
                            isSelected = uiState.activePresetType == PresetType.MEDIUM,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.applyPreset(PresetType.MEDIUM) }
                        )
                        PresetQuickButton(
                            title = "Nyaring",
                            icon = Icons.Default.VolumeUp,
                            accentColor = NeonCyan,
                            isSelected = uiState.activePresetType == PresetType.LOUD,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.applyPreset(PresetType.LOUD) }
                        )
                    }

                    // 3. Panel 5 Saluran Audio Kompak (Muat semua dalam 1 kad bersatu)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, BorderDark, RoundedCornerShape(20.dp)),
                        color = SurfaceCard
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.streamStates.forEach { streamState ->
                                CompactVolumeRow(
                                    streamState = streamState,
                                    onVolumeChanged = { viewModel.setStreamVolume(streamState.category, it) },
                                    onToggleMute = { viewModel.toggleStreamMute(streamState.category) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                }
            } else {
                // TAB 1: TETAPAN PROFIL
                VolumeSettingsScreen(
                    presetConfig = uiState.presetConfig,
                    onUpdateConfig = { viewModel.updatePresetConfig(it) },
                    onResetConfig = { viewModel.resetPresetConfig() }
                )
            }
        }
    }
}

// Komponen Baris Kompak bagi Setiap Saluran Audio (~48dp sahaja)
@Composable
private fun CompactVolumeRow(
    streamState: StreamVolumeState,
    onVolumeChanged: (Int) -> Unit,
    onToggleMute: () -> Unit
) {
    val category = streamState.category
    val isMuted = streamState.currentVolume <= streamState.minVolume || streamState.isMuted
    val activeColor = if (isMuted) RoseGlow else category.accentColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ikon saluran (Tekan untuk mute/unmute cepat)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(activeColor.copy(alpha = 0.15f))
                .border(1.dp, activeColor.copy(alpha = 0.35f), CircleShape)
                .clickable(onClick = onToggleMute),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeMute else getCategoryIcon(category),
                contentDescription = category.title,
                tint = activeColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Label Nama Saluran
        Text(
            text = category.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary,
            modifier = Modifier.width(68.dp)
        )

        // Slider Kawalan Utama
        Slider(
            value = streamState.currentVolume.toFloat(),
            onValueChange = { onVolumeChanged(it.toInt()) },
            valueRange = streamState.minVolume.toFloat()..streamState.maxVolume.toFloat(),
            steps = (streamState.maxVolume - streamState.minVolume - 1).coerceAtLeast(0),
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = Color(0xFF1E293B)
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        // Penunjuk Peratusan
        Text(
            text = "${streamState.percentage}%",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isMuted) RoseGlow else TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )
    }
}

// Butang 1-Tap Preset
@Composable
private fun PresetQuickButton(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else SurfaceCard)
            .border(
                1.dp,
                if (isSelected) accentColor else BorderDark,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) accentColor else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

private fun getCategoryIcon(category: StreamCategory): ImageVector {
    return when (category) {
        StreamCategory.MEDIA -> Icons.Default.MusicNote
        StreamCategory.RING -> Icons.Default.NotificationsActive
        StreamCategory.NOTIFICATION -> Icons.Default.ChatBubble
        StreamCategory.ALARM -> Icons.Default.Alarm
        StreamCategory.CALL -> Icons.Default.PhoneInTalk
    }
}
