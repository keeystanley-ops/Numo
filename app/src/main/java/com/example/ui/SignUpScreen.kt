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
 * Screen 3: "Create Your Numo Account" (Sign Up)
 * Features:
 * - Top Navigation Bar: Back button to return to Sign In, and "Sign In >" button
 * - Header: "Create Your \n Numo Account" in Dark Forest Green (#1E5631), Numo leaf icon
 * - Frosted glass card with 26dp rounded corners:
 *   - "Join Us" label
 *   - "Sign up with Google" button
 *   - "Sign up with Apple" button
 *   - [First Name | Last Name] row
 *   - Email Address, Password, Confirm Password inputs
 *   - "Create Account" button
 *   - "Already have an account? Sign In >" button
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

        // 1. Top Navigation Bar: Back to Sign In on Left, Sign In button on Right
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Back Button to Sign In
          IconButton(
            onClick = { onNavigateToSignIn() },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, NumoElementsCorners, CircleShape)
              .testTag("btn_signup_back")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Sign In",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(20.dp)
            )
          }

          // Go to Sign In Button in Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, NumoElementsCorners, RoundedCornerShape(20.dp))
              .bouncyClickable { onNavigateToSignIn() }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("btn_header_to_signin")
          ) {
            Text(
              text = "Sign In",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDarkForestGreen
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Go to Sign In",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Title Row: "Create Your \n Numo Account" on Left, Leaf on Right
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column(
            modifier = Modifier.testTag("signup_header_title")
          ) {
            Text(
              text = "Create Your",
              fontSize = 24.sp,
              lineHeight = 28.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.SansSerif,
              color = NumoDarkForestGreen
            )
            Text(
              text = "Numo Account",
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
              .testTag("signup_top_leaf"),
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
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // "Join Us" Label
            Text(
              text = "Join Us",
              fontSize = 13.sp,
              fontWeight = FontWeight.Normal,
              fontFamily = FontFamily.SansSerif,
              color = NumoSlateGreen,
              modifier = Modifier.testTag("signup_join_us_label")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // "Sign up with Google" Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  onGoogleSignUp()
                }
                .testTag("btn_google_signup"),
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
                  text = "Sign up with Google",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF0F172A)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // "Sign up with Apple" Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  onAppleSignUp()
                }
                .testTag("btn_apple_signup"),
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
                  text = "Sign up with Apple",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF0F172A)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // [First Name | Last Name]
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // First Name Field
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(NumoInputBg)
                  .border(1.dp, NumoElementsCorners, RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                if (firstName.isEmpty()) {
                  Text(
                    text = "First Name",
                    fontSize = 13.sp,
                    color = NumoInputPlaceholder
                  )
                }
                BasicTextField(
                  value = firstName,
                  onValueChange = { firstName = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_signup_firstname"),
                  textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Normal
                  ),
                  singleLine = true,
                  cursorBrush = SolidColor(NumoDarkForestGreen)
                )
              }

              // Last Name Field
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(NumoInputBg)
                  .border(1.dp, NumoElementsCorners, RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                if (lastName.isEmpty()) {
                  Text(
                    text = "Last Name",
                    fontSize = 13.sp,
                    color = NumoInputPlaceholder
                  )
                }
                BasicTextField(
                  value = lastName,
                  onValueChange = { lastName = it },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_signup_lastname"),
                  textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Normal
                  ),
                  singleLine = true,
                  cursorBrush = SolidColor(NumoDarkForestGreen)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Email Address Field
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (email.isEmpty()) {
                Text(
                  text = "Email Address",
                  fontSize = 13.sp,
                  color = NumoInputPlaceholder
                )
              }
              BasicTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signup_email"),
                textStyle = TextStyle(
                  fontSize = 13.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Password Field
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(12.dp))
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
                  .testTag("input_signup_password"),
                textStyle = TextStyle(
                  fontSize = 13.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Confirm Password Field
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(NumoInputBg)
                .border(1.dp, NumoElementsCorners, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (confirmPassword.isEmpty()) {
                Text(
                  text = "Confirm Password",
                  fontSize = 13.sp,
                  color = NumoInputPlaceholder
                )
              }
              BasicTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_signup_confirm_password"),
                textStyle = TextStyle(
                  fontSize = 13.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Create Account" Action Button
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, NumoActionButtonBorder, RoundedCornerShape(14.dp))
                .bouncyClickable(enabled = !isLoading && !isOAuthLoading) {
                  val fullName = "${firstName.trim()} ${lastName.trim()}".trim()
                  if (email.isBlank() || password.isBlank()) {
                    localErrorMessage = "Please fill in all required fields."
                    return@bouncyClickable
                  }
                  if (password.length < 8) {
                    localErrorMessage = "Password must be at least 8 characters long."
                    return@bouncyClickable
                  }
                  if (password != confirmPassword) {
                    localErrorMessage = "Passwords do not match."
                    return@bouncyClickable
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
                .testTag("btn_signup_submit"),
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
                    text = "Create Account",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = NumoDarkForestGreen
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. "Already have an account? Sign In" Action Button inside card
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .bouncyClickable { onNavigateToSignIn() }
                .padding(vertical = 6.dp),
              horizontalArrangement = Arrangement.Center,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Already have an account? ",
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = NumoSlateGreen
              )
              Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDarkForestGreen,
                modifier = Modifier.testTag("link_to_signin")
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go to Sign In",
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
