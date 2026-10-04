package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
    assertEquals("QR Studio Pro", appName)
  }

  @Test
  fun `qr svg exporter generates valid svg markup`() {
    val qr = com.example.data.model.QrCodeEntity(
      title = "Test QR",
      rawContent = "https://example.com"
    )
    val svg = com.example.generator.QrSvgExporter.generateSvg(qr)
    org.junit.Assert.assertTrue(svg.startsWith("<?xml"))
    org.junit.Assert.assertTrue(svg.contains("<svg"))
    org.junit.Assert.assertTrue(svg.contains("</svg>"))
  }
}
