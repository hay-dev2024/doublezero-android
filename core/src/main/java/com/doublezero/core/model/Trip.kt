package com.doublezero.core.model

/**
 * Schema
 * 앱 전반에서 사용될 공용 주행 기록 데이터 모델
 */
data class Trip(
    val id: Int,
    val date: String,
    val time: String,
    val origin: String,
    val destination: String,
    val distance: String,
    val duration: String,
    val risk: String, // "safe", "caution", "risk"
    val riskDetails: String
)