package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.GameMode
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    uiState: GameUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF090D16), Color(0xFF0F172A), Color(0xFF1E1B4B))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP STATUS BAR (Coins & Settings)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volt Coins Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Volt Coins",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.totalVoltCoins} Volt",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Audio & Haptics Toggles
            Row {
                IconButton(
                    onClick = { viewModel.setSoundEnabled(!uiState.isSoundEnabled) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Audio",
                        tint = if (uiState.isSoundEnabled) Color(0xFF38BDF8) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { viewModel.setHapticsEnabled(!uiState.isHapticsEnabled) },
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFF1E293B), CircleShape)
                        .testTag("haptic_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Vibrazione",
                        tint = if (uiState.isHapticsEnabled) Color(0xFF4ADE80) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // HERO CHARACTER ART CARD
        Card(
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Color(0xFF00F0FF)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_super_electrician),
                    contentDescription = "Super Elettricista Cablato",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC090D16)),
                                startY = 80f
                            )
                        )
                )
                // App Title Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "SUPER ELETTRICISTA",
                        color = Color(0xFFFFCC00),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "CABLAGGIO TOTALE & GNUGNU!",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // FUN SOUND PREVIEW BUTTON ("Ascolta il Gnugnu!")
        OutlinedButton(
            onClick = { viewModel.soundEngine.playGnuGnu() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("preview_gnugnu_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, Color(0xFFFF0066)),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0x22FF0066))
        ) {
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Ascolta Gnugnu",
                tint = Color(0xFFFF0066),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "⚡ TESTA IL VERSO \"GNUGNU!\" ⚡",
                color = Color(0xFFFFEE00),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "MODALITÀ DI GIOCO",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // GAME MODE 1: CAMPAIGN
        GameModeCard(
            title = "1. Turni in Cantiere (Campagna)",
            subtitle = "Avanza nei 4 turni: Officina, Condomini, Centrale e Scontro col Boss!",
            badge = "LIVELLI PROGRESSIVI",
            badgeColor = Color(0xFF38BDF8),
            icon = Icons.Default.PlayArrow,
            onClick = { viewModel.startNewGame(GameMode.CAMPAIGN, 0) },
            testTag = "start_campaign_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // GAME MODE 2: ENDLESS SURVIVAL
        GameModeCard(
            title = "2. Turno Straordinario (Sopravvivenza)",
            subtitle = "Ondate infinite di apprendisti sempre più veloci e disordinati!",
            badge = "RECORD RECORD",
            badgeColor = Color(0xFFF59E0B),
            icon = Icons.Default.Bolt,
            onClick = { viewModel.startNewGame(GameMode.ENDLESS) },
            testTag = "start_endless_button"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // GAME MODE 3: TIME ATTACK 60S
        GameModeCard(
            title = "3. Sfida Gnugnu Frenzy (60 Secondi)",
            subtitle = "Colpisci quanti più apprendisti puoi prima che scatti il timer!",
            badge = "FRENESIA 60s",
            badgeColor = Color(0xFFEF4444),
            icon = Icons.Default.Timer,
            onClick = { viewModel.startNewGame(GameMode.TIME_ATTACK) },
            testTag = "start_timed_button"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECONDARY NAVIGATION BUTTONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Workshop button
            Button(
                onClick = { viewModel.navigateTo(AppScreen.WORKSHOP) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_workshop_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Officina", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Officina", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Highscores button
            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.HIGHSCORES) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_highscores_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = "Record", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Record", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Instructions
        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.INSTRUCTIONS) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_instructions_button"),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFF475569))
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = "Guida & Attrezzi", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Guida Attrezzi Elettrici & Nemici", color = Color(0xFFCBD5E1), fontSize = 13.sp)
        }
    }
}

@Composable
private fun GameModeCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(badgeColor.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, badgeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
