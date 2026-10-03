package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class NetworkTelemetry(
    val blockNumber: Long,
    val gasPriceGwei: Double,
    val chainId: Long,
    val latencyMs: Long,
    val isLive: Boolean,
    val flashblockSubSecondLatencyMs: Long = 185
)

data class RpcCallResult(
    val method: String,
    val rawRequest: String,
    val rawResponse: String,
    val durationMs: Long,
    val isSuccess: Boolean
)

class BaseRpcClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getTelemetry(rpcUrl: String, fallbackChainId: Long): NetworkTelemetry = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val blockHex = executeRpcCall(rpcUrl, "eth_blockNumber", emptyList())
            val gasHex = executeRpcCall(rpcUrl, "eth_gasPrice", emptyList())
            val chainIdHex = executeRpcCall(rpcUrl, "eth_chainId", emptyList())

            val latency = System.currentTimeMillis() - startTime
            val blockNum = parseHexToLong(blockHex, fallback = 23948512L)
            val gasWei = parseHexToLong(gasHex, fallback = 30000000L)
            val gasGwei = (gasWei.toDouble() / 1_000_000_000.0).coerceAtLeast(0.001)
            val chainId = parseHexToLong(chainIdHex, fallback = fallbackChainId)

            NetworkTelemetry(
                blockNumber = blockNum,
                gasPriceGwei = gasGwei,
                chainId = chainId,
                latencyMs = latency,
                isLive = true
            )
        } catch (e: Exception) {
            Log.w("BaseRpcClient", "Live RPC query failed, falling back to simulated block telemetry: ${e.message}")
            val fallbackBlock = 24102830L + (System.currentTimeMillis() % 100000)
            NetworkTelemetry(
                blockNumber = fallbackBlock,
                gasPriceGwei = 0.005,
                chainId = fallbackChainId,
                latencyMs = 42,
                isLive = false
            )
        }
    }

    suspend fun executeCustomRpc(rpcUrl: String, method: String, params: List<Any> = emptyList()): RpcCallResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val requestJson = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", method)
            put("params", JSONArray(params))
        }.toString()

        try {
            val body = requestJson.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(rpcUrl)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            val duration = System.currentTimeMillis() - startTime

            // Format pretty JSON if possible
            val formattedResponse = try {
                JSONObject(responseBody).toString(2)
            } catch (e: Exception) {
                responseBody
            }

            RpcCallResult(
                method = method,
                rawRequest = JSONObject(requestJson).toString(2),
                rawResponse = formattedResponse,
                durationMs = duration,
                isSuccess = response.isSuccessful
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            val errorResponse = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("error", JSONObject().apply {
                    put("code", -32603)
                    put("message", e.localizedMessage ?: "Network error connecting to $rpcUrl")
                })
            }.toString(2)

            RpcCallResult(
                method = method,
                rawRequest = JSONObject(requestJson).toString(2),
                rawResponse = errorResponse,
                durationMs = duration,
                isSuccess = false
            )
        }
    }

    private fun executeRpcCall(rpcUrl: String, method: String, params: List<Any>): String {
        val payload = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", 1)
            put("method", method)
            put("params", JSONArray(params))
        }.toString()

        val request = Request.Builder()
            .url(rpcUrl)
            .post(payload.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("RPC status: ${response.code}")
            val body = response.body?.string() ?: throw IllegalStateException("Empty body")
            val json = JSONObject(body)
            if (json.has("result")) {
                return json.getString("result")
            } else if (json.has("error")) {
                val err = json.getJSONObject("error").optString("message", "RPC Error")
                throw IllegalStateException(err)
            }
            throw IllegalStateException("Invalid RPC response")
        }
    }

    private fun parseHexToLong(hex: String, fallback: Long): Long {
        return try {
            val clean = hex.removePrefix("0x")
            clean.toLong(16)
        } catch (e: Exception) {
            fallback
        }
    }
}
