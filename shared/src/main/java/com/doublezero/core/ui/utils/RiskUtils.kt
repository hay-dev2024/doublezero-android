package com.doublezero.core.ui.utils

import androidx.compose.ui.graphics.Color
import com.doublezero.core.ui.color.*

data class RiskStyle(
    val bg: Color,
    val text: Color,
    val label: String
)

fun getRiskColor(risk: String): RiskStyle {
    return when (risk) {
        "safe" -> RiskStyle(
            bg = GreenishGrey,
            text = DarkGreen,
            label = "Safe"
        )
        "caution" -> RiskStyle(
            bg = WarmishWhite,
            text = Orange,
            label = "Caution"
        )
        "risk" -> RiskStyle(
            bg = ReddishWhite,
            text = Red,
            label = "Risk"
        )
        else -> RiskStyle(Color.Gray, Color.White, "Unknown")
    }
}