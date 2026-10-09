package com.xyq.livetranslate

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VideoTranslateTileServiceTest {

    @Test
    fun tileClickStopsServiceWhenAlreadyRunning() {
        val service = Robolectric.buildService(VideoTranslateTileService::class.java).create().get()
        StatusBus.serviceRunning = true

        try {
            service.onClick()
            val app = ApplicationProvider.getApplicationContext<Application>()
            val startedService = shadowOf(app).nextStartedService
            assertNotNull(startedService)
            assertEquals(CaptureService::class.java.name, startedService.component?.className)
            assertEquals(CaptureService.ACTION_STOP, startedService.action)
        } finally {
            StatusBus.serviceRunning = false
        }
    }
}
