package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ClerkAuthRepository
import com.example.ui.components.Numo3DEnvelopeLockIllustration
import com.example.ui.components.NumoBackCircleButton
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

/**
 * Screen 3: Forgot Password — Exact match to the Numo onboarding reference.
 *
 * Preserves the full 2-step Clerk password reset flow:
 * - Step 1: Enter email address -> `ClerkAuthRepository.requestPasswordReset`
 * - Step 2: Enter 6-digit code + new password -> `ClerkAuthRepository.completePasswordReset`
 */
@Composable
fun ForgotPasswordScreen(
  initialEmail: String = "",
  onBack: () -> Unit,
  onNavigateToSignUp: () -> Unit = {},
  onResetSuccess: (email: String) -> Unit = {}
) {
  val coroutineScope = rememberCoroutineScope()

  var step by remember { mutableIntStateOf(1) }
  var email by remember { mutableStateOf(initialEmail) }
  var resetCode by remember { mutableStateOf("") }
  var newPassword by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var currentSignInId by remember { mutableStateOf("") }
  @Suppress("UNUSED_VARIABLE")
  var currentEmailAddressId by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isSuccessMessage by remember { mutableStateOf(false) }
  var accountNotFound by remember { mutableStateOf(false) }

  NumoScreenBackground(modifier = Modifier.testTag("forgot_password_screen")) {
    NumoFloatingDecorations(variant = 1)

    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
    ) {
      val minScreenHeight = maxHeight
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(min = minScreenHeight)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top bar with circular back button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          NumoBackCircleButton(
            onClick = {
              if (step == 2) {
                step = 1
                statusMessage = null
              } else {
                onBack()
              }
            },
            modifier = Modifier.testTag("btn_forgot_password_back")
          )
        }

        Spacer(modifier = Modifier.weight(0.32f))

        // 3D Envelope + Security Lock illustration
        Numo3DEnvelopeLockIllustration(
          modifier = Modifier.size(172.dp)
        )

      Spacer(modifier = Modifier.height(10.dp))

      // Heading
      Text(
        text = if (step == 1) "Forgot Password?" else "Set New Password",
        fontSize = 27.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Supporting instructions
      Text(
        text = if (step == 1) {
          "Enter your email address and we'll\nsend you a reset link."
        } else {
          "Enter the 6-digit code sent to\n${email.trim()} and your new password."
        },
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(22.dp))

      // Status banners (error or success)
      if (!statusMessage.isNullOrBlank()) {
        val bgColor = if (isSuccessMessage) Color(0xFFE6F9F0) else Color(0xFFFFF1F2)
        val borderColor = if (isSuccessMessage) Color(0xFF86EFAC) else Color(0xFFFECDD3)
        val tintColor = if (isSuccessMessage) NumoDS.HeroTealDark else Color(0xFFB91C1C)

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = tintColor,
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
                color = tintColor,
                fontWeight = FontWeight.Medium
              )
              if (accountNotFound) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Create a new account instead →",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.Turquoise,
                  modifier = Modifier.clickable { onNavigateToSignUp() }
                )
              }
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }

      if (step == 1) {
        // STEP 1: Recessed Email Address input
        RecessedOnboardingInput(
          value = email,
          onValueChange = {
            email = it
            statusMessage = null
            accountNotFound = false
          },
          placeholder = "Email Address",
          icon = Icons.Outlined.MailOutline,
          keyboardType = KeyboardType.Email,
          modifier = Modifier.testTag("input_forgot_password_email")
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              color = NumoDS.Turquoise,
              modifier = Modifier.size(26.dp),
              strokeWidth = 2.5.dp
            )
          }
        } else {
          NumoPrimaryButton(
            text = "Send Reset Link",
            onClick = {
              val trimmed = email.trim()
              if (trimmed.isBlank() || !trimmed.contains("@")) {
                statusMessage = "Please enter a valid email address."
                isSuccessMessage = false
                accountNotFound = false
                return@NumoPrimaryButton
              }

              isLoading = true
              statusMessage = null
              accountNotFound = false

              coroutineScope.launch {
                val result = ClerkAuthRepository.requestPasswordReset(trimmed)
                isLoading = false

                if (result.success && !result.signInId.isNullOrEmpty()) {
                  currentSignInId = result.signInId
                  currentEmailAddressId = result.emailAddressId ?: ""
                  step = 2
                  isSuccessMessage = true
                  statusMessage = "A 6-digit reset code has been sent to $trimmed. Check your inbox!"
                } else {
                  isSuccessMessage = false
                  accountNotFound = !result.isAccountFound
                  statusMessage = result.message
                }
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_send_reset_link"),
            height = 54.dp
          )
        }
      } else {
        // STEP 2: 6-digit code + new password
        RecessedOnboardingInput(
          value = resetCode,
          onValueChange = { resetCode = it.take(6) },
          placeholder = "6-Digit Reset Code",
          icon = Icons.Outlined.Pin,
          keyboardType = KeyboardType.Number,
          modifier = Modifier.testTag("input_reset_code")
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecessedOnboardingInput(
          value = newPassword,
          onValueChange = { newPassword = it },
          placeholder = "New Password (min 8 chars)",
          icon = Icons.Outlined.Lock,
          keyboardType = KeyboardType.Password,
          isPassword = true,
          passwordVisible = passwordVisible,
          onTogglePassword = { passwordVisible = !passwordVisible },
          modifier = Modifier.testTag("input_new_password")
        )

        Spacer(modifier = Modifier.height(12.dp))

        RecessedOnboardingInput(
          value = confirmPassword,
          onValueChange = { confirmPassword = it },
          placeholder = "Confirm New Password",
          icon = Icons.Outlined.Lock,
          keyboardType = KeyboardType.Password,
          isPassword = true,
          passwordVisible = false,
          modifier = Modifier.testTag("input_confirm_new_password")
        )

        Spacer(modifier = Modifier.height(22.dp))

        if (isLoading) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              color = NumoDS.Turquoise,
              modifier = Modifier.size(26.dp),
              strokeWidth = 2.5.dp
            )
          }
        } else {
          NumoPrimaryButton(
            text = "Save New Password & Sign In",
            onClick = {
              if (resetCode.length < 6) {
                statusMessage = "Please enter the complete 6-digit code."
                isSuccessMessage = false
                return@NumoPrimaryButton
              }
              if (newPassword.length < 8) {
                statusMessage = "Password must be at least 8 characters long."
                isSuccessMessage = false
                return@NumoPrimaryButton
              }
              if (newPassword != confirmPassword) {
                statusMessage = "Passwords do not match."
                isSuccessMessage = false
                return@NumoPrimaryButton
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
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_save_new_password"),
            height = 54.dp
          )
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Back to Sign In link
      Text(
        text = "Back to Sign In",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.Turquoise,
        modifier = Modifier
          .clickable { onBack() }
          .padding(vertical = 8.dp, horizontal = 14.dp)
          .testTag("btn_back_to_sign_in")
      )

      Spacer(modifier = Modifier.weight(0.68f))
      Spacer(modifier = Modifier.height(24.dp))
    }
    }
  }
}

