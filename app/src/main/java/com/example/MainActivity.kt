package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
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
import com.example.ui.components.Numo3DLogoEmblem
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoScreenBackground
import com.example.ui.onboarding.AccountConnectedScreen
import com.example.ui.onboarding.OnboardingAccountsScreen
import com.example.ui.onboarding.OnboardingCurrencyScreen
import com.example.ui.theme.MyApplicationTheme
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

class MainActivity : ComponentActivity() {
  private var pendingDeepLinkUri by mutableStateOf<Uri?>(null)
  private var isOAuthInProgressState by mutableStateOf(false)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

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

        var oauthErrorMessage by remember { mutableStateOf<String?>(null) }

        fun applyLoadedProfile(profile: NumoUserAccountData) {
          activeUserId = profile.userId
          authenticatedUser = profile.email
          if (profile.fullName.isNotBlank()) {
            pendingFullName = profile.fullName
          }
          authProvider = profile.authProvider.ifBlank { "Email" }
          userCurrency = profile.currency.ifBlank { "KES" }
          userAccounts = profile.accounts
          NumoSessionManager.applyProfileToEngine(profile)
        }

        fun persistCurrentProfile(onboardingCompleted: Boolean) {
          val email = authenticatedUser ?: pendingEmail
          if (email.isBlank()) return
          val key = NumoSessionManager.resolveUserKey(activeUserId, email)
          val profile = NumoUserAccountData(
            userKey = key,
            userId = activeUserId,
            email = email,
            fullName = pendingFullName,
            authProvider = authProvider,
            onboardingCompleted = onboardingCompleted,
            currency = userCurrency,
            availableCash = NumoFinancialEngine.availableCash,
            accounts = userAccounts
          )
          NumoSessionManager.saveProfile(profile, markAsLastAuthenticated = true)
        }

        suspend fun routeAuthenticatedUser(
          email: String,
          provider: String,
          fallbackUserId: String? = null,
          fallbackFullName: String? = null
        ) {
          val sessionCheck = ClerkAuthRepository.verifyCurrentClientSession()
          val resolvedUserId = sessionCheck.userId ?: fallbackUserId
          val resolvedEmail = sessionCheck.email?.takeIf { it.isNotBlank() } ?: email
          val resolvedName = sessionCheck.fullName ?: fallbackFullName ?: pendingFullName

          val loaded = NumoSessionManager.loadProfile(
            userId = resolvedUserId,
            email = resolvedEmail,
            fallbackFullName = resolvedName,
            fallbackProvider = provider
          ).copy(authProvider = provider)

          applyLoadedProfile(loaded)
          NumoSessionManager.saveProfile(loaded, markAsLastAuthenticated = true)

          if (loaded.onboardingCompleted) {
            startupAuthState = StartupAuthState.AUTHENTICATED
            currentScreen = AppScreen.DASHBOARD
          } else {
            startupAuthState = StartupAuthState.ONBOARDING_REQUIRED
            currentScreen = AppScreen.ONBOARDING_CURRENCY
          }
        }

        // Centralized Startup Router: wait for session restoration before routing
        LaunchedEffect(Unit) {
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
            val callbackSessionId = uri.getQueryParameter("created_session_id")

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
                sessionIdHint = callbackSessionId
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
              // Check if Clerk already has an active session on this client token
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
            } catch (e: Exception) {
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
                skipAnimation = hasCompletedSplashAnimation,
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
                currentScreen = AppScreen.WELCOME
              }
              AuthScreen(
                externalErrorMessage = oauthErrorMessage,
                isOAuthLoading = isOAuthInProgressState,
                onClearExternalError = { oauthErrorMessage = null },
                onSignInSuccess = { email ->
                  coroutineScope.launch {
                    routeAuthenticatedUser(
                      email = email,
                      provider = "Email"
                    )
                  }
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
                userEmail = if (pendingFullName.isNotBlank()) pendingFullName else (authenticatedUser ?: "Ayesha Khan"),
                provider = authProvider,
                userCurrency = userCurrency,
                customAccounts = userAccounts,
                onSignOut = {
                  coroutineScope.launch {
                    NumoSessionManager.logout()
                    startupAuthState = StartupAuthState.UNAUTHENTICATED
                    activeUserId = ""
                    authenticatedUser = null
                    pendingFullName = ""
                    userAccounts = emptyList()
                    userCurrency = "KES"
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
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pendingDeepLinkUri = intent.data
  }
}

/**
 * Subtle Numo-branded loading state shown while checking persisted Clerk session
 * on startup so authenticated users never see a flash of onboarding screens.
 */
@Composable
private fun StartupLoadingScreen() {
  NumoScreenBackground(modifier = Modifier.testTag("startup_loading_screen")) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Numo3DLogoEmblem(size = 84.dp)
      Spacer(modifier = Modifier.height(22.dp))
      CircularProgressIndicator(
        color = NumoDS.Turquoise,
        modifier = Modifier.size(26.dp),
        strokeWidth = 2.5.dp
      )
    }
  }
}
