package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ai.configureAppCheck
import com.example.auth.BiometricAuthManager
import com.example.auth.BiometricCapability
import com.example.auth.ClerkAuthRepository
import com.example.auth.NumoSessionManager
import com.example.auth.NumoUserAccountData
import com.example.auth.StartupAuthState
import com.example.data.NumoFinancialEngine
import com.example.ui.AllSetScreen
import com.example.ui.AuthScreen
import com.example.ui.DashboardScreen
import com.example.ui.ForgotPasswordScreen
import com.example.ui.SignUpScreen
import com.example.ui.VerifyCodeScreen
import com.example.ui.WelcomeScreen
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.example.ui.onboarding.AccountConnectedScreen
import com.example.ui.onboarding.OnboardingAccountsScreen
import com.example.ui.onboarding.OnboardingCurrencyScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AppScreen {
  STARTUP_LOADING,
  WELCOME,
  SIGN_IN,
  SIGN_UP,
  FORGOT_PASSWORD,
  VERIFY_CODE,
  ONBOARDING_CURRENCY,
  ONBOARDING_ACCOUNTS,
  ACCOUNT_CONNECTED,
  ALL_SET,
  DASHBOARD
}

private data class SecondFactorChallenge(
  val signInId: String,
  val email: String,
  val provider: String,
  val supportedFactors: List<String>,
  val isLocalTotp: Boolean = false,
  val userId: String = "",
  val fullName: String? = null
)

class MainActivity : FragmentActivity() {
  private var pendingDeepLinkUri by mutableStateOf<Uri?>(null)
  private var isOAuthInProgressState by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before any UI, ViewModels, or Firebase operations
    configureAppCheck(this, intent)

    // Initialize persistent Clerk token & per-user session/onboarding storage
    ClerkAuthRepository.init(applicationContext)
    NumoSessionManager.init(applicationContext)

    enableEdgeToEdge(
      statusBarStyle = androidx.activity.SystemBarStyle.light(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      ),
      navigationBarStyle = androidx.activity.SystemBarStyle.light(
        android.graphics.Color.TRANSPARENT,
        android.graphics.Color.TRANSPARENT
      )
    )
    pendingDeepLinkUri = intent?.data

    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val coroutineScope = rememberCoroutineScope()

        var startupAuthState by remember { mutableStateOf(StartupAuthState.INITIALIZING) }
        var currentScreen by remember { mutableStateOf(AppScreen.STARTUP_LOADING) }
        var hasCompletedSplashAnimation by rememberSaveable { mutableStateOf(false) }

        var activeUserId by remember { mutableStateOf("") }
        var authenticatedUser by remember { mutableStateOf<String?>(null) }
        var authProvider by remember { mutableStateOf("Email") }
        var pendingSignUpId by remember { mutableStateOf("") }
        var pendingEmail by remember { mutableStateOf("") }
        var pendingFullName by remember { mutableStateOf("") }
        var resetEmailPreload by remember { mutableStateOf("") }
        var userCurrency by remember { mutableStateOf("KES") }
        var userAccounts by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
        var biometricEnabled by remember { mutableStateOf(false) }
        var twoFactorEnabled by remember { mutableStateOf(false) }

        var oauthErrorMessage by remember { mutableStateOf<String?>(null) }
        var pendingSecondFactor by remember { mutableStateOf<SecondFactorChallenge?>(null) }
        var biometricOptInProfile by remember { mutableStateOf<NumoUserAccountData?>(null) }
        var lastBackgroundedElapsedMs by rememberSaveable { mutableLongStateOf(0L) }
        var isBiometricLocked by rememberSaveable { mutableStateOf(false) }
        var isBiometricPromptOpen by remember { mutableStateOf(false) }
        var biometricLockError by remember { mutableStateOf<String?>(null) }

        val isAuthenticatedSessionScreen = currentScreen == AppScreen.DASHBOARD ||
          currentScreen == AppScreen.ONBOARDING_CURRENCY ||
          currentScreen == AppScreen.ONBOARDING_ACCOUNTS ||
          currentScreen == AppScreen.ACCOUNT_CONNECTED ||
          currentScreen == AppScreen.ALL_SET

        fun refreshBiometricEligibleProfile() {
          val candidate = NumoSessionManager.getOptedInBiometricProfile()
          val available = BiometricAuthManager.checkAvailability(context) == BiometricCapability.AVAILABLE
          biometricOptInProfile = if (candidate != null && available) candidate else null
        }

