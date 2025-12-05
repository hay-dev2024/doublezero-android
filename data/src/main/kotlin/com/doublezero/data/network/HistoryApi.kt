package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.*

interface HistoryApi {
    @POST("/history")
    suspend fun saveHistory(
        @Body request: CreateHistoryDto,
        @Header("Authorization") auth: String
    ): Response<HistoryDto>

    @GET("/history")
    suspend fun getHistory(
        @Query("limit") limit: Int = 50,
        @Header("Authorization") auth: String
    ): Response<List<HistoryDto>>
}

