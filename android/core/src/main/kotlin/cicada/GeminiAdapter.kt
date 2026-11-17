package cicada

import java.net.URLEncoder

/// Google Gemini's native GenerateContent REST schema. Keep this translation at the provider
/// boundary; Gemini is not an OpenAI-compatible chat endpoint.
object GeminiAdapter {
    fun endpoint(base: String, model: String): String =
        base.trimEnd('/') + "/models/" + URLEncoder.encode(model, "UTF-8").replace("+", "%20") + ":generateContent"

    fun request(chat: Map<String, Any?>): Map<String, Any?> {
        val contents = mutableListOf<Map<String, Any?>>()
        val systemParts = mutableListOf<Map<String, Any?>>()
        for (raw in chat["messages"].arr()) {
            val message = raw.obj() ?: continue
            val role = message["role"].str() ?: "user"
            val parts = parts(message["content"])
            if (role == "system" || role == "developer") systemParts.addAll(parts)
            else if (parts.isNotEmpty()) contents.add(mapOf("role" to if (role == "assistant") "model" else "user", "parts" to parts))
        }
        val generation = linkedMapOf<String, Any?>()
        chat["temperature"]?.let { generation["temperature"] = it }
        chat["max_tokens"]?.let { generation["maxOutputTokens"] = it }
        if (chat["response_format"].obj()?.get("type").str() == "json_object") generation["responseMimeType"] = "application/json"
        val request = linkedMapOf<String, Any?>("contents" to contents)
        if (systemParts.isNotEmpty()) request["systemInstruction"] = mapOf("parts" to systemParts)
        if (generation.isNotEmpty()) request["generationConfig"] = generation
        return request
    }

    private fun parts(content: Any?): List<Map<String, Any?>> = when (content) {
        is String -> listOf(mapOf("text" to content))
        is List<*> -> content.mapNotNull { item ->
            val part = item.obj() ?: return@mapNotNull null
            when (part["type"].str()) {
                "text" -> part["text"].str()?.let { mapOf("text" to it) }
                "input_audio" -> {
                    val input = part["input_audio"].obj() ?: return@mapNotNull null
                    val raw = input["data"].str() ?: return@mapNotNull null
                    val data = raw.substringAfter(",", raw)
                    val mime = Regex("^data:([^;,]+)").find(raw)?.groupValues?.get(1) ?: "audio/${input["format"].str() ?: "wav"}"
                    mapOf("inlineData" to mapOf("mimeType" to mime, "data" to data))
                }
                else -> null
            }
        }
        else -> emptyList()
    }

    /// Normalize a Gemini response to the internal chat shape used by the existing text/audio
    /// parsers. This only changes the provider boundary, not the transcript or notes format.
    fun response(root: Map<String, Any?>): Map<String, Any?> {
        val candidate = root["candidates"].arr().firstOrNull().obj() ?: return emptyMap()
        val content = candidate["content"].obj()
        val text = content?.get("parts").arr().mapNotNull {
            val part = it.obj() ?: return@mapNotNull null
            if (part["thought"] == true) null else part["text"].str()
        }.joinToString("")
        val finish = when (candidate["finishReason"].str()) {
            "MAX_TOKENS" -> "length"
            "SAFETY", "RECITATION", "BLOCKLIST", "PROHIBITED_CONTENT" -> "content_filter"
            else -> "stop"
        }
        val usage = root["usageMetadata"].obj()?.let { u ->
            linkedMapOf<String, Any?>(
                "prompt_tokens" to u["promptTokenCount"],
                "completion_tokens" to u["candidatesTokenCount"],
                "total_tokens" to u["totalTokenCount"],
            ).filterValues { it != null }
        }
        val normalized = linkedMapOf<String, Any?>("choices" to listOf(mapOf("message" to mapOf("content" to text), "finish_reason" to finish)))
        if (!usage.isNullOrEmpty()) normalized["usage"] = usage
        return normalized
    }
}
