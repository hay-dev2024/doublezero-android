package com.doublezero.core.ui.utils

import androidx.compose.ui.graphics.Color

/**
 * Data를 UI로 매핑하는 유틸리티
 * core 모듈의 공용 유틸리티로 분리
 */

data class RiskStyle(
    val bg: Color,
    val text: Color,
    val label: String
)

fun getRiskColor(risk: String): RiskStyle {
    return when (risk) {
        "safe" -> RiskStyle(
            bg = Color(0xFFE8F5E9),
            text = Color(0xFF4CAF50),
            label = "Safe"
        )
        "caution" -> RiskStyle(
            bg = Color(0xFFFFF9C4),
            text = Color(0xFFFFA000),
            label = "Caution"
        )
        "risk" -> RiskStyle(
            bg = Color(0xFFFFEBEE),
            text = Color(0xFFF44336),
            label = "Risk"
        )
        else -> RiskStyle(Color.Gray, Color.White, "Unknown")
    }
}