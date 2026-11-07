package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.PresetConfig
import com.example.data.PresetType
import com.example.data.RingerModeType
import com.example.data.StreamCategory
import com.example.data.StreamVolumeState
import com.example.data.VolumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class VolumeUiState(
    val streamStates: List<StreamVolumeState> = emptyList(),
    val currentRingerMode: RingerModeType = RingerModeType.NORMAL,
    val isDndGranted: Boolean = true,
    val presetConfig: PresetConfig = PresetConfig(),
    val activePresetType: PresetType? = null,
    val userMessage: String? = null,
    val activeTab: Int = 0 // 0: Saluran Audio, 1: Profil Bunyi
)

class VolumeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VolumeRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(VolumeUiState())
    val uiState: StateFlow<VolumeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        val streams = repository.getAllStreamStates()
        val ringer = repository.getCurrentRingerMode()
        val isDnd = repository.isDndAccessGranted()
        val config = repository.loadPresetConfig()

        _uiState.update {
            it.copy(
                streamStates = streams,
                currentRingerMode = ringer,
                isDndGranted = isDnd,
                presetConfig = config
            )
        }
    }

    fun onSystemVolumeChanged() {
        val streams = repository.getAllStreamStates()
        val ringer = repository.getCurrentRingerMode()
        _uiState.update {
            it.copy(
                streamStates = streams,
                currentRingerMode = ringer
            )
        }
    }

    fun setStreamVolume(category: StreamCategory, newVolume: Int) {
        val success = repository.setStreamVolume(category.streamType, newVolume)
        if (success) {
            val updatedStream = repository.getStreamVolumeState(category)
            _uiState.update { state ->
                state.copy(
                    streamStates = state.streamStates.map {
                        if (it.category == category) updatedStream else it
                    },
                    activePresetType = null // Padam pilihan preset aktif bila pengguna usik slider manual
                )
            }
        } else {
            _uiState.update {
                it.copy(userMessage = "Gagal melaras ${category.title}. Semak izin sistem atau DND.")
            }
        }
    }

    fun toggleStreamMute(category: StreamCategory) {
        val current = _uiState.value.streamStates.find { it.category == category } ?: return
        val currentlyMuted = current.currentVolume <= current.minVolume || current.isMuted
        val success = repository.toggleStreamMute(category, currentlyMuted)
        if (success) {
            val updatedStream = repository.getStreamVolumeState(category)
            _uiState.update { state ->
                state.copy(
                    streamStates = state.streamStates.map {
                        if (it.category == category) updatedStream else it
                    },
                    activePresetType = null
                )
            }
        }
    }

    fun setRingerMode(mode: RingerModeType) {
        val success = repository.setRingerMode(mode)
        if (success) {
            _uiState.update {
                it.copy(
                    currentRingerMode = mode,
                    userMessage = "Mod deringan ditukar ke ${mode.label}"
                )
            }
            onSystemVolumeChanged()
        } else {
            _uiState.update {
                it.copy(userMessage = "Izin 'Jangan Ganggu' diperlukan untuk tukar ke mod ${mode.label}.")
            }
        }
    }

    // Logik 1-Tap Preset (Senyap, Sederhana, Nyaring)
    fun applyPreset(presetType: PresetType) {
        val config = _uiState.value.presetConfig
        val setting = when (presetType) {
            PresetType.SILENT -> config.silent
            PresetType.MEDIUM -> config.medium
            PresetType.LOUD -> config.loud
        }

        // Laras paras Media
        repository.setStreamPercentage(StreamCategory.MEDIA, setting.mediaPercent)

        // Laras saluran selain Media (Dering, Notifikasi, Penggera, Panggilan)
        val others = listOf(
            StreamCategory.RING,
            StreamCategory.NOTIFICATION,
            StreamCategory.ALARM,
            StreamCategory.CALL
        )
        others.forEach { cat ->
            repository.setStreamPercentage(cat, setting.otherPercent)
        }

        // Selaraskan mod deringan sistem
        if (presetType == PresetType.SILENT && setting.otherPercent == 0) {
            repository.setRingerMode(RingerModeType.VIBRATE)
        } else {
            repository.setRingerMode(RingerModeType.NORMAL)
        }

        val updatedStreams = repository.getAllStreamStates()
        val currentRinger = repository.getCurrentRingerMode()

        _uiState.update {
            it.copy(
                streamStates = updatedStreams,
                currentRingerMode = currentRinger,
                activePresetType = presetType,
                userMessage = "Mod ${presetType.label} diaktifkan"
            )
        }
    }

    // Simpan pelarasan peratusan baharu dari tab Tetapan / Profil Bunyi
    fun updatePresetConfig(newConfig: PresetConfig) {
        repository.savePresetConfig(newConfig)
        _uiState.update {
            it.copy(
                presetConfig = newConfig,
                userMessage = "Tetapan profil berjaya disimpan"
            )
        }
    }

    fun resetPresetConfig() {
        val reset = repository.resetPresetConfig()
        _uiState.update {
            it.copy(
                presetConfig = reset,
                userMessage = "Tetapan profil dikembalikan ke asal"
            )
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}
