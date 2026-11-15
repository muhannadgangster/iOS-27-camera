package com.example.model

import android.net.Uri

enum class CameraMode(val title: String) {
  TIME_LAPSE("TIME-LAPSE"),
  SLO_MO("SLO-MO"),
  VIDEO("VIDEO"),
  PHOTO("PHOTO"),
  PORTRAIT("PORTRAIT"),
  NIGHT("NIGHT"),
  PANO("PANO"),
  MACRO("MACRO");

  val isVideoMode: Boolean
    get() = this == VIDEO || this == SLO_MO || this == TIME_LAPSE
}

enum class FlashMode(val displayName: String) {
  AUTO("Auto"),
  ON("On"),
  OFF("Off")
}

enum class TimerSetting(val seconds: Int, val label: String) {
  OFF(0, "Off"),
  SEC_3(3, "3s"),
  SEC_10(10, "10s")
}

enum class AspectRatioSetting(val label: String, val ratio: Float) {
  RATIO_4_3("4:3", 4f / 3f),
  RATIO_16_9("16:9", 16f / 9f),
  RATIO_1_1("1:1", 1f / 1f)
}

enum class GridMode(val label: String) {
  OFF("Off"),
  RULE_OF_THIRDS("3×3"),
  SQUARE("Square"),
  GOLDEN_RATIO("Golden")
}

enum class PhotographicStyle(
  val displayName: String,
  val description: String,
  val defaultContrast: Float = 1f,
  val defaultSaturation: Float = 1f,
  val defaultWarmth: Float = 0f,
  val defaultTint: Float = 0f
) {
  ORIGINAL("Natural", "Standard balanced colors", 1f, 1f, 0f, 0f),
  VIVID("Vivid", "Punchy vibrant colors with rich pop", 1.15f, 1.35f, 0.05f, 0f),
  DRAMATIC("Dramatic", "Deep cinematic shadows and highlights", 1.35f, 0.95f, -0.05f, 0f),
  WARM("Warm", "Golden daylight and flattering warm glow", 1.05f, 1.1f, 0.35f, 0.05f),
  COOL("Cool", "Clean slate tones and crisp blue highlights", 1.05f, 1.05f, -0.3f, 0f),
  MONO("Mono", "Classic neutral black and white", 1.1f, 0f, 0f, 0f),
  SILVERTONE("Silvertone", "High key black and white with silver sheen", 1.3f, 0f, 0.1f, 0f),
  NOIR("Noir", "Cinematic crushed blacks and deep mood", 1.5f, 0f, -0.1f, 0f),
  RICH_CONTRAST("Rich Contrast", "Darkened shadows with saturated accents", 1.4f, 1.15f, 0f, 0f),
  WARM_CONTRAST("Warm Contrast", "Rich contrast with golden warmth", 1.35f, 1.15f, 0.25f, 0.05f),
  COOL_CONTRAST("Cool Contrast", "Rich contrast with icy blue clarity", 1.35f, 1.1f, -0.25f, 0f)
}

data class FilterAdjustments(
  val intensity: Float = 1.0f,      // 0..1
  val brightness: Float = 0.0f,     // -0.5..0.5
  val contrast: Float = 1.0f,       // 0.5..1.8
  val saturation: Float = 1.0f,     // 0..2
  val warmth: Float = 0.0f,         // -0.5..0.5
  val tint: Float = 0.0f,           // -0.5..0.5
  val sharpness: Float = 0.0f       // 0..1
)

enum class VideoResolution(val label: String, val width: Int, val height: Int) {
  RES_4K("4K", 3840, 2160),
  RES_1080P("1080p", 1920, 1080),
  RES_720P("720p", 1280, 720)
}

enum class VideoFps(val label: String, val fps: Int) {
  FPS_24("24 FPS", 24),
  FPS_25("25 FPS", 25),
  FPS_30("30 FPS", 30),
  FPS_60("60 FPS", 60)
}

