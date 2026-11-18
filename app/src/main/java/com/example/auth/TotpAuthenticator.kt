package com.example.auth

import android.net.Uri
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Standards-compliant RFC 6238 TOTP (HMAC-SHA1, 6 digits, 30-second step)
 * and cryptographic backup-code generator/verifier.
 */
object TotpAuthenticator {
  private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
  private const val BACKUP_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
  private const val STEP_SECONDS = 30L
  private const val DIGITS = 6
  private val POW10 = intArrayOf(1, 10, 100, 1_000, 10_000, 100_000, 1_000_000)

  fun generateSecret(byteLength: Int = 20): String {
    val bytes = ByteArray(byteLength)
    SecureRandom().nextBytes(bytes)
    return encodeBase32(bytes)
  }

  fun buildOtpAuthUri(
    email: String,
    secret: String,
    issuer: String = "Numo"
  ): String {
    val account = email.trim().ifBlank { "account@numo.app" }
    val encodedLabel = Uri.encode("$issuer:$account")
    val encodedIssuer = Uri.encode(issuer)
    val cleanSecret = secret.trim().uppercase(Locale.US).replace("=", "")
    return "otpauth://totp/$encodedLabel?secret=$cleanSecret&issuer=$encodedIssuer&algorithm=SHA1&digits=$DIGITS&period=$STEP_SECONDS"
  }

  fun generateBackupCodes(count: Int = 8): List<String> {
    val rng = SecureRandom()
    return List(count) {
      val left = buildString(4) {
        repeat(4) { append(BACKUP_ALPHABET[rng.nextInt(BACKUP_ALPHABET.length)]) }
      }
      val right = buildString(4) {
        repeat(4) { append(BACKUP_ALPHABET[rng.nextInt(BACKUP_ALPHABET.length)]) }
      }
      "$left-$right"
    }
  }

  fun generateTotpCode(
    base32Secret: String,
    timeMillis: Long = System.currentTimeMillis()
  ): String {
    val counter = (timeMillis / 1000L) / STEP_SECONDS
    return generateCodeForCounter(base32Secret, counter)
  }

  fun verifyTotpCode(
    base32Secret: String,
    code: String,
    timeMillis: Long = System.currentTimeMillis(),
    windowSteps: Int = 1
  ): Boolean {
    val cleanCode = code.trim().replace(" ", "")
    if (cleanCode.length != DIGITS || !cleanCode.all { it.isDigit() }) return false
    val keyBytes = decodeBase32(base32Secret)
    if (keyBytes.isEmpty()) return false

    val currentCounter = (timeMillis / 1000L) / STEP_SECONDS
    for (offset in -windowSteps..windowSteps) {
      val candidate = generateCodeForCounter(keyBytes, currentCounter + offset)
      if (constantTimeEquals(candidate, cleanCode)) {
        return true
      }
    }
    return false
  }

  private fun generateCodeForCounter(base32Secret: String, counter: Long): String {
    val keyBytes = decodeBase32(base32Secret)
    return generateCodeForCounter(keyBytes, counter)
  }

  private fun generateCodeForCounter(keyBytes: ByteArray, counter: Long): String {
    val msg = ByteBuffer.allocate(8).putLong(counter).array()
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(SecretKeySpec(keyBytes, "HmacSHA1"))
    val hash = mac.doFinal(msg)

    val offset = hash[hash.size - 1].toInt() and 0x0F
    val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
      ((hash[offset + 1].toInt() and 0xFF) shl 16) or
      ((hash[offset + 2].toInt() and 0xFF) shl 8) or
      (hash[offset + 3].toInt() and 0xFF)
    val otp = binary % POW10[DIGITS]
    return otp.toString().padStart(DIGITS, '0')
  }

  fun encodeBase32(data: ByteArray): String {
    if (data.isEmpty()) return ""
    val output = StringBuilder((data.size * 8 + 4) / 5)
    var buffer = data[0].toInt() and 0xFF
    var next = 1
    var bitsLeft = 8
    while (bitsLeft > 0 || next < data.size) {
      if (bitsLeft < 5) {
        if (next < data.size) {
          buffer = (buffer shl 8) or (data[next++].toInt() and 0xFF)
          bitsLeft += 8
        } else {
          val pad = 5 - bitsLeft
          buffer = buffer shl pad
          bitsLeft += pad
        }
      }
      val index = (buffer shr (bitsLeft - 5)) and 0x1F
      bitsLeft -= 5
      output.append(BASE32_ALPHABET[index])
    }
    return output.toString()
  }

  fun decodeBase32(secret: String): ByteArray {
    val clean = secret.trim().uppercase(Locale.US)
      .replace("=", "")
      .replace(" ", "")
      .replace("-", "")
    if (clean.isEmpty()) return ByteArray(0)

    val out = ArrayList<Byte>((clean.length * 5) / 8)
    var buffer = 0
    var bitsLeft = 0
    for (ch in clean) {
      val value = BASE32_ALPHABET.indexOf(ch)
      if (value < 0) continue
      buffer = (buffer shl 5) or value
      bitsLeft += 5
      if (bitsLeft >= 8) {
        out.add(((buffer shr (bitsLeft - 8)) and 0xFF).toByte())
        bitsLeft -= 8
      }
    }
    return out.toByteArray()
  }

  private fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var diff = 0
    for (i in a.indices) {
      diff = diff or (a[i].code xor b[i].code)
    }
    return diff == 0
  }
}
