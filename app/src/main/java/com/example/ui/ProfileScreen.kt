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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

@Composable
fun ProfileScreen(
  userName: String = "Ayesha Wanjiru",
  userEmail: String = "ayesha@numo.com",
  onOpenSettings: () -> Unit = {},
  onSignOut: () -> Unit = {}
) {
  NumoScreenBackground {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(14.dp))

      // 1. Top Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Profile",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = NumoDS.TextPrimary,
          modifier = Modifier
            .size(26.dp)
            .clickable { onOpenSettings() }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Centered Avatar with Mint-Cyan Halo
      Box(
        modifier = Modifier
          .size(106.dp)
          .shadow(12.dp, CircleShape, spotColor = Color(0x400A6F62))
          .clip(CircleShape)
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(Color(0xFFA6F4EC), Color(0xFF68D8CE))
            )
          )
          .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_user_avatar),
          contentDescription = "Profile Avatar",
          modifier = Modifier
            .size(98.dp)
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = userName.ifBlank { "Ayesha Wanjiru" },
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(2.dp))

      Text(
        text = userEmail.ifBlank { "ayesha@numo.com" },
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = NumoDS.TextSecondary
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 3. 7 Neumorphic Menu Item Pills
      ProfileMenuRow(
        icon = Icons.Default.Person,
        title = "Personal Information",
        topColor = NumoDS.HeroTealMid,
        bottomColor = NumoDS.HeroTealDark,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.AccountBalanceWallet,
        title = "Linked Accounts",
        topColor = NumoDS.HeroTealMid,
        bottomColor = NumoDS.HeroTealDark,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.Lock,
        title = "Security & Privacy",
        topColor = NumoDS.HeroTealMid,
        bottomColor = NumoDS.HeroTealDark,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.Notifications,
        title = "Notifications",
        topColor = Color(0xFF27272A),
        bottomColor = NumoDS.Black,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.GridView,
        title = "Categories",
        topColor = Color(0xFF27272A),
        bottomColor = NumoDS.Black,
        onClick = {}
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.Settings,
        title = "Settings",
        topColor = Color(0xFF27272A),
        bottomColor = NumoDS.Black,
        onClick = onOpenSettings
      )
      Spacer(modifier = Modifier.height(8.dp))

      ProfileMenuRow(
        icon = Icons.Default.Help,
        title = "Help & Support",
        topColor = Color(0xFF27272A),
        bottomColor = NumoDS.Black,
        onClick = onSignOut
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ProfileMenuRow(
  icon: ImageVector,
  title: String,
  topColor: Color,
  bottomColor: Color,
  onClick: () -> Unit
) {
  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 16.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 11.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      NumoClayIconBadge(
        icon = icon,
        topColor = topColor,
        bottomColor = bottomColor,
        size = 30.dp,
        iconSize = 16.dp
      )

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary,
        modifier = Modifier.weight(1f)
      )

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = null,
        tint = NumoDS.TextPrimary,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
