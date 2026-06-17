package com.thpl.naviagtion3demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thpl.naviagtion3demo.ui.theme.Naviagtion3DemoTheme

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Naviagtion3DemoTheme {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val tabs = listOf("Simple UI", "Advanced UI")

                Scaffold(
                    topBar = {
                        // Added statusBarsPadding to prevent UI from hiding under the clock/icons
                        TabRow(
                            selectedTabIndex = selectedTabIndex,
                            modifier = Modifier.statusBarsPadding()
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // Apply innerPadding to ensure content is below the TabRow
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when (selectedTabIndex) {
                            0 -> SimpleFuzzySearchScreen()
                            1 -> AdvancedFuzzySearchScreen()
                        }
                    }
                }
            }
        }
    }
}











