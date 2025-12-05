package com.doublezero.data.repository

import com.doublezero.data.model.HistoryItem
import com.doublezero.data.network.CreateHistoryDto
import com.doublezero.data.network.HistoryDto

interface HistoryRepository {
    suspend fun saveHistory(request: CreateHistoryDto): Result<HistoryDto>
    suspend fun getHistory(limit: Int = 50): Result<List<HistoryItem>>
}

