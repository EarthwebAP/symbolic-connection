package com.glyphos.symbolic.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class ApiResponse(
    val success: Boolean,
    val data: Map<String, Any>?,
    val error: String?
)

class GlyphApiClient(private val baseUrl: String = "http://glyph.seodr.ovh:3000") {

    suspend fun register(
        username: String,
        password: String,
        displayName: String,
        glyphData: String,
        glyphSvg: String
    ): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/register")
            val body = """{"username":"$username","password":"$password","displayName":"$displayName","glyphData":"$glyphData","glyphSvg":"${glyphSvg.take(100)}"}"""

            val responseStr = makeRequest(url, "POST", body)
            if (responseStr.contains("\"success\":true")) {
                ApiResponse(true, parseJson(responseStr), null)
            } else {
                ApiResponse(false, null, "Registration failed")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message)
        }
    }

    suspend fun login(username: String, password: String): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/login")
            val body = """{"username":"$username","password":"$password"}"""

            val responseStr = makeRequest(url, "POST", body)
            if (responseStr.contains("\"success\":true")) {
                ApiResponse(true, parseJson(responseStr), null)
            } else {
                ApiResponse(false, null, "Login failed")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message)
        }
    }

    suspend fun searchUsers(query: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/users/search/$query")
            val responseStr = makeRequest(url, "GET", null)
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUser(userId: Int): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/users/$userId")
            makeRequest(url, "GET", null)
            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFriends(userId: Int): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/users/$userId/friends")
            val responseStr = makeRequest(url, "GET", null)
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addFriend(userId: Int, friendId: Int): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/friends/add")
            val body = """{"userId":$userId,"friendId":$friendId}"""

            val responseStr = makeRequest(url, "POST", body)
            if (responseStr.contains("\"success\":true")) {
                ApiResponse(true, null, null)
            } else {
                ApiResponse(false, null, "Failed to add friend")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message)
        }
    }

    suspend fun sendMessage(
        senderId: Int,
        receiverId: Int,
        content: String,
        mediaUrl: String? = null,
        mediaType: String? = null,
        encodedGlyphMessage: String? = null
    ): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/messages/send")
            val body = """{"senderId":$senderId,"receiverId":$receiverId,"content":"$content"}"""

            val responseStr = makeRequest(url, "POST", body)
            if (responseStr.contains("\"success\":true")) {
                ApiResponse(true, parseJson(responseStr), null)
            } else {
                ApiResponse(false, null, "Failed to send message")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message)
        }
    }

    suspend fun getMessages(userId: Int, friendId: Int): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/messages/$userId/$friendId")
            val responseStr = makeRequest(url, "GET", null)
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun initiateCall(
        callerId: Int,
        receiverId: Int,
        callType: String,
        resonanceMode: Boolean
    ): ApiResponse = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/api/calls/initiate")
            val body = """{"callerId":$callerId,"receiverId":$receiverId,"callType":"$callType","resonanceMode":$resonanceMode}"""

            val responseStr = makeRequest(url, "POST", body)
            if (responseStr.contains("\"success\":true")) {
                ApiResponse(true, parseJson(responseStr), null)
            } else {
                ApiResponse(false, null, "Failed to initiate call")
            }
        } catch (e: Exception) {
            ApiResponse(false, null, e.message)
        }
    }

    private fun makeRequest(url: URL, method: String, body: String?): String {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        if (body != null && (method == "POST" || method == "PUT")) {
            connection.doOutput = true
            connection.outputStream.use { os ->
                os.write(body.toByteArray())
                os.flush()
            }
        }

        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun parseJson(json: String): Map<String, Any>? {
        return try {
            mapOf("response" to json)
        } catch (e: Exception) {
            null
        }
    }
}
