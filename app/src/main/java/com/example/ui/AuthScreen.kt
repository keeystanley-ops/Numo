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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.ClerkAuthRepository
import kotlinx.coroutines.launch

// Colors sampled precisely from auth.png
val NumoMintTop = Color(0xFFD4FAEF)
val NumoMintMid = Color(0xFFE6FAF3)
val NumoMintLight = Color(0xFFF3FCF8)
val NumoWhite = Color(0xFFFFFFFF)
val NumoDeepNavy = Color(0xFF001A49)
val NumoTaglineNavy = Color(0xFF0F2F4A)
val NumoSubText = Color(0xFF627D98)
val NumoBorder = Color(0xFFE2E8F0)
val NumoPlaceholder = Color(0xFF94A3B8)
val NumoEmeraldStart = Color(0xFF029766)
val NumoEmeraldEnd = Color(0xFF027858)

@Composable
fun AuthScreen(
  externalErrorMessage: String? = null,
  isOAuthLoading: Boolean = false,
  onClearExternalError: () -> Unit = {},
  onSignInSuccess: (email: String) -> Unit = {},
  onSignUpClick: () -> Unit = {},
  onGoogleClick: () -> Unit = {},
  onAppleClick: () -> Unit = {},
  onBack: () -> Unit = {},
) {
  val coroutineScope = rememberCoroutineScope()
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }
  var localErrorMessage by remember { mutableStateOf<String?>(null) }
  val displayedError = externalErrorMessage ?: localErrorMessage

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            NumoMintTop,
            NumoMintMid,
            NumoMintLight,
            NumoWhite
          )
        )
      )
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .navigationBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top row with optional back to Welcome button
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.8f))
            .border(1.dp, NumoBorder, RoundedCornerShape(12.dp))
            .clickable { onBack() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Welcome",
            tint = NumoDeepNavy,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Brand Logo & Hero Illustration
      Image(
        painter = painterResource(id = R.drawable.ic_numo_leaf),
        contentDescription = "Numo Brand Logo",
        modifier = Modifier
          .size(width = 68.dp, height = 54.dp)
          .testTag("brand_logo"),
        contentScale = ContentScale.Fit
      )

      Spacer(modifier = Modifier.height(10.dp))

      Image(
        painter = painterResource(id = R.drawable.wallet_hero),
        contentDescription = "Wallet and coins illustration",
        modifier = Modifier
          .fillMaxWidth()
          .height(145.dp)
          .padding(horizontal = 8.dp)
          .testTag("wallet_hero_image"),
        contentScale = ContentScale.Fit
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Card ("Welcome Back" Sheet)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 16.dp,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            ambientColor = Color(0x1A000000),
            spotColor = Color(0x26000000)
          ),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = NumoWhite
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 22.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Retain "Welcome Back" and "Sign in to continue your financial journey" alone
          Text(
            text = "Welcome Back",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDeepNavy,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.testTag("welcome_back_title")
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Sign in to continue your\nfinancial journey",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF334E68),
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("welcome_back_subtitle")
          )

          Spacer(modifier = Modifier.height(18.dp))

          // Email Input Field (Rounded pill)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(27.dp))
              .border(1.2.dp, NumoBorder, RoundedCornerShape(27.dp))
              .background(NumoWhite)
              .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.Email,
                contentDescription = "Email icon",
                tint = NumoPlaceholder,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Box(modifier = Modifier.weight(1f)) {
                if (email.isEmpty()) {
                  Text(
                    text = "Email address",
                    color = NumoPlaceholder,
                    fontSize = 15.sp
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
                  textStyle = TextStyle(
                    color = NumoDeepNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  cursorBrush = SolidColor(NumoEmeraldStart),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Password Input Field (Rounded pill)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .clip(RoundedCornerShape(27.dp))
              .border(1.2.dp, NumoBorder, RoundedCornerShape(27.dp))
              .background(NumoWhite)
              .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Password icon",
                tint = NumoPlaceholder,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Box(modifier = Modifier.weight(1f)) {
                if (password.isEmpty()) {
                  Text(
                    text = "Password",
                    color = NumoPlaceholder,
                    fontSize = 15.sp
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
                  visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                  textStyle = TextStyle(
                    color = NumoDeepNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  cursorBrush = SolidColor(NumoEmeraldStart),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
                )
              }
              Icon(
                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = "Toggle password visibility",
                tint = NumoPlaceholder,
                modifier = Modifier
                  .size(20.dp)
                  .clickable { passwordVisible = !passwordVisible }
                  .testTag("toggle_password_visibility")
              )
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Primary "Sign In →" Button with rich back shadow for clear visibility
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(56.dp)
              .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x55027858),
                spotColor = Color(0x80029766)
              )
              .clip(RoundedCornerShape(28.dp))
              .background(
                brush = Brush.verticalGradient(
                  colors = listOf(NumoEmeraldStart, NumoEmeraldEnd)
                )
              )
              .clickable(enabled = !isLoading) {
                // Strict validation: do not proceed to dashboard if inputs are missing
                if (email.trim().isEmpty() || password.isEmpty()) {
                  localErrorMessage = "Please enter both your email address and password."
                  return@clickable
                }

                isLoading = true
                localErrorMessage = null
                onClearExternalError()
                coroutineScope.launch {
                  val result = ClerkAuthRepository.signIn(email, password)
                  isLoading = false
                  if (result.success) {
                    onSignInSuccess(email.trim())
                  } else {
                    localErrorMessage = result.errorMessage ?: "Authentication failed with Clerk."
                  }
                }
              }
              .testTag("sign_in_button"),
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
                  text = "Sign In",
                  color = NumoWhite,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "→",
                  color = NumoWhite,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

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
              color = NumoBorder
            )
            Text(
              text = "Or",
              color = NumoSubText,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 14.dp)
            )
            HorizontalDivider(
              modifier = Modifier.weight(1f),
              thickness = 1.dp,
              color = NumoBorder
            )
          }

          Spacer(modifier = Modifier.height(18.dp))

          // Social Auth Buttons: Google & Apple
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // Google Button (Calls Clerk OAuth Google account picker)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .border(1.2.dp, NumoBorder, RoundedCornerShape(25.dp))
                .background(NumoWhite)
                .clickable(enabled = !isOAuthLoading) { onGoogleClick() }
                .testTag("google_auth_button"),
              contentAlignment = Alignment.Center
            ) {
              if (isOAuthLoading) {
                CircularProgressIndicator(
                  color = NumoEmeraldStart,
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp
                )
              } else {
                Image(
                  painter = painterResource(id = R.drawable.ic_google),
                  contentDescription = "Sign in with Google via Clerk",
                  modifier = Modifier.size(22.dp)
                )
              }
            }

            // Apple Button (Calls Clerk OAuth Apple)
            Box(
              modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .border(1.2.dp, NumoBorder, RoundedCornerShape(25.dp))
                .background(NumoWhite)
                .clickable(enabled = !isOAuthLoading) { onAppleClick() }
                .testTag("apple_auth_button"),
              contentAlignment = Alignment.Center
            ) {
              Image(
                painter = painterResource(id = R.drawable.ic_apple),
                contentDescription = "Sign in with Apple via Clerk",
                modifier = Modifier.size(22.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(22.dp))

          // Footer: Don't have an account? Sign Up
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSignUpClick() }
              .testTag("sign_up_link")
          ) {
            Text(
              text = "Don't have an account? ",
              color = Color(0xFF486581),
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Sign Up",
              color = NumoEmeraldStart,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
        }
      }
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
