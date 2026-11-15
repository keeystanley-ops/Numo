package com.example.voice

import android.util.Base64
import com.example.data.NumoFinancialEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiBackendResult {
  data class Success(
    val replyText: String,
    val spokenText: String,
    val pendingAction: PendingFinancialAction?
  ) : GeminiBackendResult()

  data class Failure(
    val friendlyMessage: String,
    val backendReachable: Boolean
  ) : GeminiBackendResult()
}

/**
 * Routes Hey Numo queries through the server-side Gemini endpoint (`/api/hey-numo/chat`)
 * so GEMINI_API_KEY never enters client code, mobile bundles, or logs.
 */
object GeminiHeyNumoClient {
  private val baseUrls = listOf(
    "http://10.0.2.2:3000", // Android emulator host loopback
    "http://127.0.0.1:3000" // Physical device via `adb reverse tcp:3000 tcp:3000`
  )

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(2500, TimeUnit.MILLISECONDS)
    .readTimeout(16000, TimeUnit.MILLISECONDS)
    .writeTimeout(5000, TimeUnit.MILLISECONDS)
    .build()

  suspend fun sendChat(
    userId: String,
    userName: String,
    message: String,
    customAccounts: List<Pair<String, String>>,
    resetHistory: Boolean
  ): GeminiBackendResult = withContext(Dispatchers.IO) {
    val payload = JSONObject().apply {
      put("userId", userId)
      put("userName", userName)
      put("message", message)
      put("resetHistory", resetHistory)
      put("financialSnapshot", buildFinancialSnapshot(customAccounts))
    }

    val mediaType = "application/json; charset=utf-8".toMediaType()
    var lastReachableError: GeminiBackendResult.Failure? = null

    for (baseUrl in baseUrls) {
      try {
        val request = Request.Builder()
          .url("$baseUrl/api/hey-numo/chat")
          .addHeader("x-numo-user-id", userId)
          .post(payload.toString().toRequestBody(mediaType))
          .build()

        httpClient.newCall(request).execute().use { response ->
          val bodyStr = response.body?.string().orEmpty()
          val json = try {
            JSONObject(bodyStr)
          } catch (_: Exception) {
            JSONObject()
          }

          if (response.isSuccessful) {
            val reply = json.optString("reply").ifBlank {
              "I checked your Numo accounts, but couldn't format a reply."
            }
            val spoken = json.optString("spokenText", reply).ifBlank { reply }
            val pendingObj = json.optJSONObject("pendingAction")
            val pendingAction = pendingObj?.let { parsePendingAction(it) }
            return@withContext GeminiBackendResult.Success(
              replyText = reply,
              spokenText = spoken,
              pendingAction = pendingAction
            )
          } else {
            val friendly = json.optString("reply").ifBlank {
              json.optString("error").ifBlank {
                "Hey Numo could not reach Gemini AI right now. Please try again."
              }
            }
            lastReachableError = GeminiBackendResult.Failure(
              friendlyMessage = friendly,
              backendReachable = true
            )
          }
        }
      } catch (_: Exception) {
        // Try next candidate backend URL
      }

      if (lastReachableError != null) {
        return@withContext lastReachableError!!
      }
    }

    GeminiBackendResult.Failure(
      friendlyMessage = "Hey Numo backend server is not reachable right now.",
      backendReachable = false
    )
  }

  suspend fun confirmAction(
    userId: String,
    actionId: String,
    confirmed: Boolean
  ) = withContext(Dispatchers.IO) {
    val payload = JSONObject().apply {
      put("userId", userId)
      put("actionId", actionId)
      put("confirmed", confirmed)
    }
    val mediaType = "application/json; charset=utf-8".toMediaType()

    for (baseUrl in baseUrls) {
      try {
        val request = Request.Builder()
          .url("$baseUrl/api/hey-numo/confirm")
          .addHeader("x-numo-user-id", userId)
          .post(payload.toString().toRequestBody(mediaType))
          .build()
        httpClient.newCall(request).execute().close()
        return@withContext
      } catch (_: Exception) {
        // Ignore if backend unreachable; local state still updates cleanly
      }
    }
  }

  suspend fun synthesizeSpeech(
    text: String,
    voice: String,
    style: String = "warm, calm, natural and conversational"
  ): ByteArray? = withContext(Dispatchers.IO) {
    val payload = JSONObject().apply {
      put("text", text)
      put("voice", voice)
      put("style", style)
    }
    val mediaType = "application/json; charset=utf-8".toMediaType()

    for (baseUrl in baseUrls) {
      try {
        val request = Request.Builder()
          .url("$baseUrl/api/hey-numo/tts")
          .post(payload.toString().toRequestBody(mediaType))
          .build()
        httpClient.newCall(request).execute().use { response ->
          if (response.isSuccessful) {
            val bodyStr = response.body?.string().orEmpty()
            val b64 = JSONObject(bodyStr).optString("audioBase64")
            if (b64.isNotBlank()) {
              return@withContext Base64.decode(b64, Base64.DEFAULT)
            }
          }
        }
      } catch (_: Exception) {
        // Try next backend URL or fall back to device TTS
      }
    }
    null
  }

