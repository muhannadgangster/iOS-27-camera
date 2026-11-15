package com.example.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Range
import android.util.Size
import com.example.model.VideoFps
import com.example.model.VideoResolution

data class HardwareCapabilities(
  val hasFlash: Boolean,
  val hasAutoFocus: Boolean,
  val isMacroSupported: Boolean,
  val supportedVideoResolutions: List<VideoResolution>,
  val supportedVideoFps: List<VideoFps>,
  val maxPhotoResolution: Size?,
  val minExposureIndex: Int,
  val maxExposureIndex: Int,
  val exposureStep: Float
)

object CameraHardwareInfo {

  fun detectCapabilities(context: Context, isFront: Boolean): HardwareCapabilities {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
      ?: return fallbackCapabilities()

    val targetFacing = if (isFront) {
      CameraCharacteristics.LENS_FACING_FRONT
    } else {
      CameraCharacteristics.LENS_FACING_BACK
    }

    try {
      for (cameraId in cameraManager.cameraIdList) {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
        if (facing == targetFacing) {
          return inspectCharacteristics(characteristics)
        }
      }
      // If target facing not found, inspect first camera
      if (cameraManager.cameraIdList.isNotEmpty()) {
        val characteristics = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList[0])
        return inspectCharacteristics(characteristics)
      }
    } catch (_: Exception) {}

    return fallbackCapabilities()
  }

  private fun inspectCharacteristics(characteristics: CameraCharacteristics): HardwareCapabilities {
    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
    val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES) ?: intArrayOf()
    val hasAutoFocus = afModes.any { it != CameraCharacteristics.CONTROL_AF_MODE_OFF }

    // Macro capability check: minimum focus distance > 0 and macro AF mode available
    val minFocusDist = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0f
    val isMacroSupported = minFocusDist > 5f || afModes.contains(CameraCharacteristics.CONTROL_AF_MODE_MACRO)

    // Exposure compensation
    val aeRange = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE) ?: Range(-4, 4)
    val aeStepRational = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
    val exposureStep = aeStepRational?.toFloat() ?: 0.5f

    // Supported Video Sizes
    val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
    val videoSizes: Array<Size> = map?.getOutputSizes(android.media.MediaRecorder::class.java)
      ?: map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()

    val supportedResolutions = mutableListOf<VideoResolution>()
    if (videoSizes.any { (it.width >= 3840 && it.height >= 2160) || (it.width >= 2160 && it.height >= 3840) }) {
      supportedResolutions.add(VideoResolution.RES_4K)
    }
    if (videoSizes.any { (it.width >= 1920 && it.height >= 1080) || (it.width >= 1080 && it.height >= 1920) }) {
      supportedResolutions.add(VideoResolution.RES_1080P)
    }
    if (videoSizes.any { (it.width >= 1280 && it.height >= 720) || (it.width >= 720 && it.height >= 1280) }) {
      supportedResolutions.add(VideoResolution.RES_720P)
    }
    if (supportedResolutions.isEmpty()) {
      supportedResolutions.add(VideoResolution.RES_1080P)
      supportedResolutions.add(VideoResolution.RES_720P)
    }

    // Supported FPS Ranges
    val fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES) ?: emptyArray()
    val maxFps = fpsRanges.maxOfOrNull { it.upper } ?: 30

    val supportedFps = mutableListOf<VideoFps>()
    if (maxFps >= 60) {
      supportedFps.add(VideoFps.FPS_60)
    }
    supportedFps.add(VideoFps.FPS_30)
    if (fpsRanges.any { it.lower <= 24 && it.upper >= 24 }) {
      supportedFps.add(VideoFps.FPS_24)
    }
    if (fpsRanges.any { it.lower <= 25 && it.upper >= 25 }) {
      supportedFps.add(VideoFps.FPS_25)
    }

    // Photo max resolution
    val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
    val maxPhotoRes = jpegSizes.maxByOrNull { it.width * it.height }

    return HardwareCapabilities(
      hasFlash = hasFlash,
      hasAutoFocus = hasAutoFocus,
      isMacroSupported = isMacroSupported,
      supportedVideoResolutions = supportedResolutions,
      supportedVideoFps = supportedFps.distinct(),
      maxPhotoResolution = maxPhotoRes,
      minExposureIndex = aeRange.lower,
      maxExposureIndex = aeRange.upper,
      exposureStep = if (exposureStep > 0f) exposureStep else 0.5f
    )
  }

  private fun fallbackCapabilities(): HardwareCapabilities {
    return HardwareCapabilities(
      hasFlash = true,
      hasAutoFocus = true,
      isMacroSupported = false,
      supportedVideoResolutions = listOf(VideoResolution.RES_1080P, VideoResolution.RES_720P),
      supportedVideoFps = listOf(VideoFps.FPS_30, VideoFps.FPS_60),
      maxPhotoResolution = Size(4032, 3024),
      minExposureIndex = -4,
      maxExposureIndex = 4,
      exposureStep = 0.5f
    )
  }
}
