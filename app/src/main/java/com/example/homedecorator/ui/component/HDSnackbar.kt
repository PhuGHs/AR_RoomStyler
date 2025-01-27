package com.example.homedecorator.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HDSnackbar() {
    var showSnackbar by remember { mutableStateOf(true) }

    if (showSnackbar) {
        Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Please select furniture before placing it.")
        }

        // Automatically hide the Snackbar after 5 seconds
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(5000) // 5 seconds
            showSnackbar = false
        }
    }
}