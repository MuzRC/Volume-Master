package com.example.data

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.util.Log

class VolumeRepository(context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val prefs = context.getSharedPreferences("volume_master_prefs", Context.MODE_PRIVATE)

    // Baca aras semasa bagi setiap saluran
    fun getStreamVolumeState(category: StreamCategory): StreamVolumeState {
        val max = audioManager.getStreamMaxVolume(category.streamType).coerceAtLeast(1)
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try { audioManager.getStreamMinVolume(category.streamType) } catch (e: Exception) { 0 }
        } else 0

        val current = audioManager.getStreamVolume(category.streamType)
        val isMuted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try { audioManager.isStreamMute(category.streamType) } catch (e: Exception) { current <= min }
        } else {
            current <= min
        }

        return StreamVolumeState(
            category = category,
            currentVolume = current,
            minVolume = min,
            maxVolume = max,
            isMuted = isMuted
        )
    }

    fun getAllStreamStates(): List<StreamVolumeState> {
        return StreamCategory.values().map { getStreamVolumeState(it) }
    }

    fun getCurrentRingerMode(): RingerModeType {
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> RingerModeType.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerModeType.VIBRATE
            else -> RingerModeType.NORMAL
        }
    }

    fun isDndAccessGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.isNotificationPolicyAccessGranted ?: true
        } else true
    }

    fun setStreamVolume(streamType: Int, volume: Int): Boolean {
        return try {
            val max = audioManager.getStreamMaxVolume(streamType)
            val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try { audioManager.getStreamMinVolume(streamType) } catch (e: Exception) { 0 }
            } else 0
            val target = volume.coerceIn(min, max)
            audioManager.setStreamVolume(streamType, target, 0)
            true
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Gagal ubah volume untuk stream $streamType", e)
            false
        }
    }

    fun setStreamPercentage(category: StreamCategory, percent: Int): Boolean {
        val max = audioManager.getStreamMaxVolume(category.streamType)
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try { audioManager.getStreamMinVolume(category.streamType) } catch (e: Exception) { 0 }
        } else 0
        val targetVolume = min + ((percent.coerceIn(0, 100) / 100f) * (max - min)).toInt()
        return setStreamVolume(category.streamType, targetVolume)
    }

    fun toggleStreamMute(category: StreamCategory, isCurrentlyMuted: Boolean): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val direction = if (isCurrentlyMuted) AudioManager.ADJUST_UNMUTE else AudioManager.ADJUST_MUTE
                audioManager.adjustStreamVolume(category.streamType, direction, 0)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            if (isCurrentlyMuted) {
                val defaultVol = (audioManager.getStreamMaxVolume(category.streamType) / 2).coerceAtLeast(1)
                setStreamVolume(category.streamType, defaultVol)
            } else {
                setStreamVolume(category.streamType, 0)
            }
        }
    }

    fun setRingerMode(mode: RingerModeType): Boolean {
        return try {
            audioManager.ringerMode = mode.modeValue
            true
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Gagal ubah ringer mode ke ${mode.name}", e)
            false
        }
    }

    // --- PENGURUSAN TETAPAN PRESET ---

    fun loadPresetConfig(): PresetConfig {
        return PresetConfig(
            silent = PresetSetting(
                mediaPercent = prefs.getInt("silent_media", 0),
                otherPercent = prefs.getInt("silent_other", 50)
            ),
            medium = PresetSetting(
                mediaPercent = prefs.getInt("medium_media", 50),
                otherPercent = prefs.getInt("medium_other", 70)
            ),
            loud = PresetSetting(
                mediaPercent = prefs.getInt("loud_media", 100),
                otherPercent = prefs.getInt("loud_other", 100)
            )
        )
    }

    fun savePresetConfig(config: PresetConfig) {
        prefs.edit().apply {
            putInt("silent_media", config.silent.mediaPercent)
            putInt("silent_other", config.silent.otherPercent)
            putInt("medium_media", config.medium.mediaPercent)
            putInt("medium_other", config.medium.otherPercent)
            putInt("loud_media", config.loud.mediaPercent)
            putInt("loud_other", config.loud.otherPercent)
            apply()
        }
    }

    fun resetPresetConfig(): PresetConfig {
        val defaultConfig = PresetConfig()
        savePresetConfig(defaultConfig)
        return defaultConfig
    }
}
