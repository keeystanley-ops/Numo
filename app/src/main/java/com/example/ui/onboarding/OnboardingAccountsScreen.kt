package com.example.ui.onboarding

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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.NumoFinancialEngine
import com.example.ui.components.Numo3DBankIllustration
import com.example.ui.components.NumoBackCircleButton
import com.example.ui.components.NumoDS
import com.example.ui.components.NumoFloatingDecorations
import com.example.ui.components.NumoPrimaryButton
import com.example.ui.components.NumoRadioIndicator
import com.example.ui.components.NumoScreenBackground
import java.text.NumberFormat
import java.util.Locale

data class ConnectedAccountItem(
  val id: String,
  val name: String,
  val institution: String,
  val balance: String
)

private data class InstitutionOption(
  val id: String,
  val label: String,
  val defaultAccountName: String,
  val accentColor: Color,
  val badgeText: String
)

private val INSTITUTION_OPTIONS = listOf(
  InstitutionOption("mpesa", "M-PESA", "M-PESA Wallet", Color(0xFF43A047), "M"),
  InstitutionOption("equity", "Equity Bank", "Equity Bank Account", Color(0xFF8D1E28), "E"),
  InstitutionOption("kcb", "KCB Bank", "KCB Bank Account", Color(0xFF2E7D32), "K"),
  InstitutionOption("scb", "Standard Chartered", "Standard Chartered", Color(0xFF0072AA), "S"),
  InstitutionOption("absa", "Absa Bank", "Absa Bank Account", Color(0xFFD32F2F), "a"),
  InstitutionOption("other", "Other Bank", "Bank Account", NumoDS.Turquoise, "B")
)

/**
 * Screen 5: Connect Your Account — Exact match to the Numo onboarding reference.
 *
 * Connects the user's chosen bank or mobile money institution and populates real
 * account data into `NumoFinancialEngine` + `onComplete` before transitioning to
 * Screen 6 (Account Connected!).
 */
@Composable
fun OnboardingAccountsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {},
  onComplete: (selectedAccounts: List<Pair<String, String>>) -> Unit
) {
  var selectedId by remember { mutableStateOf("mpesa") }
  var showLinkDialog by remember { mutableStateOf(false) }

  val selectedInstitution = INSTITUTION_OPTIONS.firstOrNull { it.id == selectedId }
    ?: INSTITUTION_OPTIONS.first()

  var accountNameInput by remember(selectedId) {
    mutableStateOf(selectedInstitution.defaultAccountName)
  }
  var balanceInput by remember {
    val engineCash = NumoFinancialEngine.availableCash.toLong()
    mutableStateOf(if (engineCash > 0L) engineCash.toString() else "")
  }
  var validationError by remember { mutableStateOf<String?>(null) }

  NumoScreenBackground(modifier = Modifier.testTag("onboarding_accounts_screen")) {
    NumoFloatingDecorations(variant = 1)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top bar with Back button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NumoBackCircleButton(
          onClick = onBack,
          modifier = Modifier.testTag("btn_accounts_back")
        )
      }

      // 3D Banking institution illustration
      Numo3DBankIllustration(
        modifier = Modifier.size(134.dp)
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Heading
      Text(
        text = "Connect Your Account",
        fontSize = 25.sp,
        fontWeight = FontWeight.ExtraBold,
        color = NumoDS.TextPrimary,
        letterSpacing = (-0.5).sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.testTag("connect_accounts_title")
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Supporting explanation
      Text(
        text = "Link your bank or mobile money\naccount for seamless tracking.",
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Normal,
        color = NumoDS.TextSecondary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(16.dp))

      // 6 Institution cards
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        INSTITUTION_OPTIONS.forEach { option ->
          val isSelected = selectedId == option.id
          InstitutionSelectionCard(
            option = option,
            isSelected = isSelected,
            onClick = { selectedId = option.id },
            modifier = Modifier.testTag("institution_option_${option.id}")
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Continue primary button — opens real account link confirmation
      NumoPrimaryButton(
        text = "Continue",
        onClick = {
          accountNameInput = selectedInstitution.defaultAccountName
          validationError = null
          showLinkDialog = true
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("btn_accounts_continue"),
        height = 54.dp
      )

      Spacer(modifier = Modifier.height(20.dp))
    }

    if (showLinkDialog) {
      AlertDialog(
        onDismissRequest = { showLinkDialog = false },
        containerColor = Color(0xFFFFF6EC),
        title = {
          Text(
            text = "Link ${selectedInstitution.label}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = NumoDS.TextPrimary
          )
        },
        text = {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
              text = "Confirm your ${selectedInstitution.label} account details to sync your real balance.",
              fontSize = 13.sp,
              color = NumoDS.TextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "Account Name",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary
              )
              DialogField(
                value = accountNameInput,
                onValueChange = {
                  accountNameInput = it
                  validationError = null
                },
                placeholder = selectedInstitution.defaultAccountName,
                keyboardType = KeyboardType.Text
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Text(
                text = "Current Balance ($currency)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDS.TextPrimary
              )
              DialogField(
                value = balanceInput,
                onValueChange = { input ->
                  balanceInput = input.filter { it.isDigit() || it == '.' }
                  validationError = null
                },
                placeholder = "0",
                keyboardType = KeyboardType.Decimal
              )
            }

            if (validationError != null) {
              Text(
                text = validationError ?: "",
                fontSize = 12.sp,
                color = NumoDS.ExpenseRed,
                fontWeight = FontWeight.Medium
              )
            }
          }
        },
        confirmButton = {
          TextButton(
            onClick = {
              val trimmedName = accountNameInput.trim()
              if (trimmedName.isEmpty()) {
                validationError = "Please enter an account name."
                return@TextButton
              }
              val parsedBalance = balanceInput.toDoubleOrNull()
              if (parsedBalance == null || parsedBalance < 0.0) {
                validationError = "Please enter a valid non-negative balance."
                return@TextButton
              }

              // Update real financial engine state with the connected account
              NumoFinancialEngine.currency = currency
              NumoFinancialEngine.availableCash = parsedBalance

              val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
                maximumFractionDigits = if (parsedBalance % 1.0 == 0.0) 0 else 2
              }.format(parsedBalance)

              showLinkDialog = false
              onComplete(listOf(Pair(trimmedName, "$currency $formatted")))
            }
          ) {
            Text(
              text = "Connect Account",
              fontWeight = FontWeight.Bold,
              color = NumoDS.Turquoise
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showLinkDialog = false }) {
            Text(
              text = "Cancel",
              color = NumoDS.TextSecondary
            )
          }
        }
      )
    }
  }
}

