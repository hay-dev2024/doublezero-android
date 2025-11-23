package com.doublezero.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

data class GoogleLoginRequest(val idToken: String)

data class UserDto(
    val _id: String,
    val email: String,
    val displayName: String?,
    val googleId: String?
)

data class AuthResponse(
    val accessToken: String,
    val expiresIn: String,
    val user: UserDto
)

// Test token response from GET /auth/test-token
data class TestTokenResponse(
    val accessToken: String,
    val message: String?,
    val userId: String?
)

interface AuthApi {
    @POST("/auth/google")
    suspend fun googleLogin(@Body body: GoogleLoginRequest): Response<AuthResponse>

    @GET("/auth/test-token")
    suspend fun getTestToken(
        @Query("userId") userId: String?,
        @Query("email") email: String?
    ): Response<TestTokenResponse>
}
