package com.thpl.naviagtion3demo.utils

import org.apache.commons.codec.language.Metaphone
import org.apache.commons.codec.language.Soundex
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance




// 1. The Strategy Interface
interface FuzzyAlgorithm {
    fun getScore(s1: String, s2: String): Double
    val name: String
    val threshold: Double // Minimum score to show in list
}

// --- ALGO 1: Levenshtein (Edit Distance) ---
object LevenshteinAlgo : FuzzyAlgorithm {
    override val name = "Levenshtein"
    override val threshold = 0.4

    override fun getScore(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1
        if (longer.isEmpty()) return 1.0
        val distance = calculate(longer.lowercase(), shorter.lowercase())
        return (longer.length - distance).toDouble() / longer.length.toDouble()
    }

    private fun calculate(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }
        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                newCost[j] = minOf(cost[j - 1] + match, cost[j] + 1, newCost[j - 1] + 1)
            }
            val swap = cost; cost = newCost; newCost = swap
        }
        return cost[lhsLength]
    }
}

// --- ALGO 2: Jaro-Winkler (Prefix Preference) ---
object JaroWinklerAlgo : FuzzyAlgorithm {
    override val name = "Jaro-Winkler"
    override val threshold = 0.80

    override fun getScore(s1: String, s2: String): Double {
        return calculate(s1.lowercase(), s2.lowercase())
    }

    private fun calculate(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val matchDistance = (maxOf(s1.length, s2.length) / 2) - 1
        val s1Matches = BooleanArray(s1.length)
        val s2Matches = BooleanArray(s2.length)
        var matches = 0
        for (i in s1.indices) {
            val start = maxOf(0, i - matchDistance)
            val end = minOf(i + matchDistance + 1, s2.length)
            for (j in start until end) {
                if (!s2Matches[j] && s1[i] == s2[j]) {
                    s1Matches[i] = true; s2Matches[j] = true; matches++; break
                }
            }
        }
        if (matches == 0) return 0.0
        var t = 0; var k = 0
        for (i in s1.indices) {
            if (s1Matches[i]) {
                while (!s2Matches[k]) k++
                if (s1[i] != s2[k]) t++
                k++
            }
        }
        val m = matches.toDouble()
        val jaro = (m / s1.length + m / s2.length + (m - t / 2) / m) / 3.0
        var prefix = 0
        for (i in 0 until minOf(4, minOf(s1.length, s2.length))) {
            if (s1[i] == s2[i]) prefix++ else break
        }
        return jaro + prefix * 0.1 * (1.0 - jaro)
    }
}

// --- ALGO 3: Trigram / N-Gram (Good for swapped letters like "Sruesh") ---
object TrigramAlgo : FuzzyAlgorithm {
    override val name = "Trigram (3-letters)"
    override val threshold = 0.3 // Low threshold because Dice coefficient can be harsh

    override fun getScore(s1: String, s2: String): Double {
        val s1Grams = generateTrigrams(s1.lowercase())
        val s2Grams = generateTrigrams(s2.lowercase())

        if (s1Grams.isEmpty() || s2Grams.isEmpty()) return 0.0

        // Count intersection
        val intersection = s1Grams.intersect(s2Grams).size

        // Dice Coefficient Formula: (2 * intersection) / (total items)
        return (2.0 * intersection) / (s1Grams.size + s2Grams.size)
    }

    private fun generateTrigrams(text: String): Set<String> {
        if (text.length < 3) return emptySet()
        val result = mutableSetOf<String>()
        for (i in 0 until text.length - 2) {
            result.add(text.substring(i, i + 3))
        }
        return result
    }
}

// --- ALGO 4: Soundex (Phonetic - Good for "Vikks" vs "Vikas") ---
object SoundexAlgo : FuzzyAlgorithm {
    override val name = "Soundex (Phonetic)"
    override val threshold = 0.9 // Soundex is usually binary (1.0 or 0.0)

    override fun getScore(s1: String, s2: String): Double {
        val code1 = getSoundex(s1)
        val code2 = getSoundex(s2)
        return if (code1 == code2) 1.0 else 0.0
    }

