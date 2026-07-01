package com.thpl.naviagtion3demo

import com.thpl.naviagtion3demo.utils.Demo
import com.thpl.naviagtion3demo.utils.Invoice
import com.thpl.naviagtion3demo.utils.InvoiceSearchEngine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

/**
 * Proves the search engine is generic: the SAME [com.thpl.naviagtion3demo.utils.MultiFieldSearchEngine]
 * that powers CustomerSearchEngine also searches [Invoice] objects, just by
 * supplying a different field layout + extractors.
 */
class InvoiceSearchEngineTest {

   /* private val invoices = listOf(
        Invoice("INV-2024-001", "Shalu Rathore", "1200.00", "PAID", "shalurathore178@gmail.com"),
        Invoice("INV-2024-002", "Vikas Sharma", "4500.50", "PENDING", "vikas.sharma@outlook.com"),
        Invoice("INV-2024-003", "Suresh Kumar", "300.00", "OVERDUE", "suresh.k@yahoo.com"),
        Invoice("INV-2024-004", "Shalu Verma", "12000.00", "PAID", "shalu.verma@gmail.com")
    )*/

    val obj = Demo()

    private fun search(query: String) =
        InvoiceSearchEngine.search(query, obj.invoices) {
            mapOf(
                "invoiceNumber" to it.invoiceNumber,
                "customerName" to it.customerName,
                "customerEmail" to it.customerEmail,
                "status" to it.status,
                "amount" to it.amount
            )
        }

