package com.example.ui.voice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NumoFinancialEngine
import com.example.ui.components.Numo3DLogoEmblem
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground
import com.example.voice.HeyNumoMessage
import com.example.voice.PendingFinancialAction
import com.example.voice.VoiceOrbState
import java.text.NumberFormat
import java.util.Locale

/**
 * SCREEN 3 — Hey Numo Conversational Interface & Transaction Confirmation (right phone in reference).
 */
@Composable
fun HeyNumoChatScreen(
  messages: List<HeyNumoMessage>,
  orbState: VoiceOrbState,
  onSendMessage: (String) -> Unit,
  onConfirmAction: (PendingFinancialAction) -> Unit,
  onCancelAction: (PendingFinancialAction) -> Unit,
  onMicClick: () -> Unit,
  onSwitchToVoiceMode: () -> Unit,
  onOpenSettings: () -> Unit,
  onBack: () -> Unit
) {
  var messageInput by remember { mutableStateOf("") }
  var showQuickPrompts by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.lastIndex)
    }
  }

  val suggestedPrompts = listOf(
    "Check my available cash",
    "How much is left in my Food budget?",
    "Record ${NumoFinancialEngine.currency} 450 for lunch from M-PESA",
    "Show my investments",
    "What bills are coming up?"
  )

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .padding(horizontal = 20.dp)
    ) {
      // Top bar: Back, "Hey Numo" pill, Menu
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        HeyNumoCircleButton(
          icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Back",
          onClick = onBack
        )

        HeyNumoStatusPill(isActive = orbState != VoiceOrbState.ERROR)

        HeyNumoCircleButton(
          icon = Icons.Default.MoreHoriz,
          contentDescription = "Voice Settings",
          onClick = onOpenSettings
        )
      }

      // Chat stream + Welcome header when empty
      LazyColumn(
        state = listState,
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (messages.isEmpty()) {
          item {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              HeyNumoOrb(
                orbState = orbState,
                size = 136.dp,
                logoSize = 56.dp
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Hi, I'm Numo",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NumoDS.TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Let's manage your money and grow your wealth together.",
                fontSize = 13.5.sp,
                color = NumoDS.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
              Spacer(modifier = Modifier.height(16.dp))
              suggestedPrompts.forEach { prompt ->
                NumoNeumorphicCard(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  cornerRadius = 16.dp,
                  onClick = { onSendMessage(prompt) }
                ) {
                  Text(
                    text = prompt,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = NumoDS.TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                  )
                }
              }
            }
          }
        }

        items(messages, key = { it.id }) { msg ->
          if (msg.isUser) {
            UserChatBubble(text = msg.text)
          } else {
            AssistantChatBubble(
              text = msg.text,
              pendingAction = msg.pendingAction,
              onConfirm = onConfirmAction,
              onCancel = onCancelAction
            )
          }
        }
      }

      // Optional quick-prompt strip toggled by '+'
      if (showQuickPrompts) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          suggestedPrompts.forEach { prompt ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(NumoDS.CardSurfaceLight)
                .border(1.dp, NumoDS.CardBorderLight, RoundedCornerShape(50))
                .clickable {
                  showQuickPrompts = false
                  onSendMessage(prompt)
                }
                .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
              Text(
                text = prompt,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextPrimary
              )
            }
          }
        }
      }

      // Bottom Composer Bar: + | Type your message... | Mic | Voice Mode toggle
      val barShape = RoundedCornerShape(28.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 14.dp)
          .shadow(10.dp, barShape, ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.35f))
          .clip(barShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFFFFF8F0), Color(0xFFF3E4D5))
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.9f), barShape)
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Color(0xFFEBDACC))
              .clickable { showQuickPrompts = !showQuickPrompts },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Quick Prompts",
              tint = NumoDS.TextPrimary,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
          ) {
            if (messageInput.isEmpty()) {
              Text(
                text = "Type your message...",
                fontSize = 14.sp,
                color = NumoDS.TextMuted
              )
            }
            BasicTextField(
              value = messageInput,
              onValueChange = { messageInput = it },
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
                  if (messageInput.isNotBlank()) {
                    val txt = messageInput.trim()
                    messageInput = ""
                    onSendMessage(txt)
                  }
                }
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          if (messageInput.isNotBlank()) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NumoDS.Turquoise)
                .clickable {
                  val txt = messageInput.trim()
                  messageInput = ""
                  onSendMessage(txt)
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          } else {
            Icon(
              imageVector = Icons.Default.Mic,
              contentDescription = "Speak",
              tint = NumoDS.TextPrimary,
              modifier = Modifier
                .size(22.dp)
                .clickable { onMicClick() }
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(NumoDS.Turquoise.copy(alpha = 0.16f))
                .clickable { onSwitchToVoiceMode() },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Voice Mode",
                tint = NumoDS.HeroTealMid,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun UserChatBubble(text: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    NumoNeumorphicCard(
      modifier = Modifier.widthIn(max = 270.dp),
      cornerRadius = 18.dp
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        Text(
          text = "You",
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.HeroTealMid
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = text,
          fontSize = 14.sp,
          lineHeight = 19.sp,
          color = NumoDS.TextPrimary
        )
      }
    }
  }
}

