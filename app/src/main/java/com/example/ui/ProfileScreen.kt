package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.animation.TopScrollProgressBar
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.staggeredEntrance

/**
 * Profile Screen matching Screen 12 of the user reference mockup:
 * - Profile header
 * - Avatar with emerald ring border
 * - Name: Ayesha Khan
 * - Email: ayesha.khan@gmail.com
 * - Numo Premium Lifetime Member hero banner
 * - Menu Items:
 *   1. Account Settings
 *   2. Notifications
 *   3. Security & Privacy
 *   4. Help & Support
 *   5. About Numo
 * - Interactive modal dialogs for each setting & Sign Out
 */
@Composable
fun ProfileScreen(
  userName: String = "Ayesha Khan",
  userEmail: String = "ayesha.khan@gmail.com",
  currency: String = "KES",
  onSignOut: () -> Unit = {}
) {
  var activeModal by remember { mutableStateOf<String?>(null) }
  var showSignOutConfirm by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFFF8FAF9))
  ) {
    TopScrollProgressBar(scrollState = scrollState)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Title
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Profile",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF0F172A),
          modifier = Modifier.testTag("profile_title")
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // USER AVATAR & INFO (Screen 12)
      Box(
        modifier = Modifier
          .size(92.dp)
          .staggeredEntrance(delayMs = 40)
          .clip(CircleShape)
          .border(3.dp, Color(0xFF02844E), CircleShape)
          .padding(3.dp)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_user_avatar),
          contentDescription = "User Avatar",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = userName,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A),
        modifier = Modifier.testTag("txt_profile_name")
      )

      Spacer(modifier = Modifier.height(3.dp))

      Text(
        text = userEmail,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B),
        modifier = Modifier.testTag("txt_profile_email")
      )

      Spacer(modifier = Modifier.height(24.dp))

      // NUMO PREMIUM CARD
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 80)
          .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0xFF02844E).copy(alpha = 0.3f))
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFF016339),
                Color(0xFF02844E),
                Color(0xFF059669)
              )
            )
          )
          .bouncyClickable { activeModal = "Premium" }
          .padding(horizontal = 20.dp, vertical = 18.dp)
          .testTag("card_numo_premium")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Star,
              contentDescription = "Crown",
              tint = Color(0xFFFBBF24),
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Numo Premium",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Lifetime Member",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color.White.copy(alpha = 0.85f)
            )
          }

          Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "Arrow",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // SETTINGS MENU GROUP CARD (Screen 12)
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 120),
        color = Color.White,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // 1. Account Settings
          ProfileMenuItem(
            icon = Icons.Outlined.Settings,
            title = "Account Settings",
            onClick = { activeModal = "Account Settings" },
            testTag = "menu_account_settings"
          )

          HorizontalDivider(
            color = Color(0xFFF1F5F9),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          // 2. Notifications
          ProfileMenuItem(
            icon = Icons.Outlined.Notifications,
            title = "Notifications",
            onClick = { activeModal = "Notifications" },
            testTag = "menu_notifications"
          )

          HorizontalDivider(
            color = Color(0xFFF1F5F9),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          // 3. Security & Privacy
          ProfileMenuItem(
            icon = Icons.Outlined.Shield,
            title = "Security & Privacy",
            onClick = { activeModal = "Security & Privacy" },
            testTag = "menu_security"
          )

          HorizontalDivider(
            color = Color(0xFFF1F5F9),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          // 4. Help & Support
          ProfileMenuItem(
            icon = Icons.Outlined.HelpOutline,
            title = "Help & Support",
            onClick = { activeModal = "Help & Support" },
            testTag = "menu_help"
          )

          HorizontalDivider(
            color = Color(0xFFF1F5F9),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 16.dp)
          )

          // 5. About Numo
          ProfileMenuItem(
            icon = Icons.Outlined.Info,
            title = "About Numo",
            onClick = { activeModal = "About Numo" },
            testTag = "menu_about"
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Sign Out Button
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .staggeredEntrance(delayMs = 160)
          .clip(RoundedCornerShape(16.dp))
          .bouncyClickable { showSignOutConfirm = true }
          .testTag("btn_profile_sign_out"),
        color = Color(0xFFFFF1F2),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Outlined.PowerSettingsNew,
            contentDescription = "Sign Out",
            tint = Color(0xFFE11D48),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Sign Out",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE11D48)
          )
        }
      }

      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  // MODALS
  when (activeModal) {
    "Premium" -> PremiumModal(onDismiss = { activeModal = null })
    "Account Settings" -> AccountSettingsModal(currency = currency, onDismiss = { activeModal = null })
    "Notifications" -> NotificationsModal(onDismiss = { activeModal = null })
    "Security & Privacy" -> SecurityModal(onDismiss = { activeModal = null })
    "Help & Support" -> HelpModal(onDismiss = { activeModal = null })
    "About Numo" -> AboutModal(onDismiss = { activeModal = null })
  }

  if (showSignOutConfirm) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirm = false },
      title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
      text = { Text("Are you sure you want to sign out of your Numo account?") },
      confirmButton = {
        Button(
          onClick = {
            showSignOutConfirm = false
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
        ) {
          Text("Sign Out")
        }
      },
      dismissButton = {
        Button(
          onClick = { showSignOutConfirm = false },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF64748B))
        ) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun ProfileMenuItem(
  icon: ImageVector,
  title: String,
  onClick: () -> Unit,
  testTag: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .bouncyClickable { onClick() }
      .padding(horizontal = 18.dp, vertical = 16.dp)
      .testTag(testTag),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = title,
      tint = Color(0xFF334155),
      modifier = Modifier.size(22.dp)
    )

    Spacer(modifier = Modifier.width(16.dp))

    Text(
      text = title,
      fontSize = 15.sp,
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFF0F172A),
      modifier = Modifier.weight(1f)
    )

    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = "Open",
      tint = Color(0xFF94A3B8),
      modifier = Modifier.size(20.dp)
    )
  }
}

