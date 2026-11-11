package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.LocalPhone
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.NumoFinancialEngine
import com.example.ui.animation.bouncyClickable
import com.example.ui.animation.formatAmount

/**
 * Send Money Modal allowing instant transfers from M-PESA or Bank Account
 */
@Composable
fun SendMoneyModal(
  currency: String = "KES",
  onDismiss: () -> Unit,
  onSentSuccess: () -> Unit
) {
  var recipientName by remember { mutableStateOf("") }
  var recipientPhone by remember { mutableStateOf("") }
  var amountInput by remember { mutableStateOf("") }
  var selectedAccount by remember { mutableStateOf("M-PESA") }
  var isSent by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 10.dp,
      modifier = Modifier.padding(12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F8F2)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = null,
                tint = Color(0xFF02844E),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Send Money",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isSent) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F8F2)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF02844E), modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("Transfer Sent!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              "$currency ${formatAmount(amountInput.toDoubleOrNull()?.toInt() ?: 0)} successfully sent to $recipientName ($recipientPhone).",
              fontSize = 13.sp,
              color = Color(0xFF64748B),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
              onClick = {
                onSentSuccess()
                onDismiss()
              },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Done")
            }
          }
        } else {
          OutlinedTextField(
            value = recipientName,
            onValueChange = { recipientName = it },
            label = { Text("Recipient Name") },
            placeholder = { Text("e.g. John Doe") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = recipientPhone,
            onValueChange = { recipientPhone = it },
            label = { Text("Phone / Account Number") },
            placeholder = { Text("e.g. 0712 345 678") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it },
            label = { Text("Amount ($currency)") },
            placeholder = { Text("0.00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(14.dp))

          Text("From Account:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF475569))
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf("M-PESA", "KCB Bank", "Cash").forEach { acc ->
              val selected = (selectedAccount == acc)
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (selected) Color(0xFFE8F8F2) else Color(0xFFF8FAFC))
                  .border(1.dp, if (selected) Color(0xFF02844E) else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                  .bouncyClickable { selectedAccount = acc }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = acc,
                  fontSize = 12.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  color = if (selected) Color(0xFF02844E) else Color(0xFF475569)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(22.dp))

          Button(
            onClick = {
              val amt = amountInput.toDoubleOrNull() ?: 0.0
              if (amt > 0) {
                NumoFinancialEngine.addTransaction(
                  title = "Transfer to ${recipientName.ifBlank { "Recipient" }}",
                  amount = amt,
                  isIncome = false,
                  category = "Transfer",
                  account = selectedAccount,
                  note = "Ref: $recipientPhone"
                )
                isSent = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("btn_confirm_send"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
            shape = RoundedCornerShape(14.dp)
          ) {
            Text("Send Now", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

/**
 * Receive / Request Money Modal showing Paybill, Account, and QR Code
 */
@Composable
fun ReceiveMoneyModal(
  currency: String = "KES",
  onDismiss: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current
  var copiedText by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 10.dp,
      modifier = Modifier.padding(12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Receive Money",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QR Code Placeholder Card
        Box(
          modifier = Modifier
            .size(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.QrCode,
              contentDescription = "QR Code",
              tint = Color(0xFF02844E),
              modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("Scan to Pay Ayesha Khan", fontSize = 11.sp, color = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Details Box
        Surface(
          modifier = Modifier.fillMaxWidth(),
          color = Color(0xFFF1F5F9),
          shape = RoundedCornerShape(14.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("M-PESA Paybill", fontSize = 11.sp, color = Color(0xFF64748B))
                Text("522522", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              IconButton(onClick = {
                clipboardManager.setText(AnnotatedString("522522"))
                copiedText = "Paybill 522522 copied"
                Toast.makeText(context, "Paybill copied!", Toast.LENGTH_SHORT).show()
              }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF02844E), modifier = Modifier.size(18.dp))
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Account Number", fontSize = 11.sp, color = Color(0xFF64748B))
                Text("1289476251", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              }
              IconButton(onClick = {
                clipboardManager.setText(AnnotatedString("1289476251"))
                copiedText = "Account 1289476251 copied"
                Toast.makeText(context, "Account number copied!", Toast.LENGTH_SHORT).show()
              }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF02844E), modifier = Modifier.size(18.dp))
              }
            }
          }
        }

        if (copiedText != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(text = copiedText!!, fontSize = 12.sp, color = Color(0xFF02844E), fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Close")
        }
      }
    }
  }
}

/**
 * Quick Services Modal opened when tapping "More" on HomeScreen
 */
@Composable
fun QuickServicesModal(
  currency: String = "KES",
  onDismiss: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenSend: () -> Unit
) {
  val context = LocalContext.current

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 10.dp,
      modifier = Modifier.padding(12.dp)
    ) {
      Column(modifier = Modifier.padding(22.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Quick Services", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val services = listOf(
          Triple("Buy Airtime", Icons.Outlined.PhoneAndroid, "Top up Safaricom / Airtel"),
          Triple("Pay Bill / KPLC", Icons.Outlined.ElectricBolt, "Pay utility & electricity bills"),
          Triple("Bank Transfer", Icons.Outlined.AccountBalance, "Transfer between bank & M-PESA"),
          Triple("MMF Deposit", Icons.Outlined.Savings, "Deposit to Sanlam/Britam MMF")
        )

        services.forEach { (name, icon, desc) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .bouncyClickable {
                when (name) {
                  "Pay Bill / KPLC" -> {
                    onDismiss()
                    onOpenCalendar()
                  }
                  "Bank Transfer" -> {
                    onDismiss()
                    onOpenSend()
                  }
                  else -> {
                    Toast.makeText(context, "$name service ready", Toast.LENGTH_SHORT).show()
                    onDismiss()
                  }
                }
              }
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F8F2)),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = name, tint = Color(0xFF02844E), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
              Text(desc, fontSize = 11.sp, color = Color(0xFF64748B))
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
        }
      }
    }
  }
}

/**
 * Notifications Bell Modal showing real alerts
 */
@Composable
fun NotificationsBellModal(
  currency: String = "KES",
  onDismiss: () -> Unit,
  onPayBill: () -> Unit,
  onRebalance: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      shadowElevation = 10.dp,
      modifier = Modifier.padding(12.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF02844E))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notifications", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Alert 1: KPLC Bill Due
        NotificationItemCard(
          title = "KPLC Electricity Bill Due",
          body = "Bill of $currency 2,500 is due in 4 days.",
          actionText = "Pay Now",
          isWarning = false,
          onAction = {
            onDismiss()
            onPayBill()
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Alert 2: Food Overspend
        NotificationItemCard(
          title = "Budget Overspend Alert",
          body = "Food & Dining is over budget by $currency 1,500.",
          actionText = "Rebalance",
          isWarning = true,
          onAction = {
            onDismiss()
            onRebalance()
          }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Alert 3: Salary
        NotificationItemCard(
          title = "Income Received",
          body = "Salary of $currency 120,000 credited to Bank Account.",
          actionText = "View",
          isWarning = false,
          onAction = onDismiss
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E)),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Done")
        }
      }
    }
  }
}

@Composable
fun NotificationItemCard(
  title: String,
  body: String,
  actionText: String,
  isWarning: Boolean,
  onAction: () -> Unit
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = if (isWarning) Color(0xFFFFF1F2) else Color(0xFFF8FAFC),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isWarning) Color(0xFFFECDD3) else Color(0xFFE2E8F0))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isWarning) Color(0xFFE11D48) else Color(0xFF0F172A))
        Spacer(modifier = Modifier.height(2.dp))
        Text(body, fontSize = 11.sp, color = Color(0xFF64748B))
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(if (isWarning) Color(0xFFE11D48) else Color(0xFF02844E))
          .bouncyClickable { onAction() }
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Text(actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }
    }
  }
}

