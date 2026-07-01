package com.thpl.naviagtion3demo.utils

import android.util.Log

class Demo {
     val customers = listOf(
        Customer("CUST-001", "Shalu Rathore", "9826099147", "shalurathore178@gmail.com", "Indore"),
        Customer("CUST-002", "Suresh Kumar", "9876543210", "suresh.k@yahoo.com", "Ahmedabad"),
        Customer("CUST-003", "Vikas Sharma", "9988776655", "vikas.sharma@outlook.com", "Mumbai"),
        Customer("CUST-004", "Shilpa Rao", "9826012345", "shilpa.rao@gmail.com", "Indore"),

        // Similar names
        Customer("CUST-005", "Shalu Sharma", "9811111111", "shalu.sharma@gmail.com", "Mumbai"),
        Customer("CUST-006", "Shailu Rathod", "9811111112", "shailu.rathod@gmail.com", "Indore"),
        Customer("CUST-007", "Shalu Rathod", "9811111113", "shalu.rathod@gmail.com", "Ujjain"),
        Customer("CUST-008", "Shalu Verma", "9811111114", "shalu.verma@gmail.com", "Jaipur"),

        // Similar surname
        Customer("CUST-009", "Vikas Rathore", "9811111115", "vikas.r@gmail.com", "Indore"),
        Customer("CUST-010", "Vikas Kumar", "9811111116", "vikas.k@gmail.com", "Ahmedabad"),
        Customer("CUST-011", "Vikash Sharma", "9811111117", "vikash.sharma@gmail.com", "Mumbai"),
    )

     val invoices = listOf(

        // Original Records
        Invoice("INV-2024-001", "Shalu Rathore", "1200.00", "PAID", "shalurathore178@gmail.com"),
        Invoice("INV-2024-002", "Vikas Sharma", "4500.50", "PENDING", "vikas.sharma@outlook.com"),
        Invoice("INV-2024-003", "Suresh Kumar", "300.00", "OVERDUE", "suresh.k@yahoo.com"),
        Invoice("INV-2024-004", "Shalu Verma", "12000.00", "PAID", "shalu.verma@gmail.com"),

        // Same Customer - Multiple Invoices
        Invoice("INV-2024-005", "Shalu Rathore", "500.00", "PENDING", "shalurathore178@gmail.com"),
        Invoice("INV-2024-006", "Shalu Rathore", "9000.00", "OVERDUE", "shalurathore178@gmail.com"),

        Invoice("INV-2024-007", "Vikas Sharma", "850.00", "PAID", "vikas.sharma@outlook.com"),
        Invoice("INV-2024-008", "Vikas Sharma", "1500.00", "PAID", "vikas.sharma@outlook.com"),

        // Similar Names
        Invoice("INV-2024-009", "Shalu Sharma", "2100.00", "PAID", "shalu.sharma@gmail.com"),
        Invoice("INV-2024-010", "Shailu Rathod", "3100.00", "PAID", "shailu.rathod@gmail.com"),
        Invoice("INV-2024-011", "Shalu Rathod", "4100.00", "PENDING", "shalu.rathod@gmail.com"),
        Invoice("INV-2024-012", "Shalu Patel", "5100.00", "OVERDUE", "shalu.patel@gmail.com"),

        Invoice("INV-2024-013", "Vikash Sharma", "1800.00", "PAID", "vikash.sharma@gmail.com"),
        Invoice("INV-2024-014", "Vikas Rathore", "2700.00", "PENDING", "vikas.r@gmail.com"),
        Invoice("INV-2024-015", "Vikas Kumar", "6200.00", "OVERDUE", "vikas.k@gmail.com"),

        // Similar Invoice Numbers
        Invoice("INV-2024-016", "Rahul Verma", "900.00", "PAID", "rahul.verma@gmail.com"),
        Invoice("INV-2024-017", "Rahul Sharma", "1100.00", "PENDING", "rahul.sharma@gmail.com"),
        Invoice("INV-2024-018", "Rahul Rathore", "2100.00", "PAID", "rahul.r@gmail.com"),

        // Duplicate Amounts
        Invoice("INV-2024-019", "Ankit Jain", "1200.00", "PAID", "ankit@gmail.com"),
        Invoice("INV-2024-020", "Rohit Gupta", "1200.00", "PENDING", "rohit@gmail.com"),
        Invoice("INV-2024-021", "Priya Sharma", "1200.00", "OVERDUE", "priya@gmail.com"),

        // Duplicate Status
        Invoice("INV-2024-022", "Neha Singh", "7800.00", "PAID", "neha@gmail.com"),
        Invoice("INV-2024-023", "Pooja Yadav", "2400.00", "PAID", "pooja@gmail.com"),
        Invoice("INV-2024-024", "Rakesh Sharma", "6400.00", "PAID", "rakesh@gmail.com"),

        Invoice("INV-2024-025", "Karan Patel", "8200.00", "PENDING", "karan@gmail.com"),
        Invoice("INV-2024-026", "Deepak Joshi", "9100.00", "PENDING", "deepak@gmail.com"),

        Invoice("INV-2024-027", "Amit Tiwari", "3500.00", "OVERDUE", "amit@gmail.com"),
        Invoice("INV-2024-028", "Amit Sharma", "3600.00", "OVERDUE", "amit.sharma@gmail.com"),

        // Gmail Heavy Dataset
        Invoice("INV-2024-029", "Raj Patel", "5000.00", "PAID", "raj@gmail.com"),
        Invoice("INV-2024-030", "Raj Sharma", "5100.00", "PAID", "raj.sharma@gmail.com"),
        Invoice("INV-2024-031", "Raj Verma", "5200.00", "PENDING", "raj.verma@gmail.com"),

        // Yahoo
        Invoice("INV-2024-032", "Sunil Kumar", "800.00", "PAID", "sunil@yahoo.com"),
        Invoice("INV-2024-033", "Manoj Kumar", "900.00", "PENDING", "manoj@yahoo.com"),

        // Outlook
        Invoice("INV-2024-034", "Kunal Shah", "1300.00", "PAID", "kunal@outlook.com"),
        Invoice("INV-2024-035", "Mohit Shah", "1500.00", "PENDING", "mohit@outlook.com"),

        // More Ranking Cases
        Invoice("INV-2024-036", "Shalu Rathore", "1200.50", "PAID", "shalurathore178@gmail.com"),
        Invoice("INV-2024-037", "Shalu Verma", "1200.10", "PAID", "shalu.verma@gmail.com"),
        Invoice("INV-2024-038", "Shalu Sharma", "1200.20", "PAID", "shalu.sharma@gmail.com"),
        Invoice("INV-2024-039", "Shailu Rathod", "1200.30", "PAID", "shailu.rathod@gmail.com"),

        // Edge Records
        Invoice("INV-2024-040", "Test Customer", "99999.99", "PAID", "test@example.com")
    )

