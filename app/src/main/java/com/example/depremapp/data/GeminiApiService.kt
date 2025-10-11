package com.example.depremapp.data

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// Request models
data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("generationConfig") val generationConfig: GenerationConfig? = null
)

data class Content(
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String? = null,
    @SerializedName("inline_data") val inlineData: InlineData? = null
)

data class InlineData(
    @SerializedName("mime_type") val mimeType: String,
    @SerializedName("data") val data: String
)

data class GenerationConfig(
    @SerializedName("temperature") val temperature: Double = 0.4,
    @SerializedName("topK") val topK: Int = 32,
    @SerializedName("topP") val topP: Double = 1.0,
    @SerializedName("maxOutputTokens") val maxOutputTokens: Int = 2048
)

// Response models
data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>?,
    @SerializedName("promptFeedback") val promptFeedback: PromptFeedback? = null
)

data class Candidate(
    @SerializedName("content") val content: ContentResponse,
    @SerializedName("finishReason") val finishReason: String? = null,
    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>? = null
)

data class ContentResponse(
    @SerializedName("parts") val parts: List<PartResponse>,
    @SerializedName("role") val role: String
)

data class PartResponse(
    @SerializedName("text") val text: String
)

data class SafetyRating(
    @SerializedName("category") val category: String,
    @SerializedName("probability") val probability: String
)

data class PromptFeedback(
    @SerializedName("safetyRatings") val safetyRatings: List<SafetyRating>?
)

interface GeminiApiService {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

// Alternative endpoint for stable API
interface GeminiApiServiceV1 {
    @POST("v1/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

