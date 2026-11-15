package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.CameraPreferences
import com.example.model.FilterAdjustments
import com.example.model.PhotographicStyle
import com.example.processing.FilterColorMatrices
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Camera", appName)
  }

  @Test
  fun `test photographic style matrix generation`() {
    val matrix = FilterColorMatrices.createAndroidColorMatrix(
      PhotographicStyle.VIVID,
      FilterAdjustments(intensity = 0.8f, contrast = 1.2f)
    )
    assertNotNull(matrix)
    assertEquals(20, matrix.array.size)
  }

  @Test
  fun `test default camera preferences and custom controls`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = CameraPreferences(context)
    val controls = prefs.getDefaultControls()
    assertTrue(controls.isNotEmpty())
    assertTrue(controls.any { it.isPinned })
  }
}