@Composable
private fun AssistantChatBubble(
  text: String,
  pendingAction: PendingFinancialAction?,
  onConfirm: (PendingFinancialAction) -> Unit,
  onCancel: (PendingFinancialAction) -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .shadow(6.dp, CircleShape)
          .clip(CircleShape)
          .background(NumoDS.CardSurfaceLight)
          .border(1.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Numo3DLogoEmblem(size = 20.dp)
      }

      Spacer(modifier = Modifier.width(8.dp))

      NumoNeumorphicCard(
        modifier = Modifier.widthIn(max = 276.dp),
        cornerRadius = 18.dp
      ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Hey Numo",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
            Spacer(modifier = Modifier.width(5.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
            )
          }
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = text,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = NumoDS.TextPrimary
          )
        }
      }
    }

    if (pendingAction != null) {
      Spacer(modifier = Modifier.height(10.dp))
      TransactionConfirmationCard(
        action = pendingAction,
        onConfirm = { onConfirm(pendingAction) },
        onCancel = { onCancel(pendingAction) }
      )
    }
  }
}

@Composable
private fun TransactionConfirmationCard(
  action: PendingFinancialAction,
  onConfirm: () -> Unit,
  onCancel: () -> Unit
) {
  val nf = NumberFormat.getNumberInstance(Locale.US).apply {
    maximumFractionDigits = if (action.amount % 1.0 == 0.0) 0 else 2
  }
  val formattedAmount = "${NumoFinancialEngine.currency} ${nf.format(action.amount)}"
  val badgeColor = when {
    action.wasConfirmed -> Color(0xFF16A34A)
    action.isResolved -> NumoDS.TextMuted
    else -> NumoDS.DuneOchre
  }

  NumoNeumorphicCard(
    modifier = Modifier.fillMaxWidth(),
    cornerRadius = 22.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Confirm Transaction",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDS.TextPrimary
        )

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(badgeColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = action.statusLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = formattedAmount,
        fontSize = 30.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp
      )

      Text(
        text = if (action.isIncome) "INCOME TRANSACTION" else "EXPENSE TRANSACTION",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextSecondary,
        letterSpacing = 0.6.sp
      )

      Spacer(modifier = Modifier.height(14.dp))

      ConfirmationDetailRow(Icons.Default.Description, "Description", action.description)
      Spacer(modifier = Modifier.height(8.dp))
      ConfirmationDetailRow(Icons.Default.AccountBalanceWallet, "Account", action.account)
      Spacer(modifier = Modifier.height(8.dp))
      ConfirmationDetailRow(Icons.Default.Category, "Category", action.category)
      Spacer(modifier = Modifier.height(8.dp))
      ConfirmationDetailRow(
        Icons.Default.TrackChanges,
        "Type",
        if (action.isIncome) "Income" else "Expense"
      )

      if (!action.isResolved) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          NumoPrimaryButton(
            text = "Confirm",
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            height = 44.dp
          )

          NumoNeumorphicCard(
            modifier = Modifier
              .weight(1f)
              .height(44.dp),
            cornerRadius = 50.dp,
            onClick = onCancel
          ) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "Cancel",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ConfirmationDetailRow(
  icon: ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = NumoDS.HeroTealMid,
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label,
      fontSize = 13.sp,
      color = NumoDS.TextSecondary,
      modifier = Modifier.weight(1f)
    )
    Text(
      text = value,
      fontSize = 13.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = NumoDS.TextPrimary
    )
  }
}
