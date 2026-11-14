package com.example.ui.onboarding

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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CurrencyRepository
import com.example.ui.animation.bouncyClickable
import com.example.ui.theme.NumoDarkForestGreen
import com.example.ui.theme.NumoElementsCorners
import com.example.ui.theme.NumoGlassCardBg
import com.example.ui.theme.NumoGlassCardBorder
import com.example.ui.theme.NumoInputBg
import com.example.ui.theme.NumoInputPlaceholder
import com.example.ui.theme.NumoMintBackground
import com.example.ui.theme.NumoSlateGreen

/**
 * Screen 1 of Onboarding: "Select Currency" matching image_c32814bd.png identically:
 * - Mint background (#E5F6F1)
 * - Top Bar: Chevron Back on left, "Select Currency" centered, Close 'X' on right
 * - Active Currency Frosted Card with flag circle, code, name, "Edit" button, and exchange rate
 * - Mint frosted search bar: "Search country or currency code"
 * - Vertical list of currencies with circular flag icons, codes, and checkmarks
 */
@Composable
fun OnboardingCurrencyScreen(
  initialCurrency: String = "USD",
  onBack: () -> Unit = {},
  onContinue: (selectedCurrency: String) -> Unit
) {
  var selectedCode by remember { mutableStateOf(initialCurrency) }
  var searchQuery by remember { mutableStateOf("") }

  val activeCurrency by remember(selectedCode) {
    derivedStateOf { CurrencyRepository.findByCode(selectedCode) }
  }

  // Pre-sorted priority currency list matching the design: EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, SEK, NZD, SGD, KES, USD
  val priorityCodes = listOf("EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "INR", "SEK", "NZD", "SGD", "KES", "USD", "ZAR", "AED")

  val displayCurrencies by remember(searchQuery) {
    derivedStateOf {
      val query = searchQuery.trim().lowercase()
      if (query.isEmpty()) {
        val priorityList = priorityCodes.mapNotNull { CurrencyRepository.findByCode(it) }
        val others = CurrencyRepository.allCurrencies.filter { it.code !in priorityCodes }
        priorityList + others
      } else {
        CurrencyRepository.allCurrencies.filter { item ->
          item.code.lowercase().contains(query) ||
            item.name.lowercase().contains(query) ||
            item.country.lowercase().contains(query) ||
            item.symbol.lowercase().contains(query)
        }
      }
    }
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = NumoMintBackground
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(NumoMintBackground)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 18.dp)
      ) {
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Top Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { onBack() },
            modifier = Modifier
              .size(36.dp)
              .testTag("btn_currency_back")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
              contentDescription = "Back",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(24.dp)
            )
          }

          Text(
            text = "Select Currency",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.SansSerif,
            color = NumoDarkForestGreen,
            modifier = Modifier.testTag("title_select_currency")
          )

          IconButton(
            onClick = { onContinue(selectedCode) },
            modifier = Modifier
              .size(36.dp)
              .testTag("btn_currency_close")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Confirm",
              tint = NumoDarkForestGreen,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Active Selected Currency Glass Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(20.dp),
              spotColor = NumoDarkForestGreen.copy(alpha = 0.08f),
              ambientColor = NumoDarkForestGreen.copy(alpha = 0.03f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(NumoGlassCardBg)
            .border(1.5.dp, NumoGlassCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("card_selected_currency")
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                // Circular Flag Badge
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, NumoElementsCorners, CircleShape),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = activeCurrency.flagEmoji,
                    fontSize = 24.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Text(
                    text = activeCurrency.code,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    color = NumoDarkForestGreen
                  )
                  Text(
                    text = activeCurrency.name,
                    fontSize = 12.sp,
                    color = NumoSlateGreen,
                    fontFamily = FontFamily.SansSerif
                  )
                }
              }

              // "Edit" Pill Button
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(NumoDarkForestGreen)
                  .bouncyClickable {
                    onContinue(selectedCode)
                  }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
                  .testTag("btn_currency_edit"),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "Edit",
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Exchange Rate
            val rateText = when (selectedCode) {
              "USD" -> "1 USD = 0.92 EUR"
              "KES" -> "1 KES = 0.0077 USD"
              "EUR" -> "1 EUR = 1.09 USD"
              "GBP" -> "1 GBP = 1.28 USD"
              else -> "1 $selectedCode = 0.92 EUR"
            }
            Text(
              text = rateText,
              fontSize = 11.5.sp,
              color = NumoSlateGreen.copy(alpha = 0.85f),
              fontFamily = FontFamily.SansSerif,
              modifier = Modifier.padding(start = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Search Bar
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.55f))
            .border(1.dp, NumoElementsCorners, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
          contentAlignment = Alignment.CenterStart
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = NumoSlateGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search country or currency code",
                  fontSize = 13.sp,
                  color = NumoInputPlaceholder,
                  fontFamily = FontFamily.SansSerif
                )
              }
              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_search_currency"),
                textStyle = TextStyle(
                  fontSize = 13.5.sp,
                  color = Color(0xFF0F172A),
                  fontWeight = FontWeight.Normal
                ),
                singleLine = true,
                cursorBrush = SolidColor(NumoDarkForestGreen)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Currency List with flags, bold codes, and checkmarks
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          items(displayCurrencies, key = { it.code }) { item ->
            val isSelected = item.code == selectedCode

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  selectedCode = item.code
                }
                .testTag("currency_row_${item.code}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Circular Flag Icon
                  Box(
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .background(Color.White)
                      .border(0.5.dp, NumoElementsCorners, CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = item.flagEmoji,
                      fontSize = 18.sp
                    )
                  }

                  Spacer(modifier = Modifier.width(14.dp))

                  Text(
                    text = item.code,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.SansSerif,
                    color = NumoDarkForestGreen
                  )
                }

                // Checkmark indicator
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = if (isSelected) NumoDarkForestGreen else NumoSlateGreen.copy(alpha = 0.5f),
                  modifier = Modifier.size(16.dp)
                )
              }

              // Subtle horizontal divider line
              HorizontalDivider(
                color = Color(0xFFD8EFE8),
                thickness = 1.dp
              )
            }
          }
        }
      }
    }
  }
}
