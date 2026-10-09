package com.example.solarsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarsystem.model.AppLanguage
import com.example.solarsystem.model.AstronomicalData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

data class SimulationState(
    val elapsedDays: Double = 0.0,
    val isPlaying: Boolean = false,
    val timeMultiplier: Double = 1.0,
    val galacticAngleDeg: Float = 0f,
    val language: AppLanguage = AppLanguage.EN,
    val showPlanetLabels: Boolean = true,
    val showMoonLabels: Boolean = false,
    val showStarLabels: Boolean = false,
    val showConstellationLines: Boolean = true,
    val showGalaxy: Boolean = true,
    val showCometOort: Boolean = false,
    val showAsteroidBelt: Boolean = true,
    val showSolarPaths: Boolean = false,
    val showHelicalPaths: Boolean = false,
    val showGalOrbit: Boolean = true,
    val pathVisibility: Map<String, Boolean> = emptyMap(),
    val focusTarget: String = "sun",
    val showAboutDialog: Boolean = false,
    val showHelpDialog: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val cameraYaw: Float = 0.6f,
    val cameraPitch: Float = 0.35f,
    val cameraDistance: Float = 450f
)

class SolarSystemViewModel : ViewModel() {
    private val epochMs: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(2000, Calendar.JANUARY, 1, 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val _uiState = MutableStateFlow(
        SimulationState(
            elapsedDays = (System.currentTimeMillis() - epochMs) / (1000.0 * 60.0 * 60.0 * 24.0),
            pathVisibility = buildMap {
                put("sun", true)
                put("apophis", true)
                AstronomicalData.planets.forEach { put(it.key, true) }
            }
        )
    )
    val uiState: StateFlow<SimulationState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (true) {
                delay(16)
                val now = System.nanoTime()
                val dtSec = (now - lastTime) / 1_000_000_000.0
                lastTime = now

                if (_uiState.value.isPlaying) {
                    val addDays = dtSec * _uiState.value.timeMultiplier
                    _uiState.update { it.copy(elapsedDays = it.elapsedDays + addDays) }
                }
            }
        }
    }

    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }

    fun setTimeMultiplier(multiplier: Double) {
        _uiState.update { it.copy(timeMultiplier = multiplier) }
    }

    fun setDateFromSliderMs(ms: Long) {
        val days = (ms - epochMs) / (1000.0 * 60.0 * 60.0 * 24.0)
        _uiState.update { it.copy(elapsedDays = days) }
    }

    fun resetToToday() {
        val days = (System.currentTimeMillis() - epochMs) / (1000.0 * 60.0 * 60.0 * 24.0)
        _uiState.update { it.copy(elapsedDays = days) }
    }

    fun setGalacticAngle(degrees: Float) {
        _uiState.update { it.copy(galacticAngleDeg = degrees) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang) }
    }

    fun setFocusTarget(target: String) {
        _uiState.update { it.copy(focusTarget = target) }
    }

    fun togglePlanetLabels() = _uiState.update { it.copy(showPlanetLabels = !it.showPlanetLabels) }
    fun toggleMoonLabels() = _uiState.update { it.copy(showMoonLabels = !it.showMoonLabels) }
    fun toggleStarLabels() = _uiState.update { it.copy(showStarLabels = !it.showStarLabels) }
    fun toggleConstellationLines() = _uiState.update { it.copy(showConstellationLines = !it.showConstellationLines) }
    fun toggleGalaxy() = _uiState.update { it.copy(showGalaxy = !it.showGalaxy) }
    fun toggleCometOort() = _uiState.update { it.copy(showCometOort = !it.showCometOort) }
    fun toggleAsteroidBelt() = _uiState.update { it.copy(showAsteroidBelt = !it.showAsteroidBelt) }
    fun toggleSolarPaths() = _uiState.update { it.copy(showSolarPaths = !it.showSolarPaths) }
    fun toggleHelicalPaths() = _uiState.update { it.copy(showHelicalPaths = !it.showHelicalPaths) }
    fun toggleGalOrbit() = _uiState.update { it.copy(showGalOrbit = !it.showGalOrbit) }

    fun togglePathItem(key: String) {
        _uiState.update { state ->
            val updated = state.pathVisibility.toMutableMap()
            updated[key] = !(updated[key] ?: true)
            state.copy(pathVisibility = updated)
        }
    }

    fun toggleAllPaths() {
        _uiState.update { state ->
            val allOn = state.pathVisibility.values.all { it }
            val updated = state.pathVisibility.keys.associateWith { !allOn }
            state.copy(pathVisibility = updated)
        }
    }

    fun setDrawerOpen(isOpen: Boolean) = _uiState.update { it.copy(isDrawerOpen = isOpen) }
    fun setShowAboutDialog(show: Boolean) = _uiState.update { it.copy(showAboutDialog = show) }
    fun setShowHelpDialog(show: Boolean) = _uiState.update { it.copy(showHelpDialog = show) }

    fun zoomIn() {
        _uiState.update { it.copy(cameraDistance = (it.cameraDistance * 0.75f).coerceAtLeast(15f)) }
    }

    fun zoomOut() {
        _uiState.update { it.copy(cameraDistance = (it.cameraDistance * 1.33f).coerceAtMost(5000f)) }
    }

    fun rotateCamera(deltaYaw: Float, deltaPitch: Float) {
        _uiState.update {
            val newYaw = it.cameraYaw + deltaYaw
            val newPitch = (it.cameraPitch + deltaPitch).coerceIn(-1.5f, 1.5f)
            it.copy(cameraYaw = newYaw, cameraPitch = newPitch)
        }
    }

    fun adjustCameraDistance(factor: Float) {
        _uiState.update {
            it.copy(cameraDistance = (it.cameraDistance * factor).coerceIn(15f, 5000f))
        }
    }

    fun resetCamera() {
        val days = (System.currentTimeMillis() - epochMs) / (1000.0 * 60.0 * 60.0 * 24.0)
        _uiState.update {
            it.copy(
                elapsedDays = days,
                isPlaying = false,
                timeMultiplier = 1.0,
                galacticAngleDeg = 0f,
                focusTarget = "sun",
                cameraYaw = 0.6f,
                cameraPitch = 0.35f,
                cameraDistance = 450f,
                showPlanetLabels = false,
                showMoonLabels = false,
                showStarLabels = false,
                showConstellationLines = true,
                showGalaxy = true,
                showCometOort = false,
                showAsteroidBelt = true,
                showSolarPaths = false,
                showHelicalPaths = false,
                showGalOrbit = true
            )
        }
    }
}
