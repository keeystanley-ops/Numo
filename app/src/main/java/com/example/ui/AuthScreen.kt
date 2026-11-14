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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
 * Screen 1: "Welcome Back to Numo" (Sign In)
 * Features:
 * - Top Navigation Bar: Back button to return to Splash, and "Sign Up >" button to go to registration
 * - Header: "Welcome Back to \n Numo" in Dark Forest Green (#1E5631), Numo leaf icon
 * - Frosted glass card with 26dp rounded corners:
 *   - "Sign in with Google" button
 *   - "Sign in with Apple" button
 *   - "Use Email" divider
 *   - "Email Address" field with active forest green outline & placeholder
 *   - "Password" field
 *   - "Forgot Password?" link
 *   - "Sign In" button
 *   - "Don't have an account? Sign Up >" direct button
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
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  var isLoading by remember { mutableStateOf(false) }
  var localErrorMessage by remember { mutableStateOf<String?>(null) }
  val displayedError = externalErrorMessage ?: localErrorMessage

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

        // 1. Top Navigation Bar: Back to Splash on Left
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
          horizontalArrangement = Arrangement.Start,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Back Button to Splash
          IconButton(
            onClick = { onBack() },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, NumoElementsCorners, CircleShape)
              .testTag("btn_back_to_splash")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Splash",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Title Row: "Welcome Back to \n Numo" on Left, Leaf on Right
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(
            modifier = Modifier.testTag("signin_header_title")
          ) {
            Text(
              text = "Welcome Back to",
              fontSize = 24.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.SansSerif,
              color = NumoDarkForestGreen
            )
            Text(
              text = "Numo",
              fontSize = 24.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.SansSerif,
              color = NumoDarkForestGreen
            )
          }

          Image(
            painter = painterResource(id = R.drawable.ic_numo_leaf_transparent),
            contentDescription = "Numo Brand Leaf",
            modifier = Modifier
              .size(36.dp)
              .testTag("signin_top_leaf"),
            contentScale = ContentScale.Fit
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Optional Error Banner
        if (!displayedError.isNullOrBlank()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFFFECEC))
              .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = displayedError,
                fontSize = 12.sp,
                color = Color(0xFFB91C1C),
                modifier = Modifier.weight(1f)
              )
              Text(
                text = "✕",
                fontSize = 13.sp,
                color = Color(0xFFB91C1C),
                modifier = Modifier
                  .clickable {
                    localErrorMessage = null
                    onClearExternalError()
                  }
                  .padding(start = 8.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. MINT GLASSMORPHISM CARD
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
            .padding(18.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // "Sign in with Google" Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  onGoogleClick()
                }
                .testTag("btn_google_signin"),
              color = Color.White.copy(alpha = 0.92f),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.ic_google),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Sign in with Google",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF0F172A)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Sign in with Apple" Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  onAppleClick()
                }
                .testTag("btn_apple_signin"),
              color = Color.White.copy(alpha = 0.92f),
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Image(
                  painter = painterResource(id = R.drawable.ic_apple),
                  contentDescription = "Apple Logo",
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Sign in with Apple",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF0F172A)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Use Email" Divider
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFC7E2D9),
                thickness = 1.dp
              )
              Text(
                text = "Use Email",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = NumoSlateGreen,
                modifier = Modifier.padding(horizontal = 12.dp)
              )
              HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = Color(0xFFC7E2D9),
                thickness = 1.dp
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // "Email Address" Label
            Text(
              text = "Email Address",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Email Address Input Field
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(
                  width = 1.5.dp,
                  color = NumoDarkForestGreen,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (email.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "| ",
                    fontSize = 14.sp,
                    color = NumoDarkForestGreen,
                    fontWeight = FontWeight.Light
                  )
                  Text(
                    text = "Email Address",
                    fontSize = 13.sp,
                    color = NumoInputPlaceholder
                  )
                }
              }
              BasicTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signin_email"),
                textStyle = TextStyle(
                  fontSize = 13.5.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // "Password" Label
            Text(
              text = "Password",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Password Input Field
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(
                  width = 1.dp,
                  color = NumoElementsCorners,
                  shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (password.isEmpty()) {
                Text(
                  text = "Password",
                  fontSize = 13.sp,
                  color = NumoInputPlaceholder
                )
              }
              BasicTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signin_password"),
                textStyle = TextStyle(
                  fontSize = 13.5.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // "Forgot Password?" Link
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              Text(
                text = "Forgot Password?",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoSlateGreen,
                modifier = Modifier
                  .clickable {
                    onForgotPasswordClick(email.trim())
                  }
                  .testTag("link_forgot_password")
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Sign In" Action Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoActionButtonBorder, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  if (email.isBlank() || password.isBlank()) {
                    localErrorMessage = "Please enter both your email address and password."
                    return@bouncyClickable
                  }

                  isLoading = true
                  localErrorMessage = null
                  onClearExternalError()

                  coroutineScope.launch {
                    val result = ClerkAuthRepository.signIn(
                      email = email.trim(),
                      pass = password
                    )
                    isLoading = false

                    if (result.success) {
                      onSignInSuccess(email.trim())
                    } else {
                      localErrorMessage = result.errorMessage ?: "Invalid email or password."
                    }
                  }
                }
                .testTag("btn_signin_submit"),
              color = NumoActionButtonBg,
              shape = RoundedCornerShape(14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                if (isLoading || isOAuthLoading) {
                  CircularProgressIndicator(
                    color = NumoDarkForestGreen,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                  )
                } else {
                  Text(
                    text = "Sign In",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDarkForestGreen
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. "Don't have an account? Sign Up" Action Button inside card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .bouncyClickable { onSignUpClick() }
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Don't have an account? ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = NumoSlateGreen
              )
              Text(
                text = "Sign Up",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDarkForestGreen,
                modifier = Modifier.testTag("link_to_signup")
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next to Sign Up",
                tint = NumoDarkForestGreen,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
