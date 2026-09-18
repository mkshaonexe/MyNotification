package com.my.notificationai

import com.my.notificationai.core.domain.engine.OtpDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OtpDetectorTest {

    private lateinit var otpDetector: OtpDetector

    @Before
    fun setup() {
        otpDetector = OtpDetector()
    }

    @Test
    fun `detects standard numeric OTP code`() {
        val title = "Google Verification"
        val text = "Your Google verification code is 492810. Do not share it."
        val (isOtp, code) = otpDetector.detect(title, text)

        assertTrue(isOtp)
        assertEquals("492810", code)
    }

    @Test
    fun `detects bank OTP with one-time password keyword`() {
        val title = "bKash"
        val text = "Your one-time password is 8372. Valid for 3 mins."
        val (isOtp, code) = otpDetector.detect(title, text)

        assertTrue(isOtp)
        assertEquals("8372", code)
    }

    @Test
    fun `rejects telecom marketing bundle containing digits`() {
        val title = "Special Offer"
        val text = "Get 2 GB for 3 days recharge offer dial *121*123#"
        val (isOtp, code) = otpDetector.detect(title, text)

        assertFalse(isOtp)
        assertEquals(null, code)
    }

    @Test
    fun `rejects promotional cashback message containing code keyword`() {
        val title = "Mega Sale"
        val text = "Use discount coupon code SALE50 for flat off and cashback offer!"
        val (isOtp, _) = otpDetector.detect(title, text)

        assertFalse(isOtp)
    }

    @Test
    fun `rejects normal chat message without OTP keywords`() {
        val title = "John Doe"
        val text = "Hey, meet me at 1234 Elm Street at 5pm."
        val (isOtp, _) = otpDetector.detect(title, text)

        assertFalse(isOtp)
    }
}