    private fun getSoundex(s: String): String {
        if (s.isEmpty()) return ""
        val x = s.uppercase().toCharArray()
        val firstLetter = x[0]

        // Convert letters to numeric codes
        for (i in x.indices) {
            x[i] = when (x[i]) {
                'B', 'F', 'P', 'V' -> '1'
                'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2'
                'D', 'T' -> '3'
                'L' -> '4'
                'M', 'N' -> '5'
                'R' -> '6'
                else -> '0' // A, E, I, O, U, H, W, Y are 0
            }
        }

        // Remove duplicates and zeros
        val output = StringBuilder().append(firstLetter)
        for (i in 1 until x.size) {
            if (x[i] != x[i - 1] && x[i] != '0') {
                output.append(x[i])
            }
        }

        // Pad with zeros to ensure length is 4 (Standard Soundex format)
        output.append("0000")
        return output.substring(0, 4)
    }
}


// --- ALGO 5: Character Match (Order doesn't matter) ---
// "hserus" will match "Suresh" perfectly (1.0)
object CharMatchAlgo : FuzzyAlgorithm {
    override val name = "Character Match (Jumble)"
    override val threshold = 0.5 // 50% letters should match

    override fun getScore(s1: String, s2: String): Double {
        val original = s1.lowercase().toCharArray().toMutableList()
        val query = s2.lowercase().toCharArray().toList()

        var matches = 0

        // Check how many letters from Query exist in Original Name
        for (char in query) {
            if (original.contains(char)) {
                matches++
                original.remove(char) // Remove so we don't count 's' twice if name has only one 's'
            }
        }

        // Formula: (2 * Matches) / (Length of Name + Length of Query)
        // This gives 1.0 if they are exact anagrams, and lower if letters are missing/extra
        val totalLength = s1.length + s2.length
        if (totalLength == 0) return 0.0

        return (2.0 * matches) / totalLength
    }
}

// --- ALGO 6: Unordered Character Subset (What you asked for) ---
// Query "su" matches "Suresh" (has s, u) AND "Lotus" (has u, s)
object UnorderedContainsAlgo : FuzzyAlgorithm {
    override val name = "Contains All Chars (Any Order)"
    override val threshold = 0.99 // Strict: Must contain ALL characters

    override fun getScore(s1: String, s2: String): Double {
        val nameChars = s1.lowercase().toMutableList() // Use MutableList to handle duplicates
        val queryChars = s2.lowercase().toCharArray()

        var matchesFound = 0

        for (char in queryChars) {
            if (nameChars.contains(char)) {
                matchesFound++
                // OPTIONAL: Remove the char if you want "bob" to require two 'b's
                // nameChars.remove(char)
            }
        }

        // If we found ALL characters from the query inside the name, return 1.0
        return if (matchesFound == queryChars.size) 1.0 else 0.0
    }
}

// --- Algo 1: Professional Levenshtein (Using Apache Commons Text) ---
object ApacheLevenshtein : FuzzyAlgorithm {
    override val name = "Apache Levenshtein"
    override val threshold = 0.4

    // Object create karne ki zarurat nahi, ye library ka class hai
    private val leven = LevenshteinDistance()

    override fun getScore(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        if (longer.isEmpty()) return 1.0

        // Direct Library Call
        val distance = leven.apply(s1.lowercase(), s2.lowercase())

        return (longer.length - distance).toDouble() / longer.length.toDouble()
    }
}

// --- Algo 2: Professional Jaro-Winkler (Using Apache Commons Text) ---
object ApacheJaroWinkler : FuzzyAlgorithm {
    override val name = "Apache Jaro-Winkler"
    override val threshold = 0.85

    private val jaro = JaroWinklerSimilarity()

    override fun getScore(s1: String, s2: String): Double {
        // Direct Library Call (Returns 0.0 to 1.0 automatically)
        return jaro.apply(s1.lowercase(), s2.lowercase())
    }
}

// --- Algo 3: Professional Soundex (Using Apache Commons Codec) ---
object ApacheSoundex : FuzzyAlgorithm {
    override val name = "Apache Soundex"
    override val threshold = 0.9

    private val soundex = Soundex()

    override fun getScore(s1: String, s2: String): Double {
        try {
            val code1 = soundex.soundex(s1)
            val code2 = soundex.soundex(s2)
            return if (code1 == code2) 1.0 else 0.0
        } catch (e: Exception) {
            return 0.0 // Handle special characters
        }
    }
}

// --- Algo 4: Metaphone (Better than Soundex) ---
// Soundex purana hai, Metaphone complex words ke liye better hai
object ApacheMetaphone : FuzzyAlgorithm {
    override val name = "Apache Metaphone"
    override val threshold = 0.9

    private val metaphone = Metaphone()

    override fun getScore(s1: String, s2: String): Double {
        val code1 = metaphone.metaphone(s1)
        val code2 = metaphone.metaphone(s2)
        return if (code1 == code2) 1.0 else 0.0
    }
}