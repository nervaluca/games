package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.ToolType
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel

@Composable
fun GameHud(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD0F172A),
                border = BorderStroke(1.dp, Color(0x4438BDF8)),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Health & Shield Bars
                    Column(modifier = Modifier.weight(1.3f)) {
                        // Breaker HP
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Salvavita",
                                tint = if (uiState.health > 35f) Color(0xFF22C55E) else Color(0xFFEF4444),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SALVAVITA: ${uiState.health.toInt()}%",
                                color = if (uiState.health > 35f) Color(0xFF4ADE80) else Color(0xFFF87171),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { uiState.health / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (uiState.health > 35f) Color(0xFF22C55E) else Color(0xFFEF4444),
                            trackColor = Color(0xFF334155),
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        // Shield 1000V Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Scudo Isolante",
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "SCUDO 1000V: ${uiState.shield.toInt()}%",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { uiState.shield / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF00F0FF),
                            trackColor = Color(0xFF1E293B),
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Score & Volt Coins
                    Column(
                        modifier = Modifier.weight(1.2f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState.score} PTS",
                            color = Color(0xFFFBBF24),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Volt Coins",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "+${uiState.voltCoinsEarnedThisRun} Volt",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.gameMode == GameMode.TIME_ATTACK) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Tempo",
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${uiState.timeAttackSecondsLeft.toInt()}s",
                                    color = Color(0xFFF43F5E),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Pause Button
                    IconButton(
                        onClick = { viewModel.pauseGame() },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pausa Gioco",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // COMBO & POWER-UP ACTIVE BANNERS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedVisibility(
                    visible = uiState.comboMultiplier > 1,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEF4444),
                        border = BorderStroke(1.5.dp, Color(0xFFFFEE00)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Combo",
                                tint = Color(0xFFFFEE00),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "GNUGNU x${uiState.comboMultiplier}!",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                uiState.activePowerUp?.let { pu ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = pu.color.copy(alpha = 0.9f),
                        border = BorderStroke(1.5.dp, Color.White),
                        shadowElevation = 6.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "${pu.symbol} ${pu.title} (${uiState.powerUpTimeLeft.toInt()}s)",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // BOTTOM ACTION ROW & TOOL BELT
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Action Buttons: BOMBA MAGNETOTERMICO & SOVRATENSIONE 380V
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // BOMBA MAGNETOTERMICO BUTTON
                ElevatedButton(
                    onClick = { viewModel.triggerMagnetoBomb() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bomb_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = if (uiState.bombCount > 0) Color(0xFFDC2626) else Color(0xFF475569),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "💣 BOMBA MAGNETO (${uiState.bombCount})",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }

                // 380V OVERDRIVE BUTTON
                ElevatedButton(
                    onClick = { viewModel.activateSuperOverdrive() },
                    modifier = Modifier
                        .weight(1.1f)
                        .scale(if (uiState.overdriveVoltage >= 99f && !uiState.isOverdriveActive) pulseScale else 1.0f)
                        .testTag("super_overdrive_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when {
                            uiState.isOverdriveActive -> Color(0xFF0284C7)
                            uiState.overdriveVoltage >= 99f -> Color(0xFF00E5FF)
                            else -> Color(0xFF1E293B)
                        },
                        contentColor = if (uiState.overdriveVoltage >= 99f) Color.Black else Color(0xFF94A3B8)
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "380V",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (uiState.isOverdriveActive) "380V ATTIVO (${uiState.overdriveTimeLeft.toInt()}s)" else if (uiState.overdriveVoltage >= 99f) "380V PRONTO! ⚡" else "CARICA: ${uiState.overdriveVoltage.toInt()}%",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }

            // Tool Weapon Switcher
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xEE0A0F1E),
                border = BorderStroke(1.5.dp, Color(0xFF334155)),
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 6.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ToolType.values().forEach { tool ->
                        val isUnlocked = uiState.unlockedTools.contains(tool)
                        val isSelected = uiState.activeTool == tool
                        val toolLevel = when (tool) {
                            ToolType.SCREWDRIVER -> uiState.screwdriverLevel
                            ToolType.PLIERS -> uiState.pliersLevel
                            ToolType.TAPE -> uiState.tapeLevel
                            ToolType.MULTIMETER -> uiState.multimeterLevel
                            ToolType.BREAKER_BOMB -> uiState.breakerLevel
                        }

                        ToolBeltItem(
                            tool = tool,
                            level = toolLevel,
                            isUnlocked = isUnlocked,
                            isSelected = isSelected,
                            onSelect = {
                                if (isUnlocked) viewModel.selectTool(tool)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolBeltItem(
    tool: ToolType,
    level: Int,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isSelected -> Color(0xFF00F0FF)
        isUnlocked -> Color(0xFF475569)
        else -> Color(0xFF1E293B)
    }

    val bgColor = when {
        isSelected -> Color(0xFF1E293B)
        isUnlocked -> Color(0xFF0F172A)
        else -> Color(0x440F172A)
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isUnlocked, onClick = onSelect)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("tool_item_${tool.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isUnlocked) tool.baseColor.copy(alpha = 0.25f) else Color(0x22FFFFFF),
                        CircleShape
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) tool.accentColor else Color(0x33FFFFFF),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = tool.displayName,
                    tint = if (isUnlocked) tool.accentColor else Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val shortName = when (tool) {
                ToolType.SCREWDRIVER -> "Cercafase"
                ToolType.PLIERS -> "Laser Pinza"
                ToolType.TAPE -> "Nastro"
                ToolType.MULTIMETER -> "Plasma"
                ToolType.BREAKER_BOMB -> "Cannone"
            }
            Text(
                text = shortName,
                color = if (isUnlocked) (if (isSelected) Color(0xFF00F0FF) else Color.White) else Color(0xFF64748B),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (isUnlocked) {
                Text(
                    text = "Lv.$level",
                    color = Color(0xFFFBBF24),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Bloccato",
                    color = Color(0xFF94A3B8),
                    fontSize = 8.sp
                )
            }
        }
    }
}
