package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DetectedLanguage
import com.example.util.LanguageDetectorUtil
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Rima AI", appName)
  }

  @Test
  fun `detect Bengali language`() {
    val detected = LanguageDetectorUtil.detectLanguage("কেমন আছো? রিমা এআই")
    assertEquals(DetectedLanguage.BENGALI, detected)
  }

  @Test
  fun `detect Banglish language`() {
    val detected = LanguageDetectorUtil.detectLanguage("kemon acho rima amake ektu help koro")
    assertEquals(DetectedLanguage.BANGLISH, detected)
  }

  @Test
  fun `detect English language`() {
    val detected = LanguageDetectorUtil.detectLanguage("Hello Rima, how are you today?")
    assertEquals(DetectedLanguage.ENGLISH, detected)
  }
}

