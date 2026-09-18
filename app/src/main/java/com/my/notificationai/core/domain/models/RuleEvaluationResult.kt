package com.my.notificationai.core.domain.models

data class RuleEvaluationResult(
    val shouldBlock: Boolean,
    val reason: String,
    val ruleId: Long? = null,
    val ruleName: String? = null,
    val isOtp: Boolean = false,
    val otpCode: String? = null,
    val isFinancial: Boolean = false,
    val isPromotional: Boolean = false
)