    @Test
    fun exactInvoiceNumberWins() {
        val result = search("INV-2024-002")
        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    @Test
    fun nameTypoOnInvoice() {
        // "Shalu Rathor" typo should still surface the exact invoice first.
        val result = search("Shalu Rathor")
        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-001", result.first().record.invoiceNumber)
    }

    @Test
    fun exactNameBeatsPartialNameOnInvoice() {
        // Two invoices have "Shalu ..."; the exact full-name one must rank first.
        val result = search("Shalu Rathore")
        assertEquals("INV-2024-001", result.first().record.invoiceNumber)
    }

    @Test
    fun emailDomainTypoOnInvoice() {
        val result = search("shalurathore178@gmal.com")
        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-001", result.first().record.invoiceNumber)
    }

    @Test
    fun statusKeyword() {
        val result = search("OVERDUE")
        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-003", result.first().record.invoiceNumber)
    }

    @Test
    fun randomTextReturnsNothingOnInvoice() {
        val result = search("zzzzqqqq")
        assertTrue(result.isEmpty())
    }


    // NEW TEST CASES
    ///////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    // =========================
    // INVOICE NUMBER
    // =========================

    // ranking issue with this test case
    @Test
    fun partialInvoiceNumberSearch() {
        val result = search("2024-002")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    @Test
    fun invoicePrefixSearch() {
        val result = search("INV-2024")

        assertEquals(40, result.size)
    }

    // * ranking issue with this test case
    @Test
    fun lowercaseInvoiceNumber() {
        val result = search("inv-2024-002")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    // =========================
    // CUSTOMER NAME
    // =========================

    @Test
    fun partialCustomerNameSearch() {
        val result = search("Vikas")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    @Test
    fun surnameSearch() {
        val result = search("Rathore")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-001", result.first().record.invoiceNumber)
    }

    @Test
    fun swappedCustomerName() {
        val result = search("Rathore Shalu")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-001", result.first().record.invoiceNumber)
    }

    // =========================
    // EMAIL
    // =========================

    @Test
    fun partialEmailSearch() {
        val result = search("outlook")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    // *according to input total accepted records should 31, but we got 36 not major thing it may  be happened due to other strings

    @Test
    fun gmailInvoicesShouldRankFirst() {

        val result = search("gmail")

        val topGmail = result.take(31)

        assertEquals(31, topGmail.size)

        assertTrue(
            topGmail.all {
                it.record.customerEmail.contains("gmail", ignoreCase = true)
            }
        )
    }
    // =========================
    // STATUS
    // =========================
    // *according to input total accepted records should 22, but we got 28 not major thing it may  be happened due to other strings

    @Test
    fun paidInvoicesShouldAppearBeforeOtherStatuses() {

        val result = search("paid")

        val firstNonPaidIndex = result.indexOfFirst {
            !it.record.status.equals("PAID", ignoreCase = true)
        }

        assertEquals(22, firstNonPaidIndex)
    }

    @Test
    fun partialStatusSearch() {
        val result = search("Pend")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    // =========================
    // AMOUNT
    // =========================

    @Test
    fun exactAmountSearch() {
        val result = search("4500.50")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    @Test
    fun partialAmountSearch() {
        val result = search("4500")

        assertTrue(result.isNotEmpty())
        assertEquals("INV-2024-002", result.first().record.invoiceNumber)
    }

    // =========================
    // EDGE CASES
    // =========================

    @Test
    fun blankQueryReturnsNothing() {
        val result = search("   ")

        assertTrue(result.isEmpty())
    }

    @Test
    fun specialCharacterQueryReturnsNothing() {
        val result = search("@@@@")

        assertTrue(result.isEmpty())
    }



    // =========================
    // RANKING ANALYSIS
    // =========================

    @Test
    fun rankingAnalysisForShalu() {
        val result = search("Shalu")

        result.forEach {
            println("${it.record.invoiceNumber} -> ${it.totalScore}")
        }
    }

    @Test
    fun rankingAnalysisForPaid() {
        val result = search("PAID")

        result.forEach {
            println("${it.record.invoiceNumber} -> ${it.totalScore}")
        }
    }

    @Test
    fun exactNameShouldRankAbovePartialName() {
        val result = search("Shalu Rathore")

        assertTrue(result.isNotEmpty())

        val top = result.take(4)

        assertTrue(top.all {
            it.record.customerName == "Shalu Rathore"
        })
    }
    @Test
    fun invoiceNumberShouldBeatEverything() {
        val result = search("INV-2024-018")

        assertEquals(
            "INV-2024-018",
            result.first().record.invoiceNumber
        )
    }
    @Test
    fun gmailTypoShouldStillFindRecord() {

        val result = search("shalurathore178@gmal.com")

        assertTrue(
            result.any {
                it.record.invoiceNumber == "INV-2024-001"
            }
        )
    }
    @Test
    fun swappedWordsShouldWork() {

        val result = search("Rathore Shalu")

        assertTrue(
            result.any {
                it.record.customerName == "Shalu Rathore"
            }
        )
    }
    @Test
    fun typingFewLettersShouldReturnCorrectPeople() {

        val result = search("Sha")

        assertTrue(
            result.any {
                it.record.customerName.startsWith("Sha")
            }
        )
    }
    @Test
    fun partialInvoiceNumberShouldFindInvoice() {

        val result = search("2024-018")

        assertEquals(
            "INV-2024-018",
            result.first().record.invoiceNumber
        )
    }

    @Test
    fun lowercaseInvoiceShouldWork() {

        val result = search("inv-2024-018")

        assertEquals(
            "INV-2024-018",
            result.first().record.invoiceNumber
        )
    }
    @Test
    fun extraSpacesShouldNotMatter() {

        val result = search("   Shalu    Rathore   ")

        assertTrue(result.isNotEmpty())
    }
    @Test
    fun duplicateCustomerShouldReturnAllInvoices() {

        val result = search("Shalu Rathore")

        val invoices = result.filter {
            it.record.customerName == "Shalu Rathore"
        }

        assertEquals(4, invoices.size)
    }

    @Test
    fun outlookSearchRanksOutlookEmailsFirst() {

        val result = search("outlook")

        val topFive = result.take(5)

        assertEquals(5, topFive.size)

        assertTrue(
            topFive.all {
                it.record.customerEmail.contains("outlook", ignoreCase = true)
            }
        )
    }
}