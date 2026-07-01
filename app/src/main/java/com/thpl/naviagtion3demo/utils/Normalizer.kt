package com.thpl.naviagtion3demo.utils

import org.apache.commons.codec.language.Metaphone
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import org.apache.commons.text.similarity.LevenshteinDistance
import java.text.Normalizer

// ════════════════════════════════════════════════════════════════
// 1. NORMALIZERS — Clean raw input before any comparison
// ════════════════════════════════════════════════════════════════




object TextNormalizer {

    fun normalizeName(input: String): String {
        return Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
            .replace(Regex("[\\p{InCombiningDiacriticalMarks}]"), "")
            .replace(Regex("[^a-zA-Z ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }

    fun normalizePhone(input: String): String {
        return input.replace(Regex("[^0-9]"), "")
    }

    fun normalizeEmail(input: String): String {
        return input.trim().lowercase()
    }

    fun normalizeGeneric(input: String): String {
        return input.trim().replace(Regex("\\s+"), " ").lowercase()
    }
}

// ════════════════════════════════════════════════════════════════
// 2. FIELD MATCHERS — Each field type has its own matching logic
// ════════════════════════════════════════════════════════════════

/**
 * Defines how a single field should be matched.
 * Each implementation encapsulates:
 *   - How to normalize that field type
 *   - Which algorithm(s) to use
 *   - How to combine sub-scores if multiple algorithms apply
 */
interface FieldMatcher {
    val fieldName: String

    /**
     * Returns a score from 0.0 (no match) to 1.0 (perfect match).
     * Receives RAW (un-normalized) strings — normalization is internal.
     */
    fun score(recordValue: String, query: String): Double
}


// --- NAME MATCHER ---
// Combines Jaro-Winkler (shape similarity) + Metaphone (phonetic similarity).
// "Vikks" vs "Vikas" → Jaro-Winkler alone gives ~0.82, but Metaphone catches
// that they sound identical (both encode to "VKS"), boosting the final score.
//
// For multi-word names ("Shalu Rathore"), it matches each query token against
// each name token and takes the best per-query-token score. This handles:
//   - Partial input: query "Rathore" matches "Shalu Rathore"
//   - Swapped order: query "Rathore Shalu" matches "Shalu Rathore"

class NameFieldMatcher(
    override val fieldName: String = "name"
) : FieldMatcher
{

    private val jaroWinkler = JaroWinklerSimilarity()
    private val metaphone = Metaphone()

    override fun score(recordValue: String, query: String): Double {
        val normRecord = TextNormalizer.normalizeName(recordValue)
        val normQuery = TextNormalizer.normalizeName(query)

        if (normRecord.isEmpty() || normQuery.isEmpty()) return 0.0

        val recordTokens = normRecord.split(" ").filter { it.isNotEmpty() }
        val queryTokens = normQuery.split(" ").filter { it.isNotEmpty() }

        if (queryTokens.isEmpty() || recordTokens.isEmpty()) return 0.0

        // For each query token, find its best match among record tokens
        val tokenScores = queryTokens.map { qt ->
            recordTokens.maxOf { rt -> singleTokenScore(rt, qt) }
        }

        return tokenScores.average()
    }

    private fun singleTokenScore(recordToken: String, queryToken: String): Double {
        // Base score: Jaro-Winkler (handles transpositions, insertions, deletions)
        var score = jaroWinkler.apply(recordToken, queryToken)

        // Phonetic bonus: if they sound alike, guarantee a floor of 0.90.
        // Catches "Vikks" → "Vikas", "Suresh" → "Sureesh"
        val recCode = try { metaphone.metaphone(recordToken) } catch (_: Exception) { "" }
        val queryCode = try { metaphone.metaphone(queryToken) } catch (_: Exception) { "" }
        if (recCode.isNotEmpty() && recCode == queryCode) {
            score = maxOf(score, 0.90)
        }

        // Prefix bonus: if the record starts with the query, small boost.
        // Catches incremental typing: "Sur" → "Suresh".
        // Skip when tokens are exactly equal (exact already = 1.0) and cap below
        // 1.0 so an exact token always outranks a prefixed-but-different token
        // (keeps "Vikas Sharma" above the phonetic variant "Vikash Sharma").
        if (recordToken != queryToken && recordToken.startsWith(queryToken) &&
            queryToken.length >= 2
        ) {
            score = minOf(0.99, score + 0.05)
        }

        return score
    }
}


// --- PHONE MATCHER ---
// Phone numbers have very different typo patterns than names:
//   - Digit transposition: 99147 → 99174 (most common, last digits)
//   - Missing/extra digit: 98260 → 9826 or 982600
//   - Wrong single digit: 98260 → 98360
//
// Jaro-Winkler is wrong here because it over-weights prefix matches.
// A phone number "9826099147" and "9826099174" share a long prefix but
// ARE DIFFERENT NUMBERS. We need Levenshtein (edit distance) which treats
// all positions equally.
//
// We also support partial/suffix matching: a user might type just the
// last 4-5 digits from memory.

class PhoneFieldMatcher(
    override val fieldName: String = "phone"
) : FieldMatcher
{

    private val levenshtein = LevenshteinDistance()

    override fun score(recordValue: String, query: String): Double {
        val normRecord = TextNormalizer.normalizePhone(recordValue)
        val normQuery = TextNormalizer.normalizePhone(query)

        if (normRecord.isEmpty() || normQuery.isEmpty()) return 0.0

        val recDigits = stripCountryCode(normRecord)
        val queryDigits = stripCountryCode(normQuery)

        if (queryDigits.isEmpty()) return 0.0

        // Minimum 3 digits to search — below that is too noisy
        if (queryDigits.length < 3) return 0.0

        // ── Strategy 1: Exact match ──
        if (recDigits == queryDigits) return 1.0

        // ── Strategy 2: Exact substring (no typo needed) ──
        if (queryDigits.length <= recDigits.length && recDigits.contains(queryDigits)) {
            return when {
                recDigits.startsWith(queryDigits) -> 1.0    // exact prefix
                recDigits.endsWith(queryDigits)   -> 0.95   // exact suffix
                else                              -> 0.90   // exact middle
            }
        }

        // ── Strategy 3: Sliding window fuzzy substring match (NEW) ──
        val swScore = slidingWindowScore(recDigits, queryDigits)

        // ── Strategy 4: Full Levenshtein (for near-length queries) ──
        val maxLen = maxOf(recDigits.length, queryDigits.length)
        val fullDist = levenshtein.apply(recDigits, queryDigits)
        val fullScore = if (maxLen == 0) 0.0
        else (maxLen - fullDist).toDouble() / maxLen.toDouble()

        return maxOf(swScore, fullScore)
    }

    /**
     * Slides a window of queryDigits.length across phoneDigits.
     * Returns the best score found.
     *
     * Example: query="9928", phone="9988776655"
     *   Window "9988" at pos 0 → edit_dist=1 → score 0.75 + prefix bonus 0.10 = 0.85
     *   Window "9887" at pos 1 → edit_dist=3 → score 0.25
     *   ...
     *   Best = 0.85
     */
    private fun slidingWindowScore(phoneDigits: String, queryDigits: String): Double {
        val qLen = queryDigits.length
        val pLen = phoneDigits.length

        // If query is longer than or equal to phone, sliding window doesn't apply
        if (qLen >= pLen) return 0.0

        var bestDist = qLen    // worst case: every digit wrong
        var bestPos = -1

        for (i in 0..pLen - qLen) {
            val window = phoneDigits.substring(i, i + qLen)
            val dist = levenshtein.apply(window, queryDigits)
            if (dist < bestDist) {
                bestDist = dist
                bestPos = i
            }
            // Early exit: can't do better than 0
            if (bestDist == 0) break
        }

        if (bestDist >= qLen) return 0.0  // every digit different = no match

        val baseScore = (qLen - bestDist).toDouble() / qLen.toDouble()

        // Position bonus: prefix matches are more valuable than middle/suffix
        // (People recall the beginning of their phone number most reliably)
        val positionBonus = when (bestPos) {
            0           -> 0.10   // prefix: strongest signal
            pLen - qLen -> 0.05   // suffix: moderate signal
            else        -> 0.0    // middle: no bonus
        }

        return minOf(1.0, baseScore + positionBonus)
    }

    /** Strip common Indian country codes. Extend for other countries. */
    private fun stripCountryCode(digits: String): String {
        return when {
            digits.length == 12 && digits.startsWith("91") -> digits.substring(2)
            digits.length == 11 && digits.startsWith("0")  -> digits.substring(1)
            else -> digits
        }
    }
}


// --- EMAIL MATCHER ---
// Splits at '@'. Local part uses Levenshtein (typos in username),
// domain uses exact match (typo in domain = completely different service).
// "shalurathore178@gmail.com" vs "shalurathore187@gmail.com" should score
// high because only the local part has a digit swap.

class EmailFieldMatcher(
    override val fieldName: String = "email"
) : FieldMatcher
{

    private val levenshtein = LevenshteinDistance()

    override fun score(recordValue: String, query: String): Double {
        val normRecord = TextNormalizer.normalizeEmail(recordValue)
        val normQuery = TextNormalizer.normalizeEmail(query)

        if (normRecord.isEmpty() || normQuery.isEmpty()) return 0.0

// If query doesn't contain '@', this is a username/domain token or a
        // word that happens to appear in an email — NOT an email-shaped query.
        if (!normQuery.contains("@")) {
            // Multi-word, non-email query (e.g. "Vikas Sharma"): email field
            // should not pretend to match it. Spurious Levenshtein scores here
            // are pure noise that pollutes ranking of name searches.
            if (normQuery.contains(" ")) return 0.0

            // Single token: a real email fragment is a substring of the address.
            if (normRecord.contains(normQuery)) return 0.9

            // Fuzzy fragment (username typo like "shalurathore187") only makes
            // sense when the query and the address are length-comparable; without
            // this guard, any short query vs a long email yields ~minLen/maxLen ≈
            // 0.45 just from the length-difference term of Levenshtein, leaking
            // unrelated text (e.g. "abcdefghxyz") through the threshold.
            val lenDiff = maxOf(normRecord.length, normQuery.length) -
                          minOf(normRecord.length, normQuery.length)
            if (normRecord.length == 0 || lenDiff > 0.3 * normRecord.length) return 0.0
            val maxLen = maxOf(normRecord.length, normQuery.length)
            val dist = levenshtein.apply(normRecord, normQuery)
            return (maxLen - dist).toDouble() / maxLen.toDouble()
        }

        val recParts = normRecord.split("@", limit = 2)
        val queryParts = normQuery.split("@", limit = 2)

        if (recParts.size != 2 || queryParts.size != 2) return 0.0

        // Domain must be a close match (1 edit max — "gmal.com" → "gmail.com")
        val domainDist = levenshtein.apply(recParts[1], queryParts[1])
        val domainScore = if (domainDist <= 1) 1.0 else 0.0

        // If domain is wrong, the whole email is wrong — no point scoring local part
        if (domainScore == 0.0) return 0.1

        // Local part: Levenshtein
        val localLen = maxOf(recParts[0].length, queryParts[0].length)
        val localDist = levenshtein.apply(recParts[0], queryParts[0])
        val localScore = if (localLen == 0) 1.0
        else (localLen - localDist).toDouble() / localLen.toDouble()

        // 70% local part, 30% domain (domain is a gate, not a gradient)
        return localScore * 0.70 + domainScore * 0.30
    }
}


// --- ID / CODE MATCHER ---
// For customer IDs, order numbers, etc. These are exact or prefix-exact.
// "CUST-001" shouldn't fuzzy-match "CUST-100" — that's a different customer.
// Only exact match or prefix match make sense here.

class IdFieldMatcher(
    override val fieldName: String = "id"
) : FieldMatcher
{

    override fun score(recordValue: String, query: String): Double {
        val normRecord = TextNormalizer.normalizeGeneric(recordValue)
        val normQuery = TextNormalizer.normalizeGeneric(query)

        return when {
            normRecord == normQuery -> 1.0                     // exact
            normRecord.startsWith(normQuery) -> 0.9            // prefix
            normRecord.contains(normQuery) -> 0.7              // substring
            else -> 0.0                                        // no match
        }
    }
}


// --- GENERIC / CITY / ADDRESS MATCHER ---
// Fallback for any text field: Jaro-Winkler with basic normalization.

class GenericFieldMatcher(
    override val fieldName: String = "generic"
) : FieldMatcher
{

    private val jaroWinkler = JaroWinklerSimilarity()

    override fun score(recordValue: String, query: String): Double {
        val normRecord = TextNormalizer.normalizeGeneric(recordValue)
        val normQuery = TextNormalizer.normalizeGeneric(query)

        if (normRecord.isEmpty() || normQuery.isEmpty()) return 0.0

        return jaroWinkler.apply(normRecord, normQuery)
    }
}


// ════════════════════════════════════════════════════════════════
// 3. SEARCH ENGINE — Orchestrates multi-field search
// ════════════════════════════════════════════════════════════════

data class SearchFieldConfig(
    val fieldName: String,
    val matcher: FieldMatcher,
    val weight: Double = 1.0,
    val threshold: Double = 0.3
)

data class SearchResult<T>(
    val record: T,
    val totalScore: Double,
    val fieldScores: Map<String, Double>,
    val bestField: String
)


class MultiFieldSearchEngine(
    private val fields: List<SearchFieldConfig>,
    private val overallThreshold: Double = 0.30,
    private val tieBreaker: Double = 0.3
)
{
    // Normalize weights so the highest weight = 1.0
    // This ensures the best possible single-field score is 1.0, not some fraction
    private val maxWeight = fields.maxOf { it.weight }

    fun <T> search(
        query: String,
        records: List<T>,
        recordToFields: (T) -> Map<String, String>
    ): List<SearchResult<T>> {

        if (query.isBlank()) return emptyList()

        return records.mapNotNull { record ->
            val fieldMap = recordToFields(record)
            val finalScores = mutableMapOf<String, Double>()  // raw 0..1 confidence per field

            for (fieldConfig in fields) {
                val recordValue = fieldMap[fieldConfig.fieldName]
                if (recordValue.isNullOrBlank()) continue

                // Score the WHOLE query against this field. Each FieldMatcher is
                // responsible for its own tokenization strategy (NameFieldMatcher
                // already matches per-token internally and averages). Do NOT also
                // take max() over individual query tokens here — doing so lets a
                // single perfect token (e.g. "shalu") wipe out a multi-word query's
                // intent, making "Shalu Rathore" tie with "Shalu Verma" at 1.0.
                val rawScore = fieldConfig.matcher.score(recordValue, query)

                // Per-field threshold gate: weak/noisy matches are zeroed.
                finalScores[fieldConfig.fieldName] =
                    if (rawScore >= fieldConfig.threshold) rawScore else 0.0
            }

            if (finalScores.isEmpty() || finalScores.values.all { it == 0.0 }) {
                return@mapNotNull null
            }

            // ── best_fields + tie_breaker aggregation ──
            // The record's primary confidence is the RAW score of its single
            // best-matching field. Weights do NOT cap the best field (that would
            // bury a lone perfect email/city match, e.g. "outlook"); instead,
            // weights act as CONFIRMATION bonuses for the OTHER matching fields,
            // so a query that agrees on multiple (weighted) fields is nudged up.
            val bestEntry = finalScores.entries.maxByOrNull { it.value }!!
            val bestRaw = bestEntry.value
            val bestField = bestEntry.key

            val otherWeightedSum = finalScores.entries
                .filter { it.key != bestField }
                .sumOf {
                    it.value * (fields.first { f -> f.fieldName == it.key }.weight / maxWeight)
                }

            val totalScore = bestRaw + tieBreaker * otherWeightedSum

            if (totalScore >= overallThreshold) {
                SearchResult(
                    record = record,
                    totalScore = totalScore,
                    fieldScores = finalScores.toMap(),
                    bestField = bestField
                )
            } else null
        }
            .sortedByDescending { it.totalScore }
    }
}

// ════════════════════════════════════════════════════════════════
// 4. CONVENIENCE — Pre-configured engine for typical customer search
// ════════════════════════════════════════════════════════════════

/**
 * Ready-to-use search engine for a customer database with
 * name, phone, email, city, and optional customer ID fields.
 */
object CustomerSearchEngine {

    private val engine = MultiFieldSearchEngine(
        fields = listOf(
            SearchFieldConfig(
                fieldName = "name",
                matcher = NameFieldMatcher(),
                weight = 5.0,       // Name is king — most searches are by name
                threshold = 0.6     // tightened: blocks unrelated words leaking in via name JW
            ),
            SearchFieldConfig(
                fieldName = "phone",
                matcher = PhoneFieldMatcher(),
                weight = 3.0,       // Phone is a strong identifier
                threshold = 0.45     // Higher threshold: partial digits are noisy
            ),
            SearchFieldConfig(
                fieldName = "email",
                matcher = EmailFieldMatcher(),
                weight = 2.0,
                threshold = 0.4
            ),
            SearchFieldConfig(
                fieldName = "city",
                matcher = GenericFieldMatcher(fieldName = "city"),
                weight = 1.0,       // Low weight: city is a filter, not a search key
                threshold = 0.7     // raised: Jaro's (m-t/2)/m term alone yields ~0.5
                                    // for unrelated text vs a short city name
            ),
            SearchFieldConfig(
                fieldName = "customerId",
                matcher = IdFieldMatcher(fieldName = "customerId"),
                weight = 4.0,       // High weight: if someone types an ID, they mean it
                threshold = 0.7     // Must be a strong match
            )
        ),
        overallThreshold = 0.40,
        tieBreaker = 0.3
    )

    /**
     * Search customers.
     *
     * @param query      Raw user input from a search box
     * @param customers  Your customer data (any type)
     * @param extractor  Lambda to extract fields from your data class
     * @return Scored, sorted results
     */
    fun <T> search(
        query: String,
        customers: List<T>,
        extractor: (T) -> Map<String, String>
    ): List<SearchResult<T>> {
        return engine.search(query, customers, extractor)
    }
}