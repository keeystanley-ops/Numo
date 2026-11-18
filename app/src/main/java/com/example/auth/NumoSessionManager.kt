package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import com.example.data.NumoFinancialEngine
import org.json.JSONArray
import org.json.JSONObject

enum class StartupAuthState {
  INITIALIZING,
  UNAUTHENTICATED,
  ONBOARDING_REQUIRED,
  ONBOARDING_COMPLETED,
  AUTHENTICATED,
  SESSION_EXPIRED
}

data class NumoUserAccountData(
  val userKey: String,
  val userId: String = "",
  val email: String = "",
  val fullName: String = "",
  val authProvider: String = "Email",
  val onboardingCompleted: Boolean = false,
  val currency: String = "KES",
  val availableCash: Double = 48750.0,
  val accounts: List<Pair<String, String>> = emptyList(),
  val biometricEnabled: Boolean = false,
  val twoFactorEnabled: Boolean = false
)

data class StartupRouteDecision(
  val authState: StartupAuthState,
  val userProfile: NumoUserAccountData? = null,
  val isFirstTimeUser: Boolean = true
)

/**
 * Centralized persistent session & per-user onboarding/account manager.
 *
 * Isolates persisted onboarding completion, currency, and linked accounts per
 * authenticated user (never storing passwords or mixing accounts).
 */
object NumoSessionManager {
  private const val PREFS_NAME = "numo_user_profiles_prefs"
  private const val KEY_PROFILE_PREFIX = "profile_"
  private const val KEY_EMAIL_TO_USERKEY_PREFIX = "email_map_"
  private const val KEY_LAST_AUTH_USER_KEY = "last_authenticated_user_key"
  private const val KEY_EXPLICIT_LOGOUT = "explicitly_logged_out"
  private const val KEY_BIOMETRIC_OPT_IN_USER_KEY = "biometric_opt_in_user_key"

  @Volatile
  private var prefs: SharedPreferences? = null

