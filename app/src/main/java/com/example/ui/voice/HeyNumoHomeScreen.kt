package com.example.ui.voice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.NumoFinancialEngine
import com.example.ui.components.NumoClayIconBadge
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.example.voice.HeyNumoSettings

/**
 * SCREEN 2 — Hey Numo Home (middle phone in reference).
 */
@Composable
fun HeyNumoHomeScreen(
  userName: String,
  onStartVoiceAssistant: () -> Unit,
  onSubmitPrompt: (String) -> Unit,
  onStartNewChat: () -> Unit,
  onOpenSettings: () -> Unit,
  onOpenNotifications: () -> Unit,
  onViewBudget: () -> Unit,
  onBack: () -> Unit
) {
  val firstName = userName.trim().substringBefore(" ").ifBlank { "Ayesha" }
  var queryInput by remember { mutableStateOf("") }

  val quickShortcuts = listOf(
    "Budget" to "How much do I have left in my Food budget?",
    "Transactions" to "Show my recent transactions",
    "Savings" to "How much have I saved?",
    "Available Cash" to "Check my available cash",
    "Expenses" to "Show my monthly expenses",
    "Net Worth" to "How much is my net worth?",
    "Investments" to "Show my investments",
    "Bills" to "What bills are coming up?",
    "Reduce Spending" to "How can I reduce my spending?"
  )

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 2)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top Header: Back + Profile Avatar + Greeting "Hey, [Name]" + Notifications + Settings
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        HeyNumoCircleButton(
          icon = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back to Dashboard",
          onClick = onBack,
          size = 38.dp,
          iconSize = 18.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(1.8.dp, NumoDS.Turquoise, CircleShape)
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_user_avatar),
            contentDescription = "User Avatar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Hey, $firstName",
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
            )
          }
          Text(
            text = "Hey Numo is ready",
            fontSize = 11.5.sp,
            color = NumoDS.TextSecondary,
            fontWeight = FontWeight.Medium
          )
        }

        HeyNumoCircleButton(
          icon = Icons.Default.Notifications,
          contentDescription = "Notifications",
          onClick = onOpenNotifications,
          size = 38.dp,
          iconSize = 18.dp
        )

        Spacer(modifier = Modifier.width(8.dp))

        HeyNumoCircleButton(
          icon = Icons.Default.Menu,
          contentDescription = "Voice Settings",
          onClick = onOpenSettings,
          size = 38.dp,
          iconSize = 18.dp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Search / Chat Pill Input: "Ask a question..."
      val searchShape = RoundedCornerShape(22.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .shadow(6.dp, searchShape, ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.25f))
          .clip(searchShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFFFFF8F0), Color(0xFFF3E4D5))
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.85f), searchShape)
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f)) {
            if (queryInput.isEmpty()) {
              Text(
                text = "Ask a question...",
                fontSize = 14.sp,
                color = NumoDS.TextMuted
              )
            }
            BasicTextField(
              value = queryInput,
              onValueChange = { queryInput = it },
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 14.sp,
                color = NumoDS.TextPrimary,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(NumoDS.Turquoise),
              keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
              keyboardActions = KeyboardActions(
                onSend = {
                  if (queryInput.isNotBlank()) {
                    val q = queryInput.trim()
                    queryInput = ""
                    onSubmitPrompt(q)
                  }
                }
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          Icon(
            imageVector = Icons.Default.GridView,
            contentDescription = "New Chat",
            tint = NumoDS.TextSecondary,
            modifier = Modifier
              .size(20.dp)
              .clickable { onStartNewChat() }
          )
          Spacer(modifier = Modifier.width(12.dp))
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = "Voice Assistant",
            tint = NumoDS.TextSecondary,
            modifier = Modifier
              .size(20.dp)
              .clickable { onStartVoiceAssistant() }
          )
          Spacer(modifier = Modifier.width(12.dp))
          Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Speak",
            tint = NumoDS.HeroTealMid,
            modifier = Modifier
              .size(22.dp)
              .clickable { onStartVoiceAssistant() }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Quick Financial Shortcut Chips (every chip executes a live query)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        quickShortcuts.forEach { (label, prompt) ->
          val icon = when (label) {
            "Budget", "Expenses" -> Icons.Default.Analytics
            "Transactions" -> Icons.Default.SwapHoriz
            "Savings" -> Icons.Default.Savings
            "Bills" -> Icons.Default.ReceiptLong
            else -> Icons.Default.AccountBalanceWallet
          }
          ShortcutChip(
            label = label,
            icon = icon,
            onClick = { onSubmitPrompt(prompt) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Hero Action Grid: Left Tall Card + Right Two Stacked Cards
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(216.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Left Tall Card: "Let's check your budget" + "View Budget"
        NumoNeumorphicCard(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          cornerRadius = 22.dp,
          onClick = onViewBudget
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
          ) {
            NumoClayIconBadge(
              icon = Icons.Default.Analytics,
              topColor = NumoDS.CyanBright,
              bottomColor = NumoDS.HeroTealDark,
              size = 56.dp,
              iconSize = 28.dp
            )

            Text(
              text = "Let's check\nyour budget",
              fontSize = 15.5.sp,
              lineHeight = 20.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary,
              textAlign = TextAlign.Center
            )

            NumoPrimaryButton(
              text = "View Budget",
              onClick = onViewBudget,
              modifier = Modifier.fillMaxWidth(),
              height = 38.dp
            )
          }
        }

        // Right Stacked Action Cards
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          NumoNeumorphicCard(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            cornerRadius = 20.dp,
            onClick = {
              onSubmitPrompt("Record ${NumoFinancialEngine.currency} 450 for lunch from M-PESA")
            }
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = null,
                  tint = NumoDS.HeroTealMid,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Record\nTransaction",
                  fontSize = 13.5.sp,
                  lineHeight = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.TextPrimary
                )
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = NumoDS.TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          NumoNeumorphicCard(
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth(),
            cornerRadius = 20.dp,
            onClick = { onSubmitPrompt("How much have I saved?") }
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Icon(
                  imageVector = Icons.Default.TrackChanges,
                  contentDescription = null,
                  tint = NumoDS.Turquoise,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "Check\nSavings Goal",
                  fontSize = 13.5.sp,
                  lineHeight = 17.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.TextPrimary
                )
              }
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = NumoDS.TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Primary Voice + Chat CTAs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        NumoPrimaryButton(
          text = "Start Voice Assistant",
          onClick = onStartVoiceAssistant,
          modifier = Modifier.weight(1f),
          height = 46.dp
        )
        NumoNeumorphicCard(
          modifier = Modifier
            .weight(0.7f)
            .height(46.dp),
          cornerRadius = 50.dp,
          onClick = onStartNewChat
        ) {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "New Chat",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Recent Conversations
      Text(
        text = "Recent Conversations",
        fontSize = 15.5.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HeyNumoSettings.recentPrompts.take(4).forEachIndexed { idx, prompt ->
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            onClick = { onSubmitPrompt(prompt) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              NumoClayIconBadge(
                icon = if (idx % 2 == 0) Icons.Default.Analytics else Icons.Default.Savings,
                topColor = if (idx % 2 == 0) NumoDS.CyanBright else Color(0xFFFCD34D),
                bottomColor = if (idx % 2 == 0) NumoDS.HeroTealMid else NumoDS.DuneOchre,
                size = 34.dp,
                iconSize = 17.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = prompt,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1
              )
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = NumoDS.TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun ShortcutChip(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  NumoNeumorphicCard(
    cornerRadius = 50.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = NumoDS.HeroTealMid,
        modifier = Modifier.size(15.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary
      )
    }
  }
}
