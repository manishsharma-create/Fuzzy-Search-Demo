package com.thpl.naviagtion3demo

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thpl.naviagtion3demo.utils.FuzzyLogic

/**
 * SCREEN 1: Simple Version (The one previously uncommented)
 * Uses default FuzzyLogic for similarity calculation.
 */
@Composable
fun SimpleFuzzySearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
) {
    val searchText by viewModel.searchText.collectAsState()
    val users by viewModel.filteredUsers.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Simple Version", style = MaterialTheme.typography.headlineSmall)
        
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.addDummyData() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Demo Data (Suresh, Ramesh...)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            label = { Text("Search (Try 'Surresh')") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(users) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = user.name, style = MaterialTheme.typography.titleMedium)
                        if (searchText.isNotEmpty()) {
                            val score = FuzzyLogic.calculateSimilarity(user.name, searchText)
                            Text(
                                text = "Match: ${(score * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * SCREEN 2: Advanced Version (The one previously commented)
 * Uses the dynamic algorithm selector (Dropdown).
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
@Composable
fun AdvancedFuzzySearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val searchText by viewModel.searchText.collectAsState()
    val users by viewModel.filteredUsers.collectAsState()
    val currentAlgo by viewModel.currentAlgo.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Advanced Version", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Algorithm Dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentAlgo.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Algorithm") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                viewModel.algorithms.forEach { algo ->
                    DropdownMenuItem(
                        text = { Text(text = algo.name) },
                        onClick = {
                            viewModel.onAlgoSelected(algo)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (users.isEmpty() && searchText.isEmpty()) {
            Button(
                onClick = { viewModel.addDummyData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Demo Data (Names)")
            }
        }

        Text(
            text = "Try typing: 'Surresh', 'Vikks', 'Manishh'",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = viewModel::onSearchTextChange,
            label = { Text("Search Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users, key = { it.id }) { user ->
                Card(elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text(text = user.name, style = MaterialTheme.typography.titleMedium)
                        if (searchText.isNotEmpty()) {
                            val score = currentAlgo.getScore(user.name, searchText)
                            Text(
                                text = "Match Score: ${String.format("%.2f", score)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if(score > 0.8) Color.Green else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
