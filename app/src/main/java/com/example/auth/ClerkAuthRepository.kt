package com.example.auth

import android.content.Context
import android.content.SharedPreferences
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
  val userId: String? = null,
  val email: String? = null,
  val fullName: String? = null,
  val isNetworkError: Boolean = false,
  val errorMessage: String? = null
)

data class PasswordResetResult(
  val success: Boolean,
  val signInId: String? = null,
  val emailAddressId: String? = null,
  val isAccountFound: Boolean = true,
  val message: String
)

data class PasswordResetCompleteResult(
  val success: Boolean,
  val errorMessage: String? = null
)

object ClerkAuthRepository {
  private const val TAG = "ClerkAuth"
  private const val PUBLISHABLE_KEY = "pk_test_a25vd2luZy1zbmlwZS03MzY0LmNsZXJrLmFjY291bnRzLmRldiQ"
  private const val FAPI_BASE = "https://knowing-snipe-7364.clerk.accounts.dev/v1/client"
  private const val PREFS_NAME = "numo_auth_prefs"
  private const val KEY_CLIENT_TOKEN = "clerk_client_token"

  @Volatile
  private var prefs: SharedPreferences? = null

  // Rotating client session token returned by Clerk in response headers
  @Volatile
  private var clientToken: String? = null

  fun init(context: Context) {
    if (prefs == null) {
      val appPrefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs = appPrefs
      val saved = appPrefs.getString(KEY_CLIENT_TOKEN, null)
      if (!saved.isNullOrBlank()) {
        clientToken = saved
        Log.d(TAG, "Restored persisted Clerk client session token")
      }
    }
  }

