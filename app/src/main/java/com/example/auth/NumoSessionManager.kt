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
  val accounts: List<Pair<String, String>> = emptyList()
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
        accounts = parsedAccounts
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
    editor.apply()
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

    // If offline/network error, keep last authenticated completed session rather than kicking user out
    if (validation.isNetworkError) {
      val lastKey = prefs?.getString(KEY_LAST_AUTH_USER_KEY, null)
      val raw = if (!lastKey.isNullOrBlank()) prefs?.getString("$KEY_PROFILE_PREFIX$lastKey", null) else null
      if (!raw.isNullOrBlank()) {
        val offlineProfile = parseProfileJson(raw, lastKey!!, null, "", null, "Email")
        if (offlineProfile.onboardingCompleted && offlineProfile.email.isNotBlank()) {
          applyProfileToEngine(offlineProfile)
          return StartupRouteDecision(
            authState = StartupAuthState.AUTHENTICATED,
            userProfile = offlineProfile,
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