        fun launchUnlockBiometricPrompt() {
          if (isBiometricPromptOpen) return
          val emailForPrompt = authenticatedUser ?: biometricOptInProfile?.email ?: "your account"
          val capability = BiometricAuthManager.checkAvailability(context)
          if (capability != BiometricCapability.AVAILABLE) {
            biometricLockError = BiometricAuthManager.availabilityMessage(capability)
            return
          }
          isBiometricPromptOpen = true
          biometricLockError = null
          BiometricAuthManager.authenticate(
            activity = this@MainActivity,
            title = "Unlock Numo",
            subtitle = "Confirm biometrics for $emailForPrompt",
            negativeButtonText = "Cancel",
            onSuccess = {
              isBiometricPromptOpen = false
              isBiometricLocked = false
              lastBackgroundedElapsedMs = 0L
              biometricLockError = null
            },
            onError = { errMsg ->
              isBiometricPromptOpen = false
              biometricLockError = errMsg
            },
            onCancelled = {
              isBiometricPromptOpen = false
              biometricLockError = "Biometric verification cancelled. Tap Unlock to continue."
            }
          )
        }

        fun applyLoadedProfile(profile: NumoUserAccountData) {
          activeUserId = profile.userId
          authenticatedUser = profile.email
          if (profile.fullName.isNotBlank()) {
            pendingFullName = profile.fullName
          }
          authProvider = profile.authProvider.ifBlank { "Email" }
          userCurrency = profile.currency.ifBlank { "KES" }
          userAccounts = profile.accounts
          biometricEnabled = profile.biometricEnabled
          twoFactorEnabled = profile.twoFactorEnabled
          NumoSessionManager.applyProfileToEngine(profile)
        }

        fun persistCurrentProfile(onboardingCompleted: Boolean) {
          val email = authenticatedUser ?: pendingEmail
          if (email.isBlank()) return
          val key = NumoSessionManager.resolveUserKey(activeUserId, email)
          val existing = NumoSessionManager.loadProfile(activeUserId, email)
          val profile = NumoUserAccountData(
            userKey = key,
            userId = activeUserId,
            email = email,
            fullName = pendingFullName,
            authProvider = authProvider,
            onboardingCompleted = onboardingCompleted,
            currency = userCurrency,
            availableCash = NumoFinancialEngine.availableCash,
            accounts = userAccounts,
            biometricEnabled = biometricEnabled || existing.biometricEnabled,
            twoFactorEnabled = twoFactorEnabled || existing.twoFactorEnabled
          )
          NumoSessionManager.saveProfile(profile, markAsLastAuthenticated = true)
        }

        suspend fun routeAuthenticatedUser(
          email: String,
          provider: String,
          fallbackUserId: String? = null,
          fallbackFullName: String? = null,
          bypassLocal2faCheck: Boolean = false
        ) {
          val sessionCheck = ClerkAuthRepository.verifyCurrentClientSession()
          val resolvedUserId = sessionCheck.userId ?: fallbackUserId
          val resolvedEmail = sessionCheck.email?.takeIf { it.isNotBlank() } ?: email
          val resolvedName = sessionCheck.fullName ?: fallbackFullName ?: pendingFullName

          val localTwoFactorActive = NumoSessionManager.hasLocalTwoFactorEnabled(resolvedUserId, resolvedEmail)
          val clerkTwoFactorVerified = sessionCheck.success && sessionCheck.twoFactorEnabled

          if (!bypassLocal2faCheck && localTwoFactorActive && !clerkTwoFactorVerified) {
            pendingSecondFactor = SecondFactorChallenge(
              signInId = "local_2fa",
              email = resolvedEmail,
              provider = provider,
              supportedFactors = listOf("totp", "backup_code"),
              isLocalTotp = true,
              userId = resolvedUserId.orEmpty(),
              fullName = resolvedName
            )
            return
          }

          val existing = NumoSessionManager.loadProfile(
            userId = resolvedUserId,
            email = resolvedEmail,
            fallbackFullName = resolvedName,
            fallbackProvider = provider
          )
          val loaded = existing.copy(
            authProvider = provider,
            twoFactorEnabled = clerkTwoFactorVerified || localTwoFactorActive || existing.twoFactorEnabled
          )

          applyLoadedProfile(loaded)
          NumoSessionManager.saveProfile(loaded, markAsLastAuthenticated = true)
          isBiometricLocked = false
          lastBackgroundedElapsedMs = 0L

          if (loaded.onboardingCompleted) {
            startupAuthState = StartupAuthState.AUTHENTICATED
            currentScreen = AppScreen.DASHBOARD
          } else {
            startupAuthState = StartupAuthState.ONBOARDING_REQUIRED
            currentScreen = AppScreen.ONBOARDING_CURRENCY
          }
        }