    fun main(){
        Log.d("msg","Demo")


        val customerResults  = CustomerSearchEngine.search(
            query = "h",
            customers = customers,
            extractor = { c ->
                mapOf(
                    "name"       to c.name,
                    "phone"      to c.phone,
                    "email"      to c.email,
                    "city"       to c.city,
                    "customerId" to c.id
                )
            }
        )

        val invoiceResults = InvoiceSearchEngine.search(
            query = "gmail",
            records = invoices,
            extractor = {
                mapOf(
                    "invoiceNumber" to it.invoiceNumber,
                    "customerName" to it.customerName,
                    "customerEmail" to it.customerEmail,
                    "status" to it.status,
                    "amount" to it.amount
                )
            }
        )


        Log.d("CustomerLogs",if(customerResults.isEmpty()) "No results found" else "Results: $customerResults")
        customerResults.forEach { result ->
            Log.d("CustomerLogs", "${result.record.name} - Score: ${"%.2f".format(result.totalScore)}")

            result.fieldScores.forEach { (field, score) ->
                Log.d("CustomerLogs", "    $field: ${"%.2f".format(score)}")
            }
        }

        Log.d("InvoiceLogs",if(invoiceResults.isEmpty()) "No results found" else "Results: $invoiceResults")
        invoiceResults.forEach { result ->
            Log.d("InvoiceLogs", "${result.record.customerName} - Score: ${"%.2f".format(result.totalScore)}")

            result.fieldScores.forEach { (field, score) ->
                Log.d("InvoiceLogs", "    $field: ${"%.2f".format(score)}")
            }
        }
    }




}


