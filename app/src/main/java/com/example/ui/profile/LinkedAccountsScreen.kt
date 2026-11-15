package com.example.ui.profile

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoNeumorphicCard
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoScreenBackground

private data class LinkedAccountEntry(
  val name: String,
  val balanceText: String,
  val badgeText: String,
  val accentColor: Color
)

@Composable
fun LinkedAccountsScreen(
  currency: String = "KES",
  customAccounts: List<Pair<String, String>> = emptyList(),
  onBack: () -> Unit
) {
  val context = LocalContext.current

  val connectedList = remember(customAccounts, currency) {
    mutableStateListOf<LinkedAccountEntry>().apply {
      if (customAccounts.isNotEmpty()) {
        customAccounts.forEach { (name, bal) ->
          val accent = when {
            name.contains("M-PESA", ignoreCase = true) -> Color(0xFF43A047)
            name.contains("Equity", ignoreCase = true) -> Color(0xFF8D1E28)
            else -> NumoDS.HeroTealMid
          }
          add(
            LinkedAccountEntry(
              name = name,
              balanceText = "active, $bal",
              badgeText = name.firstOrNull()?.uppercase() ?: "A",
              accentColor = accent
            )
          )
        }
      } else {
        add(LinkedAccountEntry("M-PESA Wallet", "active, $currency 12,500", "M", Color(0xFF43A047)))
        add(LinkedAccountEntry("Equity Bank", "active, $currency 25,400", "E", Color(0xFF8D1E28)))
      }
    }
  }

  val disconnectedList = remember {
    mutableStateListOf(
      LinkedAccountEntry("KCB Bank", "active, $currency 0", "K", Color(0xFF2E7D32)),
      LinkedAccountEntry("Standard Chartered", "active, $currency 0", "S", Color(0xFF0072AA))
    )
  }

  var showAddDialog by remember { mutableStateOf(false) }
  var newBankName by remember { mutableStateOf("") }
  var newBankBalance by remember { mutableStateOf("") }

  NumoScreenBackground {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      ProfileSubScreenTopBar(
        title = "Linked Accounts",
        onBack = onBack,
        showBackLabel = false
      )

      Column(
        modifier = Modifier
          .weight(1f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Connected",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextPrimary
        )

        connectedList.forEach { acc ->
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(acc.accentColor.copy(alpha = 0.14f))
                  .border(1.dp, acc.accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = acc.badgeText,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = acc.accentColor
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = acc.name,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = NumoDS.TextPrimary
                )
                Text(
                  text = acc.balanceText,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDS.HeroTealMid
                )
              }

              Text(
                text = "Disconnect",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.Magenta,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    connectedList.remove(acc)
                    disconnectedList.add(acc)
                    Toast.makeText(context, "${acc.name} disconnected", Toast.LENGTH_SHORT).show()
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Disconnected",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = NumoDS.TextPrimary
        )

        disconnectedList.forEach { acc ->
          NumoNeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 18.dp
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(acc.accentColor.copy(alpha = 0.14f))
                  .border(1.dp, acc.accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = acc.badgeText,
                  fontSize = 15.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = acc.accentColor
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Text(
                text = acc.name,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary,
                modifier = Modifier.weight(1f)
              )

              Text(
                text = "+ Connect",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.HeroTealMid,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    disconnectedList.remove(acc)
                    connectedList.add(acc)
                    Toast.makeText(context, "${acc.name} connected", Toast.LENGTH_SHORT).show()
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))
      }

      NumoPrimaryButton(
        text = "+ Add New Account",
        onClick = { showAddDialog = true },
        modifier = Modifier.fillMaxWidth(),
        height = 52.dp
      )

      Spacer(modifier = Modifier.height(20.dp))
    }

    if (showAddDialog) {
      AlertDialog(
        onDismissRequest = { showAddDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Add New Account",
            fontWeight = FontWeight.Bold,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileRecessedInput(
              label = "Bank or Wallet Name",
              value = newBankName,
              onValueChange = { newBankName = it },
              placeholder = "e.g. Absa Bank"
            )
            ProfileRecessedInput(
              label = "Current Balance ($currency)",
              value = newBankBalance,
              onValueChange = { newBankBalance = it.filter { ch -> ch.isDigit() || ch == '.' } },
              placeholder = "0",
              keyboardType = KeyboardType.Decimal
            )
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              val cleanName = newBankName.trim()
              if (cleanName.isNotEmpty()) {
                val bal = newBankBalance.ifBlank { "0" }
                connectedList.add(
                  LinkedAccountEntry(
                    name = cleanName,
                    balanceText = "active, $currency $bal",
                    badgeText = cleanName.first().uppercase(),
                    accentColor = NumoDS.HeroTealMid
                  )
                )
                newBankName = ""
                newBankBalance = ""
                showAddDialog = false
              }
            }
          ) {
            Text("Connect", fontWeight = FontWeight.Bold, color = NumoDS.Turquoise)
          }
        },
        dismissButton = {
          TextButton(onClick = { showAddDialog = false }) {
            Text("Cancel", color = NumoDS.TextSecondary)
          }
        }
      )
    }
  }
}