        fun triggerBiometricSignIn() {
          val targetProfile = biometricOptInProfile ?: return
          isBiometricPromptOpen = true
          BiometricAuthManager.authenticate(
            activity = this@MainActivity,
            title = "Sign in to Numo",
            subtitle = "Confirm biometrics for ${targetProfile.email}",
            negativeButtonText = "Use Password",
            onSuccess = {
              isBiometricPromptOpen = false
              coroutineScope.launch {
                oauthErrorMessage = null
                hasCompletedSplashAnimation = true
                if (ClerkAuthRepository.hasStoredClientToken()) {
                  val check = ClerkAuthRepository.verifyCurrentClientSession()
                  if (check.success && !check.email.isNullOrBlank()) {
                    routeAuthenticatedUser(
                      email = check.email,
                      provider = targetProfile.authProvider,
                      fallbackUserId = check.userId,
                      fallbackFullName = check.fullName
                    )
                    return@launch
                  }
                }
                applyLoadedProfile(targetProfile)
                NumoSessionManager.saveProfile(targetProfile, markAsLastAuthenticated = true)
                isBiometricLocked = false
                lastBackgroundedElapsedMs = 0L
                if (targetProfile.onboardingCompleted) {
                  startupAuthState = StartupAuthState.AUTHENTICATED
                  currentScreen = AppScreen.DASHBOARD
                } else {
                  startupAuthState = StartupAuthState.ONBOARDING_REQUIRED
                  currentScreen = AppScreen.ONBOARDING_CURRENCY
                }
              }
            },
            onError = { errMsg ->
              isBiometricPromptOpen = false
              oauthErrorMessage = errMsg
              Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
            },
            onCancelled = {
              isBiometricPromptOpen = false
            }
          )
        }

        // Refresh strictly opt-in biometric availability whenever SIGN_IN is shown
        LaunchedEffect(currentScreen) {
          if (currentScreen == AppScreen.SIGN_IN) {
            refreshBiometricEligibleProfile()
          }
        }