/**
 * Rounded recessed neumorphic input surface matching Screen 3's warm recessed Email field.
 */
@Composable
private fun RecessedOnboardingInput(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  icon: ImageVector,
  keyboardType: KeyboardType,
  modifier: Modifier = Modifier,
  isPassword: Boolean = false,
  passwordVisible: Boolean = false,
  onTogglePassword: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(18.dp)
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(54.dp)
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFFE6D5C3), Color(0xFFF0E2D3))
        )
      )
      .border(1.dp, Color(0xFFD8C5B2), shape)
      .padding(horizontal = 18.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color(0xFF5D534A),
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            fontSize = 14.5.sp,
            color = Color(0xFF7D7066),
            fontWeight = FontWeight.Medium
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.fillMaxWidth(),
          textStyle = TextStyle(
            fontSize = 14.5.sp,
            color = NumoDS.TextPrimary,
            fontWeight = FontWeight.Medium
          ),
          singleLine = true,
          cursorBrush = SolidColor(NumoDS.Turquoise),
          visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
          } else {
            VisualTransformation.None
          },
          keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
      }
      if (isPassword && onTogglePassword != null) {
        Icon(
          imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
          contentDescription = "Toggle password",
          tint = Color(0xFF6B5F55),
          modifier = Modifier
            .size(18.dp)
            .clickable { onTogglePassword() }
        )
      }
    }
  }
}
