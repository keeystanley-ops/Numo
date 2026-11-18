package com.example.ai

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

// App Check contract keys defined by Firebase SDK and the runtime harness.
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val HAS_HARNESS_TOKEN_KEY = "com.example.ai.HAS_HARNESS_DEBUG_TOKEN"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
  try {
    // 1. Ensure FirebaseApp is initialized before configuring App Check
    if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(context)
    }

    val persistenceKey = FirebaseApp.getInstance().persistenceKey
    val debugPrefs = context.getSharedPreferences(
      "$DEBUG_STORE_PREFIX$persistenceKey",
      Context.MODE_PRIVATE
    )
    val intentDebugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

    if (BuildConfig.DEBUG && !intentDebugToken.isNullOrEmpty()) {
      // Synchronous commit() is required before DebugAppCheckProviderFactory reads DEBUG_SECRET.
      debugPrefs.edit()
        .putString(DEBUG_SECRET_KEY, intentDebugToken)
        .putBoolean(HAS_HARNESS_TOKEN_KEY, true)
        .commit()
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
      Log.d(TAG, "DebugAppCheckProviderFactory installed with harness debug token.")
      return
    }

    val hasRegisteredHarnessToken = BuildConfig.DEBUG &&
      debugPrefs.getBoolean(HAS_HARNESS_TOKEN_KEY, false) &&
      !debugPrefs.getString(DEBUG_SECRET_KEY, null).isNullOrEmpty()

    if (hasRegisteredHarnessToken) {
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
      Log.d(TAG, "DebugAppCheckProviderFactory installed with persisted harness debug token.")
      return
    }

    // Physical device launch (release OR standalone debug APK without harness token extra):
    // Use reCAPTCHA Enterprise site key provisioned in firebase_applet_config.xml so
    // Firebase App Check does not fail with an unregistered local random debug UUID.
    val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
    val siteKey = if (resId != 0) context.getString(resId).trim() else ""
    if (siteKey.isNotBlank()) {
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
      )
      Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
    } else if (BuildConfig.DEBUG) {
      FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
        DebugAppCheckProviderFactory.getInstance()
      )
      Log.w(TAG, "recaptcha_site_key unavailable; fell back to DebugAppCheckProviderFactory.")
    } else {
      Log.e(TAG, "recaptcha_site_key string resource not found or blank.")
    }
  } catch (e: Exception) {
    Log.e(TAG, "Failed to initialize Firebase App Check", e)
  }
}
