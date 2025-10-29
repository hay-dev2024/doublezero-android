package com.doublezero.feature_home.entry

/**
 * Version 2: 자동차 애니메이션이 포함된 스플래시 화면
 */
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val CAR_MOVE_DURATION = 1600 // 1.6s
private const val CAR_FADE_DELAY = 1400L // 1.4s
private const val CAR_FADE_DURATION = 400 // 0.4s
private const val LOGO_APPEAR_DELAY = 1900L // 1.9s
private const val LOGO_APPEAR_DURATION = 600 // 0.6s
private const val SUBTITLE_APPEAR_DELAY = 2200L // 2.2s
private const val SUBTITLE_APPEAR_DURATION = 400 // 0.4s
private const val ACCENT_DELAY = 500L // 0.5s
private const val ACCENT_DURATION = 1500 // 1.5s
private const val TOTAL_DURATION = 3000L // 전체 스플래시 시간 (기존과 동일)

private val bounceEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

private val easeOutEasing = EaseOutCubic

@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    // 애니메이션 상태 관리
    var carVisible by remember { mutableStateOf(true) }
    var logoVisible by remember { mutableStateOf(false) }
    var subtitleVisible by remember { mutableStateOf(false) }
    var accentVisible by remember { mutableStateOf(false) }

    // --- 애니메이션 값 정의 ---
    val density = LocalDensity.current
    val initialCarOffsetX = with(density) { 400.dp.toPx() } // React의 x: 400

    // 1. 자동차 이동 애니메이션 (X 좌표)
    val carOffsetX by animateFloatAsState(
        targetValue = if (carVisible) 0f else initialCarOffsetX, // 시작 시 0으로 이동
        animationSpec = tween(
            durationMillis = CAR_MOVE_DURATION,
            easing = bounceEasing
        ),
        label = "CarOffsetX"
    )

    // 2. 자동차 전체 투명도 (이동 완료 후 Fade out)
    val carAlpha by animateFloatAsState(
        targetValue = if (carVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = CAR_FADE_DURATION,
            delayMillis = CAR_FADE_DELAY.toInt(), // 1.4초 후 시작
            easing = LinearEasing // 단순 fade out
        ),
        label = "CarAlpha"
    )

    // 3. 로고 애니메이션 (Opacity + Y 좌표)
    val logoAlpha by animateFloatAsState(
        targetValue = if (logoVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = LOGO_APPEAR_DURATION,
            delayMillis = LOGO_APPEAR_DELAY.toInt(), // 1.9초 후 시작
            easing = easeOutEasing
        ),
        label = "LogoAlpha"
    )
    val logoOffsetY by animateDpAsState(
        targetValue = if (logoVisible) 0.dp else 20.dp, // React의 y: 20
        animationSpec = tween(
            durationMillis = LOGO_APPEAR_DURATION,
            delayMillis = LOGO_APPEAR_DELAY.toInt(),
            easing = easeOutEasing
        ),
        label = "LogoOffsetY"
    )

    // 4. 부제 애니메이션 (Opacity)
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (subtitleVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = SUBTITLE_APPEAR_DURATION,
            delayMillis = SUBTITLE_APPEAR_DELAY.toInt(), // 2.2초 후 시작
            easing = LinearEasing
        ),
        label = "SubtitleAlpha"
    )

    // 5. 하단 액센트 라인 애니메이션 (ScaleX)
    val accentScaleX by animateFloatAsState(
        targetValue = if (accentVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = ACCENT_DURATION,
            delayMillis = ACCENT_DELAY.toInt(), // 0.5초 후 시작
            easing = LinearEasing
        ),
        label = "AccentScaleX"
    )

    // --- 애니메이션 시퀀스 및 네비게이션 트리거 ---
    LaunchedEffect(Unit) {
        // 애니메이션 상태 변경 트리거
        accentVisible = true // 액센트 라인 시작
        delay(CAR_FADE_DELAY) // 1.4초 대기
        carVisible = false // 자동차 fade out 시작
        delay(LOGO_APPEAR_DELAY - CAR_FADE_DELAY) // 1.9초 - 1.4초 = 0.5초 추가 대기
        logoVisible = true // 로고 appear 시작
        delay(SUBTITLE_APPEAR_DELAY - LOGO_APPEAR_DELAY) // 2.2초 - 1.9초 = 0.3초 추가 대기
        subtitleVisible = true // 부제 appear 시작

        // 전체 시간 후 네비게이션
        val remainingDelay = TOTAL_DURATION - SUBTITLE_APPEAR_DELAY
        if (remainingDelay > 0) {
            delay(remainingDelay)
        }
        onTimeout()
    }

    // --- UI 레이아웃 ---
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE3F2FD), // from-[#E3F2FD]
            Color(0xFFBBDEFB), // via-[#BBDEFB]
            Color(0xFFFFF8E1)  // to-[#FFF8E1]
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        // 1. 자동차 + 로고 컨테이너 (relative)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 자동차 SVG + 바퀴 (offset과 alpha 적용)
            Box(
                modifier = Modifier
                    .offset(x = with(LocalDensity.current) { carOffsetX.toDp() })
                    .alpha(carAlpha)
            ) {
                CarSvg(modifier = Modifier.size(width = 200.dp, height = 100.dp)) // SVG 크기
                // 바퀴 (CarSvg 내부 좌표 기준으로 배치)
                CarWheel(modifier = Modifier.offset(x = 33.dp, y = 75.dp - 8.dp))
                CarWheel(modifier = Modifier.offset(x = 200.dp - 33.dp - 40.dp, y = 75.dp - 8.dp)) )
            }

            // DoubleZero 로고 + 부제 (offset과 alpha 적용)
            Column(
                modifier = Modifier
                    .offset(y = logoOffsetY)
                    .alpha(logoAlpha),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DoubleZero",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2),
                    letterSpacing = (0.05 * 42).sp / 42,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = "AI-Powered Safe Navigation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2196F3),
                    textAlign = TextAlign.Center,
                    letterSpacing = (0.1 * 14).sp / 14,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .alpha(subtitleAlpha) // 부제는 로고 나타난 후 다시 fade-in
                )
            }
        }

        // 3. 하단 액센트 라인
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(4.dp)
                .graphicsLayer(scaleX = accentScaleX)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF2196F3),
                            Color(0xFF4CAF50),
                            Color(0xFF2196F3)
                        )
                    )
                )
        )
    }
}

