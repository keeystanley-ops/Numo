package com.example.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NumoFinancialEngine
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoSuccessCheckmarkEmblem
import com.example.ui.theme.NumoDS
import com.example.ui.theme.NumoPrimaryButton
import com.example.ui.theme.NumoScreenBackground
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen 6: Account Connected! — Exact match to the Numo onboarding reference.
 *
 * Populates the connected account name and balance dynamically from the actual
 * linked account list (`connectedAccounts`) or `NumoFinancialEngine` state.
 */
@Composable
fun AccountConnectedScreen(
  connectedAccounts: List<Pair<String, String>>,
  currency: String = NumoFinancialEngine.currency,
  onContinue: () -> Unit
) {
  val primaryAccount = connectedAccounts.firstOrNull()
  val accountName = primaryAccount?.first?.takeIf { it.isNotBlank() } ?: "Connected Account"
  val accountBalance = primaryAccount?.second?.takeIf { it.isNotBlank() } ?: run {
    val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
      maximumFractionDigits = if (NumoFinancialEngine.availableCash % 1.0 == 0.0) 0 else 2
    }.format(NumoFinancialEngine.availableCash)
    "$currency $formatted"
  }
  val shortInstitution = accountName
    .removeSuffix(" Wallet")
    .removeSuffix(" Account")
    .ifBlank { accountName }

  NumoScreenBackground(modifier = Modifier.testTag("account_connected_screen")) {
    NumoFloatingDecorations(variant = 3)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.weight(0.65f))

      // Raised circular surface with green checkmark
      NumoSuccessCheckmarkEmblem(
        modifier = Modifier.size(136.dp)
      )

      Spacer(modifier = Modifier.height(22.dp))

      // Heading
      Text(
        text = "Account Connected!",
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Confirmation message
      Text(
        text = "Your $shortInstitution account has been\nsuccessfully linked to Numo.",
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(28.dp))

      // Connected account information card (neumorphic warm cream card)
      val cardShape = RoundedCornerShape(22.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(
            elevation = 14.dp,
            shape = cardShape,
            ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.35f),
            spotColor = NumoDS.CardShadowWarm.copy(alpha = 0.4f)
          )
          .clip(cardShape)
          .background(
            Brush.verticalGradient(
              colors = listOf(Color(0xFFFFF8F0), Color(0xFFF3E2D0))
            )
          )
          .border(1.dp, Color(0xFFFFFBF7), cardShape)
          .padding(horizontal = 20.dp, vertical = 18.dp)
          .testTag("connected_account_info_card")
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // Account row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF43A047).copy(alpha = 0.16f))
                .border(1.dp, Color(0xFF43A047).copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Account",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextSecondary
              )
              Text(
                text = accountName,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDS.TextPrimary,
                modifier = Modifier.testTag("connected_account_name")
              )
            }
          }

          // Balance row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(NumoDS.Turquoise.copy(alpha = 0.16f))
                .border(1.dp, NumoDS.Turquoise.copy(alpha = 0.3f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = NumoDS.HeroTealDark,
                modifier = Modifier.size(18.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Balance",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = NumoDS.TextSecondary
              )
              Text(
                text = accountBalance,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NumoDS.TextPrimary,
                modifier = Modifier.testTag("connected_account_balance")
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Continue primary button
      NumoPrimaryButton(
        text = "Continue",
        onClick = onContinue,
        modifier = Modifier.testTag("btn_account_connected_continue")
      )

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}
