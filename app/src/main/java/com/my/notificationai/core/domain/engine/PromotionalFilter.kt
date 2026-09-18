package com.my.notificationai.core.domain.engine

import java.util.regex.Pattern

class PromotionalFilter {

    private val promoKeywords = listOf(
        "discount",
        "sale now",
        "flat off",
        "% off",
        "special offer",
        "recharge offer",
        "exclusive deal",
        "limited time offer",
        "coupon code",
        "dial *",
        "internet pack",
        "minute pack",
        "bundle offer",
        "free delivery"
    )

    private val bundlePattern = Pattern.compile("(?i)\\b\\d+\\s*(gb|mb|min|sms)\\b")

    fun isPromotional(title: String, text: String): Boolean {
        val combined = "$title $text".lowercase()
        val hasKeyword = promoKeywords.any { combined.contains(it) }
        val hasBundle = bundlePattern.matcher(combined).find()
        return hasKeyword || hasBundle
    }
}