        // Handle 15-second background biometric lock & OAuth browser return recovery
        DisposableEffect(lifecycleOwner) {
          val observer = LifecycleEventObserver { _, event ->
            when (event) {
              Lifecycle.Event.ON_STOP -> {
                if (
                  isAuthenticatedSessionScreen &&
                  biometricEnabled &&
                  !isOAuthInProgressState &&
                  !isBiometricPromptOpen &&
                  !isBiometricLocked
                ) {
                  lastBackgroundedElapsedMs = SystemClock.elapsedRealtime()
                }
              }
              Lifecycle.Event.ON_START -> {
                val shouldLock = BiometricAuthManager.shouldLockAfterBackground(
                  biometricEnabled = biometricEnabled,
                  isAuthenticatedSession = isAuthenticatedSessionScreen,
                  isOAuthInProgress = isOAuthInProgressState,
                  isBiometricPromptOpen = isBiometricPromptOpen,
                  lastBackgroundedElapsedMs = lastBackgroundedElapsedMs,
                  nowElapsedMs = SystemClock.elapsedRealtime()
                )
                if (shouldLock) {
                  isBiometricLocked = true
                  lastBackgroundedElapsedMs = 0L
                  launchUnlockBiometricPrompt()
                } else if (!isBiometricLocked) {
                  lastBackgroundedElapsedMs = 0L
                }
              }
              Lifecycle.Event.ON_RESUME -> {
                if (currentScreen == AppScreen.SIGN_IN) {
                  refreshBiometricEligibleProfile()
                }
                if (isOAuthInProgressState && pendingDeepLinkUri == null) {
                  coroutineScope.launch {
                    delay(750)
                    if (isOAuthInProgressState && pendingDeepLinkUri == null) {
                      val check = ClerkAuthRepository.verifyCurrentClientSession()
                      isOAuthInProgressState = false
                      if (check.success && !check.email.isNullOrBlank()) {
                        oauthErrorMessage = null
                        hasCompletedSplashAnimation = true
                        routeAuthenticatedUser(
                          email = check.email,
                          provider = authProvider,
                          fallbackUserId = check.userId,
                          fallbackFullName = check.fullName
                        )
                      } else if (check.requiresSecondFactor && !check.signInId.isNullOrBlank()) {
                        pendingSecondFactor = SecondFactorChallenge(
                          signInId = check.signInId,
                          email = check.email.orEmpty(),
                          provider = authProvider,
                          supportedFactors = check.supportedSecondFactors
                        )
                      }
                    }
                  }
                }
              }
              else -> Unit
            }
          }
          lifecycleOwner.lifecycle.addObserver(observer)
          onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
          }
        }

        // Centralized Startup Router: wait for session restoration before routing
        LaunchedEffect(Unit) {
          if (currentScreen != AppScreen.STARTUP_LOADING && authenticatedUser != null) {
            return@LaunchedEffect
          }
          val decision = NumoSessionManager.restoreSessionOnStartup()
          startupAuthState = decision.authState

          when (decision.authState) {
            StartupAuthState.AUTHENTICATED -> {
              hasCompletedSplashAnimation = true
              decision.userProfile?.let { applyLoadedProfile(it) }
              currentScreen = AppScreen.DASHBOARD
            }
            StartupAuthState.ONBOARDING_REQUIRED -> {
              hasCompletedSplashAnimation = true
              decision.userProfile?.let { applyLoadedProfile(it) }
              currentScreen = AppScreen.ONBOARDING_CURRENCY
            }
            StartupAuthState.SESSION_EXPIRED -> {
              hasCompletedSplashAnimation = true
              oauthErrorMessage = "Your session expired. Please sign in again."
              currentScreen = AppScreen.SIGN_IN
            }
            StartupAuthState.UNAUTHENTICATED, StartupAuthState.INITIALIZING, StartupAuthState.ONBOARDING_COMPLETED -> {
              if (decision.isFirstTimeUser) {
                currentScreen = AppScreen.WELCOME
              } else {
                hasCompletedSplashAnimation = true
                currentScreen = AppScreen.SIGN_IN
              }
            }
          }
        }

        // Handle incoming OAuth deep links (e.g. numo://oauth_callback)
        LaunchedEffect(pendingDeepLinkUri) {
          val uri = pendingDeepLinkUri
          if (uri != null && uri.scheme == "numo") {
            pendingDeepLinkUri = null
            isOAuthInProgressState = true

            val error = uri.getQueryParameter("error")
              ?: uri.getQueryParameter("error_description")
            val clerkStatus = uri.getQueryParameter("status")
              ?: uri.getQueryParameter("__clerk_status")
            val rotatingToken = uri.getQueryParameter("rotating_token")
              ?: uri.getQueryParameter("__clerk_db_jwt")
              ?: uri.getQueryParameter("token")
            val rotatingTokenNonce = uri.getQueryParameter("rotating_token_nonce")
            val callbackSessionId = uri.getQueryParameter("created_session_id")
              ?: uri.getQueryParameter("__clerk_created_session")

            if (!rotatingToken.isNullOrBlank()) {
              ClerkAuthRepository.setClientToken(rotatingToken)
            }

            if (!error.isNullOrEmpty() || clerkStatus == "abandoned" || clerkStatus == "failed") {
              isOAuthInProgressState = false
              val message = when {
                error?.contains("access_denied", ignoreCase = true) == true ||
                  error?.contains("cancel", ignoreCase = true) == true ||
                  clerkStatus == "abandoned" ->
                  "$authProvider authentication was cancelled."
                else -> error ?: "Sign-in with $authProvider failed. Please try again."
              }
              oauthErrorMessage = message
              Toast.makeText(context, message, Toast.LENGTH_LONG).show()
              return@LaunchedEffect
            }

            coroutineScope.launch {
              val sessionResult = ClerkAuthRepository.verifyCurrentClientSession(
                rotatingTokenHint = rotatingToken,
                sessionIdHint = callbackSessionId,
                rotatingTokenNonce = rotatingTokenNonce
              )
              isOAuthInProgressState = false

              if (sessionResult.success && !sessionResult.email.isNullOrBlank()) {
                oauthErrorMessage = null
                hasCompletedSplashAnimation = true
                routeAuthenticatedUser(
                  email = sessionResult.email,
                  provider = authProvider,
                  fallbackUserId = sessionResult.userId,
                  fallbackFullName = sessionResult.fullName
                )
                Toast.makeText(context, "Successfully authenticated with $authProvider!", Toast.LENGTH_SHORT).show()
              } else if (sessionResult.requiresSecondFactor && !sessionResult.signInId.isNullOrBlank()) {
                oauthErrorMessage = null
                pendingSecondFactor = SecondFactorChallenge(
                  signInId = sessionResult.signInId,
                  email = sessionResult.email.orEmpty(),
                  provider = authProvider,
                  supportedFactors = sessionResult.supportedSecondFactors
                )
              } else {
                val failMsg = sessionResult.errorMessage
                  ?: "Could not complete $authProvider sign-in. Please try again."
                oauthErrorMessage = failMsg
                Toast.makeText(context, failMsg, Toast.LENGTH_LONG).show()
              }
            }
          }
        }

        fun launchOAuth(provider: String, isSignUp: Boolean = false) {
          authProvider = if (provider.equals("apple", ignoreCase = true)) "Apple" else "Google"
          oauthErrorMessage = null
          isOAuthInProgressState = true
          coroutineScope.launch {
            val oauthUrl = ClerkAuthRepository.getOAuthRedirectUrl(provider, isSignUp)
              ?: ClerkAuthRepository.getOAuthRedirectUrl(provider, !isSignUp)

            if (oauthUrl.isNullOrBlank()) {
              val existing = ClerkAuthRepository.verifyCurrentClientSession()
              isOAuthInProgressState = false
              if (existing.success && !existing.email.isNullOrBlank()) {
                hasCompletedSplashAnimation = true
                routeAuthenticatedUser(
                  email = existing.email,
                  provider = authProvider,
                  fallbackUserId = existing.userId,
                  fallbackFullName = existing.fullName
                )
              } else if (existing.requiresSecondFactor && !existing.signInId.isNullOrBlank()) {
                pendingSecondFactor = SecondFactorChallenge(
                  signInId = existing.signInId,
                  email = existing.email.orEmpty(),
                  provider = authProvider,
                  supportedFactors = existing.supportedSecondFactors
                )
              } else {
                val msg = "Unable to start $authProvider sign-in. Please check your connection and try again."
                oauthErrorMessage = msg
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
              }
              return@launch
            }

            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl))
              context.startActivity(intent)
            } catch (_: Exception) {
              isOAuthInProgressState = false
              oauthErrorMessage = "Could not open browser for $authProvider sign-in."
              Toast.makeText(context, "Could not open browser for $authProvider sign-in.", Toast.LENGTH_SHORT).show()
            }
          }
        }

        AnimatedContent(
          targetState = currentScreen,
          transitionSpec = {
            if (initialState == AppScreen.WELCOME && targetState == AppScreen.SIGN_IN) {
              (slideInHorizontally(tween(420, easing = FastOutSlowInEasing)) { it } +
                fadeIn(tween(350))) togetherWith
                (slideOutHorizontally(tween(420, easing = FastOutSlowInEasing)) { -it / 2 } +
                  fadeOut(tween(300)))
            } else if (initialState == AppScreen.SIGN_IN && targetState == AppScreen.WELCOME) {
              (slideInHorizontally(tween(380, easing = FastOutSlowInEasing)) { -it / 2 } +
                fadeIn(tween(320))) togetherWith
                (slideOutHorizontally(tween(380, easing = FastOutSlowInEasing)) { it } +
                  fadeOut(tween(280)))
            } else {
              fadeIn(animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) togetherWith
                fadeOut(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing))
            }
          },
          label = "AppScreenTransition"
        ) { screen ->
          when (screen) {
            AppScreen.STARTUP_LOADING -> {
              StartupLoadingScreen()
            }

            AppScreen.WELCOME -> {
              WelcomeScreen(
                skipAnimation = false,
                onSplashAnimationFinished = {
                  hasCompletedSplashAnimation = true
                },
                onGetStarted = {
                  hasCompletedSplashAnimation = true
                  currentScreen = AppScreen.SIGN_IN
                }
              )
            }

            AppScreen.SIGN_IN -> {
              BackHandler {
                hasCompletedSplashAnimation = false
                currentScreen = AppScreen.WELCOME
              }
              AuthScreen(
                externalErrorMessage = oauthErrorMessage,
                isOAuthLoading = isOAuthInProgressState,
                biometricProfileEmail = biometricOptInProfile?.email,
                onClearExternalError = { oauthErrorMessage = null },
                onSignInSuccess = { email ->
                  coroutineScope.launch {
                    routeAuthenticatedUser(
                      email = email,
                      provider = "Email"
                    )
                  }
                },
                onSecondFactorRequired = { signInId, email, factors ->
                  pendingSecondFactor = SecondFactorChallenge(
                    signInId = signInId,
                    email = email,
                    provider = "Email",
                    supportedFactors = factors
                  )
                },
                onBiometricSignInClick = {
                  triggerBiometricSignIn()
                },
                onSignUpClick = {
                  oauthErrorMessage = null
                  currentScreen = AppScreen.SIGN_UP
                },
                onGoogleClick = {
                  launchOAuth("google", isSignUp = false)
                },
                onAppleClick = {
                  launchOAuth("apple", isSignUp = false)
                },
                onForgotPasswordClick = { typedEmail ->
                  resetEmailPreload = typedEmail
                  currentScreen = AppScreen.FORGOT_PASSWORD
                },
                onBack = {
                  hasCompletedSplashAnimation = false
                  currentScreen = AppScreen.WELCOME
                }
              )
            }

            AppScreen.FORGOT_PASSWORD -> {
              BackHandler {
                currentScreen = AppScreen.SIGN_IN
              }
              ForgotPasswordScreen(
                initialEmail = resetEmailPreload,
                onBack = {
                  currentScreen = AppScreen.SIGN_IN
                },
                onNavigateToSignUp = {
                  currentScreen = AppScreen.SIGN_UP
                },
                onResetSuccess = { email ->
                  Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                  coroutineScope.launch {
                    routeAuthenticatedUser(
                      email = email,
                      provider = "Email"
                    )
                  }
                }
              )
            }

            AppScreen.SIGN_UP -> {
              BackHandler {
                currentScreen = AppScreen.SIGN_IN
              }
              SignUpScreen(
                externalErrorMessage = oauthErrorMessage,
                isOAuthLoading = isOAuthInProgressState,
                onClearExternalError = { oauthErrorMessage = null },
                onNavigateToSignIn = {
                  oauthErrorMessage = null
                  currentScreen = AppScreen.SIGN_IN
                },
                onSignUpSuccess = { signUpId, email, fullName ->
                  pendingSignUpId = signUpId
                  pendingEmail = email
                  pendingFullName = fullName
                  currentScreen = AppScreen.VERIFY_CODE
                },
                onGoogleSignUp = {
                  launchOAuth("google", isSignUp = true)
                },
                onAppleSignUp = {
                  launchOAuth("apple", isSignUp = true)
                }
              )
            }

            AppScreen.VERIFY_CODE -> {
              BackHandler {
                currentScreen = AppScreen.SIGN_UP
              }
              VerifyCodeScreen(
                signUpId = pendingSignUpId,
                userEmail = pendingEmail,
                onVerificationSuccess = { _ ->
                  authProvider = "Email"
                  authenticatedUser = pendingEmail
                  coroutineScope.launch {
                    routeAuthenticatedUser(
                      email = pendingEmail,
                      provider = "Email",
                      fallbackFullName = pendingFullName
                    )
                  }
                },
                onBack = {
                  currentScreen = AppScreen.SIGN_UP
                }
              )
            }

            AppScreen.ONBOARDING_CURRENCY -> {
              BackHandler {
                currentScreen = AppScreen.SIGN_IN
              }
              OnboardingCurrencyScreen(
                initialCurrency = userCurrency,
                onBack = {
                  currentScreen = AppScreen.SIGN_IN
                },
                onContinue = { selectedCurrency ->
                  userCurrency = selectedCurrency
                  NumoFinancialEngine.currency = selectedCurrency
                  persistCurrentProfile(onboardingCompleted = false)
                  currentScreen = AppScreen.ONBOARDING_ACCOUNTS
                }
              )
            }

            AppScreen.ONBOARDING_ACCOUNTS -> {
              BackHandler {
                currentScreen = AppScreen.ONBOARDING_CURRENCY
              }
              OnboardingAccountsScreen(
                currency = userCurrency,
                onBack = {
                  currentScreen = AppScreen.ONBOARDING_CURRENCY
                },
                onComplete = { accounts ->
                  userAccounts = accounts
                  persistCurrentProfile(onboardingCompleted = false)
                  currentScreen = AppScreen.ACCOUNT_CONNECTED
                }
              )
            }

            AppScreen.ACCOUNT_CONNECTED -> {
              BackHandler {
                currentScreen = AppScreen.ONBOARDING_ACCOUNTS
              }
              AccountConnectedScreen(
                connectedAccounts = userAccounts,
                currency = userCurrency,
                onBack = {
                  currentScreen = AppScreen.ONBOARDING_ACCOUNTS
                },
                onContinue = {
                  persistCurrentProfile(onboardingCompleted = true)
                  currentScreen = AppScreen.ALL_SET
                }
              )
            }

            AppScreen.ALL_SET -> {
              BackHandler {
                currentScreen = AppScreen.ACCOUNT_CONNECTED
              }
              AllSetScreen(
                onGoToHome = {
                  persistCurrentProfile(onboardingCompleted = true)
                  startupAuthState = StartupAuthState.AUTHENTICATED
                  currentScreen = AppScreen.DASHBOARD
                },
                onAddMoreAccounts = {
                  currentScreen = AppScreen.ONBOARDING_ACCOUNTS
                }
              )
            }

            AppScreen.DASHBOARD -> {
              BackHandler {
                // Keep authenticated session intact when pressing Back on Dashboard
                moveTaskToBack(true)
              }
              DashboardScreen(
                userId = activeUserId,
                userEmail = if (pendingFullName.isNotBlank()) pendingFullName else (authenticatedUser ?: "Ayesha Khan"),
                rawEmail = authenticatedUser.orEmpty(),
                provider = authProvider,
                userCurrency = userCurrency,
                customAccounts = userAccounts,
                biometricEnabled = biometricEnabled,
                twoFactorEnabled = twoFactorEnabled,
                onBiometricPreferenceChanged = { enabled ->
                  biometricEnabled = enabled
                  refreshBiometricEligibleProfile()
                },
                onTwoFactorPreferenceChanged = { enabled ->
                  twoFactorEnabled = enabled
                },
                onSignOut = {
                  coroutineScope.launch {
                    NumoSessionManager.logout()
                    startupAuthState = StartupAuthState.UNAUTHENTICATED
                    activeUserId = ""
                    authenticatedUser = null
                    pendingFullName = ""
                    userAccounts = emptyList()
                    userCurrency = "KES"
                    biometricEnabled = false
                    twoFactorEnabled = false
                    oauthErrorMessage = null
                    hasCompletedSplashAnimation = true
                    currentScreen = AppScreen.SIGN_IN
                  }
                }
              )
            }
          }
        }

        // Second-Factor Verification Modal for TOTP / Backup Code sign-ins
        pendingSecondFactor?.let { challenge ->
          TwoFactorChallengeDialog(
            challenge = challenge,
            onDismiss = { pendingSecondFactor = null },
            onVerified = { verifiedSession ->
              val bypassLocal = challenge.isLocalTotp
              pendingSecondFactor = null
              oauthErrorMessage = null
              hasCompletedSplashAnimation = true
              coroutineScope.launch {
                routeAuthenticatedUser(
                  email = verifiedSession.email?.takeIf { it.isNotBlank() } ?: challenge.email,
                  provider = challenge.provider,
                  fallbackUserId = verifiedSession.userId,
                  fallbackFullName = verifiedSession.fullName,
                  bypassLocal2faCheck = bypassLocal
                )
              }
            }
          )
        }

        // Opaque Biometric Lock Overlay when returning after > 15s away with Biometric Login enabled
        if (isBiometricLocked && isAuthenticatedSessionScreen) {
          BackHandler(enabled = true) {
            moveTaskToBack(true)
          }
          BiometricLockOverlay(
            userEmail = authenticatedUser.orEmpty(),
            errorText = biometricLockError,
            onUnlockClick = { launchUnlockBiometricPrompt() },
            onSignOutClick = {
              coroutineScope.launch {
                NumoSessionManager.logout()
                isBiometricLocked = false
                isBiometricPromptOpen = false
                lastBackgroundedElapsedMs = 0L
                biometricLockError = null
                startupAuthState = StartupAuthState.UNAUTHENTICATED
                activeUserId = ""
                authenticatedUser = null
                pendingFullName = ""
                userAccounts = emptyList()
                userCurrency = "KES"
                biometricEnabled = false
                twoFactorEnabled = false
                oauthErrorMessage = null
                hasCompletedSplashAnimation = true
                currentScreen = AppScreen.SIGN_IN
              }
            }
          )
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    if (intent.hasExtra("FIREBASE_APPCHECK_DEBUG_TOKEN")) {
      configureAppCheck(this, intent)
    }
    pendingDeepLinkUri = intent.data
  }
}