  fun init(context: Context) {
    if (prefs == null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
  }

  private fun normalizeEmail(email: String): String = email.trim().lowercase()

  fun resolveUserKey(userId: String?, email: String): String {
    val cleanId = userId?.trim().orEmpty()
    if (cleanId.isNotEmpty()) return cleanId
    val cleanEmail = normalizeEmail(email)
    val mapped = prefs?.getString("$KEY_EMAIL_TO_USERKEY_PREFIX$cleanEmail", null)
    return if (!mapped.isNullOrBlank()) mapped else cleanEmail
  }

  fun loadProfile(userId: String?, email: String, fallbackFullName: String? = null, fallbackProvider: String = "Email"): NumoUserAccountData {
    val cleanEmail = normalizeEmail(email)
    val primaryKey = resolveUserKey(userId, cleanEmail)
    val p = prefs ?: return NumoUserAccountData(
      userKey = primaryKey,
      userId = userId.orEmpty(),
      email = email.trim(),
      fullName = fallbackFullName.orEmpty(),
      authProvider = fallbackProvider
    )

    // Check primary key first, then fallback to email key in case userId was newly resolved
    val rawJson = p.getString("$KEY_PROFILE_PREFIX$primaryKey", null)
      ?: p.getString("$KEY_PROFILE_PREFIX$cleanEmail", null)

    if (rawJson.isNullOrBlank()) {
      return NumoUserAccountData(
        userKey = primaryKey,
        userId = userId.orEmpty(),
        email = email.trim(),
        fullName = fallbackFullName.orEmpty(),
        authProvider = fallbackProvider
      )
    }

    return parseProfileJson(rawJson, primaryKey, userId, email, fallbackFullName, fallbackProvider)
  }

  private fun parseProfileJson(
    rawJson: String,
    primaryKey: String,
    userId: String?,
    email: String,
    fallbackFullName: String?,
    fallbackProvider: String
  ): NumoUserAccountData {
    return try {
      val obj = JSONObject(rawJson)
      val accountsArr = obj.optJSONArray("accounts") ?: JSONArray()
      val parsedAccounts = mutableListOf<Pair<String, String>>()
      for (i in 0 until accountsArr.length()) {
        val item = accountsArr.optJSONObject(i) ?: continue
        val name = item.optString("name")
        val bal = item.optString("balance")
        if (name.isNotBlank()) parsedAccounts.add(Pair(name, bal))
      }
      NumoUserAccountData(
        userKey = primaryKey,
        userId = userId?.takeIf { it.isNotBlank() } ?: obj.optString("userId"),
        email = obj.optString("email").ifBlank { email.trim() },
        fullName = obj.optString("fullName").ifBlank { fallbackFullName.orEmpty() },
        authProvider = obj.optString("authProvider").ifBlank { fallbackProvider },
        onboardingCompleted = obj.optBoolean("onboardingCompleted", false),
        currency = obj.optString("currency").ifBlank { "KES" },
        availableCash = obj.optDouble("availableCash", 48750.0),
        accounts = parsedAccounts,
        biometricEnabled = obj.optBoolean("biometricEnabled", false),
        twoFactorEnabled = obj.optBoolean("twoFactorEnabled", false)
      )
    } catch (_: Exception) {
      NumoUserAccountData(
        userKey = primaryKey,
        userId = userId.orEmpty(),
        email = email.trim(),
        fullName = fallbackFullName.orEmpty(),
        authProvider = fallbackProvider
      )
    }
  }

  fun saveProfile(profile: NumoUserAccountData, markAsLastAuthenticated: Boolean = true) {
    val p = prefs ?: return
    val accountsArr = JSONArray()
    profile.accounts.forEach { (name, balance) ->
      accountsArr.put(
        JSONObject().apply {
          put("name", name)
          put("balance", balance)
        }
      )
    }

    val json = JSONObject().apply {
      put("userId", profile.userId)
      put("email", profile.email)
      put("fullName", profile.fullName)
      put("authProvider", profile.authProvider)
      put("onboardingCompleted", profile.onboardingCompleted)
      put("currency", profile.currency)
      put("availableCash", profile.availableCash)
      put("accounts", accountsArr)
      put("biometricEnabled", profile.biometricEnabled)
      put("twoFactorEnabled", profile.twoFactorEnabled)
    }.toString()

    val editor = p.edit()
      .putString("$KEY_PROFILE_PREFIX${profile.userKey}", json)
    val cleanEmail = normalizeEmail(profile.email)
    if (cleanEmail.isNotBlank()) {
      editor.putString("$KEY_EMAIL_TO_USERKEY_PREFIX$cleanEmail", profile.userKey)
    }
    if (markAsLastAuthenticated) {
      editor.putString(KEY_LAST_AUTH_USER_KEY, profile.userKey)
      editor.putBoolean(KEY_EXPLICIT_LOGOUT, false)
    }
    if (profile.biometricEnabled) {
      editor.putString(KEY_BIOMETRIC_OPT_IN_USER_KEY, profile.userKey)
    } else if (p.getString(KEY_BIOMETRIC_OPT_IN_USER_KEY, null) == profile.userKey) {
      editor.remove(KEY_BIOMETRIC_OPT_IN_USER_KEY)
    }
    editor.apply()
  }

  fun setBiometricEnabledForUser(userId: String?, email: String, enabled: Boolean) {
    val existing = loadProfile(userId = userId, email = email)
    val updated = existing.copy(biometricEnabled = enabled)
    saveProfile(updated, markAsLastAuthenticated = false)
  }

  fun setTwoFactorEnabledForUser(userId: String?, email: String, enabled: Boolean) {
    val existing = loadProfile(userId = userId, email = email)
    val updated = existing.copy(twoFactorEnabled = enabled)
    saveProfile(updated, markAsLastAuthenticated = false)
  }

  private fun totpPrefsKey(userId: String?, email: String): String {
    val key = resolveUserKey(userId, email)
    return "totp_cfg_$key"
  }

  fun beginLocalTotpEnrollment(userId: String?, email: String): TotpSetupResult {
    val p = prefs ?: return TotpSetupResult(
      success = false,
      errorMessage = "Session storage not initialized."
    )
    val cfgKey = totpPrefsKey(userId, email)
    val existingJson = p.getString(cfgKey, null)?.let { runCatching { JSONObject(it) }.getOrNull() }
    val pendingSecret = existingJson?.optString("pendingSecret")?.takeIf { it.isNotBlank() }
      ?: TotpAuthenticator.generateSecret()
    val existingCodes = existingJson?.optJSONArray("backupCodes")?.let { arr ->
      (0 until arr.length()).mapNotNull { i -> arr.optString(i).takeIf { it.isNotBlank() } }
    }.orEmpty()
    val backupCodes = if (existingCodes.size >= 4) existingCodes else TotpAuthenticator.generateBackupCodes(8)

    val cleanEmail = email.trim().ifBlank { "user@numo.app" }
    val otpAuthUri = TotpAuthenticator.buildOtpAuthUri(email = cleanEmail, secret = pendingSecret)

    val cfg = (existingJson ?: JSONObject()).apply {
      put("pendingSecret", pendingSecret)
      put("backupCodes", JSONArray(backupCodes))
      put("mode", "local_totp")
    }
    p.edit().putString(cfgKey, cfg.toString()).commit()

    return TotpSetupResult(
      success = true,
      id = "local_totp_${resolveUserKey(userId, cleanEmail)}",
      secret = pendingSecret,
      uri = otpAuthUri,
      backupCodes = backupCodes,
      verified = false
    )
  }

  fun verifyLocalTotpEnrollment(userId: String?, email: String, code: String): TotpVerifyResult {
    val p = prefs ?: return TotpVerifyResult(
      success = false,
      errorMessage = "Session storage not initialized."
    )
    val cfgKey = totpPrefsKey(userId, email)
    val raw = p.getString(cfgKey, null)
    val json = raw?.let { runCatching { JSONObject(it) }.getOrNull() }
      ?: return TotpVerifyResult(
        success = false,
        errorMessage = "Setup session expired. Please reopen Two-Factor Auth and scan the QR code again."
      )

    val pendingSecret = json.optString("pendingSecret").takeIf { it.isNotBlank() }
      ?: json.optString("activeSecret").takeIf { it.isNotBlank() }
      ?: return TotpVerifyResult(
        success = false,
        errorMessage = "No authenticator key found. Tap Try Again to generate a new key."
      )

    if (!TotpAuthenticator.verifyTotpCode(pendingSecret, code)) {
      return TotpVerifyResult(
        success = false,
        errorMessage = "Incorrect 6-digit code. Make sure your device clock is accurate and enter the current code shown in your authenticator app."
      )
    }

    val arr = json.optJSONArray("backupCodes")
    val codes = if (arr != null && arr.length() > 0) {
      (0 until arr.length()).mapNotNull { i -> arr.optString(i).takeIf { it.isNotBlank() } }
    } else {
      TotpAuthenticator.generateBackupCodes(8)
    }

    json.put("activeSecret", pendingSecret)
    json.remove("pendingSecret")
    json.put("enabled", true)
    json.put("mode", "local_totp")
    json.put("backupCodes", JSONArray(codes))
    p.edit().putString(cfgKey, json.toString()).commit()
    setTwoFactorEnabledForUser(userId, email, true)

    return TotpVerifyResult(
      success = true,
      backupCodes = codes
    )
  }

  fun hasLocalTwoFactorEnabled(userId: String?, email: String): Boolean {
    val p = prefs ?: return false
    val raw = p.getString(totpPrefsKey(userId, email), null) ?: return false
    val json = runCatching { JSONObject(raw) }.getOrNull() ?: return false
    return json.optBoolean("enabled", false) && json.optString("activeSecret").isNotBlank()
  }

  fun verifyLocalSecondFactor(
    userId: String?,
    email: String,
    code: String,
    useBackupCode: Boolean
  ): Boolean {
    val p = prefs ?: return false
    val cfgKey = totpPrefsKey(userId, email)
    val raw = p.getString(cfgKey, null) ?: return false
    val json = runCatching { JSONObject(raw) }.getOrNull() ?: return false
    if (!json.optBoolean("enabled", false)) return false

    if (useBackupCode) {
      val cleanInput = code.trim().uppercase().replace(" ", "")
      val arr = json.optJSONArray("backupCodes") ?: return false
      val remaining = mutableListOf<String>()
      var matched = false
      for (i in 0 until arr.length()) {
        val candidate = arr.optString(i).trim()
        if (!matched && candidate.equals(cleanInput, ignoreCase = true)) {
          matched = true
        } else if (candidate.isNotBlank()) {
          remaining.add(candidate)
        }
      }
      if (matched) {
        json.put("backupCodes", JSONArray(remaining))
        p.edit().putString(cfgKey, json.toString()).commit()
      }
      return matched
    } else {
      val secret = json.optString("activeSecret")
      return secret.isNotBlank() && TotpAuthenticator.verifyTotpCode(secret, code)
    }
  }

  fun disableLocalTotp(userId: String?, email: String) {
    val p = prefs
    p?.edit()?.remove(totpPrefsKey(userId, email))?.commit()
    setTwoFactorEnabledForUser(userId, email, false)
  }

  fun getOptedInBiometricProfile(): NumoUserAccountData? {
    val p = prefs ?: return null
    val optInKey = p.getString(KEY_BIOMETRIC_OPT_IN_USER_KEY, null) ?: return null
    val raw = p.getString("$KEY_PROFILE_PREFIX$optInKey", null) ?: return null
    val profile = parseProfileJson(raw, optInKey, null, "", null, "Email")
    return if (profile.biometricEnabled && profile.email.isNotBlank()) profile else null
  }

  fun applyProfileToEngine(profile: NumoUserAccountData) {
    NumoFinancialEngine.currency = profile.currency
    if (profile.accounts.isNotEmpty() || profile.onboardingCompleted) {
      NumoFinancialEngine.availableCash = profile.availableCash
    }
  }

  fun hasCompletedUserHistory(): Boolean {
    val p = prefs ?: return false
    val lastKey = p.getString(KEY_LAST_AUTH_USER_KEY, null)
    if (!lastKey.isNullOrBlank()) {
      val raw = p.getString("$KEY_PROFILE_PREFIX$lastKey", null)
      if (!raw.isNullOrBlank()) {
        return try {
          JSONObject(raw).optBoolean("onboardingCompleted", false)
        } catch (_: Exception) {
          false
        }
      }
    }
    return false
  }

  fun wasExplicitlyLoggedOut(): Boolean = prefs?.getBoolean(KEY_EXPLICIT_LOGOUT, false) ?: false

  /**
   * Centralized startup router: waits for Clerk session restoration before
   * deciding whether to open Dashboard, Onboarding, Sign In, or Splash.
   */
  suspend fun restoreSessionOnStartup(): StartupRouteDecision {
    val hasToken = ClerkAuthRepository.hasStoredClientToken()
    if (!hasToken) {
      val firstTime = !wasExplicitlyLoggedOut() && !hasCompletedUserHistory()
      return StartupRouteDecision(
        authState = StartupAuthState.UNAUTHENTICATED,
        userProfile = null,
        isFirstTimeUser = firstTime
      )
    }

    val validation = ClerkAuthRepository.verifyCurrentClientSession()
    if (validation.success && !validation.email.isNullOrBlank()) {
      val loaded = loadProfile(
        userId = validation.userId,
        email = validation.email,
        fallbackFullName = validation.fullName
      )
      // Persist any newly discovered userId/fullName and record as active authenticated user
      saveProfile(loaded, markAsLastAuthenticated = true)
      applyProfileToEngine(loaded)

      return StartupRouteDecision(
        authState = if (loaded.onboardingCompleted) {
          StartupAuthState.AUTHENTICATED
        } else {
          StartupAuthState.ONBOARDING_REQUIRED
        },
        userProfile = loaded,
        isFirstTimeUser = false
      )
    }

    // If offline/network error OR non-revoked transient API response, preserve active completed session
    if (validation.isNetworkError || (!validation.isSessionRevoked && !wasExplicitlyLoggedOut())) {
      val lastKey = prefs?.getString(KEY_LAST_AUTH_USER_KEY, null)
      val raw = if (!lastKey.isNullOrBlank()) prefs?.getString("$KEY_PROFILE_PREFIX$lastKey", null) else null
      if (!raw.isNullOrBlank()) {
        val cachedProfile = parseProfileJson(raw, lastKey!!, null, "", null, "Email")
        if (cachedProfile.email.isNotBlank()) {
          applyProfileToEngine(cachedProfile)
          return StartupRouteDecision(
            authState = if (cachedProfile.onboardingCompleted) {
              StartupAuthState.AUTHENTICATED
            } else {
              StartupAuthState.ONBOARDING_REQUIRED
            },
            userProfile = cachedProfile,
            isFirstTimeUser = false
          )
        }
      }
    }

    // Session expired or invalid on Clerk: clear stale token, preserve saved user profiles, redirect to Sign In
    ClerkAuthRepository.clearClientToken()
    return StartupRouteDecision(
      authState = StartupAuthState.SESSION_EXPIRED,
      userProfile = null,
      isFirstTimeUser = false
    )
  }

  /**
   * Explicit user logout: clears Clerk session token & in-memory engine state,
   * while preserving all saved user profiles and financial data on disk.
   */
  suspend fun logout() {
    ClerkAuthRepository.signOut()
    prefs?.edit()?.putBoolean(KEY_EXPLICIT_LOGOUT, true)?.apply()
    NumoFinancialEngine.currency = "KES"
    NumoFinancialEngine.availableCash = 48750.0
  }
}