// Sub-modals for Profile
@Composable
fun PremiumModal(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Numo Premium Tier", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          "You are an active Lifetime Member! Enjoy:\n• Unlimited AI-powered wallet integrations\n• Real-time financial calendar alerts\n• Multi-currency global portfolio sync\n• Priority 24/7 dedicated wealth advisory",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Close")
        }
      }
    }
  }
}

@Composable
fun AccountSettingsModal(currency: String, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text("Account Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("• Primary Currency: $currency\n• Region: Global / East Africa\n• Linked Accounts: 4 Wallets active\n• Backup Status: Real-time Cloud Synced", fontSize = 13.sp, color = Color(0xFF475569), lineHeight = 22.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

@Composable
fun NotificationsModal(onDismiss: () -> Unit) {
  var pushAlerts by remember { mutableStateOf(true) }
  var billReminders by remember { mutableStateOf(true) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text("Notification Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Push Overspend Alerts", fontSize = 14.sp)
          Switch(
            checked = pushAlerts,
            onCheckedChange = { pushAlerts = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF02844E))
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Upcoming Bill Reminders", fontSize = 14.sp)
          Switch(
            checked = billReminders,
            onCheckedChange = { billReminders = it },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF02844E))
          )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Save Preferences")
        }
      }
    }
  }
}

@Composable
fun SecurityModal(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text("Security & Privacy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          "• Biometric App Lock: Enabled\n• 256-bit AES End-to-End Encryption\n• Gemini AI Zero Data Retention\n• Session Timeout: 15 minutes",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("OK")
        }
      }
    }
  }
}

@Composable
fun HelpModal(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Text("Help & Support", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          "Have questions or need assistance with your accounts?\n\n• Email: support@numo.app\n• Live Chat: Available 24/7\n• Community FAQ: numo.app/help",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 20.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Got it")
        }
      }
    }
  }
}

@Composable
fun AboutModal(onDismiss: () -> Unit) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = Color.White,
      modifier = Modifier.padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Numo", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF02844E))
        Text("Smarter Money. Brighter Future.", fontSize = 12.sp, color = Color(0xFF64748B))
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          "Version 2.4.0 (Build 42)\nDesigned for high-performance personal financial intelligence and wealth tracking.",
          fontSize = 13.sp,
          color = Color(0xFF475569),
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Close")
        }
      }
    }
  }
}
