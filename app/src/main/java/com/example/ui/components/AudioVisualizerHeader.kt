package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RingerModeType
import com.example.ui.theme.AmberGlow
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
fun AudioVisualizerHeader(
    currentRingerMode: RingerModeType,
    isMasterMuted: Boolean,
    averageLevel: Float,
    onRingerModeSelected: (RingerModeType) -> Unit,
    onToggleMasterMute: () -> Unit,
    onOpenCreatePreset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(24.dp)),
        color = SurfaceCard,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // App Title & Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(NeonCyan.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Logo Audio",
                            tint = NeonCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Volume Master",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Pusat Kawalan Audio Pintar",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Master Mute Button
                    IconButton(
                        onClick = onToggleMasterMute,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isMasterMuted) RoseGlow.copy(alpha = 0.2f) else BorderDark)
                            .testTag("master_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMasterMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (isMasterMuted) "Nyahsenyap Semua" else "Senyap Semua",
                            tint = if (isMasterMuted) RoseGlow else NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Add Custom Preset Button
                    IconButton(
                        onClick = onOpenCreatePreset,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(IndigoGlow.copy(alpha = 0.25f))
                            .border(1.dp, IndigoGlow.copy(alpha = 0.5f), CircleShape)
                            .testTag("create_preset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Simpan Profil Baharu",
                            tint = IndigoGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Equalizer Level Bars Visualizer (Canvas: Diproses terus di GPU tanpa beban recomposition)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1424))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                val barCount = 20
                val totalWidth = size.width
                val totalHeight = size.height
                val barWidth = 5.dp.toPx()
                val gap = if (barCount > 1) (totalWidth - (barCount * barWidth)) / (barCount - 1) else 0f
                val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                for (i in 0 until barCount) {
                    val threshold = i.toFloat() / barCount
                    val isLit = averageLevel >= threshold && !isMasterMuted
                    val heightRatio = if (isLit) {
                        val mod = ((i % 5) + 2) / 6f
                        (mod * pulseAlpha).coerceIn(0.25f, 1f)
                    } else {
                        0.15f
                    }

                    val currentBarHeight = totalHeight * heightRatio
                    val x = i * (barWidth + gap)
                    val y = (totalHeight - currentBarHeight) / 2f

                    val color = when {
                        isMasterMuted -> RoseGlow.copy(alpha = 0.3f)
                        isLit && i > 15 -> RoseGlow
                        isLit && i > 11 -> AmberGlow
                        isLit -> NeonCyan
                        else -> Color(0xFF1E293B)
                    }

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, currentBarHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Ringer Mode Selectors (Normal, Vibrate, Silent)
            Text(
                text = "MOD DERINGAN SISTEM",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D1424))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                RingerModeItem(
                    label = "Normal",
                    icon = Icons.Default.Notifications,
                    isSelected = currentRingerMode == RingerModeType.NORMAL,
                    activeColor = EmeraldGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { onRingerModeSelected(RingerModeType.NORMAL) }
                )
                RingerModeItem(
                    label = "Getar",
                    icon = Icons.Default.Vibration,
                    isSelected = currentRingerMode == RingerModeType.VIBRATE,
                    activeColor = IndigoGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { onRingerModeSelected(RingerModeType.VIBRATE) }
                )
                RingerModeItem(
                    label = "Senyap",
                    icon = Icons.Default.NotificationsOff,
                    isSelected = currentRingerMode == RingerModeType.SILENT,
                    activeColor = RoseGlow,
                    modifier = Modifier.weight(1f),
                    onClick = { onRingerModeSelected(RingerModeType.SILENT) }
                )
            }
        }
    }
}

@Composable
private fun RingerModeItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) activeColor.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}
