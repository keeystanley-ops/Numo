package com.example.ui.profile

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoScreenBackground

private data class FaqArticle(
  val question: String,
  val answer: String
)

private val FAQ_ITEMS = listOf(
  FaqArticle(
    question = "How to link M-PESA?",
    answer = "Open Profile → Linked Accounts, tap + Add New Account or + Connect next to M-PESA, confirm your wallet name and balance, and tap Connect."
  ),
  FaqArticle(
    question = "How to report a fraud?",
    answer = "Tap Report a Problem below immediately or contact our 24/7 support team via Chat with Support to freeze suspicious activity and secure your account."
  ),
  FaqArticle(
    question = "How to change my currency?",
    answer = "Open Profile → Settings → Currency & Region to switch your preferred base currency (KES, USD, EUR, GBP, TZS, UGX) at any time."
  )
)

@Composable
fun HelpSupportScreen(
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var searchQuery by remember { mutableStateOf("") }
  var expandedIndex by remember { mutableStateOf<Int?>(null) }
  var showReportDialog by remember { mutableStateOf(false) }
  var reportNote by remember { mutableStateOf("") }

  val filteredFaqs = remember(searchQuery) {
    if (searchQuery.isBlank()) {
      FAQ_ITEMS
    } else {
      FAQ_ITEMS.filter {
        it.question.contains(searchQuery, ignoreCase = true) ||
          it.answer.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 2)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 22.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Help & Support",
        onBack = onBack,
        showBackLabel = true
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Recessed Search Bar
      val searchShape = RoundedCornerShape(18.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .clip(searchShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFFE9DBCD), Color(0xFFF3E7DA))
            )
          )
          .border(1.dp, Color(0xFFD8C5B2), searchShape)
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(modifier = Modifier.weight(1f)) {
            if (searchQuery.isEmpty()) {
              Text(
                text = "Search help articles...",
                fontSize = 14.sp,
                color = NumoDS.TextMuted
              )
            }
            BasicTextField(
              value = searchQuery,
              onValueChange = { searchQuery = it },
              singleLine = true,
              textStyle = TextStyle(
                fontSize = 14.sp,
                color = NumoDS.TextPrimary,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(NumoDS.Turquoise),
              modifier = Modifier.fillMaxWidth()
            )
          }
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = NumoDS.TextSecondary,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Frequently Asked Questions",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = NumoDS.TextPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        filteredFaqs.forEachIndexed { idx, faq ->
          val isExpanded = expandedIndex == idx
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp,
            onClick = { expandedIndex = if (isExpanded) null else idx }
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = faq.question,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.TextPrimary,
                  modifier = Modifier.weight(1f)
                )
                Icon(
                  imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                  contentDescription = null,
                  tint = NumoDS.TextPrimary,
                  modifier = Modifier.size(20.dp)
                )
              }

              AnimatedVisibility(visible = isExpanded) {
                Text(
                  text = faq.answer,
                  fontSize = 13.sp,
                  lineHeight = 18.sp,
                  color = NumoDS.TextSecondary,
                  modifier = Modifier.padding(top = 10.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Teal "Chat with Support" Claymorphic Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .shadow(10.dp, RoundedCornerShape(50), spotColor = Color(0x55033A2E))
          .clip(RoundedCornerShape(50))
          .background(
            Brush.verticalGradient(
              colors = listOf(NumoDS.BtnTealTop, NumoDS.BtnTealMid, NumoDS.BtnTealBottom)
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
          .clickable {
            Toast.makeText(context, "Connecting you to Numo Support...", Toast.LENGTH_SHORT).show()
          },
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.ChatBubble,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Chat with Support",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Magenta "Report a Problem" Claymorphic Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .shadow(10.dp, RoundedCornerShape(50), spotColor = Color(0x559D174D))
          .clip(RoundedCornerShape(50))
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFFFF40A4), Color(0xFFD9167A), Color(0xFF8C0D52))
            )
          )
          .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(50))
          .clickable { showReportDialog = true },
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Report a Problem",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    if (showReportDialog) {
      AlertDialog(
        onDismissRequest = { showReportDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Report a Problem",
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          ProfileRecessedInput(
            label = "Describe the issue",
            value = reportNote,
            onValueChange = { reportNote = it },
            placeholder = "Tell us what happened..."
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              showReportDialog = false
              reportNote = ""
              Toast.makeText(context, "Problem report submitted. Thank you!", Toast.LENGTH_SHORT).show()
            }
          ) {
            Text("Submit", fontWeight = FontWeight.Bold, color = NumoDS.Magenta)
          }
        },
        dismissButton = {
          TextButton(onClick = { showReportDialog = false }) {
            Text("Cancel", color = NumoDS.TextSecondary)
          }
        }
      )
    }
  }
}
