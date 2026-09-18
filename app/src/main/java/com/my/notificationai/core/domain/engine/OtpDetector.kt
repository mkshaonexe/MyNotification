package com.my.notificationai.core.domain.engine

import java.util.regex.Pattern

class OtpDetector {

    private val wordBoundaryPattern = Pattern.compile("(?i)\\b(otp|2fa|v-code)\\b")

    private val phraseKeywords = listOf(
        "verification code",
        "security code",
        "one-time password",
        "one time password",
        "login code",
        "auth code",
        "confirmation code",
        "passcode",
        "secret code",
        "access code"
    )

    private val negativeKeywords = listOf(
        "gb for",
        "mb for",
        "recharge offer",
        "validity",
        "dial *",
        "cashback offer",
        "bonus",
        "discount coupon",
        "special promo",
        "promo code",
        "flat off"
    )

    private val codePattern = Pattern.compile("(?i)\\b(?:g-)?(\\d{4,8})\\b")

    /**
     * Evaluates text for OTP presence.
     * Returns Pair(isOtp, otpCode)
     */
    fun detect(title: String, text: String): Pair<Boolean, String?> {
        val combined = "$title $text".lowercase()

        // Check negative keywords first
        if (negativeKeywords.any { combined.contains(it) }) {
            return Pair(false, null)
        }

        // Check for presence of OTP keywords with word boundary for acronyms or exact phrase
        val hasOtpKeyword = wordBoundaryPattern.matcher(combined).find() || phraseKeywords.any { combined.contains(it) }
        if (!hasOtpKeyword) {
            return Pair(false, null)
        }

        // Search for 4-8 digit numeric code (e.g. 123456 or G-123456)
        val matcher = codePattern.matcher("$title $text")
        return if (matcher.find()) {
            val code = matcher.group(1)
            Pair(true, code)
        } else {
            // Still might be an OTP without isolated regex match, but mark detected
            Pair(true, null)
        }
    }
}
