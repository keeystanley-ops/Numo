package com.example.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricCapability {
  AVAILABLE,
  NOT_ENROLLED,
  UNAVAILABLE
}

object BiometricAuthManager {
  const val BIOMETRIC_REAUTH_TIMEOUT_MS = 15_000L

  private const val ALLOWED_AUTHENTICATORS =
    BiometricManager.Authenticators.BIOMETRIC_STRONG or
      BiometricManager.Authenticators.BIOMETRIC_WEAK

  fun shouldLockAfterBackground(
    biometricEnabled: Boolean,
    isAuthenticatedSession: Boolean,
    isOAuthInProgress: Boolean,
    isBiometricPromptOpen: Boolean,
    lastBackgroundedElapsedMs: Long,
    nowElapsedMs: Long
  ): Boolean {
    if (!biometricEnabled || !isAuthenticatedSession) return false
    if (isOAuthInProgress || isBiometricPromptOpen) return false
    if (lastBackgroundedElapsedMs <= 0L) return false
    return (nowElapsedMs - lastBackgroundedElapsedMs) > BIOMETRIC_REAUTH_TIMEOUT_MS
  }

  fun checkAvailability(context: Context): BiometricCapability {
    val manager = BiometricManager.from(context)
    return when (manager.canAuthenticate(ALLOWED_AUTHENTICATORS)) {
      BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapability.AVAILABLE
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapability.NOT_ENROLLED
      else -> BiometricCapability.UNAVAILABLE
    }
  }

  fun availabilityMessage(capability: BiometricCapability): String = when (capability) {
    BiometricCapability.AVAILABLE -> "Device biometrics ready."
    BiometricCapability.NOT_ENROLLED ->
      "No fingerprint or face unlock is enrolled on this device. Add one in Android Settings first."
    BiometricCapability.UNAVAILABLE ->
      "Biometric authentication is not available on this device."
  }

  fun authenticate(
    activity: FragmentActivity,
    title: String,
    subtitle: String,
    negativeButtonText: String = "Cancel",
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    onCancelled: () -> Unit = {}
  ) {
    val capability = checkAvailability(activity)
    if (capability != BiometricCapability.AVAILABLE) {
      onError(availabilityMessage(capability))
      return
    }

    val executor = ContextCompat.getMainExecutor(activity)
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onSuccess()
      }

      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
          errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
          errorCode == BiometricPrompt.ERROR_CANCELED
        ) {
          onCancelled()
        } else {
          onError(errString.toString().ifBlank { "Biometric authentication failed." })
        }
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        // System dialog remains open and notifies user to retry
      }
    }

    val prompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
      .setTitle(title)
      .setSubtitle(subtitle)
      .setNegativeButtonText(negativeButtonText)
      .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
      .build()

    prompt.authenticate(promptInfo)
  }
}