/**
 * 자동차 본체
 */
@Composable
private fun CarSvg(modifier: Modifier = Modifier) {
    val carBodyColor = Color(0xFF2196F3)
    val carRoofColor = carBodyColor.copy(alpha = 0.9f)
    val windowColor = Color(0xFFE3F2FD).copy(alpha = 0.6f)
    val bumperColor = Color(0xFF1976D2)

    Canvas(modifier = modifier) {
         // 자동차 지붕 (Path)
        val roofPath = Path().apply {
            moveTo(50f, 42f)
            lineTo(75f, 17f)
            lineTo(125f, 17f)
            lineTo(150f, 42f)
            close()
        }
        drawPath(roofPath, color = carRoofColor)

        // 자동차 몸체 (Rounded Rect)
        drawRoundRect(
            color = carBodyColor,
            topLeft = Offset(17f, 42f),
            size = Size(166f, 33f),
            cornerRadius = CornerRadius(6f)
        )

        // 창문 (Rounded Rect)
        drawRoundRect(
            color = windowColor,
            topLeft = Offset(80f, 23f),
            size = Size(37f, 20f),
            cornerRadius = CornerRadius(3f)
        )

        // 범퍼 디테일 (Rounded Rect)
        drawRoundRect(
            color = bumperColor,
            topLeft = Offset(177f, 50f),
            size = Size(6f, 17f),
            cornerRadius = CornerRadius(2f)
        )
    }
}

/**
 * 자동차 바퀴
 */
@Composable
private fun CarWheel(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    val outerColor = Color(0xFF757575)
    val innerColor = Color(0xFF424242)
    val hubColor = Color(0xFF757575)
    val borderWidth = 4.dp
    val hubSize = 8.dp

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(innerColor)
            .padding(borderWidth)
            .clip(CircleShape)
            .background(outerColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(hubSize)
                .clip(CircleShape)
                .background(hubColor)
        )
    }
}


@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(onTimeout = {})
    }
}



/**
 * Version 1: 기존의 단순 텍스트 기반 스플래시 화면
 */
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import kotlinx.coroutines.delay
//
//@Composable
//fun SplashScreen(
//    onTimeout: () -> Unit
//) {
//    // Composable이 화면에 보일 때(Launched) 딱 한 번(Unit) 실행
//    LaunchedEffect(Unit) {
//        delay(3000) // 3초 대기
//        onTimeout() // 네비게이션 트리거
//    }
//
//    val gradient = Brush.verticalGradient(
//        colors = listOf(Color(0xFF87CEEB), Color.White)
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(gradient),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "DoubleZero",
//                fontSize = 48.sp,
//                fontWeight = FontWeight.Bold,
//                letterSpacing = (-1).sp
//            )
//            Text(
//                text = "AI-Powered Navigation",
//                color = Color(0xFF2196F3),
//                modifier = Modifier.padding(top = 8.dp)
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 390, heightDp = 844)
//@Composable
//private fun SplashScreenPreview() {
//    MaterialTheme {
//        SplashScreen(onTimeout = {})
//    }
//}