@Composable
private fun TwoFactorChallengeDialog(
  challenge: SecondFactorChallenge,
  onDismiss: () -> Unit,
  onVerified: (com.example.auth.SessionValidationResult) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var useBackupCode by remember { mutableStateOf(false) }
  var code by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }
  var isVerifying by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = { if (!isVerifying) onDismiss() },
    containerColor = Color(0xFFFFF6EC),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Outlined.Lock,
          contentDescription = null,
          tint = NumoDS.HeroTealMid,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Two-Factor Verification",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = if (useBackupCode) {
            "Enter one of your one-time backup codes for ${challenge.email.ifBlank { "your account" }}."
          } else {
            "Enter the 6-digit code from your authenticator app for ${challenge.email.ifBlank { "your account" }}."
          },
          fontSize = 13.sp,
          color = NumoDS.TextSecondary,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF1E0D2))
            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          if (code.isEmpty()) {
            Text(
              text = if (useBackupCode) "Backup code" else "000000",
              fontSize = 15.sp,
              fontFamily = FontFamily.Monospace,
              color = NumoDS.TextMuted
            )
          }
          BasicTextField(
            value = code,
            onValueChange = { input ->
              code = if (useBackupCode) {
                input.trim().take(16)
              } else {
                input.filter { it.isDigit() }.take(6)
              }
              errorText = null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
              keyboardType = if (useBackupCode) KeyboardType.Text else KeyboardType.Number
            ),
            textStyle = TextStyle(
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = if (useBackupCode) 1.sp else 4.sp,
              color = NumoDS.TextPrimary
            ),
            cursorBrush = SolidColor(NumoDS.HeroTealMid),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_signin_2fa_code")
          )
        }

        if (!errorText.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorText!!,
            fontSize = 12.sp,
            color = NumoDS.ExpenseRed,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Start
        ) {
          Text(
            text = if (useBackupCode) "Use authenticator app code instead" else "Use a backup code instead",
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.HeroTealMid,
            modifier = Modifier
              .clickable {
                useBackupCode = !useBackupCode
                code = ""
                errorText = null
              }
              .padding(vertical = 4.dp)
              .testTag("btn_toggle_2fa_method")
          )
        }
      }
    },
    confirmButton = {
      NumoPrimaryButton(
        text = if (isVerifying) "Verifying..." else "Verify",
        onClick = {
          if (isVerifying) return@NumoPrimaryButton
          val trimmed = code.trim()
          if (trimmed.isEmpty()) {
            errorText = "Please enter your verification code."
            return@NumoPrimaryButton
          }
          isVerifying = true
          errorText = null
          coroutineScope.launch {
            if (challenge.isLocalTotp) {
              val valid = NumoSessionManager.verifyLocalSecondFactor(
                userId = challenge.userId,
                email = challenge.email,
                code = trimmed,
                useBackupCode = useBackupCode
              )
              isVerifying = false
              if (valid) {
                onVerified(
                  com.example.auth.SessionValidationResult(
                    success = true,
                    userId = challenge.userId,
                    email = challenge.email,
                    fullName = challenge.fullName,
                    twoFactorEnabled = true
                  )
                )
              } else {
                errorText = if (useBackupCode) {
                  "Invalid or already used backup code."
                } else {
                  "Invalid 6-digit authenticator code."
                }
              }
            } else {
              val strategy = if (useBackupCode) "backup_code" else "totp"
              val res = ClerkAuthRepository.attemptSignInSecondFactor(
                signInId = challenge.signInId,
                strategy = strategy,
                code = trimmed
              )
              isVerifying = false
              if (res.success) {
                onVerified(res)
              } else {
                errorText = res.errorMessage ?: "Invalid verification code."
              }
            }
          }
        },
        modifier = Modifier
          .width(120.dp)
          .testTag("btn_verify_signin_2fa"),
        height = 42.dp
      )
    },
    dismissButton = {
      TextButton(
        enabled = !isVerifying,
        onClick = onDismiss
      ) {
        Text("Cancel", color = NumoDS.TextSecondary)
      }
    }
  )
}

