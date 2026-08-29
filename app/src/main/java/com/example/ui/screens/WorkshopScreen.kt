package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolType
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun WorkshopScreen(
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
            .padding(16.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .testTag("workshop_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna Indietro",
                    tint = Color.White
                )
            }

            Text(
                text = "OFFICINA DEL CABLATORE",
                color = Color(0xFFFFCC00),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )

            // Current Coins
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Volt Coins",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.totalVoltCoins}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Potenzia i tuoi attrezzi isolati per infliggere più cariche elettriche ed estrarre gnugnu memorabili dagli apprendisti!",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // LIST OF TOOLS
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ToolType.values()) { tool ->
                val isUnlocked = uiState.unlockedTools.contains(tool)
                val currentLevel = when (tool) {
                    ToolType.SCREWDRIVER -> uiState.screwdriverLevel
                    ToolType.PLIERS -> uiState.pliersLevel
                    ToolType.TAPE -> uiState.tapeLevel
                    ToolType.MULTIMETER -> uiState.multimeterLevel
                    ToolType.BREAKER_BOMB -> uiState.breakerLevel
                }
                val upgradeCost = when (tool) {
                    ToolType.SCREWDRIVER -> currentLevel * 100
                    ToolType.PLIERS -> currentLevel * 150
                    ToolType.TAPE -> currentLevel * 200
                    ToolType.MULTIMETER -> currentLevel * 300
                    ToolType.BREAKER_BOMB -> currentLevel * 500
                }
                val canAfford = uiState.totalVoltCoins >= upgradeCost

                ToolUpgradeCard(
                    tool = tool,
                    level = currentLevel,
                    isUnlocked = isUnlocked,
                    upgradeCost = upgradeCost,
                    canAfford = canAfford,
                    onUpgrade = { viewModel.buyToolUpgrade(tool) }
                )
            }
        }
    }
}

@Composable
private fun ToolUpgradeCard(
    tool: ToolType,
    level: Int,
    isUnlocked: Boolean,
    upgradeCost: Int,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF334155) else Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(tool.baseColor.copy(alpha = 0.25f), CircleShape)
                        .border(1.dp, tool.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Default.Bolt else Icons.Default.Lock,
                        contentDescription = tool.displayName,
                        tint = if (isUnlocked) tool.accentColor else Color(0xFF64748B),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tool.displayName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isUnlocked) "Livello Attrezzo: $level" else "Attrezzo da Sbloccare",
                            color = Color(0xFFFBBF24),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Danno: ${tool.baseDamage + (level - 1) * 10}",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = tool.description,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onUpgrade,
                enabled = canAfford,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upgrade_tool_${tool.name.lowercase()}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUnlocked) Color(0xFF2563EB) else Color(0xFF16A34A),
                    disabledContainerColor = Color(0xFF334155)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = "Potenzia",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUnlocked) "Potenzia a Livello ${level + 1} ($upgradeCost Volt)" else "Sblocca Attrezzo ($upgradeCost Volt)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
