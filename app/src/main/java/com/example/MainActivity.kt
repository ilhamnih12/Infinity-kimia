package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.ui.screens.ChemistryGameScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChemistryViewModel

class MainActivity : ComponentActivity() {
    private val chemistryViewModel: ChemistryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ChemistryGameScreen(
                    viewModel = chemistryViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
