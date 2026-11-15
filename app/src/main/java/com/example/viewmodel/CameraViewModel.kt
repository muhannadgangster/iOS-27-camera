package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.CameraHardwareInfo
import com.example.camera.CameraXManager
import com.example.data.CameraPreferences
import com.example.data.MediaRepository
import com.example.model.AspectRatioSetting
import com.example.model.CameraControlId
import com.example.model.CameraMode
import com.example.model.CameraUiState
import com.example.model.CapturedMedia
import com.example.model.ControlItem
import com.example.model.FilterAdjustments
import com.example.model.FlashMode
import com.example.model.GridMode
import com.example.model.PhotographicStyle
import com.example.model.TimerSetting
import com.example.model.VideoFps
import com.example.model.VideoResolution
import com.example.processing.ImageFilterProcessor
import com.example.sensor.OrientationLevelSensor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

  private val context: Context = application.applicationContext
  private val preferences = CameraPreferences(context)
  private val mediaRepository = MediaRepository(context)
  private val imageFilterProcessor = ImageFilterProcessor(context)
  private val levelSensor = OrientationLevelSensor(context)

  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

  var cameraXManager: CameraXManager? = null
  private var countdownJob: Job? = null

  init {
    loadSavedPreferences()
    detectHardwareCapabilities()
    loadRecentMedia()
    startSensorListening()
  }

  private fun loadSavedPreferences() {
    _uiState.update { current ->
      current.copy(
        currentMode = preferences.lastCameraMode,
        flashMode = preferences.lastFlashMode,
        timerSetting = preferences.lastTimerSetting,
        aspectRatio = preferences.lastAspectRatio,
        gridMode = preferences.lastGridMode,
        isLevelEnabled = preferences.isLevelEnabled,
        selectedFilter = preferences.lastSelectedStyle,
        videoResolution = preferences.lastVideoResolution,
        videoFps = preferences.lastVideoFps,
        isStabilizationEnabled = preferences.isStabilizationEnabled,
        isLiquidGlassEnabled = preferences.isLiquidGlassEnabled,
        isGlassReflectionsEnabled = preferences.isGlassReflectionsEnabled,
        isBackgroundRefractionEnabled = preferences.isBackgroundRefractionEnabled,
        isMotionEffectsEnabled = preferences.isMotionEffectsEnabled,
        isPerformanceModeEnabled = preferences.isPerformanceModeEnabled,
        animationQuality = preferences.animationQuality,
        customControlsList = preferences.getCustomControls()
      )
    }
  }

  fun detectHardwareCapabilities() {
    val caps = CameraHardwareInfo.detectCapabilities(context, _uiState.value.isFrontCamera)
    _uiState.update { current ->
      current.copy(
        supportedVideoResolutions = caps.supportedVideoResolutions,
        supportedVideoFps = caps.supportedVideoFps,
        minExposureIndex = caps.minExposureIndex,
        maxExposureIndex = caps.maxExposureIndex,
        exposureStep = caps.exposureStep,
        photoResolutionText = caps.maxPhotoResolution?.let { "${(it.width * it.height) / 1_000_000} MP" } ?: "12 MP"
      )
    }
  }

  private fun startSensorListening() {
    levelSensor.startListening { rollDegrees, isAligned ->
      if (_uiState.value.isLevelEnabled) {
        _uiState.update { it.copy(levelRollDegrees = rollDegrees, isLevelAligned = isAligned) }
      }
    }
  }

  fun loadRecentMedia() {
    viewModelScope.launch {
      val mediaList = mediaRepository.getRecentCapturedMedia(limit = 60)
      _uiState.update { current ->
        current.copy(
          recentMediaList = mediaList,
          selectedMediaItem = current.selectedMediaItem ?: mediaList.firstOrNull()
        )
      }
    }
  }

  fun onModeChange(newMode: CameraMode) {
    preferences.lastCameraMode = newMode
    _uiState.update { it.copy(currentMode = newMode, isControlsPanelOpen = false) }
    cameraXManager?.configure(
      mode = newMode,
      isFront = _uiState.value.isFrontCamera,
      flash = _uiState.value.flashMode,
      videoRes = _uiState.value.videoResolution
    )
  }

  fun switchCamera() {
    val newFacing = !_uiState.value.isFrontCamera
    _uiState.update { it.copy(isFrontCamera = newFacing) }
    detectHardwareCapabilities()
    cameraXManager?.configure(
      mode = _uiState.value.currentMode,
      isFront = newFacing,
      flash = _uiState.value.flashMode,
      videoRes = _uiState.value.videoResolution
    )
  }

  fun toggleFlash() {
    val next = when (_uiState.value.flashMode) {
      FlashMode.AUTO -> FlashMode.ON
      FlashMode.ON -> FlashMode.OFF
      FlashMode.OFF -> FlashMode.AUTO
    }
    setFlash(next)
  }

  fun setFlash(mode: FlashMode) {
    preferences.lastFlashMode = mode
    _uiState.update { it.copy(flashMode = mode) }
    cameraXManager?.setFlash(mode)
  }

  fun setTimer(setting: TimerSetting) {
    preferences.lastTimerSetting = setting
    _uiState.update { it.copy(timerSetting = setting) }
  }

  fun setAspectRatio(setting: AspectRatioSetting) {
    preferences.lastAspectRatio = setting
    _uiState.update { it.copy(aspectRatio = setting) }
  }

  fun setGridMode(mode: GridMode) {
    preferences.lastGridMode = mode
    _uiState.update { it.copy(gridMode = mode) }
  }

  fun toggleLevel(enabled: Boolean) {
    preferences.isLevelEnabled = enabled
    _uiState.update { it.copy(isLevelEnabled = enabled) }
  }

  fun setZoom(ratio: Float) {
    _uiState.update { it.copy(zoomRatio = ratio) }
    cameraXManager?.setZoom(ratio)
  }

  fun setExposureIndex(index: Int) {
    val clamped = index.coerceIn(_uiState.value.minExposureIndex, _uiState.value.maxExposureIndex)
    _uiState.update { it.copy(exposureIndex = clamped) }
    cameraXManager?.setExposureIndex(clamped)
  }

  fun adjustExposureByDrag(deltaEv: Float) {
    val step = _uiState.value.exposureStep
    val deltaIndex = (deltaEv / step).toInt()
    if (deltaIndex != 0) {
      setExposureIndex(_uiState.value.exposureIndex + deltaIndex)
    }
  }

  fun selectPhotographicStyle(style: PhotographicStyle) {
    preferences.lastSelectedStyle = style
    _uiState.update { it.copy(selectedFilter = style) }
  }

  fun updateFilterAdjustments(adjustments: FilterAdjustments) {
    _uiState.update { it.copy(filterAdjustments = adjustments) }
  }

  fun resetFilterAdjustments() {
    _uiState.update { it.copy(filterAdjustments = FilterAdjustments()) }
  }

  fun setVideoResolution(res: VideoResolution) {
    preferences.lastVideoResolution = res
    _uiState.update { it.copy(videoResolution = res) }
    cameraXManager?.configure(
      mode = _uiState.value.currentMode,
      isFront = _uiState.value.isFrontCamera,
      flash = _uiState.value.flashMode,
      videoRes = res
    )
  }

  fun cycleVideoResolution() {
    val supported = _uiState.value.supportedVideoResolutions
    if (supported.isNotEmpty()) {
      val currentIndex = supported.indexOf(_uiState.value.videoResolution)
      val nextIndex = (currentIndex + 1) % supported.size
      setVideoResolution(supported[nextIndex])
    }
  }

  fun setVideoFps(fps: VideoFps) {
    preferences.lastVideoFps = fps
    _uiState.update { it.copy(videoFps = fps) }
  }

  fun cycleVideoFps() {
    val supported = _uiState.value.supportedVideoFps
    if (supported.isNotEmpty()) {
      val currentIndex = supported.indexOf(_uiState.value.videoFps)
      val nextIndex = (currentIndex + 1) % supported.size
      setVideoFps(supported[nextIndex])
    }
  }

  fun toggleStabilization(enabled: Boolean) {
    preferences.isStabilizationEnabled = enabled
    _uiState.update { it.copy(isStabilizationEnabled = enabled) }
  }

  fun toggleControlsPanel() {
    _uiState.update { it.copy(isControlsPanelOpen = !it.isControlsPanelOpen) }
  }

  fun closeControlsPanel() {
    _uiState.update { it.copy(isControlsPanelOpen = false) }
  }

  fun openCustomizeControls() {
    _uiState.update { it.copy(isCustomizeControlsOpen = true, isControlsPanelOpen = false) }
  }

  fun closeCustomizeControls() {
    _uiState.update { it.copy(isCustomizeControlsOpen = false) }
  }

  fun saveCustomControls(controls: List<ControlItem>) {
    preferences.saveCustomControls(controls)
    _uiState.update { it.copy(customControlsList = controls, isCustomizeControlsOpen = false) }
  }

  fun resetCustomControlsDefaults() {
    val defaults = preferences.getDefaultControls()
    preferences.saveCustomControls(defaults)
    _uiState.update { it.copy(customControlsList = defaults) }
  }

  fun openSettings() {
    _uiState.update { it.copy(isSettingsOpen = true, isControlsPanelOpen = false) }
  }

  fun closeSettings() {
    _uiState.update { it.copy(isSettingsOpen = false) }
  }

  fun openGallery(mediaItem: CapturedMedia? = null) {
    _uiState.update {
      it.copy(
        isGalleryOpen = true,
        selectedMediaItem = mediaItem ?: it.recentMediaList.firstOrNull(),
        isControlsPanelOpen = false
      )
    }
  }

  fun closeGallery() {
    _uiState.update { it.copy(isGalleryOpen = false) }
    loadRecentMedia()
  }

  fun onFocusTap(normX: Float, normY: Float, width: Float, height: Float) {
    _uiState.update { it.copy(focusPoint = Pair(normX, normY), isAeAfLocked = false) }
    cameraXManager?.focusAndMeterAt(normX, normY, width, height)

    // Auto-clear reticle after 3.5 seconds
    viewModelScope.launch {
      delay(3500)
      if (!_uiState.value.isAeAfLocked) {
        _uiState.update { it.copy(focusPoint = null) }
      }
    }
  }

  fun toggleAeAfLock() {
    val newLock = !_uiState.value.isAeAfLocked
    _uiState.update { it.copy(isAeAfLocked = newLock) }
    cameraXManager?.lockAeAf(newLock)
  }

  fun onShutterPress(hasAudioPermission: Boolean) {
    val state = _uiState.value

    if (state.currentMode.isVideoMode) {
      if (state.isRecordingVideo) {
        // Stop recording
        cameraXManager?.stopVideoRecording()
        _uiState.update { it.copy(isRecordingVideo = false, recordingDurationSeconds = 0L) }
      } else {
        // Start recording
        _uiState.update { it.copy(isRecordingVideo = true, recordingDurationSeconds = 0L) }
        cameraXManager?.startVideoRecording(hasAudioPermission) { error ->
          Log.e("CameraViewModel", "Recording start failed: $error")
          _uiState.update { it.copy(isRecordingVideo = false) }
        }
      }
    } else {
      // Photo capture
      val timerSec = state.timerSetting.seconds
      if (timerSec > 0) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
          for (sec in timerSec downTo 1) {
            _uiState.update { it.copy(countdownRemaining = sec) }
            delay(1000)
          }
          _uiState.update { it.copy(countdownRemaining = 0) }
          performPhotoCapture()
        }
      } else {
        performPhotoCapture()
      }
    }
  }

  private fun performPhotoCapture() {
    val state = _uiState.value
    cameraXManager?.capturePhoto(
      style = state.selectedFilter,
      adjustments = state.filterAdjustments,
      aspectRatioSetting = state.aspectRatio,
      onSuccess = { media ->
        _uiState.update { current ->
          val updated = listOf(media) + current.recentMediaList
          current.copy(
            recentMediaList = updated,
            selectedMediaItem = media
          )
        }
      },
      onError = { error ->
        Log.e("CameraViewModel", "Photo capture failed", error)
      }
    )
  }

  fun deleteMedia(media: CapturedMedia) {
    viewModelScope.launch {
      val success = mediaRepository.deleteMedia(media)
      if (success) {
        val updated = _uiState.value.recentMediaList.filter { it.uri != media.uri }
        _uiState.update { current ->
          current.copy(
            recentMediaList = updated,
            selectedMediaItem = updated.firstOrNull()
          )
        }
      }
    }
  }

  fun createShareIntent(media: CapturedMedia): Intent {
    return mediaRepository.createShareIntent(media)
  }

  // Animation & UI toggles
  fun setLiquidGlassEnabled(enabled: Boolean) {
    preferences.isLiquidGlassEnabled = enabled
    _uiState.update { it.copy(isLiquidGlassEnabled = enabled) }
  }

  fun setGlassReflectionsEnabled(enabled: Boolean) {
    preferences.isGlassReflectionsEnabled = enabled
    _uiState.update { it.copy(isGlassReflectionsEnabled = enabled) }
  }

  fun setBgRefractionEnabled(enabled: Boolean) {
    preferences.isBackgroundRefractionEnabled = enabled
    _uiState.update { it.copy(isBackgroundRefractionEnabled = enabled) }
  }

  fun setMotionEffectsEnabled(enabled: Boolean) {
    preferences.isMotionEffectsEnabled = enabled
    _uiState.update { it.copy(isMotionEffectsEnabled = enabled) }
  }

  fun setPerformanceModeEnabled(enabled: Boolean) {
    preferences.isPerformanceModeEnabled = enabled
    _uiState.update { it.copy(isPerformanceModeEnabled = enabled) }
  }

  fun setAnimationQuality(quality: String) {
    preferences.animationQuality = quality
    _uiState.update { it.copy(animationQuality = quality) }
  }

  fun resetAllSettings() {
    preferences.lastCameraMode = CameraMode.PHOTO
    preferences.lastFlashMode = FlashMode.AUTO
    preferences.lastTimerSetting = TimerSetting.OFF
    preferences.lastAspectRatio = AspectRatioSetting.RATIO_4_3
    preferences.lastGridMode = GridMode.RULE_OF_THIRDS
    preferences.isLevelEnabled = true
    preferences.lastSelectedStyle = PhotographicStyle.ORIGINAL
    preferences.lastVideoResolution = VideoResolution.RES_1080P
    preferences.lastVideoFps = VideoFps.FPS_30
    preferences.isStabilizationEnabled = true
    preferences.isLiquidGlassEnabled = true
    preferences.isGlassReflectionsEnabled = true
    preferences.isBackgroundRefractionEnabled = true
    preferences.isMotionEffectsEnabled = true
    preferences.isPerformanceModeEnabled = false
    preferences.animationQuality = "High"
    preferences.saveCustomControls(preferences.getDefaultControls())

    loadSavedPreferences()
  }

  override fun onCleared() {
    super.onCleared()
    levelSensor.stopListening()
    countdownJob?.cancel()
    cameraXManager?.release()
  }
}
