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

    private val socialOrMessagingPackages = setOf(
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.snapchat.android",
        "com.google.android.youtube",
        "org.telegram.messenger",
        "com.whatsapp"
    )

    private val strongFinancialKeywords = listOf(
        "credited",
        "debited",
        "txn",
        "transaction id",
        "acct balance",
        "account balance",
        "available balance",
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

    private val contextualFinancialPhrases = listOf(
        "money sent", "sent money", "payment sent", "amount sent", "you sent tk", "you sent bdt", "you sent $",
        "money received", "payment received", "amount received", "received tk", "received bdt", "received $",
        "money transferred", "transferred to", "transferred from", "transfer successful"
    )

    fun isFinancial(packageName: String, title: String, text: String): Boolean {
        if (trustedPackages.contains(packageName)) {
            return true
        }

        val combined = "$title $text".lowercase()

        // If it's a social or messaging app, chat notifications ("sent a photo", "received a message") are not financial
        if (socialOrMessagingPackages.contains(packageName)) {
            return strongFinancialKeywords.any { combined.contains(it) } || contextualFinancialPhrases.any { combined.contains(it) }
        }

        val hasStrong = strongFinancialKeywords.any { combined.contains(it) }
        val hasContextual = contextualFinancialPhrases.any { combined.contains(it) }
        val hasBalance = combined.contains("balance") && (combined.contains("tk") || combined.contains("bdt") || combined.contains("$") || combined.contains("account") || combined.contains("a/c"))

        return hasStrong || hasContextual || hasBalance
    }
}
