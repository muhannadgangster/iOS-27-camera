package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AspectRatioSetting
import com.example.model.CameraControlId
import com.example.model.CameraMode
import com.example.model.ControlItem
import com.example.model.FlashMode
import com.example.model.GridMode
import com.example.model.PhotographicStyle
import com.example.model.TimerSetting
import com.example.model.VideoFps
import com.example.model.VideoResolution
import org.json.JSONArray
import org.json.JSONObject

class CameraPreferences(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("ios27_camera_prefs", Context.MODE_PRIVATE)

  var lastCameraMode: CameraMode
    get() {
      val name = prefs.getString("camera_mode", CameraMode.PHOTO.name)
      return runCatching { CameraMode.valueOf(name!!) }.getOrDefault(CameraMode.PHOTO)
    }
    set(value) = prefs.edit().putString("camera_mode", value.name).apply()

  var lastFlashMode: FlashMode
    get() {
      val name = prefs.getString("flash_mode", FlashMode.AUTO.name)
      return runCatching { FlashMode.valueOf(name!!) }.getOrDefault(FlashMode.AUTO)
    }
    set(value) = prefs.edit().putString("flash_mode", value.name).apply()

  var lastTimerSetting: TimerSetting
    get() {
      val name = prefs.getString("timer_setting", TimerSetting.OFF.name)
      return runCatching { TimerSetting.valueOf(name!!) }.getOrDefault(TimerSetting.OFF)
    }
    set(value) = prefs.edit().putString("timer_setting", value.name).apply()

  var lastAspectRatio: AspectRatioSetting
    get() {
      val name = prefs.getString("aspect_ratio", AspectRatioSetting.RATIO_4_3.name)
      return runCatching { AspectRatioSetting.valueOf(name!!) }.getOrDefault(AspectRatioSetting.RATIO_4_3)
    }
    set(value) = prefs.edit().putString("aspect_ratio", value.name).apply()

  var lastGridMode: GridMode
    get() {
      val name = prefs.getString("grid_mode", GridMode.RULE_OF_THIRDS.name)
      return runCatching { GridMode.valueOf(name!!) }.getOrDefault(GridMode.RULE_OF_THIRDS)
    }
    set(value) = prefs.edit().putString("grid_mode", value.name).apply()

  var isLevelEnabled: Boolean
    get() = prefs.getBoolean("level_enabled", true)
    set(value) = prefs.edit().putBoolean("level_enabled", value).apply()

  var lastSelectedStyle: PhotographicStyle
    get() {
      val name = prefs.getString("selected_style", PhotographicStyle.ORIGINAL.name)
      return runCatching { PhotographicStyle.valueOf(name!!) }.getOrDefault(PhotographicStyle.ORIGINAL)
    }
    set(value) = prefs.edit().putString("selected_style", value.name).apply()

  var lastVideoResolution: VideoResolution
    get() {
      val name = prefs.getString("video_res", VideoResolution.RES_1080P.name)
      return runCatching { VideoResolution.valueOf(name!!) }.getOrDefault(VideoResolution.RES_1080P)
    }
    set(value) = prefs.edit().putString("video_res", value.name).apply()

  var lastVideoFps: VideoFps
    get() {
      val name = prefs.getString("video_fps", VideoFps.FPS_30.name)
      return runCatching { VideoFps.valueOf(name!!) }.getOrDefault(VideoFps.FPS_30)
    }
    set(value) = prefs.edit().putString("video_fps", value.name).apply()

  var isStabilizationEnabled: Boolean
    get() = prefs.getBoolean("stabilization_enabled", true)
    set(value) = prefs.edit().putBoolean("stabilization_enabled", value).apply()

  var isLiquidGlassEnabled: Boolean
    get() = prefs.getBoolean("liquid_glass_enabled", true)
    set(value) = prefs.edit().putBoolean("liquid_glass_enabled", value).apply()

  var isGlassReflectionsEnabled: Boolean
    get() = prefs.getBoolean("glass_reflections_enabled", true)
    set(value) = prefs.edit().putBoolean("glass_reflections_enabled", value).apply()

  var isBackgroundRefractionEnabled: Boolean
    get() = prefs.getBoolean("bg_refraction_enabled", true)
    set(value) = prefs.edit().putBoolean("bg_refraction_enabled", value).apply()

  var isMotionEffectsEnabled: Boolean
    get() = prefs.getBoolean("motion_effects_enabled", true)
    set(value) = prefs.edit().putBoolean("motion_effects_enabled", value).apply()

  var isPerformanceModeEnabled: Boolean
    get() = prefs.getBoolean("performance_mode_enabled", false)
    set(value) = prefs.edit().putBoolean("performance_mode_enabled", value).apply()

  var animationQuality: String
    get() = prefs.getString("anim_quality", "High") ?: "High"
    set(value) = prefs.edit().putString("anim_quality", value).apply()

  fun getCustomControls(): List<ControlItem> {
    val rawJson = prefs.getString("custom_controls_json", null)
    if (rawJson.isNullOrBlank()) {
      return getDefaultControls()
    }
    return try {
      val array = JSONArray(rawJson)
      val list = mutableListOf<ControlItem>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val idStr = obj.getString("id")
        val id = runCatching { CameraControlId.valueOf(idStr) }.getOrNull()
        if (id != null) {
          list.add(
            ControlItem(
              id = id,
              title = obj.optString("title", id.defaultTitle),
              isPinned = obj.optBoolean("isPinned", false),
              isEnabled = obj.optBoolean("isEnabled", true)
            )
          )
        }
      }
      // Add any missing new controls
      val existingIds = list.map { it.id }.toSet()
      val missing = getDefaultControls().filter { it.id !in existingIds }
      list + missing
    } catch (_: Exception) {
      getDefaultControls()
    }
  }

  fun saveCustomControls(controls: List<ControlItem>) {
    val array = JSONArray()
    for (item in controls) {
      val obj = JSONObject().apply {
        put("id", item.id.name)
        put("title", item.title)
        put("isPinned", item.isPinned)
        put("isEnabled", item.isEnabled)
      }
      array.put(obj)
    }
    prefs.edit().putString("custom_controls_json", array.toString()).apply()
  }

  fun getDefaultControls(): List<ControlItem> {
    return listOf(
      ControlItem(CameraControlId.FLASH, "Flash", isPinned = true, isEnabled = true),
      ControlItem(CameraControlId.NIGHT, "Night Mode", isPinned = true, isEnabled = true),
      ControlItem(CameraControlId.STYLES, "Photographic Styles", isPinned = true, isEnabled = true),
      ControlItem(CameraControlId.EXPOSURE, "Exposure", isPinned = true, isEnabled = true),
      ControlItem(CameraControlId.TIMER, "Timer", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.ASPECT_RATIO, "Aspect Ratio", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.GRID, "Camera Grid", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.LEVEL, "Horizon Level", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.RESOLUTION, "Resolution", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.FPS, "Frame Rate", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.STABILIZATION, "Stabilization", isPinned = false, isEnabled = true),
      ControlItem(CameraControlId.MACRO, "Macro Mode", isPinned = false, isEnabled = true)
    )
  }
}
