package com.example.data.remote

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SupabaseProgress(
    val id: String, // Composed of playerUuid_formula
    val player_id: String,
    val formula: String,
    val name: String,
    val discovered_at: Long
)

interface SupabaseApiService {
    @GET("rest/v1/discovered_substances")
    suspend fun getProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Query("player_id") eqPlayerId: String // e.g. eq.playerUUID in supabase REST filters
    ): List<SupabaseProgress>

    @POST("rest/v1/discovered_substances")
    suspend fun upsertProgress(
        @Header("apikey") apiKey: String,
        @Header("Authorization") bearerToken: String,
        @Header("Prefer") preferHeader: String = "resolution=merge-duplicates",
        @Body progressList: List<SupabaseProgress>
    )
}

object SupabaseClientProvider {
    fun createService(baseUrl: String): SupabaseApiService {
        // Ensure base URL ends with a slash for Retrofit
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(SupabaseApiService::class.java)
    }
}
