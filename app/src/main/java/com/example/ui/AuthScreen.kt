package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.ClerkAuthRepository
import com.example.ui.components.Numo3DWalletIllustration
import com.example.ui.components.NumoBackCircleButton
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

/**
 * SCREEN 2: Welcome / Sign In / Sign Up matching 02_welcome_auth.png
 * Preserves full ClerkAuthRepository sign-in, OAuth, and forgot-password integration.
 */
@Composable
fun AuthScreen(
  externalErrorMessage: String? = null,
  isOAuthLoading: Boolean = false,
  onClearExternalError: () -> Unit = {},
  onSignInSuccess: (email: String) -> Unit = {},
  onSignUpClick: () -> Unit = {},
  onGoogleClick: () -> Unit = {},
  onAppleClick: () -> Unit = {},
  onForgotPasswordClick: (currentEmail: String) -> Unit = {},
  onBack: () -> Unit = {},
) {
  val coroutineScope = rememberCoroutineScope()
  var showSignInForm by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var localErrorMessage by remember { mutableStateOf<String?>(null) }
  val displayedError = externalErrorMessage ?: localErrorMessage

  fun submitSignIn() {
    val cleanEmail = email.trim()
    if (cleanEmail.isEmpty() || !cleanEmail.contains("@")) {
      localErrorMessage = "Please enter a valid email address."
      return
    }
    if (password.isEmpty()) {
      localErrorMessage = "Please enter your password."
      return
    }
    localErrorMessage = null
    onClearExternalError()
    isLoading = true

    coroutineScope.launch {
      val result = ClerkAuthRepository.signIn(cleanEmail, password)
      isLoading = false
      if (result.success) {
        onSignInSuccess(cleanEmail)
      } else {
        localErrorMessage = result.errorMessage ?: "Sign in failed. Please check your credentials."
      }
    }
  }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top bar: Back button on upper-left, 3D Wallet illustration on upper-right
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        NumoBackCircleButton(
          onClick = onBack,
          modifier = Modifier
            .padding(top = 4.dp)
            .testTag("btn_welcome_back")
        )
        Numo3DWalletIllustration(
          size = 118.dp,
          modifier = Modifier.padding(end = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Welcome to Numo Heading
      Text(
        text = "Welcome to",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.SansSerif,
        color = NumoDS.TextPrimary
      )
      Text(
        text = "Numo",
        fontSize = 34.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.SansSerif,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.6).sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Your Personal Finance\nPartner",
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        color = NumoDS.TextSecondary,
        lineHeight = 21.sp
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Primary Sign Up Button (Teal Claymorphic)
      NumoPrimaryButton(
        text = "Sign Up",
        onClick = onSignUpClick,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_welcome_sign_up"),
        height = 52.dp
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Secondary Sign In Button (Black Claymorphic)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .shadow(
            elevation = 10.dp,
            shape = RoundedCornerShape(50),
            ambientColor = Color(0x40000000),
            spotColor = Color(0x55000000)
          )
          .clip(RoundedCornerShape(50))
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFF3F3F46), Color(0xFF18181B), NumoDS.Black)
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
          .clickable(enabled = !isLoading && !isOAuthLoading) {
            if (!showSignInForm) {
              showSignInForm = true
            } else {
              submitSignIn()
            }
          }
          .testTag("btn_sign_in"),
        contentAlignment = Alignment.Center
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp
          )
        } else {
          Text(
            text = if (showSignInForm) "Continue Sign In" else "Sign In",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      // Expandable Clerk Email & Password Form
      AnimatedVisibility(visible = showSignInForm) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          // Recessed Email Input
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .clip(RoundedCornerShape(50))
              .background(Color(0xFFF1E0D2))
              .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
              .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = null,
                tint = NumoDS.HeroTealMid,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Box(modifier = Modifier.weight(1f)) {
                if (email.isEmpty()) {
                  Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    color = NumoDS.TextMuted
                  )
                }
                BasicTextField(
                  value = email,
                  onValueChange = {
                    email = it
                    localErrorMessage = null
                    onClearExternalError()
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                  textStyle = TextStyle(fontSize = 14.sp, color = NumoDS.TextPrimary),
                  cursorBrush = SolidColor(NumoDS.HeroTealMid),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_email")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Recessed Password Input
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .clip(RoundedCornerShape(50))
              .background(Color(0xFFF1E0D2))
              .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
              .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = NumoDS.HeroTealMid,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Box(modifier = Modifier.weight(1f)) {
                if (password.isEmpty()) {
                  Text(
                    text = "Password",
                    fontSize = 14.sp,
                    color = NumoDS.TextMuted
                  )
                }
                BasicTextField(
                  value = password,
                  onValueChange = {
                    password = it
                    localErrorMessage = null
                    onClearExternalError()
                  },
                  singleLine = true,
                  visualTransformation = PasswordVisualTransformation(),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                  textStyle = TextStyle(fontSize = 14.sp, color = NumoDS.TextPrimary),
                  cursorBrush = SolidColor(NumoDS.HeroTealMid),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_password")
                )
              }
            }
          }
        }
      }

      if (!displayedError.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = displayedError,
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.ExpenseRed,
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // "or continue with" Divider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE6D2C2))
        Text(
          text = "or continue with",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = NumoDS.TextSecondary,
          modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE6D2C2))
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Google & Apple Neumorphic Pill Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoNeumorphicCard(
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_google_signin"),
          cornerRadius = 50.dp,
          onClick = { if (!isLoading && !isOAuthLoading) onGoogleClick() }
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_google),
              contentDescription = "Google",
              modifier = Modifier.size(20.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Google",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.TextPrimary
            )
          }
        }

        NumoNeumorphicCard(
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("btn_apple_signin"),
          cornerRadius = 50.dp,
          onClick = { if (!isLoading && !isOAuthLoading) onAppleClick() }
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_apple),
              contentDescription = "Apple",
              modifier = Modifier.size(20.dp),
              contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Apple",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.TextPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Forgot Password? Link
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onForgotPasswordClick(email.trim()) }
          .padding(vertical = 6.dp)
          .testTag("btn_forgot_password"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Forgot Password?",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.HeroTealMid
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
