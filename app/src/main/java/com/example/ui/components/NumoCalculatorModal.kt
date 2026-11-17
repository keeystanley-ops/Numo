package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * Shared Numo Calculator Modal used across Expense, Income, Transfer, and Bill Payment screens.
 */
@Composable
fun NumoCalculatorModal(
  initialAmount: Double = 0.0,
  currency: String = "KES",
  accentColor: Color = Color(0xFFD81B60),
  onDismiss: () -> Unit,
  onApply: (Double) -> Unit
) {
  var expression by remember {
    mutableStateOf(
      if (initialAmount > 0.0) {
        if (abs(initialAmount - initialAmount.toLong()) < 0.001) {
          initialAmount.toLong().toString()
        } else {
          initialAmount.toString()
        }
      } else {
        ""
      }
    )
  }
  var errorText by remember { mutableStateOf<String?>(null) }

  val evalResult = remember(expression) { evaluateMathExpression(expression) }
  val formattedResult = remember(evalResult, currency) {
    val value = evalResult.getOrNull() ?: 0.0
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
      minimumFractionDigits = if (abs(value - value.toLong()) < 0.001) 0 else 2
      maximumFractionDigits = 2
    }
    "$currency ${nf.format(value)}"
  }

  fun appendDigitOrOp(token: String) {
    errorText = null
    val isOp = token in listOf("+", "-", "×", "÷")
    if (expression.isEmpty() && isOp) {
      if (token == "-") expression = "-"
      return
    }
    if (isOp && expression.isNotEmpty() && expression.last().toString() in listOf("+", "-", "×", "÷")) {
      expression = expression.dropLast(1) + token
      return
    }
    if (token == ".") {
      val lastSegment = expression.split("+", "-", "×", "÷").lastOrNull() ?: ""
      if (lastSegment.contains(".")) return
      if (lastSegment.isEmpty()) {
        expression += "0."
        return
      }
    }
    expression += token
  }

  fun applyResult() {
    if (expression.isBlank()) {
      onApply(0.0)
      onDismiss()
      return
    }
    val res = evaluateMathExpression(expression)
    res.onSuccess { value ->
      if (value < 0.0 || value.isNaN() || value.isInfinite()) {
        errorText = "Result must be positive"
      } else {
        onApply(value)
        onDismiss()
      }
    }.onFailure { err ->
      errorText = err.message ?: "Invalid expression"
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = NumoDS.PeachBgTop,
      shadowElevation = 16.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Calculator",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = NumoDS.TextPrimary
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = NumoDS.TextMuted)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Display Panel
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = NumoDS.CardSurfaceLight,
          shadowElevation = 2.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.End
          ) {
            Text(
              text = expression.ifBlank { "0" },
              fontSize = 16.sp,
              color = NumoDS.TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (errorText != null) errorText!! else formattedResult,
              fontSize = if (errorText != null) 15.sp else 28.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (errorText != null) NumoDS.ExpenseRed else NumoDS.TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad Grid
        val buttonGap = 10.dp
        Column(verticalArrangement = Arrangement.spacedBy(buttonGap)) {
          // Row 1: C, ÷, ×, ⌫
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonGap)
          ) {
            CalcKeyButton(
              label = "C",
              textColor = Color(0xFFD81B60),
              containerColor = Color(0xFFD81B60).copy(alpha = 0.12f),
              modifier = Modifier.weight(1f),
              onClick = {
                expression = ""
                errorText = null
              }
            )
            CalcKeyButton(
              label = "÷",
              textColor = NumoDS.TextPrimary,
              containerColor = NumoDS.CardSurface,
              modifier = Modifier.weight(1f),
              onClick = { appendDigitOrOp("÷") }
            )
            CalcKeyButton(
              label = "×",
              textColor = NumoDS.TextPrimary,
              containerColor = NumoDS.CardSurface,
              modifier = Modifier.weight(1f),
              onClick = { appendDigitOrOp("×") }
            )
            Box(
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(NumoDS.CardSurface)
                .clickable {
                  errorText = null
                  if (expression.isNotEmpty()) expression = expression.dropLast(1)
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = NumoDS.TextPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Rows 2-4 + tall "=" button on the right
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonGap)
          ) {
            // Left 3 columns (4 rows)
            Column(
              modifier = Modifier.weight(3f),
              verticalArrangement = Arrangement.spacedBy(buttonGap)
            ) {
              Row(horizontalArrangement = Arrangement.spacedBy(buttonGap)) {
                listOf("7", "8", "9").forEach { d ->
                  CalcKeyButton(d, NumoDS.TextPrimary, NumoDS.CardSurfaceLight, Modifier.weight(1f)) { appendDigitOrOp(d) }
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(buttonGap)) {
                listOf("4", "5", "6").forEach { d ->
                  CalcKeyButton(d, NumoDS.TextPrimary, NumoDS.CardSurfaceLight, Modifier.weight(1f)) { appendDigitOrOp(d) }
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(buttonGap)) {
                listOf("1", "2", "3").forEach { d ->
                  CalcKeyButton(d, NumoDS.TextPrimary, NumoDS.CardSurfaceLight, Modifier.weight(1f)) { appendDigitOrOp(d) }
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(buttonGap)) {
                CalcKeyButton("0", NumoDS.TextPrimary, NumoDS.CardSurfaceLight, Modifier.weight(2f)) { appendDigitOrOp("0") }
                CalcKeyButton(".", NumoDS.TextPrimary, NumoDS.CardSurfaceLight, Modifier.weight(1f)) { appendDigitOrOp(".") }
              }
            }

            // Right column: -, +, tall =/Done
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(buttonGap)
            ) {
              CalcKeyButton("-", NumoDS.TextPrimary, NumoDS.CardSurface, Modifier.fillMaxWidth()) { appendDigitOrOp("-") }
              CalcKeyButton("+", NumoDS.TextPrimary, NumoDS.CardSurface, Modifier.fillMaxWidth()) { appendDigitOrOp("+") }
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(118.dp)
                  .shadow(4.dp, RoundedCornerShape(18.dp))
                  .clip(RoundedCornerShape(18.dp))
                  .background(accentColor)
                  .clickable { applyResult() },
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "=",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Text(
                    text = "Done",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalcKeyButton(
  label: String,
  textColor: Color,
  containerColor: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .height(54.dp)
      .shadow(2.dp, RoundedCornerShape(16.dp))
      .clip(RoundedCornerShape(16.dp))
      .background(containerColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 20.sp,
      fontWeight = FontWeight.SemiBold,
      color = textColor,
      textAlign = TextAlign.Center
    )
  }
}

/**
 * Safe recursive-descent math evaluator supporting +, -, ×, ÷, *, / and decimals.
 */
fun evaluateMathExpression(raw: String): Result<Double> {
  val expr = raw.trim().replace("×", "*").replace("÷", "/").replace(" ", "")
  if (expr.isEmpty()) return Result.success(0.0)
  // Strip trailing operator for live preview
  val clean = if (expr.last() in listOf('+', '-', '*', '/')) expr.dropLast(1) else expr
  if (clean.isEmpty()) return Result.success(0.0)

  return try {
    var pos = 0
    fun peek(): Char? = if (pos < clean.length) clean[pos] else null

    fun parseNumber(): Double {
      val start = pos
      if (peek() == '-') pos++
      var dots = 0
      while (pos < clean.length && (clean[pos].isDigit() || clean[pos] == '.')) {
        if (clean[pos] == '.') dots++
        if (dots > 1) throw IllegalArgumentException("Invalid number")
        pos++
      }
      val sub = clean.substring(start, pos)
      return sub.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid syntax")
    }

    fun parseTerm(): Double {
      var x = parseNumber()
      while (peek() == '*' || peek() == '/') {
        val op = clean[pos++]
        val y = parseNumber()
        if (op == '*') x *= y
        else {
          if (abs(y) < 1e-9) throw IllegalArgumentException("Cannot divide by zero")
          x /= y
        }
      }
      return x
    }

    fun parseExpr(): Double {
      var x = parseTerm()
      while (peek() == '+' || peek() == '-') {
        val op = clean[pos++]
        val y = parseTerm()
        x = if (op == '+') x + y else x - y
      }
      return x
    }

    val result = parseExpr()
    if (pos != clean.length) throw IllegalArgumentException("Invalid expression")
    Result.success(result)
  } catch (e: Exception) {
    Result.failure(e)
  }
}
