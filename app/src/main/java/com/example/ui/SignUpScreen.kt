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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.ClerkAuthRepository
import kotlinx.coroutines.launch

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

  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }
  var localErrorMessage by remember { mutableStateOf<String?>(null) }
  val displayedError = externalErrorMessage ?: localErrorMessage

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF7FDFB)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFD2FCEF),
              Color(0xFFE6FAF3),
              Color(0xFFF4FDF9),
              Color(0xFFFFFFFF)
            )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(18.dp))

        // 1. Numo Brand Mark
        Image(
          painter = painterResource(id = R.drawable.ic_numo_leaf),
          contentDescription = "Numo Brand Logo",
          modifier = Modifier
            .size(width = 68.dp, height = 54.dp)
            .testTag("signup_brand_logo"),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Title: "Numo"
        Text(
          text = "Numo",
          fontSize = 36.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF001A49),
          letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Headline: "Create Your Account" (with Account in green)
        val titleText = buildAnnotatedString {
          append("Create Your ")
          withStyle(SpanStyle(color = Color(0xFF029766))) {
            append("Account")
          }
        }
        Text(
          text = titleText,
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF001A49),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Subtitle
        Text(
          text = "Take control of your finances and\nbuild a brighter future.",
          fontSize = 15.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF334E68),
          lineHeight = 22.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Input Fields
        // A. Full Name
        InputFieldRow(
          value = fullName,
          onValueChange = {
            fullName = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "Full Name",
          icon = {
            Icon(
              imageVector = Icons.Outlined.Person,
              contentDescription = "Full Name",
              tint = Color(0xFF029766),
              modifier = Modifier.size(20.dp)
            )
          },
          testTag = "full_name_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // B. Email Address
        InputFieldRow(
          value = email,
          onValueChange = {
            email = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "Email Address",
          keyboardType = KeyboardType.Email,
          icon = {
            Icon(
              imageVector = Icons.Outlined.Email,
              contentDescription = "Email",
              tint = Color(0xFF029766),
              modifier = Modifier.size(20.dp)
            )
          },
          testTag = "signup_email_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // C. Password
        InputFieldRow(
          value = password,
          onValueChange = {
            password = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "Password",
          keyboardType = KeyboardType.Password,
          isPassword = true,
          passwordVisible = passwordVisible,
          onTogglePassword = { passwordVisible = !passwordVisible },
          icon = {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = "Password",
              tint = Color(0xFF029766),
              modifier = Modifier.size(20.dp)
            )
          },
          testTag = "signup_password_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // D. Confirm Password
        InputFieldRow(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            localErrorMessage = null
            onClearExternalError()
          },
          placeholder = "Confirm Password",
          keyboardType = KeyboardType.Password,
          isPassword = true,
          passwordVisible = confirmPasswordVisible,
          onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
          icon = {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = "Confirm Password",
              tint = Color(0xFF029766),
              modifier = Modifier.size(20.dp)
            )
          },
          testTag = "signup_confirm_password_input"
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. Create Account Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
              elevation = 12.dp,
              shape = RoundedCornerShape(27.dp),
              ambientColor = Color(0x55027858),
              spotColor = Color(0x80029766)
            )
            .clip(RoundedCornerShape(27.dp))
            .background(
              brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF029766), Color(0xFF027858))
              )
            )
            .clickable(enabled = !isLoading) {
              // Validations
              when {
                fullName.trim().isEmpty() -> {
                  localErrorMessage = "Please enter your full name."
                }
                email.trim().isEmpty() -> {
                  localErrorMessage = "Please enter your email address."
                }
                !email.contains("@") -> {
                  localErrorMessage = "Please enter a valid email address."
                }
                password.isEmpty() -> {
                  localErrorMessage = "Please enter a password."
                }
                password.length < 8 -> {
                  localErrorMessage = "Password must be at least 8 characters long."
                }
                password != confirmPassword -> {
                  localErrorMessage = "Passwords do not match. Please verify."
                }
                else -> {
                  isLoading = true
                  localErrorMessage = null
                  onClearExternalError()
                  coroutineScope.launch {
                    val result = ClerkAuthRepository.signUp(email, password, fullName)
                    isLoading = false
                    if (result.success && !result.signUpId.isNullOrEmpty()) {
                      onSignUpSuccess(result.signUpId, email.trim(), fullName.trim())
                    } else {
                      localErrorMessage = result.errorMessage ?: "Failed to create account with Clerk."
                    }
                  }
                }
              }
            }
            .testTag("create_account_button"),
          contentAlignment = Alignment.Center
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              color = Color.White,
              modifier = Modifier.size(24.dp),
              strokeWidth = 2.5.dp
            )
          } else {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Text(
                text = "Create Account",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "→",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // "Or" Divider
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color(0xFFE2E8F0)
          )
          Text(
            text = "Or",
            color = Color(0xFF627D98),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp)
          )
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = Color(0xFFE2E8F0)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Full-width Social Sign Up Buttons
        // Google
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
            .background(Color.White)
            .clickable { onGoogleSignUp() }
            .testTag("google_signup_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.ic_google),
              contentDescription = "Google",
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Sign up with Google",
              color = Color(0xFF001A49),
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Apple
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(26.dp))
            .background(Color.White)
            .clickable { onAppleSignUp() }
            .testTag("apple_signup_button"),
          contentAlignment = Alignment.Center
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
              painter = painterResource(id = R.drawable.ic_apple),
              contentDescription = "Apple",
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Sign up with Apple",
              color = Color(0xFF001A49),
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 8. Footer: Already have an account? Sign In
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSignIn() }
            .testTag("navigate_to_signin_link")
        ) {
          Text(
            text = "Already have an account? ",
            color = Color(0xFF486581),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = "Sign In",
            color = Color(0xFF029766),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 9. Terms of Service badge
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Outlined.Security,
            contentDescription = "Protected",
            tint = Color(0xFF029766),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "By creating an account, you agree to our Terms of Service and Privacy Policy.",
            color = Color(0xFF627D98),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Start
          )
        }

        Spacer(modifier = Modifier.height(28.dp))
      }

      // Floating Error Notification (hovers at bottom above controls without pushing content)
      androidx.compose.animation.AnimatedVisibility(
        visible = displayedError != null,
        enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut(),
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x33B91C1C), spotColor = Color(0x4DB91C1C))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFEF2F2))
            .border(1.2.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEF4444)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = displayedError ?: "",
              color = Color(0xFF991B1B),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "✕",
              color = Color(0xFF991B1B),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              modifier = Modifier
                .clickable {
                  localErrorMessage = null
                  onClearExternalError()
                }
                .padding(4.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun InputFieldRow(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  icon: @Composable () -> Unit,
  keyboardType: KeyboardType = KeyboardType.Text,
  isPassword: Boolean = false,
  passwordVisible: Boolean = false,
  onTogglePassword: () -> Unit = {},
  testTag: String = ""
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(54.dp)
      .clip(RoundedCornerShape(27.dp))
      .border(1.2.dp, Color(0xFFE2E8F0), RoundedCornerShape(27.dp))
      .background(Color.White)
      .padding(horizontal = 18.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      icon()
      Spacer(modifier = Modifier.width(12.dp))
      Box(modifier = Modifier.weight(1f)) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            color = Color(0xFF94A3B8),
            fontSize = 15.sp
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = true,
          visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
          textStyle = TextStyle(
            color = Color(0xFF001A49),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(Color(0xFF029766)),
          keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
          modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
        )
      }
      if (isPassword) {
        Icon(
          imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
          contentDescription = "Toggle password visibility",
          tint = Color(0xFF94A3B8),
          modifier = Modifier
            .size(20.dp)
            .clickable { onTogglePassword() }
        )
      }
    }
  }
}
