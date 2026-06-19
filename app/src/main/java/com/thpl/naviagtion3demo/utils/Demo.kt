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

    fun main(){
        Log.d("msg","Demo")


        // One line to search:
        val results = CustomerSearchEngine.search(
            query = "abcdefghxyz",
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
        Log.d("msg",results.toString())

      /*  results.forEach { result ->
            Log.d("msg","${result.record.name} — Score: ${"%.2f".format(result.totalScore)}")
            result.fieldScores.forEach { (field, score) ->
                Log.d("msg","$field: ${"%.2f".format(score)}")
            }
        }*/

        results.forEach { result ->
            Log.d("SearchDemo", "${result.record.name} - Score: ${"%.2f".format(result.totalScore)}")

            result.fieldScores.forEach { (field, score) ->
                Log.d("SearchDemo", "    $field: ${"%.2f".format(score)}")
            }
        }
    }




}


