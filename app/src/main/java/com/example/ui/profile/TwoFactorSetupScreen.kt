package com.example.ui.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ClerkAuthRepository
import com.example.auth.NumoSessionManager
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch

private enum class TwoFactorUiStage {
  LOADING,
  SETUP_TOTP,
  BACKUP_CODES,
  ACTIVE_ENABLED,
  ERROR_STATE
}

@Composable
fun TwoFactorSetupScreen(
  userId: String,
  userEmail: String,
  isCurrentlyEnabled: Boolean,
  onTwoFactorStatusChanged: (Boolean) -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val coroutineScope = rememberCoroutineScope()

  BackHandler { onBack() }

  var stage by remember {
    mutableStateOf(
      if (isCurrentlyEnabled) TwoFactorUiStage.ACTIVE_ENABLED else TwoFactorUiStage.LOADING
    )
  }
  var totpSecret by remember { mutableStateOf("") }
  var totpUri by remember { mutableStateOf("") }
  var backupCodes by remember { mutableStateOf<List<String>>(emptyList()) }
  var verificationCode by remember { mutableStateOf("") }
  var statusError by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }
  var showDisableDialog by remember { mutableStateOf(false) }

  suspend fun startEnrollment() {
    stage = TwoFactorUiStage.LOADING
    statusError = null
    val status = ClerkAuthRepository.fetchUserSecurityStatus()
    val localActive = NumoSessionManager.hasLocalTwoFactorEnabled(userId, userEmail)
    if ((status.success && status.twoFactorEnabled) || localActive) {
      NumoSessionManager.setTwoFactorEnabledForUser(userId, userEmail, true)
      onTwoFactorStatusChanged(true)
      stage = TwoFactorUiStage.ACTIVE_ENABLED
      return
    }
    val created = ClerkAuthRepository.createTotp(userId = userId, email = userEmail)
    if (created.success) {
      totpSecret = created.secret
      totpUri = created.uri.ifBlank {
        "otpauth://totp/Numo:${Uri.encode(userEmail)}?secret=${created.secret}&issuer=Numo"
      }
      if (created.backupCodes.isNotEmpty()) {
        backupCodes = created.backupCodes
      }
      stage = TwoFactorUiStage.SETUP_TOTP
    } else {
      statusError = created.errorMessage
        ?: "Could not start authenticator setup. Please try again."
      stage = TwoFactorUiStage.ERROR_STATE
    }
  }

  LaunchedEffect(Unit) {
    val liveStatus = ClerkAuthRepository.fetchUserSecurityStatus()
    val localActive = NumoSessionManager.hasLocalTwoFactorEnabled(userId, userEmail)
    if ((liveStatus.success && liveStatus.twoFactorEnabled) || localActive) {
      NumoSessionManager.setTwoFactorEnabledForUser(userId, userEmail, true)
      onTwoFactorStatusChanged(true)
      stage = TwoFactorUiStage.ACTIVE_ENABLED
    } else if (!isCurrentlyEnabled) {
      startEnrollment()
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
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Two-Factor Auth",
        onBack = onBack,
        showBackLabel = false
      )

      Spacer(modifier = Modifier.height(14.dp))

      when (stage) {
        TwoFactorUiStage.LOADING -> {
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              CircularProgressIndicator(
                color = NumoDS.HeroTealMid,
                modifier = Modifier.size(34.dp),
                strokeWidth = 3.dp
              )
              Spacer(modifier = Modifier.height(16.dp))
              Text(
                text = "Preparing Authenticator Setup...",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary
              )
            }
          }
        }

        TwoFactorUiStage.ERROR_STATE -> {
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = null,
                tint = NumoDS.ExpenseRed,
                modifier = Modifier.size(38.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "2FA Setup Unavailable",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = statusError ?: "Unable to connect to authentication service.",
                fontSize = 13.sp,
                color = NumoDS.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
              )
              Spacer(modifier = Modifier.height(18.dp))
              NumoPrimaryButton(
                text = "Try Again",
                onClick = {
                  coroutineScope.launch { startEnrollment() }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_2fa_retry"),
                height = 48.dp
              )
            }
          }
        }

        TwoFactorUiStage.SETUP_TOTP -> {
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.QrCode2,
                  contentDescription = null,
                  tint = NumoDS.HeroTealMid,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Scan with Authenticator App",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.TextPrimary
                )
              }

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = "Scan this QR code in Google Authenticator, 1Password, or Authy — or copy the setup key below.",
                fontSize = 12.5.sp,
                color = NumoDS.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
              )

              Spacer(modifier = Modifier.height(16.dp))

              // White high-contrast QR container
              Box(
                modifier = Modifier
                  .size(196.dp)
                  .clip(RoundedCornerShape(18.dp))
                  .background(Color.White)
                  .border(1.5.dp, Color(0xFFE7D7C9), RoundedCornerShape(18.dp))
                  .padding(14.dp),
                contentAlignment = Alignment.Center
              ) {
                QrCodeView(
                  data = totpUri,
                  modifier = Modifier.fillMaxSize()
                )
              }

              Spacer(modifier = Modifier.height(16.dp))

              // Copyable Manual Secret Key
              if (totpSecret.isNotBlank()) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF3E4D7))
                    .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .clickable {
                      clipboardManager.setText(AnnotatedString(totpSecret))
                      Toast.makeText(context, "Setup key copied!", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
                    .testTag("btn_copy_totp_secret")
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "Manual Setup Key",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NumoDS.TextMuted
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = totpSecret,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NumoDS.TextPrimary
                      )
                    }
                    Icon(
                      imageVector = Icons.Outlined.ContentCopy,
                      contentDescription = "Copy secret key",
                      tint = NumoDS.HeroTealMid,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Open installed authenticator app via otpauth:// deep link
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .clip(RoundedCornerShape(50))
                  .background(Color.White.copy(alpha = 0.72f))
                  .border(1.dp, NumoDS.HeroTealMid.copy(alpha = 0.35f), RoundedCornerShape(50))
                  .clickable {
                    runCatching {
                      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(totpUri)))
                    }.onFailure {
                      clipboardManager.setText(AnnotatedString(totpSecret))
                      Toast.makeText(
                        context,
                        "No authenticator app link handler found — setup key copied instead.",
                        Toast.LENGTH_LONG
                      ).show()
                    }
                  }
                  .testTag("btn_open_authenticator_app"),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = NumoDS.HeroTealMid,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Open Authenticator App",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NumoDS.HeroTealMid
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = "Verify Code",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = NumoDS.TextPrimary
          )

          Spacer(modifier = Modifier.height(8.dp))

          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
            ) {
              Text(
                text = "Enter the 6-digit code shown in your authenticator app",
                fontSize = 12.5.sp,
                color = NumoDS.TextSecondary
              )
              Spacer(modifier = Modifier.height(10.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(50.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0xFFF1E0D2))
                  .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                  .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
              ) {
                if (verificationCode.isEmpty()) {
                  Text(
                    text = "000000",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NumoDS.TextMuted
                  )
                }
                BasicTextField(
                  value = verificationCode,
                  onValueChange = { input ->
                    val digits = input.filter { it.isDigit() }.take(6)
                    verificationCode = digits
                    statusError = null
                  },
                  singleLine = true,
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  textStyle = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 4.sp,
                    color = NumoDS.TextPrimary
                  ),
                  cursorBrush = SolidColor(NumoDS.HeroTealMid),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_2fa_setup_code")
                )
              }

              if (!statusError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = statusError!!,
                  fontSize = 12.sp,
                  color = NumoDS.ExpenseRed,
                  fontWeight = FontWeight.Medium
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              NumoPrimaryButton(
                text = if (isSubmitting) "Verifying..." else "Verify & Activate 2FA",
                onClick = {
                  if (isSubmitting) return@NumoPrimaryButton
                  if (verificationCode.length < 6) {
                    statusError = "Please enter the 6-digit code from your authenticator app."
                    return@NumoPrimaryButton
                  }
                  isSubmitting = true
                  statusError = null
                  coroutineScope.launch {
                    val verifyRes = ClerkAuthRepository.verifyTotpEnrollment(
                      code = verificationCode,
                      userId = userId,
                      email = userEmail
                    )
                    isSubmitting = false
                    if (verifyRes.success) {
                      if (verifyRes.backupCodes.isNotEmpty()) {
                        backupCodes = verifyRes.backupCodes
                      }
                      NumoSessionManager.setTwoFactorEnabledForUser(userId, userEmail, true)
                      onTwoFactorStatusChanged(true)
                      Toast.makeText(
                        context,
                        "Two-Factor Authentication activated!",
                        Toast.LENGTH_SHORT
                      ).show()
                      stage = if (backupCodes.isNotEmpty()) {
                        TwoFactorUiStage.BACKUP_CODES
                      } else {
                        TwoFactorUiStage.ACTIVE_ENABLED
                      }
                    } else {
                      statusError = verifyRes.errorMessage
                        ?: "Invalid verification code. Please check your authenticator app and try again."
                    }
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_verify_activate_2fa"),
                height = 50.dp
              )
            }
          }
        }

        TwoFactorUiStage.BACKUP_CODES -> {
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(54.dp)
                  .clip(CircleShape)
                  .background(NumoDS.MintGlassLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.VerifiedUser,
                  contentDescription = null,
                  tint = NumoDS.HeroTealMid,
                  modifier = Modifier.size(28.dp)
                )
              }

              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Save Your Backup Codes",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Store these one-time recovery codes safely in case you lose access to your authenticator app.",
                fontSize = 12.5.sp,
                color = NumoDS.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
              )

              Spacer(modifier = Modifier.height(16.dp))

              // 2-column grid of backup codes
              val chunked = backupCodes.chunked(2)
              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                chunked.forEach { pair ->
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    pair.forEach { code ->
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .clip(RoundedCornerShape(12.dp))
                          .background(Color(0xFFF3E4D7))
                          .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                          .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        Text(
                          text = code,
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          fontFamily = FontFamily.Monospace,
                          color = NumoDS.TextPrimary
                        )
                      }
                    }
                    if (pair.size == 1) {
                      Spacer(modifier = Modifier.weight(1f))
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .clip(RoundedCornerShape(50))
                  .background(Color.White.copy(alpha = 0.75f))
                  .border(1.dp, NumoDS.HeroTealMid.copy(alpha = 0.35f), RoundedCornerShape(50))
                  .clickable {
                    clipboardManager.setText(AnnotatedString(backupCodes.joinToString("\n")))
                    Toast.makeText(context, "All backup codes copied!", Toast.LENGTH_SHORT).show()
                  }
                  .testTag("btn_copy_all_backup_codes"),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = NumoDS.HeroTealMid,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Copy All Backup Codes",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NumoDS.HeroTealMid
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              NumoPrimaryButton(
                text = "Done",
                onClick = onBack,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("btn_2fa_backup_done"),
                height = 50.dp
              )
            }
          }
        }

        TwoFactorUiStage.ACTIVE_ENABLED -> {
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(58.dp)
                  .clip(CircleShape)
                  .background(NumoDS.MintGlassLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Outlined.CheckCircle,
                  contentDescription = null,
                  tint = NumoDS.SuccessGreen,
                  modifier = Modifier.size(32.dp)
                )
              }

              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Authenticator App Active",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Your Numo account ($userEmail) requires a 6-digit authenticator or backup code whenever you sign in on a new session.",
                fontSize = 13.sp,
                color = NumoDS.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
              )

              Spacer(modifier = Modifier.height(20.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .clip(RoundedCornerShape(50))
                  .background(NumoDS.ExpenseRed.copy(alpha = 0.1f))
                  .border(1.dp, NumoDS.ExpenseRed.copy(alpha = 0.35f), RoundedCornerShape(50))
                  .clickable(enabled = !isSubmitting) {
                    showDisableDialog = true
                  }
                  .testTag("btn_disable_2fa"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (isSubmitting) "Disabling..." else "Disable Two-Factor Authentication",
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.ExpenseRed
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }

    if (showDisableDialog) {
      AlertDialog(
        onDismissRequest = { if (!isSubmitting) showDisableDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Disable Two-Factor Auth?",
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          Text(
            text = "Removing your authenticator app lowers account protection. You can re-enable 2FA anytime from Security & Privacy or Settings.",
            fontSize = 13.5.sp,
            color = NumoDS.TextSecondary
          )
        },
        confirmButton = {
          TextButton(
            enabled = !isSubmitting,
            onClick = {
              isSubmitting = true
              coroutineScope.launch {
                val (ok, err) = ClerkAuthRepository.disableTotp(
                  userId = userId,
                  email = userEmail
                )
                isSubmitting = false
                showDisableDialog = false
                if (ok) {
                  NumoSessionManager.setTwoFactorEnabledForUser(userId, userEmail, false)
                  onTwoFactorStatusChanged(false)
                  Toast.makeText(context, "Two-Factor Authentication disabled.", Toast.LENGTH_SHORT).show()
                  onBack()
                } else {
                  Toast.makeText(
                    context,
                    err ?: "Could not disable 2FA.",
                    Toast.LENGTH_LONG
                  ).show()
                }
              }
            }
          ) {
            Text(
              text = if (isSubmitting) "Disabling..." else "Disable",
              fontWeight = FontWeight.Bold,
              color = NumoDS.ExpenseRed
            )
          }
        },
        dismissButton = {
          TextButton(
            enabled = !isSubmitting,
            onClick = { showDisableDialog = false }
          ) {
            Text("Cancel", color = NumoDS.TextSecondary)
          }
        }
      )
    }
  }
}

@Composable
private fun QrCodeView(
  data: String,
  modifier: Modifier = Modifier
) {
  val qrBitmap: ImageBitmap? = remember(data) {
    runCatching {
      val size = 512
      val hints = mapOf(EncodeHintType.MARGIN to 1)
      val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
      val pixels = IntArray(size * size)
      val black = android.graphics.Color.parseColor("#0B0B0B")
      val white = android.graphics.Color.WHITE
      for (y in 0 until size) {
        val offset = y * size
        for (x in 0 until size) {
          pixels[offset + x] = if (bitMatrix.get(x, y)) black else white
        }
      }
      android.graphics.Bitmap.createBitmap(pixels, size, size, android.graphics.Bitmap.Config.ARGB_8888)
        .asImageBitmap()
    }.getOrNull()
  }

  if (qrBitmap == null) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
      Text(
        text = "QR unavailable",
        fontSize = 12.sp,
        color = NumoDS.TextMuted
      )
    }
    return
  }

  androidx.compose.foundation.Image(
    bitmap = qrBitmap,
    contentDescription = "Authenticator QR Code",
    modifier = modifier
  )
}