/**
 * Hands-Free Conversational Voice Assistant ("Hey Numo") Modal
 * Directly implementing PLAN.md Section 4.4:
 * - Hands-free simulated voice waveform
 * - Quick financial voice prompts
 * - Mandatory Action Confirmation Preview Card before database write!
 */
@Composable
fun HeyNumoVoiceAssistantModal(
  currency: String = "KES",
  onDismiss: () -> Unit
) {
  var transcription by remember { mutableStateOf("Listening for commands...") }
  var pendingAction by remember {
    mutableStateOf<Triple<String, Double, String>?>(null) // Title, Amount, Category
  }
  var isExecuted by remember { mutableStateOf(false) }

  val transition = rememberInfiniteTransition(label = "micPulse")
  val pulseScale by transition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding(),
      color = Color(0xFF0B1914).copy(alpha = 0.95f)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Hey Numo",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF34D399)
          )
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Pulsing Mic Circle
        Box(
          modifier = Modifier
            .size((90 * pulseScale).dp)
            .clip(CircleShape)
            .background(Color(0xFF02844E).copy(alpha = 0.35f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFF02844E)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Mic, contentDescription = "Mic", tint = Color.White, modifier = Modifier.size(34.dp))
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = transcription,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = Color.White,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Mandatory Action Confirmation Preview Card (PLAN.md Section 4.4)
        if (pendingAction != null) {
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF34D399))
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Text(
                text = "Action Confirmation Preview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF34D399)
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Action:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Text(pendingAction!!.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Amount:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Text("$currency ${formatAmount(pendingAction!!.second.toInt())}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Category:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Text(pendingAction!!.third, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
              Spacer(modifier = Modifier.height(6.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Account:", color = Color(0xFF94A3B8), fontSize = 13.sp)
                Text("M-PESA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              Spacer(modifier = Modifier.height(18.dp))

              if (isExecuted) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Committed to Ledger!", color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Button(
                    onClick = { pendingAction = null },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                  ) {
                    Text("Cancel")
                  }

                  Button(
                    onClick = {
                      NumoFinancialEngine.addTransaction(
                        title = pendingAction!!.first,
                        amount = pendingAction!!.second,
                        isIncome = false,
                        category = pendingAction!!.third,
                        account = "M-PESA",
                        note = "Voice Assistant entry"
                      )
                      isExecuted = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF02844E))
                  ) {
                    Text("Confirm")
                  }
                }
              }
            }
          }
        } else {
          // Voice Prompt Suggestions Chips
          Text(
            text = "Try saying or tapping:",
            fontSize = 13.sp,
            color = Color(0xFF94A3B8)
          )

          Spacer(modifier = Modifier.height(14.dp))

          val suggestions = listOf(
            Triple("Record 500 for Transport", 500.0, "Transport"),
            Triple("Record 1,200 for Groceries", 1200.0, "Food & Dining"),
            Triple("Pay KPLC Electricity Bill", 2500.0, "Bills & Utilities"),
            Triple("Deposit 5,000 to Emergency Fund", 5000.0, "Savings & Goals")
          )

          suggestions.forEach { (cmd, amt, cat) ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .bouncyClickable {
                  transcription = "\"$cmd\""
                  pendingAction = Triple(cmd, amt, cat)
                }
                .padding(vertical = 4.dp),
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF1E293B).copy(alpha = 0.8f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(cmd, fontSize = 13.sp, color = Color.White)
              }
            }
          }
        }
      }
    }
  }
}
