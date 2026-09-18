package com.my.notificationai.core.domain.engine

class FinancialDetector {

    private val trustedPackages = setOf(
        "com.bKash.customerapp",
        "com.konasl.nagad",
        "com.dbbl.mbs.upay",
        "com.ibbl.cellfin",
        "com.thecitybank.citytouch",
        "com.ebl.skybanking",
        "com.bracbank.astha",
        "com.sc.breezebd",
        "com.paypal.android.p2pmobile",
        "com.stripe.android",
        "com.squareup.cash",
        "com.google.android.apps.walletnfcrel",
        "com.revolut.revolut"
    )

    private val financialKeywords = listOf(
        "received",
        "sent",
        "transferred",
        "credited",
        "debited",
        "balance",
        "txn",
        "transaction",
        "statement",
        "payment confirmed",
        "payment successful",
        "deposit",
        "withdrawn",
        "withdrawal",
        "bill payment",
        "invoice paid",
        "cash in",
        "cash out",
        "bdt",
        "tk."
    )

    fun isFinancial(packageName: String, title: String, text: String): Boolean {
        if (trustedPackages.contains(packageName)) {
            return true
        }
        val combined = "$title $text".lowercase()
        return financialKeywords.any { combined.contains(it) }
    }
}
