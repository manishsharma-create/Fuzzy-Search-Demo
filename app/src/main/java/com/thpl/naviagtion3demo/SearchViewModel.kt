package com.thpl.naviagtion3demo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thpl.naviagtion3demo.room.AppDatabase
import com.thpl.naviagtion3demo.room.User
import com.thpl.naviagtion3demo.utils.ApacheJaroWinkler
import com.thpl.naviagtion3demo.utils.ApacheLevenshtein
import com.thpl.naviagtion3demo.utils.ApacheMetaphone
import com.thpl.naviagtion3demo.utils.ApacheSoundex
import com.thpl.naviagtion3demo.utils.CharMatchAlgo
import com.thpl.naviagtion3demo.utils.FuzzyAlgorithm
import com.thpl.naviagtion3demo.utils.FuzzyLogic
import com.thpl.naviagtion3demo.utils.JaroWinklerAlgo
import com.thpl.naviagtion3demo.utils.LevenshteinAlgo
import com.thpl.naviagtion3demo.utils.SoundexAlgo
import com.thpl.naviagtion3demo.utils.TrigramAlgo
import com.thpl.naviagtion3demo.utils.UnorderedContainsAlgo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(
        application,
        scope = viewModelScope
    )
    private val dao = db.userDao()


    private val _currentAlgo = MutableStateFlow<FuzzyAlgorithm>(LevenshteinAlgo)
    val currentAlgo = _currentAlgo.asStateFlow()

    // 1. The Raw Search Text
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // 2. The Raw Data from Room
    private val _users = dao.getAllUsers()

    // 3. The Filtered Result (The "Fuzzy" Logic)
    // We combine the DB list and the Search Text.
    // If the text changes, we re-run the fuzzy filter.
  /*  val filteredUsers: StateFlow<List<User>> = _users.combine(_searchText) { users, text ->
        if (text.isBlank()) {
            users
        } else {
            // Apply Fuzzy Logic here
            users
                .filter { user -> FuzzyLogic.isMatch(user.name, text) }
                .sortedByDescending { user ->
                    // Sort by most similar
                    FuzzyLogic.calculateSimilarity(user.name, text)
                }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )*/


    // In SearchViewModel.kt

    val filteredUsers = combine(_users, _searchText, _currentAlgo) { users, query, algo ->
        if (query.isBlank()) {
            users.sortedBy { it.name }
        } else {
            users
                .map { user ->
                    // Split the name into words and find the best match score for the query
                    val words = user.name.split("\\s+".toRegex())
                    val maxScore = words.maxOfOrNull { word -> algo.getScore(word, query) } ?: 0.0
                    user to maxScore
                }
                .filter { (_, score) -> score >= algo.threshold } // Filter out non-matches
                .sortedWith(
                    compareByDescending<Pair<User, Double>> { (_, score) -> score } // 1. Sort by Score
                        .thenBy { (user, _) ->
                            // 2. Secondary Sort: Put "Starts With" matches at the top
                            !user.name.lowercase().startsWith(query.lowercase())
                        }
                )
                .map { (user, _) -> user }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun addDummyData() {
        viewModelScope.launch {
            val dummyUsers = listOf(
                User(name = "Suresh"),
                User(name = "Ramesh"),
                User(name = "Vikas"),
                User(name = "Manish"),
                User(name = "Hitesh")
            )
            dao.insertAll(dummyUsers)
        }
    }


    val algorithms = listOf(
        LevenshteinAlgo,
        JaroWinklerAlgo,
        TrigramAlgo,
        SoundexAlgo,
        CharMatchAlgo,
        UnorderedContainsAlgo,
        ApacheLevenshtein,
        ApacheMetaphone,
        ApacheSoundex,
        ApacheJaroWinkler

    )



    fun onAlgoSelected(algo: FuzzyAlgorithm) {
        _currentAlgo.value = algo
    }
}



