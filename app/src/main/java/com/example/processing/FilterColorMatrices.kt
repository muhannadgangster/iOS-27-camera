package com.example.processing

import android.graphics.ColorMatrix as AndroidColorMatrix
import androidx.compose.ui.graphics.ColorMatrix as ComposeColorMatrix
import com.example.model.FilterAdjustments
import com.example.model.PhotographicStyle

object FilterColorMatrices {

  fun createAndroidColorMatrix(
    style: PhotographicStyle,
    adjustments: FilterAdjustments
  ): AndroidColorMatrix {
    val matrix = AndroidColorMatrix()

    if (style == PhotographicStyle.ORIGINAL &&
      adjustments.intensity == 1f &&
      adjustments.brightness == 0f &&
      adjustments.contrast == 1f &&
      adjustments.saturation == 1f &&
      adjustments.warmth == 0f &&
      adjustments.tint == 0f
    ) {
      return matrix // Identity
    }

    // 1. Base style transformation
    val baseMatrix = getBaseStyleMatrix(style)

    // 2. Adjust intensity: interpolate between Identity and baseMatrix
    val interpolatedMatrix = if (adjustments.intensity < 1.0f) {
      interpolateWithIdentity(baseMatrix, adjustments.intensity)
    } else {
      baseMatrix
    }

    // 3. Apply Saturation
    val effectiveSaturation = adjustments.saturation * style.defaultSaturation
    val satMatrix = AndroidColorMatrix()
    satMatrix.setSaturation(effectiveSaturation.coerceIn(0f, 3f))

    // 4. Apply Contrast & Brightness
    val effectiveContrast = adjustments.contrast * style.defaultContrast
    val contrastMatrix = createContrastBrightnessMatrix(
      effectiveContrast.coerceIn(0.2f, 2.5f),
      adjustments.brightness.coerceIn(-0.5f, 0.5f)
    )

    // 5. Apply Warmth & Tint
    val effectiveWarmth = adjustments.warmth + style.defaultWarmth
    val effectiveTint = adjustments.tint + style.defaultTint
    val temperatureMatrix = createWarmthTintMatrix(
      effectiveWarmth.coerceIn(-0.6f, 0.6f),
      effectiveTint.coerceIn(-0.6f, 0.6f)
    )

    // Concatenate all matrices
    matrix.postConcat(interpolatedMatrix)
    matrix.postConcat(satMatrix)
    matrix.postConcat(contrastMatrix)
    matrix.postConcat(temperatureMatrix)

    return matrix
  }

  fun createComposeColorMatrix(
    style: PhotographicStyle,
    adjustments: FilterAdjustments
  ): ComposeColorMatrix {
    val androidMatrix = createAndroidColorMatrix(style, adjustments)
    return ComposeColorMatrix(androidMatrix.array)
  }

