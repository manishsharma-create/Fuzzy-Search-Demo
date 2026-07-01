package com.thpl.naviagtion3demo.utils

/**
 * Example: a totally different domain object [Invoice] searched by the SAME
 * generic engine used for [Customer]. The only thing that changes is the
 * [SearchFieldConfig] list (which fields exist, which matcher each uses,
 * weight + threshold). No engine code is duplicated.
 */
data class Invoice(
    val invoiceNumber: String,   // e.g. "INV-2024-001"
    val customerName: String,     // free-text name on the invoice
    val amount: String,           // kept as String so a matcher can compare it
    val status: String,           // PAID / PENDING / OVERDUE
    val customerEmail: String
)

/**
 * Pre-configured search engine for [Invoice]. Intentionally different from
 * [CustomerSearchEngine] to prove the engine is domain-agnostic:
 *   - invoiceNumber is the strongest field (weight 5, strict IdFieldMatcher)
 *   - customerName uses the same NameFieldMatcher (typos + phonetics + token order)
 *   - customerEmail reuses EmailFieldMatcher
 *   - status uses a strict GenericFieldMatcher (exact keyword)
 *   - amount uses GenericFieldMatcher (so "1200" prefix-matches amount text)
 */
object InvoiceSearchEngine {

    private val engine = MultiFieldSearchEngine(
        fields = listOf(
            SearchFieldConfig(
                fieldName = "invoiceNumber",
                matcher = IdFieldMatcher(fieldName = "invoiceNumber"),
                weight = 5.0,
                threshold = 0.7
            ),
            SearchFieldConfig(
                fieldName = "customerName",
                matcher = NameFieldMatcher(fieldName = "customerName"),
                weight = 4.0,
                threshold = 0.6
            ),
            SearchFieldConfig(
                fieldName = "customerEmail",
                matcher = EmailFieldMatcher(fieldName = "customerEmail"),
                weight = 2.0,
                threshold = 0.4
            ),
            SearchFieldConfig(
                fieldName = "status",
                matcher = GenericFieldMatcher(fieldName = "status"),
                weight = 1.5,
                threshold = 0.8         // status is a keyword: require near-exact
            ),
            SearchFieldConfig(
                fieldName = "amount",
                matcher = GenericFieldMatcher(fieldName = "amount"),
                weight = 1.0,
                threshold = 0.7
            )
        ),
        overallThreshold = 0.40,
        tieBreaker = 0.3
    )

    fun <T> search(
        query: String,
        records: List<T>,
        extractor: (T) -> Map<String, String>
    ): List<SearchResult<T>> = engine.search(query, records, extractor)
}