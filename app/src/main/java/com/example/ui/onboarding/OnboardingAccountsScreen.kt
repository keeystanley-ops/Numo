package com.example.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CurrencyRepository
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoGlassCardBg
import com.example.ui.theme.NumoGlassCardBorder
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen

data class ConnectedAccountItem(
  val id: String,
  val name: String,
  val institution: String,
  val balance: String
)

/**
 * Screen 2 of Onboarding: "Connect Accounts"
 * Matches screenshot image_c32814bd.png (center screen) identically:
 * - Mint background (#E5F6F1)
 * - Centered 3D Frosted Glass "N" pebble emblem
 * - Centered title: "Connect Accounts"
 * - Frosted pill input: "Search banks or services..."
 * - Frosted Starting Balance card: "Starting Balance", formatted currency balance, currency name, divider, "Transactions: 1"
 * - "CONNECTED ACCOUNTS" uppercase header
 * - Frosted pill input: "Search connected accounts..."
 * - List of connected accounts (if any added)
 * - Frosted pill button: "FINISH SETUP"
 * - Subtext button: "+ Link a New Account"
 */
@Composable
fun OnboardingAccountsScreen(
  currency: String = "KES",
  onBack: () -> Unit = {},
  onComplete: (selectedAccounts: List<Pair<String, String>>) -> Unit
) {
  val currencyInfo = remember(currency) { CurrencyRepository.findByCode(currency) }

  var serviceSearchQuery by remember { mutableStateOf("") }
  var accountSearchQuery by remember { mutableStateOf("") }

  var balanceAmount by remember { mutableStateOf("48,200") }
  var isEditingBalance by remember { mutableStateOf(false) }
  var editBalanceInput by remember { mutableStateOf(balanceAmount) }

  var showLinkDialog by remember { mutableStateOf(false) }
  var newAccountName by remember { mutableStateOf("") }
  var newAccountBalance by remember { mutableStateOf("10,000") }

  // Default initial account
  val connectedAccounts = remember {
    mutableStateListOf<ConnectedAccountItem>()
  }

  // Pre-configured bank suggestions when linking
  val suggestedInstitutions = listOf(
    "M-PESA",
    "KCB Bank",
    "Equity Bank",
    "Co-operative Bank",
    "NCBA Bank",
    "Stanbic Bank",
    "Absa Bank",
    "Standard Chartered",
    "Cash Wallet"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = NumoMintBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(18.dp))

        // 1. Centered 3D Frosted Glass "N" Pebble Emblem
        Image(
          painter = painterResource(id = R.drawable.ic_numo_n_pebble),
          contentDescription = "Numo N Logo",
          modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x181E5631), ambientColor = Color(0x0C1E5631))
            .testTag("onboarding_accounts_logo"),
          contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Centered Title
        Text(
          text = "Connect Accounts",
          fontSize = 21.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDarkForestGreen,
          textAlign = TextAlign.Center,
          modifier = Modifier.testTag("connect_accounts_title")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Top Frosted Pill Search Bar ("Search banks or services...")
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
              elevation = 2.dp,
              shape = RoundedCornerShape(16.dp),
              spotColor = Color(0x101E5631),
              ambientColor = Color(0x081E5631)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search banks",
              tint = NumoSlateGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
              value = serviceSearchQuery,
              onValueChange = { serviceSearchQuery = it },
              singleLine = true,
              textStyle = TextStyle(
                color = NumoDarkForestGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
              ),
              cursorBrush = SolidColor(NumoDarkForestGreen),
              modifier = Modifier
                .weight(1f)
                .testTag("search_banks_input"),
              decorationBox = { innerTextField ->
                if (serviceSearchQuery.isEmpty()) {
                  Text(
                    text = "Search banks or services...",
                    color = NumoSlateGreen.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                  )
                }
                innerTextField()
              }
            )
            if (serviceSearchQuery.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = NumoSlateGreen,
                modifier = Modifier
                  .size(16.dp)
                  .clickable { serviceSearchQuery = "" }
              )
            }
          }
        }

        // Suggestions Dropdown if user is typing
        AnimatedVisibility(
          visible = serviceSearchQuery.isNotBlank(),
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          val filteredInstitutions = suggestedInstitutions.filter {
            it.contains(serviceSearchQuery, ignoreCase = true)
          }
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(NumoGlassCardBg)
              .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(14.dp))
              .padding(vertical = 4.dp)
          ) {
            if (filteredInstitutions.isEmpty()) {
              Text(
                text = "+ Add \"$serviceSearchQuery\" as Custom Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = NumoDarkForestGreen,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    connectedAccounts.add(
                      ConnectedAccountItem(
                        id = System.currentTimeMillis().toString(),
                        name = serviceSearchQuery,
                        institution = serviceSearchQuery,
                        balance = "0"
                      )
                    )
                    serviceSearchQuery = ""
                  }
                  .padding(horizontal = 14.dp, vertical = 10.dp)
              )
            } else {
              filteredInstitutions.forEach { inst ->
                Text(
                  text = "+ Connect $inst",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium,
                  color = NumoDarkForestGreen,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      connectedAccounts.add(
                        ConnectedAccountItem(
                          id = System.currentTimeMillis().toString(),
                          name = inst,
                          institution = inst,
                          balance = "25,000"
                        )
                      )
                      serviceSearchQuery = ""
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Frosted Glass Starting Balance Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 3.dp,
              shape = RoundedCornerShape(22.dp),
              spotColor = Color(0x151E5631),
              ambientColor = Color(0x0A1E5631)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag("starting_balance_card")
        ) {
          Column {
            Text(
              text = "Starting Balance",
              fontSize = 13.sp,
              fontWeight = FontWeight.Normal,
              color = NumoSlateGreen
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  editBalanceInput = balanceAmount
                  isEditingBalance = true
                },
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "${currencyInfo.code} $balanceAmount",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NumoDarkForestGreen,
                modifier = Modifier.testTag("starting_balance_amount")
              )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
              text = currencyInfo.name,
              fontSize = 12.sp,
              fontWeight = FontWeight.Normal,
              color = NumoSlateGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
              color = Color(0xFFD8EFE8),
              thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Transactions: ${1 + connectedAccounts.size}",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = NumoDarkForestGreen
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 5. Section Header: "CONNECTED ACCOUNTS"
        Text(
          text = "CONNECTED ACCOUNTS",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDarkForestGreen,
          letterSpacing = 0.5.sp,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("connected_accounts_header")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 6. Second Frosted Pill Search Bar ("Search connected accounts...")
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(
              elevation = 2.dp,
              shape = RoundedCornerShape(16.dp),
              spotColor = Color(0x101E5631),
              ambientColor = Color(0x081E5631)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search accounts",
              tint = NumoSlateGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
              value = accountSearchQuery,
              onValueChange = { accountSearchQuery = it },
              singleLine = true,
              textStyle = TextStyle(
                color = NumoDarkForestGreen,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
              ),
              cursorBrush = SolidColor(NumoDarkForestGreen),
              modifier = Modifier
                .weight(1f)
                .testTag("search_connected_input"),
              decorationBox = { innerTextField ->
                if (accountSearchQuery.isEmpty()) {
                  Text(
                    text = "Search connected accounts...",
                    color = NumoSlateGreen.copy(alpha = 0.65f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                  )
                }
                innerTextField()
              }
            )
            if (accountSearchQuery.isNotEmpty()) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = NumoSlateGreen,
                modifier = Modifier
                  .size(16.dp)
                  .clickable { accountSearchQuery = "" }
              )
            }
          }
        }

        // List of any user added accounts
        if (connectedAccounts.isNotEmpty()) {
          Spacer(modifier = Modifier.height(10.dp))
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val visibleList = if (accountSearchQuery.isBlank()) {
              connectedAccounts
            } else {
              connectedAccounts.filter {
                it.name.contains(accountSearchQuery, ignoreCase = true) ||
                  it.institution.contains(accountSearchQuery, ignoreCase = true)
              }
            }
            items(visibleList, key = { it.id }) { acc ->
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(NumoGlassCardBg)
                  .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(14.dp))
                  .padding(horizontal = 14.dp, vertical = 10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = acc.name,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = NumoDarkForestGreen
                    )
                    Text(
                      text = "${currencyInfo.code} ${acc.balance}",
                      fontSize = 11.sp,
                      color = NumoSlateGreen
                    )
                  }
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = NumoSlateGreen.copy(alpha = 0.7f),
                    modifier = Modifier
                      .size(18.dp)
                      .clickable { connectedAccounts.remove(acc) }
                  )
                }
              }
            }
          }
        } else {
          Spacer(modifier = Modifier.weight(1f))
        }

        // 7. Bottom Actions: "FINISH SETUP" & "+ Link a New Account"
        Spacer(modifier = Modifier.height(20.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(
              elevation = 3.dp,
              shape = RoundedCornerShape(16.dp),
              spotColor = Color(0x151E5631),
              ambientColor = Color(0x081E5631)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(NumoGlassCardBg)
            .border(1.dp, NumoGlassCardBorder, RoundedCornerShape(16.dp))
            .clickable {
              val list = mutableListOf(
                Pair("Primary Account (${currencyInfo.code})", "${currencyInfo.code} $balanceAmount")
              )
              connectedAccounts.forEach { acc ->
                list.add(Pair(acc.name, "${currencyInfo.code} ${acc.balance}"))
              }
              onComplete(list)
            }
            .testTag("finish_setup_button"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "FINISH SETUP",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = NumoDarkForestGreen,
            letterSpacing = 0.6.sp
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "+ Link a New Account",
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          color = NumoSlateGreen,
          modifier = Modifier
            .clickable { showLinkDialog = true }
            .testTag("link_new_account_button")
            .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }

  // Edit Starting Balance Dialog
  if (isEditingBalance) {
    AlertDialog(
      onDismissRequest = { isEditingBalance = false },
      containerColor = Color.White,
      title = {
        Text(
          text = "Edit Starting Balance",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDarkForestGreen
        )
      },
      text = {
        Column {
          Text(
            text = "Enter your starting balance in ${currencyInfo.code}:",
            fontSize = 13.sp,
            color = NumoSlateGreen
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = editBalanceInput,
            onValueChange = { editBalanceInput = it },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NumoDarkForestGreen,
              unfocusedBorderColor = NumoGlassCardBorder,
              focusedTextColor = NumoDarkForestGreen
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (editBalanceInput.isNotBlank()) {
              balanceAmount = editBalanceInput.trim()
            }
            isEditingBalance = false
          }
        ) {
          Text("Save", color = NumoDarkForestGreen, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { isEditingBalance = false }) {
          Text("Cancel", color = NumoSlateGreen)
        }
      }
    )
  }

  // Link New Account Dialog
  if (showLinkDialog) {
    AlertDialog(
      onDismissRequest = { showLinkDialog = false },
      containerColor = Color.White,
      title = {
        Text(
          text = "Link a New Account",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = NumoDarkForestGreen
        )
      },
      text = {
        Column {
          Text(
            text = "Select or type bank / service name:",
            fontSize = 12.sp,
            color = NumoSlateGreen
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = newAccountName,
            onValueChange = { newAccountName = it },
            placeholder = { Text("e.g. M-PESA, KCB Bank, Chase") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NumoDarkForestGreen,
              unfocusedBorderColor = NumoGlassCardBorder,
              focusedTextColor = NumoDarkForestGreen
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Balance (${currencyInfo.code}):",
            fontSize = 12.sp,
            color = NumoSlateGreen
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedTextField(
            value = newAccountBalance,
            onValueChange = { newAccountBalance = it },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NumoDarkForestGreen,
              unfocusedBorderColor = NumoGlassCardBorder,
              focusedTextColor = NumoDarkForestGreen
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (newAccountName.isNotBlank()) {
              connectedAccounts.add(
                ConnectedAccountItem(
                  id = System.currentTimeMillis().toString(),
                  name = newAccountName.trim(),
                  institution = newAccountName.trim(),
                  balance = newAccountBalance.ifBlank { "0" }
                )
              )
              newAccountName = ""
              newAccountBalance = "10,000"
            }
            showLinkDialog = false
          }
        ) {
          Text("Connect", color = NumoDarkForestGreen, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLinkDialog = false }) {
          Text("Cancel", color = NumoSlateGreen)
        }
      }
    )
  }
}
