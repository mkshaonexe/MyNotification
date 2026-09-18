package com.my.notificationai

import com.my.notificationai.core.domain.engine.PromotionalFilter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PromotionalFilterTest {

    private lateinit var promotionalFilter: PromotionalFilter

    @Before
    fun setup() {
        promotionalFilter = PromotionalFilter()
    }

    @Test
    fun `detects telecom bundle offers`() {
        val isPromo = promotionalFilter.isPromotional(
            title = "GP Offer",
            text = "Enjoy 5 GB internet pack for 7 days at only 108 Tk! Dial *121*1#."
        )
        assertTrue(isPromo)
    }

    @Test
    fun `detects discount and sale promotions`() {
        val isPromo = promotionalFilter.isPromotional(
            title = "Special Offer",
            text = "Flat 50% off on all items! Use coupon code today."
        )
        assertTrue(isPromo)
    }

    @Test
    fun `rejects ordinary SMS messages`() {
        val isPromo = promotionalFilter.isPromotional(
            title = "+123456789",
            text = "Are you coming home for dinner tonight?"
        )
        assertFalse(isPromo)
    }
}
