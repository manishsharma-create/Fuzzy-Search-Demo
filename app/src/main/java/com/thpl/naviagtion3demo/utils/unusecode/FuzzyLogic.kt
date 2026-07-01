package com.thpl.naviagtion3demo.utils.unusecode


/*
object FuzzyLogic {

    // Returns true if the name matches the query with roughly 40% similarity allowance
    fun isMatch(name: String, query: String): Boolean {
        if (query.isBlank()) return true
        val similarity = calculateSimilarity(name, query)
        return similarity > 0.4 // Threshold: 0.4 means "At least 40% similar"
    }

    // Similarity Score: 0.0 (No match) to 1.0 (Perfect match)
    fun calculateSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length > s2.length) s1 else s2
        val shorter = if (s1.length > s2.length) s2 else s1
        if (longer.isEmpty()) return 1.0
        val distance = levenshtein(longer.lowercase(), shorter.lowercase())
        return (longer.length - distance).toDouble() / longer.length.toDouble()
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}*/
