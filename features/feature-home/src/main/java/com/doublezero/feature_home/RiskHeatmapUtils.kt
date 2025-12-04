package com.doublezero.feature_home

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlin.random.Random

object RiskHeatmapUtils {

    // 위험도 단계별 색상 정의 (1: 초록, 2: 노랑, 3: 빨강)
    private val COLOR_SAFE = Color.rgb(0, 255, 0)      // Green
    private val COLOR_CAUTION = Color.rgb(255, 255, 0) // Yellow
    private val COLOR_DANGER = Color.rgb(255, 0, 0)    // Red

    data class RiskSegment(
        val points: List<LatLng>,
        val riskLevel: Int // 1, 2, 3
    )

    /**
     * 전체 경로를 받아서, 현실적인 '구간별 위험도'가 적용된 히트맵 프로바이더 리스트를 반환합니다.
     * 반환값: List<Pair<HeatmapTileProvider, Int>> -> (프로바이더, 그 구간의 색상)
     */
    fun createSegmentedHeatmapProviders(
        fullPath: List<LatLng>
    ): List<Pair<HeatmapTileProvider, Int>> {
        if (fullPath.size < 2) return emptyList()

        // 1. 경로를 500m~1km 단위의 랜덤한 구간으로 자름 (현실적인 더미 데이터 생성)
        val segments = splitPathIntoRandomSegments(fullPath)

        val providers = mutableListOf<Pair<HeatmapTileProvider, Int>>()

        // 2. 각 구간별로 별도의 히트맵 생성
        segments.forEach { segment ->
            val color = when (segment.riskLevel) {
                3 -> COLOR_DANGER
                2 -> COLOR_CAUTION
                else -> COLOR_SAFE
            }

            // 해당 구간의 색상만 사용하는 단색 그라데이션 생성
            val gradient = Gradient(
                intArrayOf(color, color), // 시작과 끝 색상을 동일하게
                floatArrayOf(0.2f, 1.0f)
            )

            // 선을 점으로 채우기 (보간) - 점 간격을 15m로 설정
            val densePoints = densifyPolyline(segment.points, intervalMeters = 15.0)

            // 데이터 변환
            val weightedData = densePoints.map { WeightedLatLng(it, 1.0) }

            if (weightedData.isNotEmpty()) {
                val provider = HeatmapTileProvider.Builder()
                    .weightedData(weightedData)
                    .radius(30) // 반지름
                    .opacity(0.6) // 투명도
                    .gradient(gradient)
                    .build()

                providers.add(Pair(provider, segment.riskLevel))
            }
        }

        return providers
    }

    /**
     * 경로를 랜덤한 길이로 자르고, 랜덤한 위험도를 부여하는 함수 (더미 데이터 생성기)
     */
    private fun splitPathIntoRandomSegments(path: List<LatLng>): List<RiskSegment> {
        val segments = mutableListOf<RiskSegment>()
        var currentIndex = 0
        val totalPoints = path.size

        while (currentIndex < totalPoints - 1) {
            // 랜덤하게 구간 길이 결정 (전체 포인트의 10% ~ 30% 길이)
            val segmentLength = Random.nextInt((totalPoints * 0.1).toInt(), (totalPoints * 0.3).toInt()).coerceAtLeast(2)
            val endIndex = (currentIndex + segmentLength).coerceAtMost(totalPoints)

            // 구간 자르기
            val segmentPoints = path.subList(currentIndex, endIndex)

            // 랜덤 위험도 부여 (1: 50%, 2: 30%, 3: 20% 확률)
            val rand = Random.nextDouble()
            val risk = when {
                rand < 0.5 -> 1 // Safe
                rand < 0.8 -> 2 // Caution
                else -> 3       // Danger
            }

            segments.add(RiskSegment(segmentPoints, risk))

            // 다음 구간 시작점 (연결성을 위해 현재 구간의 끝점에서 시작)
            currentIndex = endIndex - 1
        }
        return segments
    }

    /**
     * 백엔드에서 받은 riskPoints 데이터로 tier별 히트맵 프로바이더 리스트 생성
     * 각 tier(0,1,2)마다 별도의 단일 색상 원형 히트맵 생성
     * @param riskPoints 백엔드의 RiskPointDto 리스트
     * @param isDynamic 동적 업데이트 여부 (30초 주기 SSE 데이터)
     * @return List<HeatmapTileProvider> - tier별로 분리된 프로바이더 리스트
     */
    fun createHeatmapFromRiskPoints(
        riskPoints: List<com.doublezero.data.network.RiskPointDto>,
        isDynamic: Boolean = false
    ): List<HeatmapTileProvider> {
        if (riskPoints.isEmpty()) return emptyList()

        // 🔍 중복 제거: 동일 좌표(0.0001도 이내)는 하나만 유지
        val uniquePoints = riskPoints.distinctBy { point ->
            val latKey = (point.lat * 10000).toInt()
            val lonKey = (point.lon * 10000).toInt()
            "$latKey,$lonKey,${point.tier}"
        }

        android.util.Log.d(
            "RiskHeatmapUtils",
            "createHeatmapFromRiskPoints: ${riskPoints.size} points → ${uniquePoints.size} unique (isDynamic=$isDynamic)"
        )

        val providers = mutableListOf<HeatmapTileProvider>()

        // tier별로 그룹화 (0=Low/초록, 1=Medium/노랑, 2=High/빨강)
        val groupedByTier = uniquePoints.groupBy { it.tier }

        groupedByTier.forEach { (tier, points) ->
            // 해당 tier의 단일 색상 결정
            val color = when (tier) {
                2 -> COLOR_DANGER   // 빨강
                1 -> COLOR_CAUTION  // 노랑
                else -> COLOR_SAFE  // 초록
            }

            // 단일 색상 그라데이션 생성 (시작과 끝을 같은 색으로)
            val gradient = Gradient(
                intArrayOf(color, color),
                floatArrayOf(0.2f, 1.0f)
            )

            val weightedData = points.map { point ->
                WeightedLatLng(
                    LatLng(point.lat, point.lon),
                    1.0  // 동일한 intensity로 원형 표시
                )
            }

            if (weightedData.isNotEmpty()) {
                // 🎨 동적 히트맵은 약간 작고 투명하게 (겹침 방지)
                val radius = if (isDynamic) 35 else 40
                val opacity = if (isDynamic) 0.5 else 0.6

                val provider = HeatmapTileProvider.Builder()
                    .weightedData(weightedData)
                    .gradient(gradient)
                    .radius(radius)
                    .opacity(opacity)
                    .build()

                providers.add(provider)
            }
        }

        return providers
    }

    /**
     * 선을 점으로 쪼개는 보간 함수 (이전과 동일)
     */
    private fun densifyPolyline(points: List<LatLng>, intervalMeters: Double): List<LatLng> {
        val denseList = mutableListOf<LatLng>()
        if (points.size < 2) return points

        for (i in 0 until points.size - 1) {
            val start = points[i]
            val end = points[i + 1]
            val distance = SphericalUtil.computeDistanceBetween(start, end)
            val numberOfPoints = (distance / intervalMeters).toInt()

            denseList.add(start)
            for (j in 1..numberOfPoints) {
                val fraction = j.toDouble() / (numberOfPoints + 1)
                denseList.add(SphericalUtil.interpolate(start, end, fraction))
            }
        }
        denseList.add(points.last())
        return denseList
    }
}