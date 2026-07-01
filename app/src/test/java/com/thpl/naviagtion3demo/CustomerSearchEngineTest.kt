package com.thpl.naviagtion3demo

import com.thpl.naviagtion3demo.utils.Customer
import com.thpl.naviagtion3demo.utils.CustomerSearchEngine
import com.thpl.naviagtion3demo.utils.Demo
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CustomerSearchEngineTest {

    val obj = Demo()

    private fun search(query: String) =
        CustomerSearchEngine.search(query, obj.customers) {
            mapOf(
                "customerId" to it.id,
                "name" to it.name,
                "phone" to it.phone,
                "email" to it.email,
                "city" to it.city
            )
        }

    // Same name found
    @Test
    fun exactNameMatch() {
        val result = search("Shalu Rathore")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    // Issue: Incorrect ranking in search results.
    // When searching for "Shalu Rathore", the expected top result is CUST-001 (Shalu Rathore). However, the search engine ranks CUST-008 (Shalu Verma) higher and returns it as the first result.
    // Expected: CUST-001 (Shalu Rathore)
    // Actual: CUST-008 (Shalu Verma)
    // Impact: Users may see less relevant results before the exact or closest match, which can affect search accuracy and user experience.
   @Test
    fun typoNameMatch() {
        val result = search("Shalu Rathor")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun phoneSuffixMatch() {
        val result = search("9147")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun emailDomainTypo() {
        val result = search("shalurathore178@gmal.com")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }


    @Test
    fun emptyQueryReturnsNothing() {
        val result = search("")

        assertTrue(result.isEmpty())
    }

    @Test
    fun gmailShouldReturnOnlyGmailUsers() {
        val result = search("gmail")

        result.forEach {
            println("${it.record.id} -> ${it.totalScore}")
        }
    }

    // =========================
// NAME SEARCH TESTS
// =========================

    @Test
    fun partialFirstNameMatch() {
        val result = search("Shalu")

        assertTrue(result.isNotEmpty())

        val ids = result.map { it.record.id }

        assertTrue(ids.contains("CUST-001"))
        assertTrue(ids.contains("CUST-005"))
        assertTrue(ids.contains("CUST-007"))
        assertTrue(ids.contains("CUST-008"))
    }

    @Test
    fun partialLastNameMatch() {
        val result = search("Rathore")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun swappedNameOrder() {
        val result = search("Rathore Shalu")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun phoneticMatch() {
        val result = search("Vikash")

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun caseInsensitiveSearch() {
        val result = search("SHALU RATHORE")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

// =========================
// PHONE SEARCH TESTS
// =========================

    @Test
    fun phonePrefixMatch() {
        val result = search("9826")

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun phoneWithCountryCode() {
        val result = search("+91 9826099147")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun phoneWithLeadingZero() {
        val result = search("09826099147")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun phoneTypoMatch() {
        val result = search("9826099174")

        assertTrue(result.isNotEmpty())
    }

// =========================
// EMAIL SEARCH TESTS
// =========================

    @Test
    fun gmailSearch() {
        val result = search("gmail")

        assertTrue(result.isNotEmpty())
    }

    // Issue: Incorrect ranking in search results.mainly due to name ranking real result come at 10th position
   @Test
    fun outlookSearch() {
        val result = search("outlook")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-003", result.first().record.id)
    }

    @Test
    fun partialEmailSearch() {
        val result = search("shalurathore178")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

// =========================
// CITY SEARCH TESTS
// =========================

    @Test
    fun citySearchIndore() {
        val result = search("Indore")

        assertTrue(result.size >= 2)
    }

    @Test
    fun citySearchMumbai() {
        val result = search("Mumbai")

        assertTrue(result.isNotEmpty())
    }

// =========================
// CUSTOMER ID SEARCH
// =========================

    @Test
    fun exactCustomerIdMatch() {
        val result = search("CUST-001")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    @Test
    fun partialCustomerIdMatch() {
        val result = search("001")

        assertTrue(result.isNotEmpty())
    }

// =========================
// RANKING TESTS (IMPORTANT)
// =========================

    @Test
    fun exactMatchShouldRankAboveSimilarNames() {
        val result = search("Shalu Rathore")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-001", result.first().record.id)
    }

    // same ranking issue accepted result comes on 3 position instead of 1
 @Test
    fun exactVikasShouldRankAboveVariants() {
        val result = search("Vikas Sharma")

        assertTrue(result.isNotEmpty())
        assertEquals("CUST-003", result.first().record.id)
    }

    @Test
    fun cityAndNameCombinationShouldPreferBothMatches() {
        val result = search("Vikas Indore")

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun cityAndNameCombinationShouldPreferCorrectCustomer() {
        val result = search("Shalu Indore")

        assertTrue(result.isNotEmpty())
    }

// =========================
// EDGE CASES
// =========================

    @Test
    fun blankSpacesQuery() {
        val result = search("     ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun specialCharactersQuery() {
        val result = search("@@@@")

        assertTrue(result.isEmpty())
    }

    // Ideally, this query should return no results. The current overallThreshold (0.30)
    // may be too low, causing unrelated records to pass the relevance filter.
@Test
    fun randomTextQuery() {
        val result = search("abcdefghxyz")

        assertTrue(result.isEmpty())
    }

    @Test
    fun veryShortPhoneQuery() {
        val result = search("91")

        assertTrue(result.isEmpty())
    }

// =========================
// BUG HUNTING TESTS
// =========================

    @Test
    fun conflictingQueryShaluMumbai() {
        val result = search("Shalu Mumbai")

        result.forEach {
            println("${it.record.id} -> ${it.totalScore}")
        }
    }

    @Test
    fun conflictingQueryVikasIndore() {
        val result = search("Vikas Indore")

        result.forEach {
            println("${it.record.id} -> ${it.totalScore}")
        }
    }

    @Test
    fun rankingAnalysisForShalu() {
        val result = search("Shalu")

        result.forEach {
            println("${it.record.name} -> ${it.totalScore}")
        }
    }
}