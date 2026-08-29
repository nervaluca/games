package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.GameOverDialog
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameHud
import com.example.ui.components.PauseDialog
import com.example.ui.components.VictoryDialog
import com.example.ui.screens.HighScoresScreen
import com.example.ui.screens.InstructionsScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.WorkshopScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090D16)
                ) {
                    SuperElectricianApp()
                }
            }
        }
    }
}

@Composable
fun SuperElectricianApp(
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState.currentScreen) {
        AppScreen.MAIN_MENU -> {
            MainMenuScreen(viewModel = viewModel, uiState = uiState)
        }
        AppScreen.GAMEPLAY -> {
            Box(modifier = Modifier.fillMaxSize()) {
                GameCanvas(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )
                GameHud(
                    viewModel = viewModel,
                    uiState = uiState,
                    modifier = Modifier.fillMaxSize()
                )

                if (uiState.isPaused) {
                    PauseDialog(viewModel = viewModel, uiState = uiState)
                }

                if (uiState.isGameOver) {
                    GameOverDialog(viewModel = viewModel, uiState = uiState)
                }

                if (uiState.isVictory) {
                    VictoryDialog(viewModel = viewModel, uiState = uiState)
                }
            }
        }
        AppScreen.WORKSHOP -> {
            WorkshopScreen(viewModel = viewModel, uiState = uiState)
        }
        AppScreen.HIGHSCORES -> {
            HighScoresScreen(viewModel = viewModel, uiState = uiState)
        }
        AppScreen.INSTRUCTIONS -> {
            InstructionsScreen(viewModel = viewModel)
        }
    }
}
