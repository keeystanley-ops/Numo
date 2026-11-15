package com.example.ui.profile

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ClerkAuthRepository
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import kotlinx.coroutines.launch

@Composable
fun SecurityPrivacyScreen(
  userEmail: String,
  onSignOut: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var biometricEnabled by remember { mutableStateOf(true) }
  var twoFactorEnabled by remember { mutableStateOf(false) }
  var showPasswordDialog by remember { mutableStateOf(false) }
  var isSendingReset by remember { mutableStateOf(false) }

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
        modifier = Modifier.fillMaxWidth(),
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
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.CenterFocusWeak,
              contentDescription = null,
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
              text = "Biometric Login",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextPrimary,
              modifier = Modifier.weight(1f)
            )
            NumoPillToggle(
              checked = biometricEnabled,
              onCheckedChange = { biometricEnabled = it },
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
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Outlined.Lock,
              contentDescription = null,
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
              text = "Two-Factor\nAuthentication",
              fontSize = 14.5.sp,
              lineHeight = 19.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDS.TextPrimary,
              modifier = Modifier.weight(1f)
            )
            NumoPillToggle(
              checked = twoFactorEnabled,
              onCheckedChange = { twoFactorEnabled = it },
              activeColorTop = NumoDS.CyanBright,
              activeColorBottom = NumoDS.HeroTealMid
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
