package com.example.chatbox

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("chat/completions")
    suspend fun chat(
        @Header("Authorization") authorization: String,
        @Header("HTTP-Referer") referer: String,
        @Header("X-OpenRouter-Title") title: String,
        @Body request: MainActivity.ChatRequest
    ): Response<MainActivity.ChatResponse>
}