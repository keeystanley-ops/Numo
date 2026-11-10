package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.auth.ClerkAuthRepository
import com.example.ui.AuthScreen
import com.example.ui.DashboardScreen
import com.example.ui.SignUpScreen
import com.example.ui.VerifyCodeScreen
import com.example.ui.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

enum class AppScreen {
  WELCOME,
  SIGN_IN,
  SIGN_UP,
  VERIFY_CODE,
  DASHBOARD
}

class MainActivity : ComponentActivity() {
  private var pendingDeepLinkUri by mutableStateOf<Uri?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    pendingDeepLinkUri = intent?.data

    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
        var authenticatedUser by remember { mutableStateOf<String?>(null) }
        var authProvider by remember { mutableStateOf("Email") }
        var pendingSignUpId by remember { mutableStateOf("") }
        var pendingEmail by remember { mutableStateOf("") }

        var oauthErrorMessage by remember { mutableStateOf<String?>(null) }
        var isOAuthInProgress by remember { mutableStateOf(false) }

        // Handle incoming OAuth deep links (e.g. numo://oauth_callback)
        LaunchedEffect(pendingDeepLinkUri) {
          val uri = pendingDeepLinkUri
          if (uri != null && uri.scheme == "numo") {
            pendingDeepLinkUri = null
            isOAuthInProgress = true

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
              isOAuthInProgress = false
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
              isOAuthInProgress = false

              if (sessionResult.success && !sessionResult.email.isNullOrBlank()) {
                oauthErrorMessage = null
                authenticatedUser = sessionResult.email
                currentScreen = AppScreen.DASHBOARD
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
          isOAuthInProgress = true
          coroutineScope.launch {
            val oauthUrl = ClerkAuthRepository.getOAuthRedirectUrl(provider, isSignUp)
              ?: ClerkAuthRepository.getOAuthUrl(provider)

            try {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl))
              context.startActivity(intent)
            } catch (e: Exception) {
              isOAuthInProgress = false
              oauthErrorMessage = "Could not open browser for $authProvider sign-in."
              Toast.makeText(context, "Could not open browser for $authProvider sign-in.", Toast.LENGTH_SHORT).show()
            }
          }
        }

        when (currentScreen) {
          AppScreen.WELCOME -> {
            WelcomeScreen(
              onGetStarted = {
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
              isOAuthLoading = isOAuthInProgress,
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
              onBack = {
                currentScreen = AppScreen.WELCOME
              }
            )
          }

          AppScreen.SIGN_UP -> {
            BackHandler {
              currentScreen = AppScreen.SIGN_IN
            }
            SignUpScreen(
              externalErrorMessage = oauthErrorMessage,
              isOAuthLoading = isOAuthInProgress,
              onClearExternalError = { oauthErrorMessage = null },
              onNavigateToSignIn = {
                oauthErrorMessage = null
                currentScreen = AppScreen.SIGN_IN
              },
              onSignUpSuccess = { signUpId, email ->
                pendingSignUpId = signUpId
                pendingEmail = email
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
                currentScreen = AppScreen.DASHBOARD
              },
              onBack = {
                currentScreen = AppScreen.SIGN_UP
              }
            )
          }

          AppScreen.DASHBOARD -> {
            BackHandler {
              authenticatedUser = null
              currentScreen = AppScreen.WELCOME
            }
            DashboardScreen(
              userEmail = authenticatedUser ?: "user@numo.app",
              provider = authProvider,
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

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pendingDeepLinkUri = intent.data
  }
}
