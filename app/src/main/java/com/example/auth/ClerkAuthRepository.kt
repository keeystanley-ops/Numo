package com.example.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SignUpResult(
  val success: Boolean,
  val signUpId: String? = null,
  val status: String? = null,
  val errorMessage: String? = null
)

data class VerificationResult(
  val success: Boolean,
  val sessionId: String? = null,
  val status: String? = null,
  val errorMessage: String? = null
)

data class SignInResult(
  val success: Boolean,
  val sessionId: String? = null,
  val status: String? = null,
  val errorMessage: String? = null
)

data class SessionValidationResult(
  val success: Boolean,
  val sessionId: String? = null,
  val email: String? = null,
  val errorMessage: String? = null
)

object ClerkAuthRepository {
  private const val TAG = "ClerkAuth"
  private const val PUBLISHABLE_KEY = "pk_test_a25vd2luZy1zbmlwZS03MzY0LmNsZXJrLmFjY291bnRzLmRldiQ"
  private const val FAPI_BASE = "https://knowing-snipe-7364.clerk.accounts.dev/v1/client"

  // Rotating client session token returned by Clerk in response headers
  @Volatile
  private var clientToken: String? = null

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()

  /**
   * Builds the Authorization header. In Clerk native mobile flows,
   * requests must pass the client session JWT once established.
   */
  private fun getAuthHeader(): String {
    val token = clientToken
    return if (!token.isNullOrBlank()) {
      if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
    } else {
      "Bearer $PUBLISHABLE_KEY"
    }
  }

  fun setClientToken(token: String) {
    clientToken = token.removePrefix("Bearer ").trim()
    Log.d(TAG, "Explicitly set Clerk client session token")
  }

  fun clearClientToken() {
    clientToken = null
  }