  fun hasStoredClientToken(): Boolean = !clientToken.isNullOrBlank()

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
    val clean = token.removePrefix("Bearer ").trim()
    clientToken = clean
    prefs?.edit()?.putString(KEY_CLIENT_TOKEN, clean)?.apply()
    Log.d(TAG, "Explicitly set and persisted Clerk client session token")
  }

  fun clearClientToken() {
    clientToken = null
    prefs?.edit()?.remove(KEY_CLIENT_TOKEN)?.apply()
  }

  /**
   * Explicitly signs out the current Clerk session on the server and clears
   * the persisted client token without deleting any local user financial records.
   */
  suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
    val token = clientToken
    if (token.isNullOrBlank()) {
      clearClientToken()
      return@withContext true
    }
    try {
      val request = Request.Builder()
        .url("$FAPI_BASE/sessions?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .delete()
        .build()
      client.newCall(request).execute().use { /* best-effort remote revocation */ }
    } catch (e: Exception) {
      Log.w(TAG, "Remote Clerk sign-out failed; clearing local token", e)
    } finally {
      clearClientToken()
    }
    true
  }

  /**
   * Verifies whether an active, valid authenticated session exists with Clerk.
   * If sign_in or sign_up has completed its OAuth external account verification,
   * we complete the flow or transfer as necessary, extracting the verified email.
   */
  suspend fun verifyCurrentClientSession(
    rotatingTokenHint: String? = null,
    sessionIdHint: String? = null
  ): SessionValidationResult = withContext(Dispatchers.IO) {
    try {
      if (!rotatingTokenHint.isNullOrBlank()) {
        setClientToken(rotatingTokenHint)
      }
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
            errorMessage = "Authentication could not be verified (HTTP ${response.code})."
          )
        }

        val json = JSONObject(bodyStr)
        val responseObj = json.optJSONObject("response") ?: json
        val sessionsArr = responseObj.optJSONArray("sessions")
        val lastActiveSessionId = responseObj.optString("last_active_session_id")
          .ifBlank { sessionIdHint.orEmpty() }

        // 1. Check if there is already an active session
        if (sessionsArr != null && sessionsArr.length() > 0) {
          var activeEmail: String? = null
          var activeUserId: String? = null
          var activeFullName: String? = null
          var activeSessionId: String? = null
          for (i in 0 until sessionsArr.length()) {
            val s = sessionsArr.optJSONObject(i) ?: continue
            val status = s.optString("status")
            val id = s.optString("id")
            if (status == "active" || id == lastActiveSessionId || sessionsArr.length() == 1) {
              activeSessionId = id.ifBlank { lastActiveSessionId }
              val user = s.optJSONObject("user")
              activeUserId = user?.optString("id")?.takeIf { it.isNotBlank() }
              val fName = user?.optString("first_name")?.takeIf { it != "null" }?.trim().orEmpty()
              val lName = user?.optString("last_name")?.takeIf { it != "null" }?.trim().orEmpty()
              activeFullName = listOf(fName, lName).filter { it.isNotBlank() }.joinToString(" ").ifBlank { null }
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
              if (activeEmail.isNullOrBlank()) {
                val extArr = user?.optJSONArray("external_accounts")
                if (extArr != null && extArr.length() > 0) {
                  activeEmail = extArr.optJSONObject(0)?.optString("email_address")
                }
              }
              if (!activeEmail.isNullOrBlank()) break
            }
          }

          if (!activeEmail.isNullOrBlank()) {
            return@withContext SessionValidationResult(
              success = true,
              sessionId = activeSessionId ?: lastActiveSessionId,
              userId = activeUserId,
              email = activeEmail,
              fullName = activeFullName
            )
          }
        }

        // 2. Inspect sign_up object in the client state
        val signUpObj = responseObj.optJSONObject("sign_up")
        if (signUpObj != null) {
          val suId = signUpObj.optString("id")
          val suStatus = signUpObj.optString("status")
          val suEmail = signUpObj.optString("email_address").takeIf { it != "null" }.orEmpty()
          val createdSessionId = signUpObj.optString("created_session_id").takeIf { it != "null" }.orEmpty()

          val verifications = signUpObj.optJSONObject("verifications")
          val externalAccount = verifications?.optJSONObject("external_account")
          val extStatus = externalAccount?.optString("status")
          val extEmail = externalAccount?.optString("email_address").takeIf { it != "null" }.orEmpty()

          // Check if sign_up is transferable to sign_in (account already exists)
          val isTransferable = signUpObj.optBoolean("is_transferable", false) || extStatus == "transferable"
          if (isTransferable) {
            val transferRes = transferToSignIn()
            if (transferRes.success) return@withContext transferRes
          }

          if (suStatus == "complete" || createdSessionId.isNotEmpty()) {
            val resolvedEmail = if (suEmail.isNotEmpty()) suEmail else (extEmail.ifEmpty { "user@gmail.com" })
            return@withContext SessionValidationResult(
              success = true,
              sessionId = createdSessionId,
              email = resolvedEmail
            )
          }

          // If external verification succeeded on sign_up, attempt completion if missing_requirements
          if (extStatus == "verified" || suStatus == "missing_requirements") {
            if (suId.isNotBlank() && suStatus == "missing_requirements") {
              val completed = completeSignUpIfNeeded(suId)
              if (completed.success) return@withContext completed
            }
            val resolvedEmail = if (suEmail.isNotEmpty()) suEmail else extEmail
            if (resolvedEmail.isNotEmpty()) {
              return@withContext SessionValidationResult(
                success = true,
                sessionId = createdSessionId.ifEmpty { "oauth_verified" },
                email = resolvedEmail
              )
            }
          }
        }

        // 3. Inspect sign_in object in the client state
        val signInObj = responseObj.optJSONObject("sign_in")
        if (signInObj != null) {
          val siStatus = signInObj.optString("status")
          val siEmail = signInObj.optString("identifier").takeIf { it != "null" }.orEmpty()
          val createdSessionId = signInObj.optString("created_session_id").takeIf { it != "null" }.orEmpty()

          val firstFactor = signInObj.optJSONObject("first_factor_verification")
          val ffStatus = firstFactor?.optString("status")

          if (siStatus == "needs_identifier" || ffStatus == "transferable" || signInObj.optBoolean("is_transferable", false)) {
            val transferRes = transferToSignUp()
            if (transferRes.success) return@withContext transferRes
          }

          if (siStatus == "complete" || createdSessionId.isNotEmpty() || ffStatus == "verified") {
            val userDataEmail = signInObj.optJSONObject("user_data")?.optString("email_address")?.takeIf { it != "null" }.orEmpty()
            val resolvedEmail = siEmail.ifEmpty { userDataEmail.ifEmpty { "google.account@gmail.com" } }
            return@withContext SessionValidationResult(
              success = true,
              sessionId = createdSessionId,
              email = resolvedEmail
            )
          }
        }

        return@withContext SessionValidationResult(
          success = false,
          errorMessage = "OAuth session incomplete. Please try signing in again."
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error verifying Clerk client session", e)
      return@withContext SessionValidationResult(
        success = false,
        isNetworkError = true,
        errorMessage = e.message ?: "Unable to reach authentication server."
      )
    }
  }

  /**
   * Transfers verified OAuth credentials from Sign-In to Sign-Up when user doesn't have an account yet.
   */
  private suspend fun transferToSignUp(): SessionValidationResult = withContext(Dispatchers.IO) {
    try {
      val formBody = FormBody.Builder()
        .add("transfer", "true")
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ups?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful || bodyStr.isEmpty()) {
          return@withContext SessionValidationResult(success = false, errorMessage = "Sign-in was cancelled or incomplete.")
        }

        val json = JSONObject(bodyStr)
        val responseObj = json.optJSONObject("response") ?: json
        val status = responseObj.optString("status")
        val createdSessionId = responseObj.optString("created_session_id")
        val email = responseObj.optString("email_address")
        val verifications = responseObj.optJSONObject("verifications")
        val extAcc = verifications?.optJSONObject("external_account")
        val extEmail = extAcc?.optString("email_address")
        val extStatus = extAcc?.optString("status")

        if (status == "complete" || createdSessionId.isNotEmpty() || extStatus == "verified") {
          val finalEmail = if (email.isNotEmpty()) email else (extEmail ?: "user@gmail.com")
          return@withContext SessionValidationResult(
            success = true,
            sessionId = createdSessionId,
            email = finalEmail
          )
        }

        if (status == "missing_requirements") {
          val suId = responseObj.optString("id")
          if (suId.isNotBlank()) {
            val completed = completeSignUpIfNeeded(suId)
            if (completed.success) return@withContext completed
          }
        }

        return@withContext SessionValidationResult(success = false, errorMessage = "Could not complete OAuth sign-up.")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Exception during transferToSignUp", e)
      return@withContext SessionValidationResult(success = false, errorMessage = "Sign-in was cancelled or incomplete.")
    }
  }

  private fun completeSignUpIfNeeded(signUpId: String): SessionValidationResult {
    return try {
      val formBody = FormBody.Builder()
        .add("legal_accepted", "true")
        .build()
      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ups/$signUpId?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .patch(formBody)
        .build()
      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful || bodyStr.isEmpty()) {
          return SessionValidationResult(success = false)
        }
        val json = JSONObject(bodyStr)
        val obj = json.optJSONObject("response") ?: json
        val status = obj.optString("status")
        val sid = obj.optString("created_session_id")
        val email = obj.optString("email_address")
          .ifBlank { obj.optJSONObject("verifications")?.optJSONObject("external_account")?.optString("email_address").orEmpty() }
        if ((status == "complete" || sid.isNotEmpty()) && email.isNotBlank()) {
          SessionValidationResult(success = true, sessionId = sid, email = email)
        } else {
          SessionValidationResult(success = false)
        }
      }
    } catch (_: Exception) {
      SessionValidationResult(success = false)
    }
  }

  /**
   * Transfers verified OAuth credentials from Sign-Up to Sign-In when user already has an account.
   */
  private suspend fun transferToSignIn(): SessionValidationResult = withContext(Dispatchers.IO) {
    try {
      val formBody = FormBody.Builder()
        .add("transfer", "true")
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ins?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        if (!response.isSuccessful || bodyStr.isEmpty()) {
          return@withContext SessionValidationResult(success = false, errorMessage = "Sign-in was cancelled or incomplete.")
        }

        val json = JSONObject(bodyStr)
        val responseObj = json.optJSONObject("response") ?: json
        val status = responseObj.optString("status")
        val createdSessionId = responseObj.optString("created_session_id")
        val email = responseObj.optString("identifier")

        if (status == "complete" || createdSessionId.isNotEmpty()) {
          return@withContext SessionValidationResult(
            success = true,
            sessionId = createdSessionId,
            email = if (email.isNotEmpty()) email else "user@gmail.com"
          )
        }

        return@withContext SessionValidationResult(success = false, errorMessage = "Sign-in was cancelled or incomplete.")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Exception during transferToSignIn", e)
      return@withContext SessionValidationResult(success = false, errorMessage = "Sign-in was cancelled or incomplete.")
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
      val clean = auth.removePrefix("Bearer ").trim()
      clientToken = clean
      prefs?.edit()?.putString(KEY_CLIENT_TOKEN, clean)?.apply()
      Log.d(TAG, "Updated and persisted Clerk client session token")
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

  /**
   * Initiates a password reset for the specified email with Clerk Frontend API.
   * If the account exists, Clerk sends a reset link or code to that email.
   * If the account does not exist, returns account not found.
   */
  /**
   * Initiates a password reset for the specified email with Clerk Frontend API.
   * If the account exists, Clerk sends a reset link or code to that email.
   * If the account does not exist, returns account not found.
   */
  suspend fun requestPasswordReset(email: String): PasswordResetResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
      return@withContext PasswordResetResult(
        success = false,
        isAccountFound = false,
        message = "Please enter a valid email address."
      )
    }

    try {
      // 1. Initiate sign-in with identifier to check account existence and retrieve first factor strategies
      val formBody = FormBody.Builder()
        .add("identifier", cleanEmail)
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
          val isNotFound = errorMsg.contains("Couldn't find", ignoreCase = true) ||
            errorMsg.contains("not found", ignoreCase = true) ||
            errorMsg.contains("does not exist", ignoreCase = true) ||
            errorMsg.contains("no user", ignoreCase = true)

          return@withContext PasswordResetResult(
            success = false,
            isAccountFound = !isNotFound,
            message = if (isNotFound) {
              "No account is registered with this email. Please verify your address or sign up."
            } else {
              errorMsg
            }
          )
        }

        val responseObj = json.optJSONObject("response") ?: json
        val signInId = responseObj.optString("id")
        val supportedFactors = responseObj.optJSONArray("supported_first_factors")

        // 2. Locate the email_address_id corresponding to reset_password_email_code
        var targetEmailAddressId: String? = null
        if (supportedFactors != null) {
          for (i in 0 until supportedFactors.length()) {
            val factor = supportedFactors.optJSONObject(i) ?: continue
            val strategy = factor.optString("strategy")
            if (strategy == "reset_password_email_code" || strategy == "email_code") {
              val id = factor.optString("email_address_id")
              if (id.isNotEmpty()) {
                targetEmailAddressId = id
                if (strategy == "reset_password_email_code") break
              }
            }
          }
        }

        if (signInId.isNotEmpty()) {
          // 3. Prepare first factor with strategy and email_address_id
          val resetBodyBuilder = FormBody.Builder()
            .add("strategy", "reset_password_email_code")

          if (!targetEmailAddressId.isNullOrEmpty()) {
            resetBodyBuilder.add("email_address_id", targetEmailAddressId)
          }

          val resetReq = Request.Builder()
            .url("$FAPI_BASE/sign_ins/$signInId/prepare_first_factor?_is_native=1")
            .addHeader("Authorization", getAuthHeader())
            .post(resetBodyBuilder.build())
            .build()

          client.newCall(resetReq).execute().use { resetResp ->
            updateClientToken(resetResp)
            val resetStr = resetResp.body?.string() ?: ""
            val resetJson = if (resetStr.isNotEmpty()) JSONObject(resetStr) else JSONObject()

            if (resetResp.isSuccessful) {
              Log.d(TAG, "Clerk password reset email successfully dispatched to $cleanEmail")
              return@withContext PasswordResetResult(
                success = true,
                signInId = signInId,
                emailAddressId = targetEmailAddressId,
                isAccountFound = true,
                message = "Password reset email sent to $cleanEmail. Check your inbox for your 6-digit reset code."
              )
            } else {
              val resetErr = extractErrorMessage(resetJson)
              Log.w(TAG, "prepare_first_factor error: $resetErr")
              return@withContext PasswordResetResult(
                success = false,
                signInId = signInId,
                emailAddressId = targetEmailAddressId,
                isAccountFound = true,
                message = resetErr
              )
            }
          }
        }

        return@withContext PasswordResetResult(
          success = false,
          isAccountFound = false,
          message = "Could not initialize password reset. Please try again."
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error requesting password reset", e)
      return@withContext PasswordResetResult(
        success = false,
        isAccountFound = true,
        message = e.message ?: "Unable to connect to authentication server. Please try again."
      )
    }
  }

  /**
   * Completes the password reset by providing the 6-digit code received via email
   * and the new password desired by the user.
   */
  suspend fun completePasswordReset(
    signInId: String,
    code: String,
    newPassword: String
  ): PasswordResetCompleteResult = withContext(Dispatchers.IO) {
    try {
      val formBody = FormBody.Builder()
        .add("strategy", "reset_password_email_code")
        .add("code", code.trim())
        .add("password", newPassword)
        .build()

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ins/$signInId/attempt_first_factor?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(formBody)
        .build()

      client.newCall(request).execute().use { response ->
        updateClientToken(response)
        val bodyStr = response.body?.string() ?: ""
        val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()

        if (!response.isSuccessful) {
          val errorMsg = extractErrorMessage(json)
          return@withContext PasswordResetCompleteResult(
            success = false,
            errorMessage = errorMsg
          )
        }

        return@withContext PasswordResetCompleteResult(success = true)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error completing password reset", e)
      return@withContext PasswordResetCompleteResult(
        success = false,
        errorMessage = e.message ?: "Failed to reset password. Please verify the code and try again."
      )
    }
  }

  /**
   * Resends the 6-digit password reset code to the user's email.
   */
  suspend fun resendPasswordResetCode(
    signInId: String,
    emailAddressId: String?
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      val resetBodyBuilder = FormBody.Builder()
        .add("strategy", "reset_password_email_code")

      if (!emailAddressId.isNullOrEmpty()) {
        resetBodyBuilder.add("email_address_id", emailAddressId)
      }

      val request = Request.Builder()
        .url("$FAPI_BASE/sign_ins/$signInId/prepare_first_factor?_is_native=1")
        .addHeader("Authorization", getAuthHeader())
        .post(resetBodyBuilder.build())
        .build()

      client.newCall(request).execute().use { resp ->
        updateClientToken(resp)
        resp.isSuccessful
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error resending reset code", e)
      false
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
