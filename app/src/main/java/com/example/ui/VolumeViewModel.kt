package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RingerModeType
import com.example.data.StreamCategory
import com.example.data.StreamVolumeState
import com.example.data.VolumePreset
import com.example.data.VolumeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class VolumeUiState(
    val streamStates: List<StreamVolumeState> = emptyList(),
    val currentRingerMode: RingerModeType = RingerModeType.NORMAL,
    val isDndGranted: Boolean = true,
    val activePreviewCategory: StreamCategory? = null,
    val defaultPresets: List<VolumePreset> = emptyList(),
    val customPresets: List<VolumePreset> = emptyList(),
    val activePresetId: String? = null,
    val isMasterMuted: Boolean = false,
    val userMessage: String? = null,
    val showCreatePresetDialog: Boolean = false,
    val activeTab: Int = 0 // 0: Saluran (Streams), 1: Profil (Presets)
)

class VolumeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VolumeRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(VolumeUiState())
    val uiState: StateFlow<VolumeUiState> = _uiState.asStateFlow()

    private var previewJob: Job? = null
    private var previousVolumeCache = mutableMapOf<StreamCategory, Int>()

    init {
        loadInitialData()
    }

    fun loadInitialData() {
        val streams = repository.getAllStreamStates()
        val ringer = repository.getCurrentRingerMode()
        val isDnd = repository.isDndAccessGranted()
        val defaults = repository.getDefaultPresets()
        val customs = repository.loadCustomPresets()
        val isAllZero = streams.all { it.currentVolume <= it.minVolume }

        _uiState.update {
            it.copy(
                streamStates = streams,
                currentRingerMode = ringer,
                isDndGranted = isDnd,
                defaultPresets = defaults,
                customPresets = customs,
                isMasterMuted = isAllZero
            )
        }
    }

    fun onSystemVolumeChanged() {
        // Triggered by BroadcastReceiver
        val streams = repository.getAllStreamStates()
        val ringer = repository.getCurrentRingerMode()
        val isAllZero = streams.all { it.currentVolume <= it.minVolume }
        _uiState.update {
            it.copy(
                streamStates = streams,
                currentRingerMode = ringer,
                isMasterMuted = isAllZero
            )
        }
    }

    fun setStreamVolume(category: StreamCategory, newVolume: Int) {
        val success = repository.setStreamVolume(category.streamType, newVolume)
        if (success) {
            val updatedStream = repository.getStreamVolumeState(category)
            _uiState.update { state ->
                val list = state.streamStates.map {
                    if (it.category == category) updatedStream else it
                }
                state.copy(
                    streamStates = list,
                    activePresetId = null,
                    isMasterMuted = list.all { it.currentVolume <= it.minVolume }
                )
            }
        } else {
            _uiState.update {
                it.copy(userMessage = "Tidak dapat melaras saluran ${category.title}. Semak kebenaran sistem atau mod Jangan Ganggu.")
            }
        }
    }

    fun stepVolume(category: StreamCategory, delta: Int) {
        val current = _uiState.value.streamStates.find { it.category == category } ?: return
        val target = (current.currentVolume + delta).coerceIn(current.minVolume, current.maxVolume)
        setStreamVolume(category, target)
    }

    fun toggleStreamMute(category: StreamCategory) {
        val current = _uiState.value.streamStates.find { it.category == category } ?: return
        val currentlyMuted = current.currentVolume <= current.minVolume || current.isMuted
        val success = repository.toggleStreamMute(category, currentlyMuted)
        if (success) {
            val updatedStream = repository.getStreamVolumeState(category)
            _uiState.update { state ->
                val list = state.streamStates.map {
                    if (it.category == category) updatedStream else it
                }
                state.copy(streamStates = list)
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
                it.copy(
                    userMessage = "Kebenaran 'Akses Jangan Ganggu' diperlukan untuk menukar ke mod ${mode.label}."
                )
            }
        }
    }

    fun toggleMasterMute() {
        val currentState = _uiState.value
        if (currentState.isMasterMuted) {
            // Restore previous volumes or set to 50%
            StreamCategory.values().forEach { cat ->
                val cached = previousVolumeCache[cat]
                if (cached != null && cached > 0) {
                    repository.setStreamVolume(cat.streamType, cached)
                } else {
                    repository.setStreamPercentage(cat, 50)
                }
            }
            repository.setRingerMode(RingerModeType.NORMAL)
            _uiState.update { it.copy(isMasterMuted = false, userMessage = "Semua saluran dinyahsenyap") }
        } else {
            // Cache current non-zero volumes
            currentState.streamStates.forEach {
                if (it.currentVolume > 0) {
                    previousVolumeCache[it.category] = it.currentVolume
                }
            }
            // Mute all streams
            StreamCategory.values().forEach { cat ->
                repository.setStreamVolume(cat.streamType, 0)
            }
            repository.setRingerMode(RingerModeType.VIBRATE)
            _uiState.update { it.copy(isMasterMuted = true, userMessage = "Semua saluran telah disenyapkan") }
        }
        onSystemVolumeChanged()
    }

    fun playPreview(category: StreamCategory) {
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            _uiState.update { it.copy(activePreviewCategory = category) }
            repository.playStreamPreview(category)
            delay(1200)
            repository.stopAnyActiveTone()
            _uiState.update { it.copy(activePreviewCategory = null) }
        }
    }

    fun stopPreview() {
        previewJob?.cancel()
        repository.stopAnyActiveTone()
        _uiState.update { it.copy(activePreviewCategory = null) }
    }

    fun applyPreset(preset: VolumePreset) {
        repository.setStreamPercentage(StreamCategory.MEDIA, preset.mediaPercent)
        repository.setStreamPercentage(StreamCategory.RING, preset.ringPercent)
        repository.setStreamPercentage(StreamCategory.NOTIFICATION, preset.notificationPercent)
        repository.setStreamPercentage(StreamCategory.ALARM, preset.alarmPercent)
        repository.setStreamPercentage(StreamCategory.CALL, preset.callPercent)
        repository.setRingerMode(preset.ringerMode)

        val updatedStreams = repository.getAllStreamStates()
        _uiState.update {
            it.copy(
                streamStates = updatedStreams,
                currentRingerMode = preset.ringerMode,
                activePresetId = preset.id,
                userMessage = "Profil '${preset.name}' telah diaktifkan!"
            )
        }
    }

    fun saveCustomPreset(name: String, description: String) {
        val currentStates = _uiState.value.streamStates
        val mediaP = currentStates.find { it.category == StreamCategory.MEDIA }?.percentage ?: 50
        val ringP = currentStates.find { it.category == StreamCategory.RING }?.percentage ?: 50
        val notifP = currentStates.find { it.category == StreamCategory.NOTIFICATION }?.percentage ?: 50
        val alarmP = currentStates.find { it.category == StreamCategory.ALARM }?.percentage ?: 70
        val callP = currentStates.find { it.category == StreamCategory.CALL }?.percentage ?: 60

        val newPreset = VolumePreset(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            name = name.ifBlank { "Profil Saya" },
            description = description.ifBlank { "Profil kelantangan tersuai" },
            iconKey = "tune",
            ringerMode = _uiState.value.currentRingerMode,
            mediaPercent = mediaP,
            ringPercent = ringP,
            notificationPercent = notifP,
            alarmPercent = alarmP,
            callPercent = callP,
            isCustom = true
        )

        repository.saveCustomPreset(newPreset)
        val updatedCustoms = repository.loadCustomPresets()
        _uiState.update {
            it.copy(
                customPresets = updatedCustoms,
                activePresetId = newPreset.id,
                showCreatePresetDialog = false,
                userMessage = "Profil '${newPreset.name}' berjaya disimpan!"
            )
        }
    }

    fun deleteCustomPreset(id: String) {
        repository.deleteCustomPreset(id)
        val updatedCustoms = repository.loadCustomPresets()
        _uiState.update {
            it.copy(
                customPresets = updatedCustoms,
                userMessage = "Profil telah dipadam"
            )
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    fun setShowCreatePresetDialog(show: Boolean) {
        _uiState.update { it.copy(showCreatePresetDialog = show) }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopAnyActiveTone()
    }
}
