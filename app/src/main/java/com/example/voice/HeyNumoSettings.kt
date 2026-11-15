package com.example.voice

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray

data class HeyNumoVoiceOption(
  val id: String,
  val character: String,
  val description: String,
  val isRecommended: Boolean = false
)

/**
 * Persistent Voice Assistant settings & recent conversation history.
 */
object HeyNumoSettings {
  private const val PREFS_NAME = "hey_numo_voice_prefs"
  private const val KEY_WAKE_WORD = "wake_word_enabled"
  private const val KEY_VOICE_RESPONSE = "voice_response_enabled"
  private const val KEY_SPEECH_RATE = "speech_rate"
  private const val KEY_LANGUAGE_TAG = "language_tag"
  private const val KEY_HAPTIC = "haptic_feedback"
  private const val KEY_VOICE_NAME = "voice_name"
  private const val KEY_RECENT_PROMPTS = "recent_prompts"

  val voiceOptions = listOf(
    HeyNumoVoiceOption("Sulafat", "Warm · Female", "Warm, calm companion — default", isRecommended = true),
    HeyNumoVoiceOption("Vindemiatrix", "Gentle · Female", "Soft and reassuring"),
    HeyNumoVoiceOption("Achernar", "Soft · Female", "Calm and soothing"),
    HeyNumoVoiceOption("Aoede", "Breezy · Female", "Casual and conversational"),
    HeyNumoVoiceOption("Despina", "Smooth · Female", "Polished and professional"),
    HeyNumoVoiceOption("Achird", "Friendly · Male", "Friendly alternative")
  )

  private var prefs: SharedPreferences? = null

  var wakeWordEnabled by mutableStateOf(false)
    private set
  var voiceResponseEnabled by mutableStateOf(true)
    private set
  var speechRate by mutableFloatStateOf(1.0f)
    private set
  var languageTag by mutableStateOf("en-US")
    private set
  var hapticFeedbackEnabled by mutableStateOf(true)
    private set
  var selectedVoice by mutableStateOf("Sulafat")
    private set

  val recentPrompts = mutableStateListOf(
    "How much do I have left in Food?",
    "Can I save KES 2,000 this month?"
  )

  fun init(context: Context) {
    if (prefs != null) return
    val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs = p
    wakeWordEnabled = p.getBoolean(KEY_WAKE_WORD, false)
    voiceResponseEnabled = p.getBoolean(KEY_VOICE_RESPONSE, true)
    speechRate = p.getFloat(KEY_SPEECH_RATE, 1.0f).coerceIn(0.75f, 1.5f)
    languageTag = p.getString(KEY_LANGUAGE_TAG, "en-US") ?: "en-US"
    hapticFeedbackEnabled = p.getBoolean(KEY_HAPTIC, true)
    selectedVoice = p.getString(KEY_VOICE_NAME, "Sulafat") ?: "Sulafat"

    val rawPrompts = p.getString(KEY_RECENT_PROMPTS, null)
    if (!rawPrompts.isNullOrBlank()) {
      try {
        val arr = JSONArray(rawPrompts)
        if (arr.length() > 0) {
          recentPrompts.clear()
          for (i in 0 until arr.length()) {
            val item = arr.optString(i)
            if (item.isNotBlank()) recentPrompts.add(item)
          }
        }
      } catch (_: Exception) {
      }
    }
  }

  fun updateWakeWord(enabled: Boolean) {
    wakeWordEnabled = enabled
    prefs?.edit()?.putBoolean(KEY_WAKE_WORD, enabled)?.apply()
  }

  fun updateVoiceResponse(enabled: Boolean) {
    voiceResponseEnabled = enabled
    prefs?.edit()?.putBoolean(KEY_VOICE_RESPONSE, enabled)?.apply()
  }

  fun updateSpeechRate(rate: Float) {
    val clamped = rate.coerceIn(0.75f, 1.5f)
    speechRate = clamped
    prefs?.edit()?.putFloat(KEY_SPEECH_RATE, clamped)?.apply()
  }

  fun updateLanguage(tag: String) {
    languageTag = tag
    prefs?.edit()?.putString(KEY_LANGUAGE_TAG, tag)?.apply()
  }

  fun updateHaptic(enabled: Boolean) {
    hapticFeedbackEnabled = enabled
    prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
  }

  fun updateSelectedVoice(voice: String) {
    selectedVoice = voice
    prefs?.edit()?.putString(KEY_VOICE_NAME, voice)?.apply()
  }

  fun recordRecentPrompt(prompt: String) {
    val clean = prompt.trim()
    if (clean.isEmpty()) return
    recentPrompts.removeAll { it.equals(clean, ignoreCase = true) }
    recentPrompts.add(0, clean)
    if (recentPrompts.size > 6) {
      recentPrompts.removeAt(recentPrompts.lastIndex)
    }
    val arr = JSONArray()
    recentPrompts.forEach { arr.put(it) }
    prefs?.edit()?.putString(KEY_RECENT_PROMPTS, arr.toString())?.apply()
  }

  fun clearRecentPrompts() {
    recentPrompts.clear()
    prefs?.edit()?.remove(KEY_RECENT_PROMPTS)?.apply()
  }
}