  private fun getBaseStyleMatrix(style: PhotographicStyle): AndroidColorMatrix {
    val m = AndroidColorMatrix()
    when (style) {
      PhotographicStyle.ORIGINAL -> {
        // Identity
      }
      PhotographicStyle.VIVID -> {
        // Boost vibrancy, subtle warm highlight
        m.set(
          floatArrayOf(
            1.12f, 0f, 0f, 0f, 5f,
            0f, 1.12f, 0f, 0f, 5f,
            0f, 0f, 1.05f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.DRAMATIC -> {
        // High contrast, moodier midtones and deep shadows
        m.set(
          floatArrayOf(
            1.25f, -0.05f, -0.05f, 0f, -15f,
            -0.05f, 1.25f, -0.05f, 0f, -15f,
            -0.05f, -0.05f, 1.2f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.WARM -> {
        // Warm golden tones, boosted red/amber, gentle blue dip
        m.set(
          floatArrayOf(
            1.15f, 0f, 0f, 0f, 15f,
            0f, 1.05f, 0f, 0f, 5f,
            0f, 0f, 0.88f, 0f, -10f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.COOL -> {
        // Cool crisp slate tones, boosted blues, crisp whites
        m.set(
          floatArrayOf(
            0.9f, 0f, 0f, 0f, -8f,
            0f, 1.02f, 0f, 0f, 2f,
            0f, 0f, 1.2f, 0f, 16f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.MONO -> {
        // Balanced neutral monochrome
        val r = 0.299f; val g = 0.587f; val b = 0.114f
        m.set(
          floatArrayOf(
            r, g, b, 0f, 0f,
            r, g, b, 0f, 0f,
            r, g, b, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.SILVERTONE -> {
        // Crisp high-key black & white with silver highlights
        val r = 0.33f; val g = 0.55f; val b = 0.12f
        m.set(
          floatArrayOf(
            r * 1.25f, g * 1.25f, b * 1.25f, 0f, 15f,
            r * 1.25f, g * 1.25f, b * 1.25f, 0f, 15f,
            r * 1.25f, g * 1.25f, b * 1.25f, 0f, 20f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.NOIR -> {
        // Dramatic crushed black, high contrast noir
        val r = 0.25f; val g = 0.65f; val b = 0.10f
        m.set(
          floatArrayOf(
            r * 1.45f, g * 1.45f, b * 1.45f, 0f, -25f,
            r * 1.45f, g * 1.45f, b * 1.45f, 0f, -25f,
            r * 1.45f, g * 1.45f, b * 1.45f, 0f, -25f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.RICH_CONTRAST -> {
        // Punchy saturated shadows, bright highlights
        m.set(
          floatArrayOf(
            1.3f, 0f, 0f, 0f, -12f,
            0f, 1.3f, 0f, 0f, -12f,
            0f, 0f, 1.3f, 0f, -12f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.WARM_CONTRAST -> {
        // High contrast + golden glow
        m.set(
          floatArrayOf(
            1.32f, 0f, 0f, 0f, 8f,
            0f, 1.25f, 0f, 0f, -2f,
            0f, 0f, 1.05f, 0f, -18f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
      PhotographicStyle.COOL_CONTRAST -> {
        // High contrast + icy cyan/slate highlights
        m.set(
          floatArrayOf(
            1.15f, 0f, 0f, 0f, -18f,
            0f, 1.22f, 0f, 0f, -5f,
            0f, 0f, 1.38f, 0f, 12f,
            0f, 0f, 0f, 1f, 0f
          )
        )
      }
    }
    return m
  }

  private fun interpolateWithIdentity(target: AndroidColorMatrix, factor: Float): AndroidColorMatrix {
    val identity = AndroidColorMatrix()
    val t = factor.coerceIn(0f, 1f)
    val idArr = identity.array
    val targetArr = target.array
    val resultArr = FloatArray(20)

    for (i in 0 until 20) {
      resultArr[i] = idArr[i] + (targetArr[i] - idArr[i]) * t
    }
    val res = AndroidColorMatrix()
    res.set(resultArr)
    return res
  }

  private fun createContrastBrightnessMatrix(contrast: Float, brightness: Float): AndroidColorMatrix {
    val c = contrast
    val b = brightness * 255f
    val t = (1.0f - c) / 2.0f * 255.0f
    val m = AndroidColorMatrix()
    m.set(
      floatArrayOf(
        c, 0f, 0f, 0f, t + b,
        0f, c, 0f, 0f, t + b,
        0f, 0f, c, 0f, t + b,
        0f, 0f, 0f, 1f, 0f
      )
    )
    return m
  }

  private fun createWarmthTintMatrix(warmth: Float, tint: Float): AndroidColorMatrix {
    // Warmth: shift Red (+) and Blue (-)
    // Tint: shift Green (+) and Red/Blue (-)
    val rOffset = (warmth * 45f) - (tint * 15f)
    val gOffset = (tint * 35f)
    val bOffset = (-warmth * 45f) - (tint * 15f)

    val m = AndroidColorMatrix()
    m.set(
      floatArrayOf(
        1f, 0f, 0f, 0f, rOffset,
        0f, 1f, 0f, 0f, gOffset,
        0f, 0f, 1f, 0f, bOffset,
        0f, 0f, 0f, 1f, 0f
      )
    )
    return m
  }
}
