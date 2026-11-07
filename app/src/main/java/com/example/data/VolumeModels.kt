package com.example.data

import android.media.AudioManager
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.StreamAlarmColor
import com.example.ui.theme.StreamCallColor
import com.example.ui.theme.StreamMediaColor
import com.example.ui.theme.StreamNotificationColor
import com.example.ui.theme.StreamRingColor

// 5 Kategori Saluran Utama Sistem
enum class StreamCategory(
    val streamType: Int,
    val title: String,
    val defaultIconName: String,
    val accentColor: Color
) {
    MEDIA(
        streamType = AudioManager.STREAM_MUSIC,
        title = "Media",
        defaultIconName = "music_note",
        accentColor = StreamMediaColor
    ),
    RING(
        streamType = AudioManager.STREAM_RING,
        title = "Dering",
        defaultIconName = "notifications_active",
        accentColor = StreamRingColor
    ),
    NOTIFICATION(
        streamType = AudioManager.STREAM_NOTIFICATION,
        title = "Notifikasi",
        defaultIconName = "chat_bubble",
        accentColor = StreamNotificationColor
    ),
    ALARM(
        streamType = AudioManager.STREAM_ALARM,
        title = "Penggera",
        defaultIconName = "alarm",
        accentColor = StreamAlarmColor
    ),
    CALL(
        streamType = AudioManager.STREAM_VOICE_CALL,
        title = "Panggilan",
        defaultIconName = "phone_in_talk",
        accentColor = StreamCallColor
    )
}

// State Paras Volume Semasa untuk Setiap Saluran
data class StreamVolumeState(
    val category: StreamCategory,
    val currentVolume: Int,
    val minVolume: Int,
    val maxVolume: Int,
    val isMuted: Boolean = false
) {
    val progress: Float
        get() = if (maxVolume > minVolume) {
            ((currentVolume - minVolume).toFloat() / (maxVolume - minVolume).toFloat()).coerceIn(0f, 1f)
        } else 0f

    val percentage: Int
        get() = (progress * 100).toInt()
}

// 3 Mod Deringan Asas Sistem Telefon
enum class RingerModeType(val modeValue: Int, val label: String) {
    NORMAL(AudioManager.RINGER_MODE_NORMAL, "Normal"),
    VIBRATE(AudioManager.RINGER_MODE_VIBRATE, "Getar"),
    SILENT(AudioManager.RINGER_MODE_SILENT, "Senyap")
}

// 3 Jenis Preset 1-Tap
enum class PresetType(val label: String) {
    SILENT("Senyap"),
    MEDIUM("Sederhana"),
    LOUD("Nyaring")
}

// Konfigurasi Peratusan Setiap Preset (Nilai default yang boleh diubah di tetapan)
data class PresetSetting(
    val mediaPercent: Int,
    val otherPercent: Int
)

data class PresetConfig(
    val silent: PresetSetting = PresetSetting(mediaPercent = 0, otherPercent = 50),
    val medium: PresetSetting = PresetSetting(mediaPercent = 50, otherPercent = 70),
    val loud: PresetSetting = PresetSetting(mediaPercent = 100, otherPercent = 100)
)
