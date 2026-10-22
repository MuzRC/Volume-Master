package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StreamCategory
import com.example.data.StreamVolumeState
import com.example.ui.theme.BorderDark
import com.example.ui.theme.RoseGlow
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VolumeCard(
    streamState: StreamVolumeState,
    isPreviewing: Boolean,
    onVolumeChanged: (Int) -> Unit,
    onStepVolume: (Int) -> Unit,
    onToggleMute: () -> Unit,
    onPlayPreview: () -> Unit,
    onStopPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = streamState.category
    val accentColor = category.accentColor

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderDark, RoundedCornerShape(20.dp)),
        color = SurfaceCard,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Icon, Title, Subtitle, Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = category.title,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = category.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                // Volume Level and Percentage Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${streamState.percentage}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = if (streamState.currentVolume <= streamState.minVolume) RoseGlow else accentColor
                    )
                    Text(
                        text = "${streamState.currentVolume} / ${streamState.maxVolume}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Segmented Meter Bar (Menggunakan 1 Canvas untuk hapuskan bebanan 16 Box Layout Node)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF0F172A))
            ) {
                val totalSegments = 16
                val barHeight = size.height
                val gap = 2.dp.toPx()
                val totalGaps = gap * (totalSegments - 1)
                val segmentWidth = (size.width - totalGaps) / totalSegments
                val radius = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                for (s in 0 until totalSegments) {
                    val threshold = s.toFloat() / totalSegments
                    val isSegmentLit = streamState.progress >= threshold && streamState.currentVolume > streamState.minVolume
                    val color = if (isSegmentLit) accentColor else Color(0xFF1E293B)
                    val x = s * (segmentWidth + gap)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, 0f),
                        size = Size(segmentWidth, barHeight),
                        cornerRadius = radius
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Volume Slider
            Slider(
                value = streamState.currentVolume.toFloat(),
                onValueChange = { onVolumeChanged(it.toInt()) },
                valueRange = streamState.minVolume.toFloat()..streamState.maxVolume.toFloat(),
                steps = (streamState.maxVolume - streamState.minVolume - 1).coerceAtLeast(0),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = Color(0xFF1E293B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slider_${category.name.lowercase()}")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row: [-] [+] [Mute] [Uji Bunyi]
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Stepper Minus Button
                IconButton(
                    onClick = { onStepVolume(-1) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1424))
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .testTag("minus_${category.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Kurangkan ${category.title}",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Stepper Plus Button
                IconButton(
                    onClick = { onStepVolume(1) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1424))
                        .border(1.dp, BorderDark, RoundedCornerShape(12.dp))
                        .testTag("plus_${category.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah ${category.title}",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Mute / Unmute Toggle Button
                val isMuted = streamState.currentVolume <= streamState.minVolume || streamState.isMuted
                FilledTonalButton(
                    onClick = onToggleMute,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isMuted) RoseGlow.copy(alpha = 0.2f) else Color(0xFF0D1424),
                        contentColor = if (isMuted) RoseGlow else TextSecondary
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1f)
                        .border(1.dp, if (isMuted) RoseGlow.copy(alpha = 0.4f) else BorderDark, RoundedCornerShape(12.dp))
                        .testTag("mute_${category.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMuted) "Senyap" else "Mute",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                // Sound Preview / Test Button (Zero overhead & tiada animasi jam latar)
                ElevatedButton(
                    onClick = {
                        if (isPreviewing) onStopPreview() else onPlayPreview()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (isPreviewing) accentColor else accentColor.copy(alpha = 0.15f),
                        contentColor = if (isPreviewing) Color(0xFF051626) else accentColor
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .weight(1.2f)
                        .testTag("preview_${category.name.lowercase()}")
                ) {
                    Icon(
                        imageVector = if (isPreviewing) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPreviewing) "Berhenti" else "Uji Bunyi",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
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
