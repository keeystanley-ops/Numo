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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.DiscoveredWallet
import com.example.data.WalletIntelligenceService
import kotlinx.coroutines.launch

data class ConfiguredAccount(
  val id: String,
  val name: String,
  val category: String,
  val balance: String,
  val annualInterestPercent: Double,
  val yieldDescription: String,
  val brandColorHex: String,
  val iconType: String
)

/**
 * Step 2 of Onboarding: Dynamic Wallet & Account Setup.
 * No hardcoded wallets. Powered by Gemini / WalletIntelligenceService to discover,
 * analyze, and configure any financial wallet, bank, or MMF typed by the user in ~1-2s.
 */
@Composable
fun OnboardingAccountsScreen(
  currency: String = "KES",
  onBack: () -> Unit,
  onComplete: (selectedAccounts: List<Pair<String, String>>) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current

  var searchQuery by remember { mutableStateOf("") }
  var isSearching by remember { mutableStateOf(false) }
  var discoveredWallet by remember { mutableStateOf<DiscoveredWallet?>(null) }
  var customStartingBalance by remember { mutableStateOf("") }

  // The list of accounts the user has added
  val userAccounts = remember { mutableStateListOf<ConfiguredAccount>() }

  val samplePills = listOf(
    "M-PESA",
    "Etica MMF",
    "KCB Bank",
    "Zidisha MMF",
    "M-Shwari",
    "Sanlam MMF",
    "Equity Bank",
    "Stima SACCO",
    "Cash on Hand"
  )

  fun resolveIcon(iconType: String): ImageVector {
    return when (iconType.uppercase()) {
      "PHONE" -> Icons.Default.PhoneAndroid
      "TRENDING_UP" -> Icons.Default.TrendingUp
      "BANK" -> Icons.Default.AccountBalance
      "SAVINGS" -> Icons.Default.Savings
      "CARD" -> Icons.Default.CreditCard
      "CASH" -> Icons.Default.Payments
      "CRYPTO" -> Icons.Default.CurrencyExchange
      else -> Icons.Default.AccountBalance
    }
  }

  fun parseColor(hex: String): Color {
    return try {
      val clean = hex.removePrefix("#")
      Color(android.graphics.Color.parseColor("#$clean"))
    } catch (e: Exception) {
      Color(0xFF029766)
    }
  }

  fun performSearch(queryText: String) {
    if (queryText.isBlank()) return
    keyboardController?.hide()
    isSearching = true
    coroutineScope.launch {
      val result = WalletIntelligenceService.searchAndAnalyzeWallet(queryText)
      discoveredWallet = result
      customStartingBalance = result.defaultBalance
      isSearching = false
    }
  }

  // Initial trigger: Search M-PESA so user immediately sees how it works on arrival
  LaunchedEffect(Unit) {
    if (discoveredWallet == null && userAccounts.isEmpty()) {
      performSearch("M-PESA")
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFFF7FDFB)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              Color(0xFFD2FCEF),
              Color(0xFFE6FAF3),
              Color(0xFFF4FDF9),
              Color(0xFFFFFFFF)
            )
          )
        )
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top Navigation & Step Indicator
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White.copy(alpha = 0.85f))
              .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
              .clickable { onBack() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color(0xFF001A49),
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "STEP 2 OF 2",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF029766),
                letterSpacing = 1.2.sp
              )
              Text(
                text = "Dynamic Account Setup",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { 1f },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
              color = Color(0xFF029766),
              trackColor = Color(0xFFE2E8F0)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logo & Title
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_numo_leaf),
            contentDescription = "Numo Brand Logo",
            modifier = Modifier
              .size(width = 44.dp, height = 34.dp)
              .testTag("onboarding_accounts_logo"),
            contentScale = ContentScale.Fit
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Connect Accounts",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF001A49)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Type any wallet, bank, or Money Market Fund (e.g. M-PESA, Etica MMF, KCB). AI discovers its behavior and compound yields in 1-2s.",
          fontSize = 12.sp,
          color = Color(0xFF475569),
          textAlign = TextAlign.Center,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x1A029766))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, Color(0xFF029766), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color(0xFF029766),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            textStyle = TextStyle(
              color = Color(0xFF001A49),
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
            cursorBrush = SolidColor(Color(0xFF029766)),
            modifier = Modifier
              .weight(1f)
              .testTag("account_search_input"),
            decorationBox = { innerTextField ->
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Type wallet/MMF (e.g. Etica, M-PESA, KCB)...",
                  color = Color(0xFF94A3B8),
                  fontSize = 13.sp
                )
              }
              innerTextField()
            }
          )

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF029766))
              .clickable { performSearch(searchQuery) }
              .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
          ) {
            if (isSearching) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = "AI Search",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Search",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestion Pills
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(samplePills) { pill ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                .clickable {
                  searchQuery = pill
                  performSearch(pill)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "+ $pill",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF029766)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Discovered Wallet Card
        discoveredWallet?.let { wallet ->
          val brandColor = parseColor(wallet.brandColorHex)
          val icon = resolveIcon(wallet.iconType)

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x33029766),
                spotColor = Color(0x40029766)
              )
              .clip(RoundedCornerShape(22.dp))
              .background(Color.White)
              .border(1.8.dp, brandColor.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
              .padding(18.dp)
          ) {
            Column {
              // Top Bar: AI Verified Tag & Category
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFF029766).copy(alpha = 0.15f))
                      .padding(horizontal = 8.dp, vertical = 3.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = Color(0xFF029766),
                        modifier = Modifier.size(12.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = "AI Verified Intelligence",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF029766)
                      )
                    }
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(brandColor.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = wallet.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = brandColor
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Icon & Institution
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(brandColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = wallet.name,
                    tint = brandColor,
                    modifier = Modifier.size(26.dp)
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Text(
                    text = wallet.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF001A49)
                  )
                  Text(
                    text = wallet.institution,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Financial Behavior Highlight
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (wallet.annualInterestPercent > 0) Color(0xFFF0FDF8) else Color(0xFFF8FAFC)
                  )
                  .border(
                    1.dp,
                    if (wallet.annualInterestPercent > 0) Color(0xFF86EFAC) else Color(0xFFE2E8F0),
                    RoundedCornerShape(12.dp)
                  )
                  .padding(12.dp)
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = if (wallet.annualInterestPercent > 0) "📈 " else "💳 ",
                      fontSize = 13.sp
                    )
                    Text(
                      text = wallet.yieldDescription,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (wallet.annualInterestPercent > 0) Color(0xFF029766) else Color(0xFF334155)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = wallet.behaviorSummary,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 15.sp
                  )

                  if (wallet.annualInterestPercent > 0) {
                    val balNum = customStartingBalance.replace(",", "").toDoubleOrNull() ?: 100000.0
                    val yearlyEarn = balNum * (wallet.annualInterestPercent / 100.0)
                    val monthlyEarn = yearlyEarn / 12.0

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Est. Interest: +$currency ${String.format("%,.0f", monthlyEarn)}/mo (+${String.format("%,.0f", yearlyEarn)}/yr)",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF029766)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Starting Balance Input
              Text(
                text = "Starting Balance ($currency):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFF8FAFC))
                  .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                  .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "$currency ",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF029766)
                )
                BasicTextField(
                  value = customStartingBalance,
                  onValueChange = { customStartingBalance = it },
                  singleLine = true,
                  textStyle = TextStyle(
                    color = Color(0xFF001A49),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                  ),
                  cursorBrush = SolidColor(Color(0xFF029766)),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  modifier = Modifier
                    .weight(1f)
                    .testTag("starting_balance_input")
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Add Account Button
              Button(
                onClick = {
                  val newAcc = ConfiguredAccount(
                    id = wallet.id,
                    name = wallet.name,
                    category = wallet.category,
                    balance = customStartingBalance.ifBlank { wallet.defaultBalance },
                    annualInterestPercent = wallet.annualInterestPercent,
                    yieldDescription = wallet.yieldDescription,
                    brandColorHex = wallet.brandColorHex,
                    iconType = wallet.iconType
                  )
                  // Avoid duplicates
                  userAccounts.removeAll { it.name.equals(wallet.name, ignoreCase = true) }
                  userAccounts.add(newAcc)
                  searchQuery = ""
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(46.dp)
                  .testTag("add_account_button"),
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFF029766)
                ),
                shape = RoundedCornerShape(14.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Add to My Accounts",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Configured Accounts Section
        if (userAccounts.isNotEmpty()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "YOUR ACTIVE ACCOUNTS (${userAccounts.size})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF64748B),
              letterSpacing = 1.sp
            )
            Text(
              text = "Live Ledger Ready",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF029766)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            userAccounts.forEach { acc ->
              val brandColor = parseColor(acc.brandColorHex)
              val icon = resolveIcon(acc.iconType)

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0A000000))
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color.White)
                  .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                  .padding(14.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                      modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(brandColor.copy(alpha = 0.15f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = icon,
                        contentDescription = acc.name,
                        tint = brandColor,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text(
                        text = acc.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001A49)
                      )
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = acc.category,
                          fontSize = 11.sp,
                          color = Color(0xFF64748B)
                        )
                        if (acc.annualInterestPercent > 0) {
                          Spacer(modifier = Modifier.width(6.dp))
                          Text(
                            text = "• ${acc.annualInterestPercent}% p.a.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF029766)
                          )
                        }
                      }
                    }
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "$currency ${acc.balance}",
                      fontSize = 14.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFF001A49)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Remove",
                      tint = Color(0xFFEF4444),
                      modifier = Modifier
                        .size(18.dp)
                        .clickable { userAccounts.remove(acc) }
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Finish & Launch Button
        Button(
          onClick = {
            // If user has not explicitly clicked "Add" yet but has discovered wallet, add it
            if (userAccounts.isEmpty() && discoveredWallet != null) {
              val w = discoveredWallet!!
              userAccounts.add(
                ConfiguredAccount(
                  id = w.id,
                  name = w.name,
                  category = w.category,
                  balance = customStartingBalance.ifBlank { w.defaultBalance },
                  annualInterestPercent = w.annualInterestPercent,
                  yieldDescription = w.yieldDescription,
                  brandColorHex = w.brandColorHex,
                  iconType = w.iconType
                )
              )
            }

            val result = userAccounts.map {
              Pair(it.name, "$currency ${it.balance}")
            }
            onComplete(result)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
              elevation = 12.dp,
              shape = RoundedCornerShape(27.dp),
              ambientColor = Color(0x55027858),
              spotColor = Color(0x80029766)
            )
            .testTag("accounts_continue_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF029766)
          ),
          shape = RoundedCornerShape(27.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = if (userAccounts.isNotEmpty()) "Finish & Launch (${userAccounts.size} Accounts)" else "Finish & Launch",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.2.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "→",
              color = Color.White,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
