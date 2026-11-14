package com.example.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

  NumoScreenBackground(modifier = Modifier.testTag("verify_code_screen")) {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Back Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NumoBackCircleButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_verify_back")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3D Envelope illustration
      Numo3DEnvelopeLockIllustration(
        modifier = Modifier.size(144.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Verify Your Email",
        fontSize = 27.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.SansSerif,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Enter the 6-digit verification code sent to\n$userEmail",
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Error message if any
      if (errorMessage != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF1F2))
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
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

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
          for (i in 0 until 6) {
            val digit = if (i < code.length) code[i].toString() else ""
            val isFocused = (i == code.length)
            val boxShape = RoundedCornerShape(16.dp)

            Box(
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .shadow(
                  elevation = if (isFocused) 8.dp else 4.dp,
                  shape = boxShape,
                  ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.25f)
                )
                .clip(boxShape)
                .background(
                  Brush.verticalGradient(
                    colors = listOf(Color(0xFFFAF0E4), Color(0xFFEEDCC8))
                  )
                )
                .border(
                  width = if (isFocused) 1.8.dp else 1.dp,
                  color = if (isFocused) NumoDS.Turquoise else Color(0xFFD8C5B2),
                  shape = boxShape
                ),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = digit,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Verify & Continue Button
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
          text = "Verify & Continue",
          onClick = {
            if (code.length < 6) {
              errorMessage = "Please enter the full 6-digit verification code."
              return@NumoPrimaryButton
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
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("verify_continue_button"),
          height = 54.dp
        )
      }

      Spacer(modifier = Modifier.height(22.dp))

      // Resend Code action
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Didn't receive the code? ",
          color = NumoDS.TextSecondary,
          fontSize = 14.sp
        )
        if (resendCountdown > 0) {
          Text(
            text = "Resend in ${resendCountdown}s",
            color = NumoDS.TextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
        } else {
          Text(
            text = "Resend Code",
            color = NumoDS.Turquoise,
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

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
