package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Yemen4GRepository
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
        assertEquals("يمن فورجي", appName)
    }

    @Test
    fun `test repository packages and captcha generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = Yemen4GRepository(context)
        val captcha = repository.getCurrentCaptcha()
        assertNotNull(captcha)
        assertEquals(5, captcha.length)

        val packages = repository.getOfficialPackages()
        assertTrue(packages.isNotEmpty())
        assertTrue(packages.any { it.isPopular })
    }
}
