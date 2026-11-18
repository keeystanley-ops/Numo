package com.example.ui.profile

import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.auth.BiometricAuthManager
import com.example.auth.BiometricCapability
import com.example.auth.ClerkAuthRepository
import com.example.auth.NumoSessionManager
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

private fun Context.findFragmentActivity(): FragmentActivity? {
  var current: Context? = this
  while (current is ContextWrapper) {
    if (current is FragmentActivity) return current
    current = current.baseContext
  }
  return current as? FragmentActivity
}

@Composable
fun SecurityPrivacyScreen(
  userId: String = "",
  userEmail: String,
  biometricEnabled: Boolean,
  twoFactorEnabled: Boolean,
  onBiometricChanged: (Boolean) -> Unit,
  onOpenTwoFactorSetup: () -> Unit,
  onTwoFactorSynced: (Boolean) -> Unit = {},
  onSignOut: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val coroutineScope = rememberCoroutineScope()

  var showPasswordDialog by remember { mutableStateOf(false) }
  var isSendingReset by remember { mutableStateOf(false) }
  var biometricCapability by remember { mutableStateOf(BiometricAuthManager.checkAvailability(context)) }

  LaunchedEffect(Unit) {
    val liveStatus = ClerkAuthRepository.fetchUserSecurityStatus()
    val localActive = NumoSessionManager.hasLocalTwoFactorEnabled(userId, userEmail)
    if (liveStatus.success) {
      onTwoFactorSynced(liveStatus.twoFactorEnabled || localActive)
    } else if (localActive && !twoFactorEnabled) {
      onTwoFactorSynced(true)
    }
  }

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        biometricCapability = BiometricAuthManager.checkAvailability(context)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  fun handleBiometricToggle(wantEnabled: Boolean) {
    if (!wantEnabled) {
      onBiometricChanged(false)
      Toast.makeText(context, "Biometric login turned off.", Toast.LENGTH_SHORT).show()
      return
    }
    val capability = BiometricAuthManager.checkAvailability(context)
    biometricCapability = capability
    if (capability != BiometricCapability.AVAILABLE) {
      Toast.makeText(
        context,
        BiometricAuthManager.availabilityMessage(capability),
        Toast.LENGTH_LONG
      ).show()
      return
    }
    val activity = context.findFragmentActivity()
    if (activity == null) {
      Toast.makeText(
        context,
        "Unable to open biometric prompt right now.",
        Toast.LENGTH_SHORT
      ).show()
      return
    }
    BiometricAuthManager.authenticate(
      activity = activity,
      title = "Enable Biometric Login",
      subtitle = "Verify your fingerprint or face unlock for $userEmail",
      negativeButtonText = "Cancel",
      onSuccess = {
        onBiometricChanged(true)
        Toast.makeText(context, "Biometric login enabled!", Toast.LENGTH_SHORT).show()
      },
      onError = { errMsg ->
        Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
      },
      onCancelled = {
        // Leave toggle OFF since the user cancelled biometric enrollment confirmation
      }
    )
  }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Security & Privacy",
        onBack = onBack,
        showBackLabel = false
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Section 1: Password Management
      Text(
        text = "Password Management",
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoPrimaryButton(
        text = "Change Password",
        onClick = { showPasswordDialog = true },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_change_password"),
        height = 50.dp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Section 2: Authentications
      Text(
        text = "2. Authentications",
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { handleBiometricToggle(!biometricEnabled) }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("row_biometric_login"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.CenterFocusWeak,
              contentDescription = "Biometric Login",
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Biometric Login",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = when {
                  biometricEnabled -> "Enabled • Locks after 15s away"
                  biometricCapability == BiometricCapability.NOT_ENROLLED ->
                    "No biometrics enrolled in device Settings"
                  biometricCapability == BiometricCapability.UNAVAILABLE ->
                    "Not available on this device"
                  else -> "Off • Tap to unlock with fingerprint or face"
                },
                fontSize = 11.5.sp,
                color = if (biometricEnabled) NumoDS.HeroTealMid else NumoDS.TextMuted
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            NumoPillToggle(
              checked = biometricEnabled,
              onCheckedChange = { handleBiometricToggle(it) },
              activeColorTop = NumoDS.CyanBright,
              activeColorBottom = NumoDS.HeroTealMid
            )
          }

          HorizontalDivider(
            color = NumoDS.CardShadowWarm.copy(alpha = 0.25f),
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onOpenTwoFactorSetup() }
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .testTag("row_two_factor_auth"),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = "Two-Factor Authentication",
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Two-Factor Authentication",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (twoFactorEnabled) {
                  "Authenticator App Active"
                } else {
                  "Tap to set up Authenticator App"
                },
                fontSize = 11.5.sp,
                color = if (twoFactorEnabled) NumoDS.SuccessGreen else NumoDS.TextMuted
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            NumoPillToggle(
              checked = twoFactorEnabled,
              onCheckedChange = { onOpenTwoFactorSetup() },
              activeColorTop = NumoDS.CyanBright,
              activeColorBottom = NumoDS.HeroTealMid
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Open Two-Factor Authentication Setup",
              tint = NumoDS.TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Section 3: Active Sessions
      Text(
        text = "3. Active Sessions",
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      NumoNeumorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.Smartphone,
            contentDescription = null,
            tint = NumoDS.HeroTealMid,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "This Device",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = NumoDS.TextPrimary
            )
            Text(
              text = "Active now • ${userEmail.ifBlank { "Signed in" }}",
              fontSize = 12.sp,
              color = NumoDS.HeroTealMid
            )
          }
          Text(
            text = "Log Out",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDS.Magenta,
            modifier = Modifier
              .clickable { onSignOut() }
              .padding(horizontal = 6.dp, vertical = 4.dp)
              .testTag("btn_security_logout")
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))
    }

    if (showPasswordDialog) {
      AlertDialog(
        onDismissRequest = { if (!isSendingReset) showPasswordDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Change Password",
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          Text(
            text = "We will send a 6-digit password reset verification code to $userEmail.",
            fontSize = 13.5.sp,
            color = NumoDS.TextSecondary
          )
        },
        confirmButton = {
          TextButton(
            enabled = !isSendingReset,
            onClick = {
              isSendingReset = true
              coroutineScope.launch {
                val res = ClerkAuthRepository.requestPasswordReset(userEmail)
                isSendingReset = false
                showPasswordDialog = false
                Toast.makeText(
                  context,
                  if (res.success) "Reset code sent to $userEmail" else res.message,
                  Toast.LENGTH_LONG
                ).show()
              }
            }
          ) {
            Text(
              text = if (isSendingReset) "Sending..." else "Send Code",
              fontWeight = FontWeight.Bold,
              color = NumoDS.Turquoise
            )
          }
        },
        dismissButton = {
          TextButton(
            enabled = !isSendingReset,
            onClick = { showPasswordDialog = false }
          ) {
            Text("Cancel", color = NumoDS.TextSecondary)
          }
        }
      )
    }
  }
}
