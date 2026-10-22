package com.example.data

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class VolumeRepository(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    private val prefs = context.getSharedPreferences("volume_master_prefs", Context.MODE_PRIVATE)

    private var activeRingtone: Ringtone? = null
    private var activeToneGen: ToneGenerator? = null

    fun getStreamVolumeState(category: StreamCategory): StreamVolumeState {
        val max = audioManager.getStreamMaxVolume(category.streamType).coerceAtLeast(1)
        val min = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                audioManager.getStreamMinVolume(category.streamType)
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
        val current = audioManager.getStreamVolume(category.streamType)
        val isMuted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                audioManager.isStreamMute(category.streamType)
            } catch (e: Exception) {
                current <= min
            }
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
        } else {
            true
        }
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
        } catch (e: SecurityException) {
            Log.e("VolumeRepository", "SecurityException setting volume for stream $streamType", e)
            false
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Error setting volume for stream $streamType", e)
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
            } catch (e: SecurityException) {
                Log.e("VolumeRepository", "SecurityException toggling mute", e)
                false
            }
        } else {
            if (isCurrentlyMuted) {
                val half = (audioManager.getStreamMaxVolume(category.streamType) / 2).coerceAtLeast(1)
                setStreamVolume(category.streamType, half)
            } else {
                setStreamVolume(category.streamType, 0)
            }
        }
    }

    fun setRingerMode(mode: RingerModeType): Boolean {
        return try {
            audioManager.ringerMode = mode.modeValue
            true
        } catch (e: SecurityException) {
            Log.e("VolumeRepository", "DND access required to change ringer mode to $mode", e)
            false
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Failed to change ringer mode", e)
            false
        }
    }

    suspend fun playStreamPreview(category: StreamCategory) = withContext(Dispatchers.IO) {
        stopAnyActiveTone()
        try {
            when (category) {
                StreamCategory.RING -> {
                    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, uri)
                    activeRingtone = r
                    r?.play()
                }
                StreamCategory.NOTIFICATION -> {
                    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, uri)
                    activeRingtone = r
                    r?.play()
                }
                StreamCategory.ALARM -> {
                    val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(context, uri)
                    activeRingtone = r
                    r?.play()
                }
                StreamCategory.MEDIA -> {
                    activeToneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
                    activeToneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
                }
                StreamCategory.CALL -> {
                    activeToneGen = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 80)
                    activeToneGen?.startTone(ToneGenerator.TONE_SUP_RINGTONE, 600)
                }
            }
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Error playing preview sound for ${category.name}", e)
        }
    }

    fun stopAnyActiveTone() {
        try {
            activeRingtone?.let {
                if (it.isPlaying) {
                    it.stop()
                }
            }
            activeRingtone = null

            activeToneGen?.release()
            activeToneGen = null
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Error stopping preview tone", e)
        }
    }

    fun getDefaultPresets(): List<VolumePreset> {
        return listOf(
            VolumePreset(
                id = "preset_normal",
                name = "Normal & Seimbang",
                description = "Tetapan harian serba boleh untuk semua keperluan",
                iconKey = "volume_up",
                ringerMode = RingerModeType.NORMAL,
                mediaPercent = 65,
                ringPercent = 70,
                notificationPercent = 70,
                alarmPercent = 85,
                callPercent = 75
            ),
            VolumePreset(
                id = "preset_meeting",
                name = "Mesyuarat & Kerja",
                description = "Deringan getar, media disenyapkan agar fokus kerja",
                iconKey = "vibration",
                ringerMode = RingerModeType.VIBRATE,
                mediaPercent = 0,
                ringPercent = 0,
                notificationPercent = 0,
                alarmPercent = 60,
                callPercent = 50
            ),
            VolumePreset(
                id = "preset_sleep",
                name = "Malam & Tidur",
                description = "Senyap sepenuhnya kecuali penggera keselamatan pagi",
                iconKey = "bedtime",
                ringerMode = RingerModeType.SILENT,
                mediaPercent = 0,
                ringPercent = 0,
                notificationPercent = 0,
                alarmPercent = 90,
                callPercent = 30
            ),
            VolumePreset(
                id = "preset_outdoor",
                name = "Luar / Bising",
                description = "Kelantangan maksimum 100% untuk persekitaran bising",
                iconKey = "campaign",
                ringerMode = RingerModeType.NORMAL,
                mediaPercent = 100,
                ringPercent = 100,
                notificationPercent = 100,
                alarmPercent = 100,
                callPercent = 100
            ),
            VolumePreset(
                id = "preset_entertainment",
                name = "Muzik & Pawagam",
                description = "Media bertenaga tinggi dengan notifikasi lembut",
                iconKey = "movie",
                ringerMode = RingerModeType.NORMAL,
                mediaPercent = 85,
                ringPercent = 30,
                notificationPercent = 20,
                alarmPercent = 70,
                callPercent = 60
            )
        )
    }

    fun loadCustomPresets(): List<VolumePreset> {
        val jsonString = prefs.getString("custom_presets_json", null) ?: return emptyList()
        val list = mutableListOf<VolumePreset>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val modeStr = obj.optString("ringerMode", "NORMAL")
                val mode = try { RingerModeType.valueOf(modeStr) } catch (e: Exception) { RingerModeType.NORMAL }
                list.add(
                    VolumePreset(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        description = obj.optString("description", "Profil tersuai"),
                        iconKey = obj.optString("iconKey", "tune"),
                        ringerMode = mode,
                        mediaPercent = obj.optInt("mediaPercent", 50),
                        ringPercent = obj.optInt("ringPercent", 50),
                        notificationPercent = obj.optInt("notificationPercent", 50),
                        alarmPercent = obj.optInt("alarmPercent", 70),
                        callPercent = obj.optInt("callPercent", 60),
                        isCustom = true
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("VolumeRepository", "Error parsing custom presets", e)
        }
        return list
    }

    fun saveCustomPreset(preset: VolumePreset) {
        val existing = loadCustomPresets().filter { it.id != preset.id }.toMutableList()
        existing.add(preset.copy(isCustom = true))
        saveCustomPresetsList(existing)
    }

    fun deleteCustomPreset(id: String) {
        val existing = loadCustomPresets().filter { it.id != id }
        saveCustomPresetsList(existing)
    }

    private fun saveCustomPresetsList(list: List<VolumePreset>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("description", item.description)
                put("iconKey", item.iconKey)
                put("ringerMode", item.ringerMode.name)
                put("mediaPercent", item.mediaPercent)
                put("ringPercent", item.ringPercent)
                put("notificationPercent", item.notificationPercent)
                put("alarmPercent", item.alarmPercent)
                put("callPercent", item.callPercent)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("custom_presets_json", jsonArray.toString()).apply()
    }
}
