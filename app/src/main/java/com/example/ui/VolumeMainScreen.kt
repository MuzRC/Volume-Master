package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioVisualizerHeader
import com.example.ui.components.CreatePresetDialog
import com.example.ui.components.DndBanner
import com.example.ui.components.PresetDetailCard
import com.example.ui.components.QuickProfileChipsRow
import com.example.ui.components.VolumeCard
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.NeonCyan
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

    val averageProgress = if (uiState.streamStates.isNotEmpty()) {
        uiState.streamStates.map { it.progress }.average().toFloat()
    } else 0.5f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Equalizer,
                            contentDescription = "Saluran Audio"
                        )
                    },
                    label = { Text("Saluran Audio", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_streams")
                )
                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Profil & Pratetap"
                        )
                    },
                    label = { Text("Profil Bunyi", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.2f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("tab_presets")
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .align(Alignment.TopCenter)
            ) {
                if (uiState.activeTab == 0) {
                    // STREAMS TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Master Visualizer & Ringer Controller
                        item {
                            AudioVisualizerHeader(
                                currentRingerMode = uiState.currentRingerMode,
                                isMasterMuted = uiState.isMasterMuted,
                                averageLevel = averageProgress,
                                onRingerModeSelected = { viewModel.setRingerMode(it) },
                                onToggleMasterMute = { viewModel.toggleMasterMute() },
                                onOpenCreatePreset = { viewModel.setShowCreatePresetDialog(true) }
                            )
                        }

                        // Quick Presets Row
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MOD PANTAS SATU SENTUHAN",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "Pilih Profil",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                QuickProfileChipsRow(
                                    presets = uiState.defaultPresets + uiState.customPresets,
                                    activePresetId = uiState.activePresetId,
                                    onPresetSelected = { viewModel.applyPreset(it) }
                                )
                            }
                        }

                        // DND Policy Banner if needed
                        if (!uiState.isDndGranted) {
                            item {
                                DndBanner()
                            }
                        }

                        // Section Label
                        item {
                            Text(
                                text = "KAWALAN SALURAN INDIVIDU",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextMuted,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        // Individual Volume Cards
                        items(uiState.streamStates, key = { it.category.name }) { streamState ->
                            VolumeCard(
                                streamState = streamState,
                                isPreviewing = uiState.activePreviewCategory == streamState.category,
                                onVolumeChanged = { viewModel.setStreamVolume(streamState.category, it) },
                                onStepVolume = { viewModel.stepVolume(streamState.category, it) },
                                onToggleMute = { viewModel.toggleStreamMute(streamState.category) },
                                onPlayPreview = { viewModel.playPreview(streamState.category) },
                                onStopPreview = { viewModel.stopPreview() }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                } else {
                    // PRESETS & PROFILES TAB
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Header Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(SurfaceCard)
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Profil Audio & Pratetap",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tukar kesemua aras kelantangan telefon serentak mengikut situasi harian anda.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    ElevatedButton(
                                        onClick = { viewModel.setShowCreatePresetDialog(true) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.elevatedButtonColors(
                                            containerColor = NeonCyan,
                                            contentColor = Color(0xFF041726)
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Rakam Profil Semasa",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        if (uiState.customPresets.isNotEmpty()) {
                            item {
                                Text(
                                    text = "PROFIL TERSUAI ANDA",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = EmeraldGlow
                                )
                            }
                            items(uiState.customPresets, key = { it.id }) { preset ->
                                PresetDetailCard(
                                    preset = preset,
                                    isActive = preset.id == uiState.activePresetId,
                                    onApply = { viewModel.applyPreset(preset) },
                                    onDelete = { viewModel.deleteCustomPreset(preset.id) }
                                )
                            }
                        }

                        item {
                            Text(
                                text = "PROFIL BAWAAN SISTEM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextMuted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(uiState.defaultPresets, key = { it.id }) { preset ->
                            PresetDetailCard(
                                preset = preset,
                                isActive = preset.id == uiState.activePresetId,
                                onApply = { viewModel.applyPreset(preset) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            // Create Preset Dialog
            if (uiState.showCreatePresetDialog) {
                CreatePresetDialog(
                    onDismiss = { viewModel.setShowCreatePresetDialog(false) },
                    onSave = { name, desc ->
                        viewModel.saveCustomPreset(name, desc)
                    }
                )
            }
        }
    }
}