@Composable
private fun DialogField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  keyboardType: KeyboardType
) {
  val shape = RoundedCornerShape(12.dp)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(shape)
      .background(Color(0xFFEDE0D1))
      .border(1.dp, Color(0xFFD6C2AF), shape)
      .padding(horizontal = 12.dp),
    contentAlignment = Alignment.CenterStart
  ) {
    if (value.isEmpty()) {
      Text(
        text = placeholder,
        fontSize = 13.5.sp,
        color = NumoDS.TextMuted
      )
    }
    BasicTextField(
      value = value,
      onValueChange = onValueChange,
      singleLine = true,
      textStyle = TextStyle(
        fontSize = 14.sp,
        color = NumoDS.TextPrimary,
        fontWeight = FontWeight.Medium
      ),
      cursorBrush = SolidColor(NumoDS.Turquoise),
      keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
private fun InstitutionSelectionCard(
  option: InstitutionOption,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  val cardBrush = if (isSelected) {
    Brush.verticalGradient(
      colors = listOf(Color(0xFFFFF8F0), Color(0xFFF6E7D6))
    )
  } else {
    Brush.verticalGradient(
      colors = listOf(Color(0xFFFFF5EA), Color(0xFFF3E2D0))
    )
  }
  val borderColor = if (isSelected) NumoDS.Turquoise.copy(alpha = 0.55f) else Color(0xFFFFFBF6)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(56.dp)
      .shadow(
        elevation = if (isSelected) 10.dp else 6.dp,
        shape = shape,
        ambientColor = NumoDS.CardShadowWarm.copy(alpha = 0.3f),
        spotColor = NumoDS.CardShadowWarm.copy(alpha = 0.35f)
      )
      .clip(shape)
      .background(cardBrush)
      .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
      .clickable { onClick() }
      .padding(horizontal = 16.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      InstitutionBadgeIcon(option = option, modifier = Modifier.size(30.dp))

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = option.label,
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = NumoDS.TextPrimary,
        modifier = Modifier.weight(1f)
      )

      NumoRadioIndicator(selected = isSelected)
    }
  }
}

@Composable
private fun InstitutionBadgeIcon(option: InstitutionOption, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .clip(if (option.id == "absa" || option.id == "other") CircleShape else RoundedCornerShape(8.dp))
      .background(option.accentColor.copy(alpha = 0.14f))
      .border(1.dp, option.accentColor.copy(alpha = 0.3f), if (option.id == "absa" || option.id == "other") CircleShape else RoundedCornerShape(8.dp)),
    contentAlignment = Alignment.Center
  ) {
    if (option.id == "other") {
      Canvas(modifier = Modifier.size(16.dp)) {
        val w = size.width
        val h = size.height
        drawRect(option.accentColor, Offset(w * 0.15f, h * 0.35f), Size(w * 0.7f, h * 0.5f))
        drawRect(option.accentColor, Offset(w * 0.08f, h * 0.82f), Size(w * 0.84f, h * 0.12f))
      }
    } else {
      Text(
        text = option.badgeText,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = option.accentColor
      )
    }
  }
}
