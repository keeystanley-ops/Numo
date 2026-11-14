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
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.components.Numo3DWalletIllustration
import com.example.ui.components.NumoBackCircleButton
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

/**
 * Sign Up Screen — Uses the Numo warm cream + turquoise + claymorphic design system
 * while preserving all ClerkAuthRepository registration, validation, and OAuth logic.
 */
@Composable
fun SignUpScreen(
  externalErrorMessage: String? = null,
  isOAuthLoading: Boolean = false,
  onClearExternalError: () -> Unit = {},
  onNavigateToSignIn: () -> Unit,
  onSignUpSuccess: (signUpId: String, email: String, fullName: String) -> Unit,
  onGoogleSignUp: () -> Unit,
  onAppleSignUp: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()

  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }
  var localErrorMessage by remember { mutableStateOf<String?>(null) }
  val displayedError = externalErrorMessage ?: localErrorMessage

  fun submitSignUp() {
    val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
    if (email.isBlank() || !email.contains("@")) {
      localErrorMessage = "Please enter a valid email address."
      return
    }
    if (password.isBlank()) {
      localErrorMessage = "Please enter a password."
      return
    }
    if (password.length < 8) {
      localErrorMessage = "Password must be at least 8 characters long."
      return
    }
    if (password != confirmPassword) {
      localErrorMessage = "Passwords do not match."
      return
    }

    isLoading = true
    localErrorMessage = null
    onClearExternalError()

    coroutineScope.launch {
      val result = ClerkAuthRepository.signUp(
        email = email.trim(),
        pass = password,
        fullName = fullName.ifBlank { "User" }
      )
      isLoading = false

      if (result.success && !result.signUpId.isNullOrBlank()) {
        onSignUpSuccess(result.signUpId, email.trim(), fullName.ifBlank { "Ayesha Khan" })
      } else {
        localErrorMessage = result.errorMessage ?: "Sign-up failed. Please try again."
      }
    }
  }

  NumoScreenBackground(modifier = Modifier.testTag("signup_screen")) {
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

      // Top bar: Back button on upper-left, 3D Wallet accent on upper-right
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        NumoBackCircleButton(
          onClick = onNavigateToSignIn,
          modifier = Modifier
            .padding(top = 4.dp)
            .testTag("btn_signup_back")
        )
        Numo3DWalletIllustration(
          size = 86.dp,
          modifier = Modifier.padding(end = 4.dp)
        )
      }

      // Heading
      Text(
        text = "Create Account",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.SansSerif,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.6).sp
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Start your smart financial journey with Numo.",
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = NumoDS.TextSecondary
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Error banner
      if (!displayedError.isNullOrBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF1F2))
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = Color(0xFFB91C1C),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = displayedError,
              fontSize = 12.5.sp,
              lineHeight = 17.sp,
              color = Color(0xFFB91C1C),
              fontWeight = FontWeight.Medium
            )
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }

      // First Name & Last Name row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        SignUpRecessedField(
          value = firstName,
          onValueChange = {
            firstName = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "First Name",
          icon = Icons.Outlined.Person,
          keyboardType = KeyboardType.Text,
          modifier = Modifier
            .weight(1f)
            .testTag("input_signup_firstname")
        )
        SignUpRecessedField(
          value = lastName,
          onValueChange = {
            lastName = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "Last Name",
          icon = Icons.Outlined.Person,
          keyboardType = KeyboardType.Text,
          modifier = Modifier
            .weight(1f)
            .testTag("input_signup_lastname")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Email Address
      SignUpRecessedField(
        value = email,
        onValueChange = {
          email = it
          localErrorMessage = null
          onClearExternalError()
        },
        placeholder = "Email Address",
        icon = Icons.Outlined.MailOutline,
        keyboardType = KeyboardType.Email,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_signup_email")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Password
      SignUpRecessedField(
        value = password,
        onValueChange = {
          password = it
          localErrorMessage = null
          onClearExternalError()
        },
        placeholder = "Password (min 8 characters)",
        icon = Icons.Outlined.Lock,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = passwordVisible,
        onTogglePassword = { passwordVisible = !passwordVisible },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_signup_password")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Confirm Password
      SignUpRecessedField(
        value = confirmPassword,
        onValueChange = {
          confirmPassword = it
          localErrorMessage = null
          onClearExternalError()
        },
        placeholder = "Confirm Password",
        icon = Icons.Outlined.Lock,
        keyboardType = KeyboardType.Password,
        isPassword = true,
        passwordVisible = confirmPasswordVisible,
        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_signup_confirm_password")
      )

      Spacer(modifier = Modifier.height(18.dp))

      // Create Account Primary Claymorphic Button
      if (isLoading || isOAuthLoading) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
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
          text = "Create Account",
          onClick = { submitSignUp() },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_signup_submit"),
          height = 52.dp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Divider
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = NumoDS.TextMuted.copy(alpha = 0.25f)
        )
        Text(
          text = "  or sign up with  ",
          fontSize = 12.5.sp,
          color = NumoDS.TextSecondary,
          fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = NumoDS.TextMuted.copy(alpha = 0.25f)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Google & Apple Claymorphic Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        NumoNeumorphicCard(
          modifier = Modifier
            .weight(1f)
            .height(50.dp)
            .testTag("btn_google_signup"),
          cornerRadius = 50.dp,
          onClick = { if (!isLoading && !isOAuthLoading) onGoogleSignUp() }
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_google),
              contentDescription = "Google",
              modifier = Modifier.size(20.dp)
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
            .height(50.dp)
            .testTag("btn_apple_signup"),
          cornerRadius = 50.dp,
          onClick = { if (!isLoading && !isOAuthLoading) onAppleSignUp() }
        ) {
          Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_apple),
              contentDescription = "Apple",
              modifier = Modifier.size(18.dp)
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

      Spacer(modifier = Modifier.height(18.dp))

      // Already have an account? Sign In
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToSignIn() }
          .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Already have an account? ",
          fontSize = 13.5.sp,
          color = NumoDS.TextSecondary
        )
        Text(
          text = "Sign In",
          fontSize = 13.5.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.Turquoise,
          modifier = Modifier.testTag("link_to_signin")
        )
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}

@Composable
private fun SignUpRecessedField(
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
      .height(52.dp)
      .clip(shape)
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFFE6D5C3), Color(0xFFF0E2D3))
        )
      )
      .border(1.dp, Color(0xFFD8C5B2), shape)
      .padding(horizontal = 16.dp),
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
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            fontSize = 13.5.sp,
            color = Color(0xFF7D7066),
            fontWeight = FontWeight.Medium,
            maxLines = 1
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          modifier = Modifier.fillMaxWidth(),
          textStyle = TextStyle(
            fontSize = 14.sp,
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
