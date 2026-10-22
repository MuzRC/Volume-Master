package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VolumePreset
import com.example.ui.theme.BorderDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuickProfileChipsRow(
    presets: List<VolumePreset>,
    activePresetId: String?,
    onPresetSelected: (VolumePreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(presets, key = { it.id }) { preset ->
            val isActive = preset.id == activePresetId
            val chipColor = if (isActive) NeonCyan else Color(0xFF131B2E)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(chipColor.copy(alpha = if (isActive) 0.25f else 1f))
                    .border(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) NeonCyan else BorderDark,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPresetSelected(preset) }
                    .testTag("preset_chip_${preset.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPresetIcon(preset.iconKey),
                    contentDescription = preset.name,
                    tint = if (isActive) NeonCyan else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun PresetDetailCard(
    preset: VolumePreset,
    isActive: Boolean,
    onApply: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                if (isActive) NeonCyan.copy(alpha = 0.6f) else BorderDark,
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isActive) NeonCyan.copy(alpha = 0.2f) else IndigoGlow.copy(alpha = 0.15f))
                        .border(1.dp, if (isActive) NeonCyan else IndigoGlow.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPresetIcon(preset.iconKey),
                        contentDescription = preset.name,
                        tint = if (isActive) NeonCyan else IndigoGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        if (preset.isCustom) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EmeraldGlow.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Tersuai",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldGlow,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (onDelete != null && preset.isCustom) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Padam Profil",
                            tint = RoseGlow.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage Bars Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PresetMiniStat(label = "Media", percent = preset.mediaPercent)
                PresetMiniStat(label = "Dering", percent = preset.ringPercent)
                PresetMiniStat(label = "Notif", percent = preset.notificationPercent)
                PresetMiniStat(label = "Penggera", percent = preset.alarmPercent)
                PresetMiniStat(label = "Panggilan", percent = preset.callPercent)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            FilledTonalButton(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isActive) EmeraldGlow.copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.15f),
                    contentColor = if (isActive) EmeraldGlow else NeonCyan
                )
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActive) "Sedang Aktif" else "Gunakan Profil Ini",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun PresetMiniStat(label: String, percent: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

fun getPresetIcon(iconKey: String): ImageVector {
    return when (iconKey) {
        "bedtime" -> Icons.Default.Bedtime
        "vibration" -> Icons.Default.Vibration
        "campaign" -> Icons.Default.Campaign
        "movie" -> Icons.Default.Movie
        "volume_up" -> Icons.Default.VolumeUp
        else -> Icons.Default.Tune
    }
}
