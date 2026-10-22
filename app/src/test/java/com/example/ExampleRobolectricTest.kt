package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.RingerModeType
import com.example.data.StreamCategory
import com.example.data.StreamVolumeState
import com.example.data.VolumePreset
import com.example.data.VolumeRepository
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
    assertEquals("Volume Master", appName)
  }

  @Test
  fun `verify default presets loaded`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = VolumeRepository(context)
    val defaults = repo.getDefaultPresets()
    assertEquals(5, defaults.size)
    assertNotNull(defaults.find { it.id == "preset_normal" })
    assertNotNull(defaults.find { it.id == "preset_meeting" })
    assertNotNull(defaults.find { it.id == "preset_sleep" })
    assertNotNull(defaults.find { it.id == "preset_outdoor" })
  }

  @Test
  fun `stream volume state progress calculation`() {
    val state = StreamVolumeState(
      category = StreamCategory.MEDIA,
      currentVolume = 7,
      minVolume = 0,
      maxVolume = 10
    )
    assertEquals(0.7f, state.progress, 0.01f)
    assertEquals(70, state.percentage)
  }

  @Test
  fun `save and load custom preset`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = VolumeRepository(context)
    val custom = VolumePreset(
      id = "test_custom_1",
      name = "Gaming Siang",
      description = "Kelantangan khas untuk permainan",
      iconKey = "tune",
      ringerMode = RingerModeType.NORMAL,
      mediaPercent = 90,
      ringPercent = 40,
      notificationPercent = 20,
      alarmPercent = 80,
      callPercent = 60,
      isCustom = true
    )
    repo.saveCustomPreset(custom)

    val list = repo.loadCustomPresets()
    assertTrue(list.any { it.id == "test_custom_1" && it.name == "Gaming Siang" })

    // Clean up
    repo.deleteCustomPreset("test_custom_1")
    val listAfter = repo.loadCustomPresets()
    assertTrue(listAfter.none { it.id == "test_custom_1" })
  }
}
