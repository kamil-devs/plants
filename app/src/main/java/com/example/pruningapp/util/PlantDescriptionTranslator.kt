package com.example.pruningapp.util

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Singleton wrapper around ML Kit EN→PL translator.
 * Always falls back to the original English text on any failure so the UI
 * never shows an empty description or crashes.
 */
object PlantDescriptionTranslator {

    private val translator by lazy {
        Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.POLISH)
                .build()
        )
    }

    suspend fun translate(text: String): String {
        if (text.isBlank()) return text
        return try {
            val modelReady = suspendCancellableCoroutine<Boolean> { cont ->
                translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
                    .addOnSuccessListener { cont.resume(true) }
                    .addOnFailureListener { cont.resume(false) }
            }
            if (!modelReady) return text

            suspendCancellableCoroutine { cont ->
                translator.translate(text)
                    .addOnSuccessListener { cont.resume(it) }
                    .addOnFailureListener { cont.resume(text) }
            }
        } catch (_: Exception) {
            text  // Safe fallback: return original English
        }
    }
}
