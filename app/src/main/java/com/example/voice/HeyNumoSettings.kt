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

data class HeyNumoModelOption(
  val id: String,
  val label: String,
  val description: String,
  val isRecommended: Boolean = false
)

/**
 * Persistent Voice & Gemini AI Assistant settings and recent conversation history.
 */
object HeyNumoSettings {
  private const val PREFS_NAME = "hey_numo_voice_prefs"
  private const val KEY_WAKE_WORD = "wake_word_enabled"
  private const val KEY_VOICE_RESPONSE = "voice_response_enabled"
  private const val KEY_SPEECH_RATE = "speech_rate"
  private const val KEY_LANGUAGE_TAG = "language_tag"
  private const val KEY_HAPTIC = "haptic_feedback"
  private const val KEY_VOICE_NAME = "voice_name"
  private const val KEY_VOICE_MIGRATED_MALE_V1 = "voice_migrated_male_v1"
  private const val KEY_MODEL_NAME = "gemini_model_name"
  private const val KEY_RECENT_PROMPTS = "recent_prompts"

  const val DEFAULT_GEMINI_MODEL = "gemini-3.8-flash"
  const val DEFAULT_VOICE_NAME = "Charon"

  val modelOptions = listOf(
    HeyNumoModelOption(
      id = "gemini-3.8-flash",
      label = "Gemini 3.8 Flash",
      description = "Best all-around speed & financial reasoning (Default)",
      isRecommended = true
    ),
    HeyNumoModelOption(
      id = "gemini-3.5-flash-lite",
      label = "Gemini 3.5 Flash-Lite",
      description = "Ultra-fast responses for quick budget & cash checks"
    ),
    HeyNumoModelOption(
      id = "gemini-4-argon",
      label = "Gemini 4 Argon",
      description = "Deepest reasoning for portfolio, tax & debt strategy"
    )
  )

  val voiceOptions = listOf(
    HeyNumoVoiceOption("Charon", "Warm · Male", "Deep, calm Gemini Live tone — default", isRecommended = true),
    HeyNumoVoiceOption("Fenrir", "Rich Baritone · Male", "Expressive and assured"),
    HeyNumoVoiceOption("Orus", "Firm · Male", "Clear and confident"),
    HeyNumoVoiceOption("Puck", "Upbeat · Male", "Energetic and conversational"),
    HeyNumoVoiceOption("Achird", "Friendly · Male", "Warm and approachable"),
    HeyNumoVoiceOption("Sulafat", "Warm · Female", "Gentle conversational alternative")
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
  var selectedVoice by mutableStateOf(DEFAULT_VOICE_NAME)
    private set
  var selectedModel by mutableStateOf(DEFAULT_GEMINI_MODEL)
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

    val storedVoice = p.getString(KEY_VOICE_NAME, DEFAULT_VOICE_NAME) ?: DEFAULT_VOICE_NAME
    val migratedMale = p.getBoolean(KEY_VOICE_MIGRATED_MALE_V1, false)
    if (!migratedMale) {
      val upgraded = if (storedVoice == "Sulafat") DEFAULT_VOICE_NAME else storedVoice
      selectedVoice = if (voiceOptions.any { it.id == upgraded }) upgraded else DEFAULT_VOICE_NAME
      p.edit()
        .putString(KEY_VOICE_NAME, selectedVoice)
        .putBoolean(KEY_VOICE_MIGRATED_MALE_V1, true)
        .apply()
    } else {
      selectedVoice = if (voiceOptions.any { it.id == storedVoice }) storedVoice else DEFAULT_VOICE_NAME
    }

    val storedModel = p.getString(KEY_MODEL_NAME, DEFAULT_GEMINI_MODEL) ?: DEFAULT_GEMINI_MODEL
    selectedModel = if (modelOptions.any { it.id == storedModel }) storedModel else DEFAULT_GEMINI_MODEL

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
    val resolved = if (voiceOptions.any { it.id == voice }) voice else DEFAULT_VOICE_NAME
    selectedVoice = resolved
    prefs?.edit()
      ?.putString(KEY_VOICE_NAME, resolved)
      ?.putBoolean(KEY_VOICE_MIGRATED_MALE_V1, true)
      ?.apply()
  }

  fun updateSelectedModel(modelId: String) {
    val resolved = if (modelOptions.any { it.id == modelId }) modelId else DEFAULT_GEMINI_MODEL
    selectedModel = resolved
    prefs?.edit()?.putString(KEY_MODEL_NAME, resolved)?.apply()
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