enum class CameraControlId(val defaultTitle: String) {
  FLASH("Flash"),
  NIGHT("Night Mode"),
  TIMER("Timer"),
  EXPOSURE("Exposure"),
  STYLES("Photographic Styles"),
  ASPECT_RATIO("Aspect Ratio"),
  GRID("Camera Grid"),
  LEVEL("Horizon Level"),
  RESOLUTION("Resolution"),
  FPS("Frame Rate"),
  STABILIZATION("Stabilization"),
  MACRO("Macro Mode")
}

data class ControlItem(
  val id: CameraControlId,
  val title: String,
  val isPinned: Boolean = false,
  val isEnabled: Boolean = true
)

data class CapturedMedia(
  val uri: Uri,
  val path: String,
  val displayName: String,
  val dateTaken: Long,
  val isVideo: Boolean,
  val durationMs: Long = 0L,
  val width: Int = 0,
  val height: Int = 0,
  val sizeBytes: Long = 0L
)

data class CameraUiState(
  val currentMode: CameraMode = CameraMode.PHOTO,
  val isFrontCamera: Boolean = false,
  val flashMode: FlashMode = FlashMode.AUTO,
  val timerSetting: TimerSetting = TimerSetting.OFF,
  val aspectRatio: AspectRatioSetting = AspectRatioSetting.RATIO_4_3,
  val gridMode: GridMode = GridMode.RULE_OF_THIRDS,
  val isLevelEnabled: Boolean = true,
  val levelRollDegrees: Float = 0f,
  val isLevelAligned: Boolean = false,
  val exposureIndex: Int = 0,
  val minExposureIndex: Int = -4,
  val maxExposureIndex: Int = 4,
  val exposureStep: Float = 0.5f,
  val zoomRatio: Float = 1.0f,
  val minZoomRatio: Float = 1.0f,
  val maxZoomRatio: Float = 5.0f,
  val selectedFilter: PhotographicStyle = PhotographicStyle.ORIGINAL,
  val filterAdjustments: FilterAdjustments = FilterAdjustments(),
  val isAeAfLocked: Boolean = false,
  val focusPoint: Pair<Float, Float>? = null,
  val isRecordingVideo: Boolean = false,
  val isRecordingPaused: Boolean = false,
  val recordingDurationSeconds: Long = 0L,
  val countdownRemaining: Int = 0,
  val isControlsPanelOpen: Boolean = false,
  val activeControlDetail: CameraControlId? = null,
  val isSettingsOpen: Boolean = false,
  val isCustomizeControlsOpen: Boolean = false,
  val isGalleryOpen: Boolean = false,
  val selectedMediaItem: CapturedMedia? = null,
  val recentMediaList: List<CapturedMedia> = emptyList(),
  val isTorchOn: Boolean = false,
  val isStabilizationEnabled: Boolean = true,
  val videoResolution: VideoResolution = VideoResolution.RES_1080P,
  val videoFps: VideoFps = VideoFps.FPS_30,
  val photoResolutionText: String = "12 MP",
  val supportedVideoResolutions: List<VideoResolution> = listOf(VideoResolution.RES_1080P, VideoResolution.RES_720P),
  val supportedVideoFps: List<VideoFps> = listOf(VideoFps.FPS_30, VideoFps.FPS_60),
  val animationQuality: String = "High",
  val isLiquidGlassEnabled: Boolean = true,
  val isGlassReflectionsEnabled: Boolean = true,
  val isBackgroundRefractionEnabled: Boolean = true,
  val isMotionEffectsEnabled: Boolean = true,
  val isPerformanceModeEnabled: Boolean = false,
  val customControlsList: List<ControlItem> = emptyList()
) {
  val formattedExposureEv: String
    get() {
      val ev = exposureIndex * exposureStep
      return if (ev >= 0f) "+%.1f".format(ev) else "%.1f".format(ev)
    }

  val formattedRecordingTime: String
    get() {
      val minutes = recordingDurationSeconds / 60
      val seconds = recordingDurationSeconds % 60
      return "%02d:%02d".format(minutes, seconds)
    }
}
