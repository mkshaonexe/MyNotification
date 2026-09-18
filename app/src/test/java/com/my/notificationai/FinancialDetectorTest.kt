package com.my.notificationai

import com.my.notificationai.core.domain.engine.FinancialDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FinancialDetectorTest {

    private lateinit var financialDetector: FinancialDetector

    @Before
    fun setup() {
        financialDetector = FinancialDetector()
    }

    @Test
    fun `detects trusted financial packages`() {
        val isFinancial = financialDetector.isFinancial(
            packageName = "com.bKash.customerapp",
            title = "New Update",
            text = "Welcome to your account"
        )
        assertTrue(isFinancial)
    }

    @Test
    fun `detects transaction keywords from untrusted or generic packages`() {
        val isFinancial = financialDetector.isFinancial(
            packageName = "com.google.android.apps.messaging",
            title = "Bank Alert",
            text = "Your account has been credited with Tk. 5,000. Balance is BDT 12,400. Txn ID: 98124."
        )
        assertTrue(isFinancial)
    }

    @Test
    fun `rejects non-financial message`() {
        val isFinancial = financialDetector.isFinancial(
            packageName = "com.instagram.android",
            title = "Instagram",
            text = "John liked your photo."
        )
        assertFalse(isFinancial)
    }
}
