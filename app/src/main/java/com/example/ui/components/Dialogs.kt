package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.GameMode
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GameOverDialog(
    viewModel: GameViewModel,
    uiState: GameUiState
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(2.dp, Color(0xFFEF4444)),
            shadowElevation = 16.dp,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = "Cortocircuito",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SALVAVITA SCATTATO!",
                    color = Color(0xFFEF4444),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gli apprendisti hanno mandato in corto il quadro elettrico!",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Stats Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        StatRow(label = "Punteggio:", value = "${uiState.score} PTS", color = Color(0xFFFBBF24))
                        StatRow(label = "Versi GNUGNU generati:", value = "${uiState.gnugnuCount} ⚡", color = Color(0xFF00E5FF))
                        StatRow(label = "Max Combo raggiunta:", value = "x${uiState.maxComboAchieved}", color = Color(0xFFFF0055))
                        StatRow(label = "Volt Monete guadagnate:", value = "+${uiState.voltCoinsEarnedThisRun}", color = Color(0xFF4ADE80))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Button(
                    onClick = { viewModel.restartCurrentGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("retry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Riprova")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Riprova Turno", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.WORKSHOP) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("go_workshop_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Officina", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Officina", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("menu_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF64748B))
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menu", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VictoryDialog(
    viewModel: GameViewModel,
    uiState: GameUiState
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A192F),
            border = BorderStroke(2.dp, Color(0xFF22C55E)),
            shadowElevation = 16.dp,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Vittoria",
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (uiState.gameMode == GameMode.CAMPAIGN) "TURNO COMPLETATO!" else "TEMPO SCADUTO!",
                    color = Color(0xFF4ADE80),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tutti gli apprendisti sono stati rimessi in riga a suon di attrezzi e GNUGNU!",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Stats Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        StatRow(label = "Punteggio:", value = "${uiState.score} PTS", color = Color(0xFFFBBF24))
                        StatRow(label = "Versi GNUGNU:", value = "${uiState.gnugnuCount} ⚡", color = Color(0xFF00E5FF))
                        StatRow(label = "Max Combo:", value = "x${uiState.maxComboAchieved}", color = Color(0xFFFF0055))
                        StatRow(label = "Volt Guadagnati:", value = "+${uiState.voltCoinsEarnedThisRun}", color = Color(0xFF4ADE80))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.gameMode == GameMode.CAMPAIGN && uiState.currentLevelIndex < viewModel.levels.size - 1) {
                    Button(
                        onClick = { viewModel.nextLevel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_level_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Prossimo Turno")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prossimo Turno di Lavoro", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.WORKSHOP) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("victory_workshop_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Officina", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Officina", color = Color(0xFF38BDF8), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("victory_menu_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF64748B))
                    ) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Menu", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    viewModel: GameViewModel,
    uiState: GameUiState
) {
    Dialog(
        onDismissRequest = { viewModel.resumeGame() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
            shadowElevation = 16.dp,
            modifier = Modifier.padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSA LAVORI",
                    color = Color(0xFF38BDF8),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.resumeGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Riprendi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Riprendi Lavori", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.restartCurrentGame() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Riavvia", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Riavvia Turno", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = "Esci al Menu", tint = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Esci al Menu", color = Color(0xFFEF4444))
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 12.sp)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
