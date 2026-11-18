package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auth.NumoSessionManager
import com.example.auth.NumoUserAccountData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Numo", appName)
  }

  @Test
  fun `biometric login is strictly opt-in and persists per user`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    NumoSessionManager.init(context)

    val profile = NumoUserAccountData(
      userKey = "usr_bio_test",
      userId = "usr_bio_test",
      email = "tester@numo.app",
      fullName = "Test User",
      authProvider = "Google",
      onboardingCompleted = true
    )
    NumoSessionManager.saveProfile(profile, markAsLastAuthenticated = true)

    assertFalse(profile.biometricEnabled)
    assertNull(NumoSessionManager.getOptedInBiometricProfile())

    NumoSessionManager.setBiometricEnabledForUser("usr_bio_test", "tester@numo.app", true)
    val optedIn = NumoSessionManager.getOptedInBiometricProfile()
    assertNotNull(optedIn)
    assertEquals("tester@numo.app", optedIn?.email)
    assertTrue(optedIn?.biometricEnabled == true)

    NumoSessionManager.setBiometricEnabledForUser("usr_bio_test", "tester@numo.app", false)
    assertNull(NumoSessionManager.getOptedInBiometricProfile())
  }

  @Test
  fun `two factor preference persists per user`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    NumoSessionManager.init(context)

    val profile = NumoUserAccountData(
      userKey = "usr_2fa_test",
      userId = "usr_2fa_test",
      email = "2fa@numo.app",
      onboardingCompleted = true
    )
    NumoSessionManager.saveProfile(profile, markAsLastAuthenticated = false)

    NumoSessionManager.setTwoFactorEnabledForUser("usr_2fa_test", "2fa@numo.app", true)
    val reloaded = NumoSessionManager.loadProfile("usr_2fa_test", "2fa@numo.app")
    assertTrue(reloaded.twoFactorEnabled)
  }

  @Test
  fun `gemini model preference defaults to gemini-3_8-flash and persists updates`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.voice.HeyNumoSettings.init(context)

    assertEquals("gemini-3.8-flash", com.example.voice.HeyNumoSettings.selectedModel)

    com.example.voice.HeyNumoSettings.updateSelectedModel("gemini-3.5-flash-lite")
    assertEquals("gemini-3.5-flash-lite", com.example.voice.HeyNumoSettings.selectedModel)

    com.example.voice.HeyNumoSettings.updateSelectedModel("invalid-model")
    assertEquals("gemini-3.8-flash", com.example.voice.HeyNumoSettings.selectedModel)
  }

  @Test
  fun `hey numo chat viewmodel confirms and cancels pending actions safely`() {
    val vm = com.example.voice.HeyNumoChatViewModel()
    val initialCash = com.example.data.NumoFinancialEngine.availableCash

    val cancelAction = com.example.voice.PendingFinancialAction(
      kind = com.example.voice.PendingActionKind.TRANSACTION,
      amount = 250.0,
      description = "Coffee",
      account = "M-PESA",
      category = "Food & Dining",
      isIncome = false
    )
    vm.cancelAction(cancelAction) {}
    assertEquals(initialCash, com.example.data.NumoFinancialEngine.availableCash, 0.01)
    assertTrue(vm.messages.value.last().text.contains("cancelled", ignoreCase = true))

    val confirmAction = com.example.voice.PendingFinancialAction(
      kind = com.example.voice.PendingActionKind.TRANSACTION,
      amount = 100.0,
      description = "Snack",
      account = "M-PESA",
      category = "Food & Dining",
      isIncome = false
    )
    vm.confirmAction(confirmAction) {}
    assertEquals(initialCash - 100.0, com.example.data.NumoFinancialEngine.availableCash, 0.01)
    assertTrue(vm.messages.value.last().text.contains("Done!", ignoreCase = true))

    vm.clearConversation()
    assertTrue(vm.messages.value.isEmpty())
    assertEquals(com.example.voice.ChatUiState.Idle, vm.uiState.value)
  }

  @Test
  fun `account ready animation raw resource and strings exist`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val resId = R.raw.account_ready_animation
    assertTrue(resId != 0)
    context.resources.openRawResource(resId).use { stream ->
      assertTrue(stream.read() != -1)
    }
    assertEquals("Go to Dashboard", context.getString(R.string.all_set_go_to_dashboard))
    assertEquals("Explore Features", context.getString(R.string.all_set_explore_features))
  }

  @Test
  fun `rfc 6238 totp authenticator and local 2fa enrollment work end to end`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    NumoSessionManager.init(context)

    val secret = com.example.auth.TotpAuthenticator.generateSecret()
    assertTrue(secret.length >= 16)
    val uri = com.example.auth.TotpAuthenticator.buildOtpAuthUri("2fa-e2e@numo.app", secret)
    assertTrue(uri.startsWith("otpauth://totp/"))
    assertTrue(uri.contains("secret=$secret"))

    val fixedTime = 1_760_000_000_000L
    val validCode = com.example.auth.TotpAuthenticator.generateTotpCode(secret, fixedTime)
    assertEquals(6, validCode.length)
    assertTrue(com.example.auth.TotpAuthenticator.verifyTotpCode(secret, validCode, fixedTime))
    assertFalse(com.example.auth.TotpAuthenticator.verifyTotpCode(secret, "000000", fixedTime))

    val userId = "usr_totp_e2e"
    val email = "2fa-e2e@numo.app"
    val setup = NumoSessionManager.beginLocalTotpEnrollment(userId, email)
    assertTrue(setup.success)
    assertTrue(setup.secret.isNotBlank())
    assertTrue(setup.backupCodes.size >= 8)

    val liveCode = com.example.auth.TotpAuthenticator.generateTotpCode(setup.secret)
    val verified = NumoSessionManager.verifyLocalTotpEnrollment(userId, email, liveCode)
    assertTrue(verified.success)
    assertTrue(NumoSessionManager.hasLocalTwoFactorEnabled(userId, email))

    // Single-use backup code verification succeeds once, then fails reuse
    val backup = verified.backupCodes.first()
    assertTrue(NumoSessionManager.verifyLocalSecondFactor(userId, email, backup, useBackupCode = true))
    assertFalse(NumoSessionManager.verifyLocalSecondFactor(userId, email, backup, useBackupCode = true))

    NumoSessionManager.disableLocalTotp(userId, email)
    assertFalse(NumoSessionManager.hasLocalTwoFactorEnabled(userId, email))
  }

  @Test
  fun `biometric lock triggers only when returning after more than 15 seconds`() {
    val t0 = 100_000L
    assertFalse(
      com.example.auth.BiometricAuthManager.shouldLockAfterBackground(
        biometricEnabled = true,
        isAuthenticatedSession = true,
        isOAuthInProgress = false,
        isBiometricPromptOpen = false,
        lastBackgroundedElapsedMs = t0,
        nowElapsedMs = t0 + 14_999L
      )
    )
    assertFalse(
      com.example.auth.BiometricAuthManager.shouldLockAfterBackground(
        biometricEnabled = true,
        isAuthenticatedSession = true,
        isOAuthInProgress = false,
        isBiometricPromptOpen = false,
        lastBackgroundedElapsedMs = t0,
        nowElapsedMs = t0 + 15_000L
      )
    )
    assertTrue(
      com.example.auth.BiometricAuthManager.shouldLockAfterBackground(
        biometricEnabled = true,
        isAuthenticatedSession = true,
        isOAuthInProgress = false,
        isBiometricPromptOpen = false,
        lastBackgroundedElapsedMs = t0,
        nowElapsedMs = t0 + 15_001L
      )
    )
    // Suppressed during OAuth or active prompt or when opt-in is OFF
    assertFalse(
      com.example.auth.BiometricAuthManager.shouldLockAfterBackground(
        biometricEnabled = true,
        isAuthenticatedSession = true,
        isOAuthInProgress = true,
        isBiometricPromptOpen = false,
        lastBackgroundedElapsedMs = t0,
        nowElapsedMs = t0 + 30_000L
      )
    )
    assertFalse(
      com.example.auth.BiometricAuthManager.shouldLockAfterBackground(
        biometricEnabled = false,
        isAuthenticatedSession = true,
        isOAuthInProgress = false,
        isBiometricPromptOpen = false,
        lastBackgroundedElapsedMs = t0,
        nowElapsedMs = t0 + 30_000L
      )
    )
  }

  @Test
  fun `hey numo default voice is warm male Charon`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.example.voice.HeyNumoSettings.init(context)
    assertEquals("Charon", com.example.voice.HeyNumoSettings.DEFAULT_VOICE_NAME)
    assertEquals("Charon", com.example.voice.HeyNumoSettings.voiceOptions.first().id)
    assertEquals("Charon", com.example.voice.HeyNumoSettings.selectedVoice)
  }
}
