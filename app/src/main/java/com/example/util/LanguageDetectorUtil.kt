package com.example.util

import java.util.Locale

enum class DetectedLanguage {
    BENGALI,
    BANGLISH,
    ENGLISH
}

object LanguageDetectorUtil {
    // Bengali Unicode range: 0x0980 to 0x09FF
    private val bengaliCharRegex = Regex("[\\u0980-\\u09FF]")

    // Common Banglish keyword markers
    private val banglishKeywords = listOf(
        "kemon", "acho", "achen", "ki", "koro", "korcho", "korchen", "amake", "tomake", "apnake",
        "tumi", "apni", "ami", "amra", "kothay", "kobe", "keno", "kibhabe", "kivabe", "dhonnobad",
        "bhalo", "valo", "shundor", "khobor", "shob", "thik", "ache", "nai", "jani", "dekho",
        "bolun", "bolo", "ekta", "kore", "dao", "din", "shunen", "shuno", "khabar", "porashona",
        "kaj", "somoy", "shomoy", "kichu", "sob", "bujhte", "partesi", "parchi", "hobe", "hobena",
        "onek", "khub", "shathe", "sathe", "shobaike", "shokal", "raat", "bikel", "dupur"
    )

    fun detectLanguage(text: String): DetectedLanguage {
        if (text.isBlank()) return DetectedLanguage.ENGLISH

        var bengaliCharCount = 0
        var latinCharCount = 0

        for (char in text) {
            if (char.toString().matches(bengaliCharRegex)) {
                bengaliCharCount++
            } else if (char.isLetter() && char.code in 65..122) {
                latinCharCount++
            }
        }

        if (bengaliCharCount > 0 && bengaliCharCount >= latinCharCount * 0.3) {
            return DetectedLanguage.BENGALI
        }

        if (latinCharCount > 0) {
            val lowerText = text.lowercase(Locale.ROOT)
            val words = lowerText.split(Regex("[^a-z]+")).filter { it.isNotBlank() }
            val banglishMatchCount = words.count { word -> banglishKeywords.contains(word) }

            if (banglishMatchCount >= 1 || (words.isNotEmpty() && banglishMatchCount.toDouble() / words.size >= 0.15)) {
                return DetectedLanguage.BANGLISH
            }
        }

        return DetectedLanguage.ENGLISH
    }

    /**
     * Splits text into language-specific fragments (Bengali vs English)
     * for natural multi-language Text-To-Speech pronunciation.
     */
    data class TtsSegment(val text: String, val isBengali: Boolean)

    fun splitIntoTtsSegments(fullText: String): List<TtsSegment> {
        // Clean markdown symbols first for speech
        val cleanText = fullText
            .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), " [Code block skipped] ")
            .replace(Regex("`([^`]+)`"), "$1")
            .replace(Regex("[#*_~>\\[\\]]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanText.isBlank()) return emptyList()

        // Split by sentences (handling Bengali danda '।' and standard '.', '?', '!', '\n')
        val sentenceDelimiters = Regex("([।\\.\\?\\!\\n]+)")
        val tokens = cleanText.split(sentenceDelimiters).map { it.trim() }.filter { it.isNotBlank() }

        if (tokens.isEmpty()) {
            val isBn = cleanText.any { it.toString().matches(bengaliCharRegex) }
            return listOf(TtsSegment(cleanText, isBn))
        }

        val segments = mutableListOf<TtsSegment>()
        for (token in tokens) {
            val hasBengali = token.any { it.toString().matches(bengaliCharRegex) }
            segments.add(TtsSegment(token, hasBengali))
        }

        // Merge adjacent same-language segments for smoother audio playback
        val merged = mutableListOf<TtsSegment>()
        for (seg in segments) {
            if (merged.isNotEmpty() && merged.last().isBengali == seg.isBengali) {
                val prev = merged.removeAt(merged.size - 1)
                merged.add(TtsSegment("${prev.text}. ${seg.text}", prev.isBengali))
            } else {
                merged.add(seg)
            }
        }

        return merged
    }
}
