package com.example.ui

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.ClerkAuthRepository
import com.example.ui.animation.bouncyClickable
import com.example.ui.theme.NumoActionButtonBg
import com.example.ui.theme.NumoActionButtonBorder
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoElementsCorners
import com.example.ui.theme.NumoGlassCardBg
import com.example.ui.theme.NumoGlassCardBorder
import com.example.ui.theme.NumoInputBg
import com.example.ui.theme.NumoInputPlaceholder
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen
import kotlinx.coroutines.launch

/**
 * Screen: "Forgot Password" (Password Reset Flow)
 * Step 1: User enters email, Clerk dispatches 6-digit reset code to inbox.
 * Step 2: User enters the 6-digit code and their new password to complete the reset.
 */
@Composable
fun ForgotPasswordScreen(
  initialEmail: String = "",
  onBack: () -> Unit,
  onNavigateToSignUp: () -> Unit = {},
  onResetSuccess: (email: String) -> Unit = {}
) {
  val coroutineScope = rememberCoroutineScope()

  var email by remember { mutableStateOf(initialEmail) }
  var resetCode by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  // Flow step: 1 = Email submission, 2 = Code & New password entry, 3 = Reset completed
  var step by remember { mutableStateOf(1) }
  var currentSignInId by remember { mutableStateOf("") }
  var currentEmailAddressId by remember { mutableStateOf<String?>(null) }

  var isLoading by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSuccessMessage by remember { mutableStateOf(false) }
  var accountNotFound by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = NumoMintBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(NumoMintBackground)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp)
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Navigation Bar: Back button on Left, Brand leaf on Right
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              if (step == 2) {
                step = 1
                statusMessage = null
              } else {
                onBack()
              }
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, NumoElementsCorners, CircleShape)
              .testTag("btn_forgot_password_back")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(20.dp)
            )
          }

          Image(
            painter = painterResource(id = R.drawable.ic_numo_leaf_transparent),
            contentDescription = "Numo Brand Leaf",
            modifier = Modifier
              .size(36.dp)
              .testTag("forgot_password_top_leaf"),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Title & Subtitle
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
        ) {
          Text(
            text = if (step == 1) "Reset Your" else "New",
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = NumoDarkForestGreen
          )
          Text(
            text = if (step == 1) "Password" else "Password",
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = NumoDarkForestGreen
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = if (step == 1) {
              "Enter your account email and Clerk will send a password reset code directly to your inbox."
            } else {
              "Enter the 6-digit reset code sent to ${email.trim()} and choose your new password."
            },
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.SansSerif,
            color = NumoSlateGreen
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 3. Status Notification Banners (Success or Error)
        if (isSuccessMessage && !statusMessage.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFFE6F9F0))
              .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(16.dp))
              .padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = NumoDarkForestGreen,
                modifier = Modifier
                  .size(20.dp)
                  .padding(top = 1.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = statusMessage ?: "",
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                color = NumoDarkForestGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
              )
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        } else if (!isSuccessMessage && !statusMessage.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(Color(0xFFFFF1F2))
              .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
              .padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.Top) {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = Color(0xFFB91C1C),
                modifier = Modifier
                  .size(20.dp)
                  .padding(top = 1.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = statusMessage ?: "",
                  fontSize = 12.5.sp,
                  lineHeight = 17.sp,
                  color = Color(0xFFB91C1C)
                )

                if (accountNotFound) {
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Create a new account instead →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDarkForestGreen,
                    modifier = Modifier.clickable { onNavigateToSignUp() }
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        // 4. MINT GLASSMORPHISM CARD
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(26.dp),
              spotColor = NumoDarkForestGreen.copy(alpha = 0.08f),
              ambientColor = NumoDarkForestGreen.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(NumoGlassCardBg)
            .border(1.5.dp, NumoGlassCardBorder, RoundedCornerShape(26.dp))
            .padding(20.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            if (step == 1) {
              // --- STEP 1: ENTER EMAIL TO RECEIVE RESET CODE ---
              Text(
                text = "Email Address",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDarkForestGreen
              )

              Spacer(modifier = Modifier.height(6.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(NumoInputBg)
                  .border(
                    width = 1.5.dp,
                    color = if (email.isNotEmpty()) NumoDarkForestGreen else NumoElementsCorners,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                if (email.isEmpty()) {
                  Text(
                    text = "name@example.com",
                    fontSize = 13.5.sp,
                    color = NumoInputPlaceholder
                  )
                }
                BasicTextField(
                  value = email,
                  onValueChange = {
                    email = it
                    statusMessage = null
                    accountNotFound = false
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_forgot_password_email"),
                  textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Normal
                  ),
                  singleLine = true,
                  cursorBrush = SolidColor(NumoDarkForestGreen),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Action Button: "Send Reset Link"
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .border(1.dp, NumoActionButtonBorder, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading) {
                  val trimmed = email.trim()
                  if (trimmed.isBlank() || !trimmed.contains("@")) {
                    statusMessage = "Please enter a valid email address."
                    isSuccessMessage = false
                    accountNotFound = false
                    return@bouncyClickable
                  }

                  isLoading = true
                  statusMessage = null
                  accountNotFound = false

                  coroutineScope.launch {
                    val result = ClerkAuthRepository.requestPasswordReset(trimmed)
                    isLoading = false

                    if (result.success && !result.signInId.isNullOrEmpty()) {
                      currentSignInId = result.signInId
                      currentEmailAddressId = result.emailAddressId
                      step = 2
                      isSuccessMessage = true
                      statusMessage = "A 6-digit reset code has been sent to $trimmed. Check your inbox!"
                    } else {
                      isSuccessMessage = false
                      accountNotFound = !result.isAccountFound
                      statusMessage = result.message
                    }
                  }
                }
                .testTag("btn_send_reset_link"),
              color = NumoActionButtonBg,
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                if (isLoading) {
                  CircularProgressIndicator(
                    color = NumoDarkForestGreen,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                  )
                } else {
                  Text(
                    text = "Send Reset Link",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDarkForestGreen
                  )
                }
              }
            }
          } else {
            // --- STEP 2: ENTER CODE & NEW PASSWORD ---
            Text(
              text = "6-Digit Reset Code",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(
                  width = 1.5.dp,
                  color = if (resetCode.isNotEmpty()) NumoDarkForestGreen else NumoElementsCorners,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (resetCode.isEmpty()) {
                Text(
                  text = "Enter 6-digit code from email",
                  fontSize = 13.5.sp,
                  color = NumoInputPlaceholder
                )
              }
              BasicTextField(
                value = resetCode,
                onValueChange = { resetCode = it.take(6) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_reset_code"),
                textStyle = TextStyle(
                  fontSize = 14.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Medium,
                  letterSpacing = 2.sp
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "New Password",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(
                  width = 1.dp,
                  color = NumoElementsCorners,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(modifier = Modifier.weight(1f)) {
                  if (newPassword.isEmpty()) {
                    Text(
                      text = "At least 8 characters",
                      fontSize = 13.5.sp,
                      color = NumoInputPlaceholder
                    )
                  }
                  BasicTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("input_new_password"),
                    textStyle = TextStyle(
                      fontSize = 14.sp,
                      color = Color(0xFF0F172A),
                      fontWeight = FontWeight.Normal
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(NumoDarkForestGreen),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                  )
                }

                Icon(
                  imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                  contentDescription = "Toggle password",
                  tint = NumoSlateGreen,
                  modifier = Modifier
                    .size(18.dp)
                    .clickable { passwordVisible = !passwordVisible }
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Confirm New Password",
              fontSize = 12.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(
                  width = 1.dp,
                  color = NumoElementsCorners,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (confirmPassword.isEmpty()) {
                Text(
                  text = "Confirm your password",
                  fontSize = 13.5.sp,
                  color = NumoInputPlaceholder
                )
              }
              BasicTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_confirm_new_password"),
                textStyle = TextStyle(
                  fontSize = 14.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button: "Save New Password & Sign In"
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoActionButtonBorder, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading) {
                  if (resetCode.length < 6) {
                    statusMessage = "Please enter the complete 6-digit code."
                    isSuccessMessage = false
                    return@bouncyClickable
                  }
                  if (newPassword.length < 8) {
                    statusMessage = "Password must be at least 8 characters long."
                    isSuccessMessage = false
                    return@bouncyClickable
                  }
                  if (newPassword != confirmPassword) {
                    statusMessage = "Passwords do not match."
                    isSuccessMessage = false
                    return@bouncyClickable
                  }

                  isLoading = true
                  statusMessage = null

                  coroutineScope.launch {
                    val result = ClerkAuthRepository.completePasswordReset(
                      signInId = currentSignInId,
                      code = resetCode.trim(),
                      newPassword = newPassword
                    )
                    isLoading = false

                    if (result.success) {
                      isSuccessMessage = true
                      statusMessage = "Password reset successful! Logging you in..."
                      onResetSuccess(email.trim())
                    } else {
                      isSuccessMessage = false
                      statusMessage = result.errorMessage ?: "Failed to reset password. Please verify the code."
                    }
                  }
                }
                .testTag("btn_save_new_password"),
              color = NumoActionButtonBg,
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                if (isLoading) {
                  CircularProgressIndicator(
                    color = NumoDarkForestGreen,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                  )
                } else {
                  Text(
                    text = "Save New Password & Sign In",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDarkForestGreen
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resend code link
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Didn't receive email? Resend code",
                fontSize = 12.5.sp,
                color = NumoSlateGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                  .clickable {
                    coroutineScope.launch {
                      val resent = ClerkAuthRepository.resendPasswordResetCode(currentSignInId, currentEmailAddressId)
                      if (resent) {
                        isSuccessMessage = true
                        statusMessage = "A fresh code was sent to your email!"
                      }
                    }
                  }
                  .padding(4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Secondary Action: Return to Sign In
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .bouncyClickable { onBack() }
              .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Remember your password? ",
              fontSize = 13.sp,
              color = NumoSlateGreen
            )
            Text(
              text = "Sign In",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDarkForestGreen,
              modifier = Modifier.testTag("link_forgot_back_to_signin")
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Back to Sign In",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
}