  /**
   * Verifies whether an active, valid authenticated session exists with Clerk.
   * Strictly required when returning from OAuth browser redirect to ensure
   * the user did not click 'cancel' or leave without completing authentication.
   */
  suspend fun verifyCurrentClientSession(): SessionValidationResult = withContext(Dispatchers.IO) {
    try {
      val request = Request.Builder()
        .url("$FAPI_BASE?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .get()
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful || bodyStr.isEmpty()) {
          return@withContext SessionValidationResult(
            success = false,
            errorMessage = "No active session found. Sign-in was cancelled or not completed."
          )
        }

        val json = JSONObject(bodyStr)
        val responseObj = json.optJSONObject("response") ?: json
        val sessionsArr = responseObj.optJSONArray("sessions")
        val lastActiveSessionId = responseObj.optString("last_active_session_id")

        if (sessionsArr != null && sessionsArr.length() > 0) {
          // Look for an active session matching lastActiveSessionId or the first active session
          var activeEmail: String? = null
          for (i in 0 until sessionsArr.length()) {
            val s = sessionsArr.optJSONObject(i) ?: continue
            val status = s.optString("status")
            val id = s.optString("id")
            if (status == "active" || id == lastActiveSessionId) {
              val user = s.optJSONObject("user")
              val primaryEmailId = user?.optString("primary_email_address_id")
              val emailsArr = user?.optJSONArray("email_addresses")
              if (emailsArr != null && emailsArr.length() > 0) {
                for (j in 0 until emailsArr.length()) {
                  val emObj = emailsArr.optJSONObject(j) ?: continue
                  val emId = emObj.optString("id")
                  val address = emObj.optString("email_address")
                  if (emId == primaryEmailId || activeEmail == null) {
                    activeEmail = address
                  }
                }
              }
              if (activeEmail != null) break
            }
          }

          if (activeEmail != null) {
            return@withContext SessionValidationResult(
              success = true,
              sessionId = lastActiveSessionId,
              email = activeEmail
            )
          }
        }

        // Also check if sign_up or sign_in external account is still pending or abandoned
        val signUpObj = responseObj.optJSONObject("sign_up")
        if (signUpObj != null) {
          val suStatus = signUpObj.optString("status")
          if (suStatus != "complete") {
            return@withContext SessionValidationResult(
              success = false,
              errorMessage = "Sign-up was cancelled or incomplete (status: $suStatus)."
            )
          }
        }

        val signInObj = responseObj.optJSONObject("sign_in")
        if (signInObj != null) {
          val siStatus = signInObj.optString("status")
          if (siStatus != "complete") {
            return@withContext SessionValidationResult(
              success = false,
              errorMessage = "Sign-in was cancelled or incomplete (status: $siStatus)."
            )
          }
        }

        return@withContext SessionValidationResult(
          success = false,
          errorMessage = "Authentication was cancelled before completing."
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error verifying Clerk client session", e)
      return@withContext SessionValidationResult(
        success = false,
        errorMessage = "Could not verify authentication session."
      )
    }
  }

  /**
   * Captures and stores any rotating Authorization / Clerk-Db-Jwt header
   * sent back by Clerk.
   */
  private fun updateClientToken(response: Response) {
    val auth = response.header("Authorization")
      ?: response.header("authorization")
      ?: response.header("Clerk-Db-Jwt")
      ?: response.header("clerk-db-jwt")

    if (!auth.isNullOrBlank()) {
      clientToken = auth.removePrefix("Bearer ").trim()
      Log.d(TAG, "Updated Clerk client session token")
    }
  }

  /**
   * Requests an official OAuth verification URL from Clerk Frontend API for Google or Apple.
   * isSignUp: true for sign-up flow, false for sign-in flow.
   */
  suspend fun getOAuthRedirectUrl(provider: String, isSignUp: Boolean = false): String? = withContext(Dispatchers.IO) {
    try {
      val strategy = if (provider.equals("apple", ignoreCase = true)) "oauth_apple" else "oauth_google"
      val endpoint = if (isSignUp) "$FAPI_BASE/sign_ups?_is_native=1" else "$FAPI_BASE/sign_ins?_is_native=1"

      val formBody = FormBody.Builder()
        .add("strategy", strategy)
        .add("redirect_url", "numo://oauth_callback")
        .add("action_complete_redirect_url", "numo://oauth_callback")
        .build()

      val request = Request.Builder()
        .url(endpoint)
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        if (bodyStr.isEmpty()) return@withContext null

        val json = JSONObject(bodyStr)
        val responseObj = json.optJSONObject("response") ?: json

        // 1. Check sign_up external account verification
        val verifications = responseObj.optJSONObject("verifications")
        val externalAccount = verifications?.optJSONObject("external_account")
        val suUrl = externalAccount?.optString("external_verification_redirect_url")
        if (!suUrl.isNullOrEmpty()) {
          Log.d(TAG, "Clerk OAuth URL generated (sign_up): $suUrl")
          return@withContext suUrl
        }

        // 2. Check sign_in first factor verification
        val firstFactor = responseObj.optJSONObject("first_factor_verification")
        val siUrl = firstFactor?.optString("external_verification_redirect_url")
        if (!siUrl.isNullOrEmpty()) {
          Log.d(TAG, "Clerk OAuth URL generated (sign_in): $siUrl")
          return@withContext siUrl
        }

        null
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching OAuth URL from Clerk for $provider", e)
      null
    }
  }

  /**
   * Safe fallback OAuth URL if offline or API returns error.
   */
  fun getOAuthUrl(provider: String): String {
    return if (provider.equals("apple", ignoreCase = true)) {
      "https://appleid.apple.com/auth/authorize?client_id=numo.app&response_type=code&scope=name%20email"
    } else {
      "https://accounts.google.com/o/oauth2/v2/auth?client_id=787459168867-0v2orf3qo56uocsi84iroseoahhuovdm.apps.googleusercontent.com&response_type=code&scope=openid%20profile%20email"
    }
  }

  /**
   * Initiates Sign Up with Clerk Frontend API.
   */
  suspend fun signUp(
    email: String,
    pass: String,
    fullName: String
  ): SignUpResult = withContext(Dispatchers.IO) {
    try {
      val names = fullName.trim().split(" ", limit = 2)
      val firstName = names.getOrNull(0) ?: ""
      val lastName = names.getOrNull(1) ?: ""

      val formBody = FormBody.Builder()
        .add("email_address", email.trim())
        .add("password", pass)
        .apply {
          if (firstName.isNotEmpty()) add("first_name", firstName)
          if (lastName.isNotEmpty()) add("last_name", lastName)
        }
        .build()

      // Include _is_native=1 query parameter required for native client flow
      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ups?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()

        if (!response.isSuccessful) {
          val errorMsg = extractErrorMessage(json)
          return@withContext SignUpResult(success = false, errorMessage = errorMsg)
        }

        val responseObj = json.optJSONObject("response") ?: json
        val signUpId = responseObj.optString("id")
        val status = responseObj.optString("status")

        // Prepare email verification OTP dispatch
        if (signUpId.isNotEmpty()) {
          val (prepOk, prepErr) = prepareEmailVerification(signUpId)
          if (!prepOk) {
            return@withContext SignUpResult(
              success = false,
              errorMessage = prepErr ?: "Failed to send verification code. Please try again."
            )
          }
        }

        return@withContext SignUpResult(
          success = true,
          signUpId = signUpId,
          status = status
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error during signUp", e)
      return@withContext SignUpResult(
        success = false,
        errorMessage = e.message ?: "Failed to connect to Clerk server."
      )
    }
  }

  /**
   * Prepares email verification code (dispatches 6-digit OTP to user's inbox from Clerk).
   */
  private fun prepareEmailVerification(signUpId: String): Pair<Boolean, String?> {
    return try {
      val formBody = FormBody.Builder()
        .add("strategy", "email_code")
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ups/$signUpId/prepare_verification?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()

        if (response.isSuccessful) {
          Log.d(TAG, "Clerk email verification code successfully dispatched")
          Pair(true, null)
        } else {
          val errorMsg = extractErrorMessage(json)
          Log.w(TAG, "Clerk prepare_verification failed: $errorMsg")
          Pair(false, errorMsg)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Exception in prepareEmailVerification", e)
      Pair(false, e.message ?: "Failed to dispatch email code")
    }
  }

  /**
   * Resends the email verification code.
   */
  suspend fun resendVerificationCode(signUpId: String): Boolean = withContext(Dispatchers.IO) {
    val (ok, _) = prepareEmailVerification(signUpId)
    ok
  }

  /**
   * Attempts verification with the 6-digit email code.
   */
  suspend fun attemptVerification(
    signUpId: String,
    code: String
  ): VerificationResult = withContext(Dispatchers.IO) {
    try {
      val formBody = FormBody.Builder()
        .add("strategy", "email_code")
        .add("code", code.trim())
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ups/$signUpId/attempt_verification?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()

        if (!response.isSuccessful) {
          val errorMsg = extractErrorMessage(json)
          return@withContext VerificationResult(success = false, errorMessage = errorMsg)
        }

        val responseObj = json.optJSONObject("response") ?: json
        val createdSessionId = responseObj.optString("created_session_id")
        val status = responseObj.optString("status")

        // Also check if email verification object marked status as verified
        val verifications = responseObj.optJSONObject("verifications")
        val emailVerification = verifications?.optJSONObject("email_address")
        val emailStatus = emailVerification?.optString("status")

        val isCompleted = status == "complete" ||
          createdSessionId.isNotEmpty() ||
          emailStatus == "verified"

        return@withContext VerificationResult(
          success = isCompleted,
          sessionId = if (createdSessionId.isNotEmpty()) createdSessionId else "session_verified",
          status = status,
          errorMessage = if (!isCompleted) "Verification incomplete. Please retry." else null
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error during attemptVerification", e)
      return@withContext VerificationResult(
        success = false,
        errorMessage = e.message ?: "Verification failed."
      )
    }
  }

  /**
   * Signs in an existing user with email and password via Clerk.
   */
  suspend fun signIn(email: String, pass: String): SignInResult = withContext(Dispatchers.IO) {
    try {
      val formBody = FormBody.Builder()
        .add("identifier", email.trim())
        .add("password", pass)
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ins?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()

        if (!response.isSuccessful) {
          val errorMsg = extractErrorMessage(json)
          return@withContext SignInResult(success = false, errorMessage = errorMsg)
        }

        val responseObj = json.optJSONObject("response") ?: json
        var createdSessionId = responseObj.optString("created_session_id")
        var status = responseObj.optString("status")
        val signInId = responseObj.optString("id")

        // If Clerk requires first factor attempt (e.g. password factor)
        if (status == "needs_first_factor" && signInId.isNotEmpty()) {
          val factorBody = FormBody.Builder()
            .add("strategy", "password")
            .add("password", pass)
            .build()

          val factorReq = Request.Builder()
            .url("$FAPI_BASE/sign_ins/$signInId/attempt_first_factor?_is_native=1")
            .addHeader("Authorization", getAuthHeader())
            .post(factorBody)
            .build()

          client.newCall(factorReq).execute().use { fResponse ->
            updateClientToken(fResponse)
            val fStr = fResponse.body?.string() ?: ""
            val fJson = if (fStr.isNotEmpty()) JSONObject(fStr) else JSONObject()
            if (fResponse.isSuccessful) {
              val fObj = fJson.optJSONObject("response") ?: fJson
              status = fObj.optString("status")
              createdSessionId = fObj.optString("created_session_id")
            }
          }
        }

        val isSuccess = status == "complete" ||
          status == "needs_client_trust" ||
          createdSessionId.isNotEmpty()

        return@withContext SignInResult(
          success = isSuccess,
          sessionId = if (createdSessionId.isNotEmpty()) createdSessionId else "session_authenticated",
          status = status,
          errorMessage = if (!isSuccess) "Sign in failed: status is $status" else null
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error during signIn", e)
      return@withContext SignInResult(
        success = false,
        errorMessage = e.message ?: "Failed to sign in."
      )
    }
  }

  private fun extractErrorMessage(json: JSONObject): String {
    val errorsArr = json.optJSONArray("errors")
      ?: json.optJSONObject("meta")?.optJSONArray("errors")

    if (errorsArr != null && errorsArr.length() > 0) {
      val errObj = errorsArr.optJSONObject(0)
      if (errObj != null) {
        val longMessage = errObj.optString("long_message")
        if (longMessage.isNotEmpty()) return longMessage
        val message = errObj.optString("message")
        if (message.isNotEmpty()) return message
      }
    }
    return "Authentication failed. Please check your details and try again."
  }
}
