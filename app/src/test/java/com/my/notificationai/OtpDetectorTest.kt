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
    fun `rejects words containing otp as substring like hotpot`() {
        val title = "Dinner"
        val text = "Let's eat hotpot tonight with 1234 friends"
        val (isOtp, _) = otpDetector.detect(title, text)

        assertFalse(isOtp)
    }

    @Test
    fun `detects Google verification code with G prefix`() {
        val title = "Google"
        val text = "G-938210 is your Google verification code"
        val (isOtp, code) = otpDetector.detect(title, text)

        assertTrue(isOtp)
        assertEquals("938210", code)
    }

    @Test
    fun `detects 2FA acronym with code`() {
        val title = "GitHub"
        val text = "Your 2FA code is 829103"
        val (isOtp, code) = otpDetector.detect(title, text)

        assertTrue(isOtp)
        assertEquals("829103", code)
    }
}