/**
 * Full-screen opaque biometric lock overlay shown whenever the user returns after
 * spending > 15 seconds outside the app with Biometric Login enabled.
 */
@Composable
private fun BiometricLockOverlay(
  userEmail: String,
  errorText: String?,
  onUnlockClick: () -> Unit,
  onSignOutClick: () -> Unit
) {
  NumoScreenBackground(modifier = Modifier.testTag("biometric_lock_overlay")) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(74.dp)
          .clip(CircleShape)
          .background(NumoDS.MintGlassLight)
          .border(1.5.dp, NumoDS.Turquoise.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Outlined.Fingerprint,
          contentDescription = "Biometric Lock",
          tint = NumoDS.HeroTealMid,
          modifier = Modifier.size(38.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Numo is Locked",
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = if (userEmail.isNotBlank()) {
          "Welcome back, $userEmail. Verify your biometrics to unlock your accounts."
        } else {
          "Verify your biometrics to unlock your Numo workspace."
        },
        fontSize = 14.sp,
        color = NumoDS.TextSecondary,
        lineHeight = 20.sp
      )

      if (!errorText.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = errorText,
          fontSize = 12.5.sp,
          color = NumoDS.ExpenseRed,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(26.dp))

      NumoPrimaryButton(
        text = "Unlock with Biometrics",
        onClick = onUnlockClick,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_unlock_biometrics"),
        height = 52.dp
      )

      Spacer(modifier = Modifier.height(12.dp))

      TextButton(
        onClick = onSignOutClick,
        modifier = Modifier.testTag("btn_lock_sign_out")
      ) {
        Text(
          text = "Sign Out Instead",
          fontSize = 13.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextSecondary
        )
      }
    }
  }
}

/**
 * Subtle Numo-branded loading state shown while checking persisted Clerk session
 * on startup so authenticated users never see a flash of onboarding screens.
 */
@Composable
private fun StartupLoadingScreen() {
  // Plain warm cream surface while checking persisted session so no 'N' emblem
  // flashes before the piggy-bank splash animation begins.
  NumoScreenBackground(modifier = Modifier.testTag("startup_loading_screen")) {
    Box(modifier = Modifier.fillMaxSize())
  }
}