  private fun parsePendingAction(obj: JSONObject): PendingFinancialAction {
    val kindStr = obj.optString("kind", "TRANSACTION")
    val kind = when (kindStr) {
      "PAY_BILL" -> PendingActionKind.PAY_BILL
      "CONTRIBUTE_GOAL" -> PendingActionKind.CONTRIBUTE_GOAL
      "CREATE_BUDGET" -> PendingActionKind.CREATE_BUDGET
      "UPDATE_TRANSACTION" -> PendingActionKind.UPDATE_TRANSACTION
      "UPDATE_ASSET" -> PendingActionKind.UPDATE_ASSET
      else -> PendingActionKind.TRANSACTION
    }
    return PendingFinancialAction(
      id = obj.optString("id").ifBlank { java.util.UUID.randomUUID().toString() },
      kind = kind,
      amount = obj.optDouble("amount", 0.0),
      description = obj.optString("description", "Transaction"),
      account = obj.optString("account", "M-PESA"),
      category = obj.optString("category", "Other"),
      isIncome = obj.optBoolean("isIncome", false),
      statusLabel = obj.optString("statusLabel", "Awaiting Approval"),
      isResolved = false,
      wasConfirmed = false
    )
  }

  private fun buildFinancialSnapshot(customAccounts: List<Pair<String, String>>): JSONObject {
    val accountsArr = JSONArray().apply {
      put(JSONObject().apply {
        put("name", "Available Cash / Primary Wallet")
        put("balance", NumoFinancialEngine.availableCash)
      })
      customAccounts.forEach { (name, balance) ->
        put(JSONObject().apply {
          put("name", name)
          put("balance", balance)
        })
      }
    }

    val categoriesArr = JSONArray().apply {
      NumoFinancialEngine.categories.forEach { cat ->
        put(JSONObject().apply {
          put("name", cat.name)
          put("limit", cat.limit)
          put("spent", cat.spent)
          put("remaining", (cat.limit - cat.spent).coerceAtLeast(0.0))
        })
      }
    }

    val txArr = JSONArray().apply {
      NumoFinancialEngine.transactions.take(15).forEach { tx ->
        put(JSONObject().apply {
          put("title", tx.title)
          put("amount", tx.amount)
          put("isIncome", tx.isIncome)
          put("category", tx.category)
          put("account", tx.account)
          put("timestamp", tx.timestamp)
        })
      }
    }

    val billsArr = JSONArray().apply {
      NumoFinancialEngine.bills.forEach { bill ->
        put(JSONObject().apply {
          put("title", bill.title)
          put("amount", bill.amount)
          put("dueDateText", bill.dueDateText)
          put("category", bill.category)
        })
      }
    }

    val goalsArr = JSONArray().apply {
      NumoFinancialEngine.goals.forEach { goal ->
        put(JSONObject().apply {
          put("title", goal.title)
          put("savedAmount", goal.savedAmount)
          put("targetAmount", goal.targetAmount)
          put("monthlyContribution", goal.monthlyContribution)
        })
      }
    }

    val holdingsArr = JSONArray().apply {
      NumoFinancialEngine.investmentHoldings.forEach { h ->
        put(JSONObject().apply {
          put("name", h.name)
          put("assetType", h.assetType)
          put("currentValuation", h.currentValuation)
          put("investedCapital", h.investedCapital)
          put("annualYieldPercent", h.annualYieldPercent)
        })
      }
    }

    return JSONObject().apply {
      put("currency", NumoFinancialEngine.currency)
      put("availableCash", NumoFinancialEngine.availableCash)
      put("totalNetWorth", NumoFinancialEngine.totalNetWorth)
      put("monthlyBudgetTarget", NumoFinancialEngine.monthlyBudgetTarget)
      put("totalSpent", NumoFinancialEngine.totalSpent)
      put("totalRemaining", NumoFinancialEngine.totalRemaining)
      put("totalPortfolioValuation", NumoFinancialEngine.totalPortfolioValuation)
      put("totalInvestedCapital", NumoFinancialEngine.totalInvestedCapital)
      put("totalPortfolioReturn", NumoFinancialEngine.totalPortfolioReturn)
      put("totalPortfolioReturnPercent", NumoFinancialEngine.totalPortfolioReturnPercent)
      put("accounts", accountsArr)
      put("categories", categoriesArr)
      put("transactions", txArr)
      put("bills", billsArr)
      put("goals", goalsArr)
      put("investmentHoldings", holdingsArr)
    }
  }
}
