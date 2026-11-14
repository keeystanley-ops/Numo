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
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.auth.ClerkAuthRepository
import com.example.data.NumoFinancialEngine
import com.example.ui.AllSetScreen
import com.example.ui.AuthScreen
import com.example.ui.DashboardScreen
import com.example.ui.ForgotPasswordScreen
import com.example.ui.SignUpScreen
import com.example.ui.VerifyCodeScreen
import com.example.ui.WelcomeScreen
import com.example.ui.onboarding.AccountConnectedScreen
import com.example.ui.onboarding.OnboardingAccountsScreen
import com.example.ui.onboarding.OnboardingCurrencyScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppScreen {
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

  override fun onResume() {
    super.onResume()
    // When user switches back to the app without completing or after cancelling in the browser,
    // reset the loading state so the sign in / sign up button never gets stuck in a loop.
    isOAuthInProgressState = false
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
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

        var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
        var hasCompletedSplashAnimation by rememberSaveable { mutableStateOf(false) }
        var authenticatedUser by remember { mutableStateOf<String?>(null) }
        var authProvider by remember { mutableStateOf("Email") }
        var pendingSignUpId by remember { mutableStateOf("") }
        var pendingEmail by remember { mutableStateOf("") }
        var pendingFullName by remember { mutableStateOf("") }
        var resetEmailPreload by remember { mutableStateOf("") }
        var userCurrency by remember { mutableStateOf("KES") }
        var userAccounts by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

        var oauthErrorMessage by remember { mutableStateOf<String?>(null) }

        // Handle incoming OAuth deep links (e.g. numo://oauth_callback)
        LaunchedEffect(pendingDeepLinkUri) {
          val uri = pendingDeepLinkUri
          if (uri != null && uri.scheme == "numo") {
            pendingDeepLinkUri = null
            isOAuthInProgressState = true

            // Check if user clicked cancel or provider returned an error query param
            val error = uri.getQueryParameter("error")
              ?: uri.getQueryParameter("error_description")
            val clerkStatus = uri.getQueryParameter("status")
            val rotatingToken = uri.getQueryParameter("__clerk_db_jwt")
              ?: uri.getQueryParameter("token")

            if (rotatingToken != null) {
              ClerkAuthRepository.setClientToken(rotatingToken)
            }

            if (!error.isNullOrEmpty() || clerkStatus == "abandoned" || clerkStatus == "failed") {
              isOAuthInProgressState = false
              val message = when {
                error?.contains("access_denied", ignoreCase = true) == true ||
                  error?.contains("cancel", ignoreCase = true) == true ->
                  "$authProvider authentication was cancelled."
                else -> error ?: "Sign-in with $authProvider failed. Please try again."
              }
              oauthErrorMessage = message
              Toast.makeText(context, message, Toast.LENGTH_LONG).show()
              return@LaunchedEffect
            }

            // Verify session from Clerk backend
            coroutineScope.launch {
              val sessionResult = ClerkAuthRepository.verifyCurrentClientSession()
              isOAuthInProgressState = false

              if (sessionResult.success && !sessionResult.email.isNullOrBlank()) {
                oauthErrorMessage = null
                authenticatedUser = sessionResult.email
                // Start onboarding sequence (Currency -> Accounts -> All Set -> Dashboard)
                currentScreen = AppScreen.ONBOARDING_CURRENCY
                Toast.makeText(context, "Successfully authenticated with $authProvider!", Toast.LENGTH_SHORT).show()
              } else {
                // If Clerk returned no verified session (e.g. cancelled in Chrome before completing)
                val failMsg = sessionResult.errorMessage
                  ?: "$authProvider sign-in was not completed or was cancelled."
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
              ?: ClerkAuthRepository.getOAuthUrl(provider)

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
            fadeIn(animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)) togetherWith
              fadeOut(animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing))
          },
          label = "AppScreenTransition"
        ) { screen ->
          when (screen) {
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
                authProvider = "Email"
                authenticatedUser = email
                currentScreen = AppScreen.DASHBOARD
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
                authProvider = "Email"
                authenticatedUser = email
                Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                currentScreen = AppScreen.DASHBOARD
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
              onVerificationSuccess = { sessionId ->
                authProvider = "Email"
                authenticatedUser = pendingEmail
                // Once verified, proceed to onboarding setup (Currency -> Accounts -> All Set)
                currentScreen = AppScreen.ONBOARDING_CURRENCY
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
                // Only show Screen 6 (Account Connected) after account connection succeeds
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
              onContinue = {
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
                currentScreen = AppScreen.DASHBOARD
              },
              onAddMoreAccounts = {
                currentScreen = AppScreen.ONBOARDING_ACCOUNTS
              }
            )
          }

          AppScreen.DASHBOARD -> {
            BackHandler {
              authenticatedUser = null
              currentScreen = AppScreen.WELCOME
            }
            DashboardScreen(
              userEmail = if (!pendingFullName.isNullOrBlank()) pendingFullName else (authenticatedUser ?: "Ayesha Khan"),
              provider = authProvider,
              userCurrency = userCurrency,
              customAccounts = userAccounts,
              onSignOut = {
                authenticatedUser = null
                currentScreen = AppScreen.WELCOME
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
