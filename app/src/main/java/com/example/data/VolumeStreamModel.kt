package com.example.data

import android.media.AudioManager
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StreamAlarmColor
import com.example.ui.theme.StreamCallColor
import com.example.ui.theme.StreamMediaColor
import com.example.ui.theme.StreamNotificationColor
import com.example.ui.theme.StreamRingColor

enum class StreamCategory(
    val streamType: Int,
    val title: String,
    val subtitle: String,
    val defaultIconName: String,
    val accentColor: Color
) {
    MEDIA(
        streamType = AudioManager.STREAM_MUSIC,
        title = "Media & Muzik",
        subtitle = "Video, lagu, permainan & audio aplikasi",
        defaultIconName = "music_note",
        accentColor = StreamMediaColor
    ),
    RING(
        streamType = AudioManager.STREAM_RING,
        title = "Nada Dering",
        subtitle = "Panggilan masuk telefon",
        defaultIconName = "notifications_active",
        accentColor = StreamRingColor
    ),
    NOTIFICATION(
        streamType = AudioManager.STREAM_NOTIFICATION,
        title = "Notifikasi",
        subtitle = "Mesej, e-mel & amaran sistem",
        defaultIconName = "chat_bubble",
        accentColor = StreamNotificationColor
    ),
    ALARM(
        streamType = AudioManager.STREAM_ALARM,
        title = "Penggera & Jam",
        subtitle = "Jam loceng & peringatan masa",
        defaultIconName = "alarm",
        accentColor = StreamAlarmColor
    ),
    CALL(
        streamType = AudioManager.STREAM_VOICE_CALL,
        title = "Panggilan Suara",
        subtitle = "Kelantangan pembesar telinga panggilan",
        defaultIconName = "phone_in_talk",
        accentColor = StreamCallColor
    )
}

data class StreamVolumeState(
    val category: StreamCategory,
    val currentVolume: Int,
    val minVolume: Int,
    val maxVolume: Int,
    val isMuted: Boolean = false,
    val isPreviewingSound: Boolean = false
) {
    val progress: Float
        get() = if (maxVolume > minVolume) {
            ((currentVolume - minVolume).toFloat() / (maxVolume - minVolume).toFloat()).coerceIn(0f, 1f)
        } else 0f

    val percentage: Int
        get() = (progress * 100).toInt()
}

enum class RingerModeType(val modeValue: Int, val label: String) {
    NORMAL(AudioManager.RINGER_MODE_NORMAL, "Normal"),
    VIBRATE(AudioManager.RINGER_MODE_VIBRATE, "Getar"),
    SILENT(AudioManager.RINGER_MODE_SILENT, "Senyap")
}

data class VolumePreset(
    val id: String,
    val name: String,
    val description: String,
    val iconKey: String,
    val ringerMode: RingerModeType = RingerModeType.NORMAL,
    val mediaPercent: Int,
    val ringPercent: Int,
    val notificationPercent: Int,
    val alarmPercent: Int,
    val callPercent: Int,
    val isCustom: Boolean = false
)
