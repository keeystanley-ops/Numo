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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CurrencyRegion
import com.example.data.CurrencyRepository
import com.example.data.GlobalCurrency

/**
 * Step 1 of Onboarding: Global Currency Selection.
 * Provides a comprehensive, searchable catalog of world currencies with instant filtering,
 * regional chips, live preview card, and Numo's signature mint-green & white aesthetic.
 */
@Composable
fun OnboardingCurrencyScreen(
  initialCurrency: String = "KES",
  onContinue: (selectedCurrency: String) -> Unit
) {
  var selectedCode by remember { mutableStateOf(initialCurrency) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedRegion by remember { mutableStateOf(CurrencyRegion.ALL) }

  val activeCurrency by remember(selectedCode) {
    derivedStateOf { CurrencyRepository.findByCode(selectedCode) }
  }

  val filteredCurrencies by remember(searchQuery, selectedRegion) {
    derivedStateOf {
      val query = searchQuery.trim().lowercase()
      CurrencyRepository.allCurrencies.filter { item ->
        val matchesRegion = when (selectedRegion) {
          CurrencyRegion.ALL -> true
          CurrencyRegion.POPULAR -> item.isPopular
          else -> item.region == selectedRegion
        }
        val matchesQuery = if (query.isEmpty()) true else {
          item.code.lowercase().contains(query) ||
            item.name.lowercase().contains(query) ||
            item.country.lowercase().contains(query) ||
            item.symbol.lowercase().contains(query)
        }
        matchesRegion && matchesQuery
      }
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
          .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Progress Indicator (Step 1 of 2)
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "STEP 1 OF 2",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF029766),
            letterSpacing = 1.2.sp
          )
          Text(
            text = "Global Localization",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF64748B)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
          progress = { 0.5f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape),
          color = Color(0xFF029766),
          trackColor = Color(0xFFE2E8F0)
        )

        Spacer(modifier = Modifier.height(14.dp))

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
              .testTag("onboarding_logo"),
            contentScale = ContentScale.Fit
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Operating Currency",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            color = Color(0xFF001A49)
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "Select any global currency. Numo will calculate your accounts, balances, and net worth using this benchmark.",
          fontSize = 12.sp,
          color = Color(0xFF475569),
          textAlign = TextAlign.Center,
          lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Active Selection Hero Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .shadow(
              elevation = 6.dp,
              shape = RoundedCornerShape(20.dp),
              ambientColor = Color(0x33029766),
              spotColor = Color(0x40029766)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
              Brush.horizontalGradient(
                colors = listOf(Color(0xFF029766), Color(0xFF00B074), Color(0xFF00C884))
              )
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = activeCurrency.flagEmoji,
                fontSize = 36.sp
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = activeCurrency.code,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color.White.copy(alpha = 0.25f))
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = activeCurrency.symbol,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
                Text(
                  text = "${activeCurrency.name} • ${activeCurrency.country}",
                  fontSize = 12.sp,
                  color = Color.White.copy(alpha = 0.9f)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Preview",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.8f)
              )
              Text(
                text = "${activeCurrency.symbol} ${activeCurrency.sampleAmount}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
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
              fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(Color(0xFF029766)),
            modifier = Modifier
              .weight(1f)
              .testTag("currency_search_input"),
            decorationBox = { innerTextField ->
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search currency (e.g. KES, USD, Euro, Naira)...",
                  color = Color(0xFF94A3B8),
                  fontSize = 13.sp
                )
              }
              innerTextField()
            }
          )
          if (searchQuery.isNotEmpty()) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Clear",
              tint = Color(0xFF64748B),
              modifier = Modifier
                .size(18.dp)
                .clickable { searchQuery = "" }
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Regional Filter Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(CurrencyRegion.values()) { region ->
            val isSelected = (selectedRegion == region)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) Color(0xFF029766) else Color.White)
                .border(
                  width = 1.dp,
                  color = if (isSelected) Color(0xFF029766) else Color(0xFFE2E8F0),
                  shape = RoundedCornerShape(12.dp)
                )
                .clickable { selectedRegion = region }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = region.label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF475569)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Currencies List
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (filteredCurrencies.isEmpty()) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 30.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(36.dp)
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "No currencies found matching \"$searchQuery\"",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                  )
                }
              }
            }
          }

          items(filteredCurrencies, key = { it.code }) { item ->
            val isSelected = (item.code == selectedCode)
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .shadow(
                  elevation = if (isSelected) 6.dp else 1.dp,
                  shape = RoundedCornerShape(16.dp),
                  ambientColor = if (isSelected) Color(0x33029766) else Color(0x0A000000),
                  spotColor = if (isSelected) Color(0x40029766) else Color(0x14000000)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) Color(0xFFF0FDF8) else Color.White)
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) Color(0xFF029766) else Color(0xFFE2E8F0),
                  shape = RoundedCornerShape(16.dp)
                )
                .clickable { selectedCode = item.code }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("currency_option_${item.code}")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Text(
                    text = item.flagEmoji,
                    fontSize = 24.sp
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = item.code,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001A49)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "(${item.symbol})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF029766)
                      )
                    }
                    Text(
                      text = "${item.name} • ${item.country}",
                      fontSize = 11.sp,
                      color = Color(0xFF64748B),
                      maxLines = 1
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFF029766) else Color.Transparent)
                    .border(
                      width = 1.5.dp,
                      color = if (isSelected) Color(0xFF029766) else Color(0xFFCBD5E1),
                      shape = CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Continue Button
        Button(
          onClick = { onContinue(selectedCode) },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
              elevation = 10.dp,
              shape = RoundedCornerShape(26.dp),
              ambientColor = Color(0x55027858),
              spotColor = Color(0x80029766)
            )
            .testTag("currency_continue_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF029766)
          ),
          shape = RoundedCornerShape(26.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Continue with $selectedCode",
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

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}
