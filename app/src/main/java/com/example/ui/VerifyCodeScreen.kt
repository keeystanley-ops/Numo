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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.auth.ClerkAuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerifyCodeScreen(
  signUpId: String,
  userEmail: String,
  onVerificationSuccess: (sessionId: String) -> Unit,
  onBack: () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  var code by remember { mutableStateOf("") }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var resendCountdown by remember { mutableIntStateOf(45) }

  LaunchedEffect(resendCountdown) {
    if (resendCountdown > 0) {
      delay(1000)
      resendCountdown--
    }
  }

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
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Back Button
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White)
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
              .clickable { onBack() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF001A49),
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Numo Brand Logo
        Image(
          painter = painterResource(id = R.drawable.ic_numo_leaf),
          contentDescription = "Numo Brand Logo",
          modifier = Modifier
            .size(width = 68.dp, height = 54.dp),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Verify Your Email",
          fontSize = 28.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.SansSerif,
          color = Color(0xFF001A49)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Enter the 6-digit verification code sent to\n$userEmail",
          fontSize = 15.sp,
          fontWeight = FontWeight.Medium,
          color = Color(0xFF334E68),
          lineHeight = 22.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Error message if any
        if (errorMessage != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFFFEEEE))
              .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(14.dp))
              .padding(14.dp)
          ) {
            Text(
              text = errorMessage ?: "",
              color = Color(0xFFB91C1C),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              textAlign = TextAlign.Center,
              modifier = Modifier.fillMaxWidth()
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
        }

        // 6-digit OTP code boxes
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          // Hidden TextField receiving keystrokes
          BasicTextField(
            value = code,
            onValueChange = {
              if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                code = it
                errorMessage = null
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("otp_code_hidden_input")
          )

          // Visible digit boxes
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            for (i in 0 until 6) {
              val digit = if (i < code.length) code[i].toString() else ""
              val isFocused = (i == code.length)

              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(14.dp))
                  .background(Color.White)
                  .border(
                    width = if (isFocused) 2.dp else 1.2.dp,
                    color = if (isFocused) Color(0xFF029766) else Color(0xFFCBD5E1),
                    shape = RoundedCornerShape(14.dp)
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = digit,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF001A49)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Verify & Continue Button
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
              if (code.length < 6) {
                errorMessage = "Please enter the full 6-digit verification code."
                return@clickable
              }
              isLoading = true
              errorMessage = null
              coroutineScope.launch {
                val res = ClerkAuthRepository.attemptVerification(signUpId, code)
                isLoading = false
                if (res.success) {
                  onVerificationSuccess(res.sessionId ?: "session_verified")
                } else {
                  errorMessage = res.errorMessage ?: "Verification failed. The code may be incorrect or expired."
                }
              }
            }
            .testTag("verify_continue_button"),
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
                text = "Verify & Continue",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Resend Code action
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Didn't receive the code? ",
            color = Color(0xFF64748B),
            fontSize = 14.sp
          )
          if (resendCountdown > 0) {
            Text(
              text = "Resend in ${resendCountdown}s",
              color = Color(0xFF94A3B8),
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          } else {
            Text(
              text = "Resend Code",
              color = Color(0xFF029766),
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable {
                coroutineScope.launch {
                  resendCountdown = 45
                  ClerkAuthRepository.resendVerificationCode(signUpId)
                }
              }
            )
          }
        }
      }
    }
  }
}
