package com.example.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.model.ApprenticeType
import com.example.model.Enemy
import com.example.model.EnemyBulletType
import com.example.model.EnemySpark
import com.example.model.FloatingCoin
import com.example.model.GameParticle
import com.example.model.GnuGnuPopup
import com.example.model.PowerUpType
import com.example.model.Projectile
import com.example.model.Shockwave
import com.example.model.ShooterPowerUp
import com.example.model.ToolType
import com.example.viewmodel.GameUiState
import com.example.viewmodel.GameViewModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvas(
    viewModel: GameViewModel,
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var lastFrameTime by remember { mutableLongStateOf(0L) }
    var scrollOffset by remember { mutableFloatStateOf(0f) }

    // 60 FPS update loop
    LaunchedEffect(uiState.isPlaying, uiState.isPaused) {
        if (uiState.isPlaying && !uiState.isPaused) {
            lastFrameTime = System.currentTimeMillis()
            while (true) {
                withInfiniteAnimationFrameMillis { _ ->
                    val now = System.currentTimeMillis()
                    val dt = if (lastFrameTime > 0L) (now - lastFrameTime) / 1000f else 0.016f
                    lastFrameTime = now
                    scrollOffset = (scrollOffset + dt * 180f) % 600f
                    viewModel.updateGameTick(dt)
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("game_canvas")
            .pointerInput(uiState.isPlaying, uiState.isPaused) {
                detectTapGestures { offset ->
                    val scaleX = viewModel.canvasWidth / size.width
                    val scaleY = viewModel.canvasHeight / size.height
                    viewModel.movePlayer(offset.x * scaleX, offset.y * scaleY)
                }
            }
            .pointerInput(uiState.isPlaying, uiState.isPaused) {
                detectDragGestures { change, _ ->
                    val scaleX = viewModel.canvasWidth / size.width
                    val scaleY = viewModel.canvasHeight / size.height
                    // Smooth direct finger follow for spaceship navigation
                    viewModel.movePlayer(change.position.x * scaleX, change.position.y * scaleY)
                }
            }
    ) {
        val cw = size.width
        val ch = size.height
        viewModel.canvasWidth = cw
        viewModel.canvasHeight = ch

        // Apply screen shake offset if active
        val shakeX = if (uiState.screenShakeIntensity > 0f) (Random.nextFloat() - 0.5f) * uiState.screenShakeIntensity else 0f
        val shakeY = if (uiState.screenShakeIntensity > 0f) (Random.nextFloat() - 0.5f) * uiState.screenShakeIntensity else 0f

        // 1. Draw Vertical Scrolling Electric Grid & Conduits
        drawShooterBackground(cw, ch, scrollOffset, uiState.isOverdriveActive)

        // 2. Draw Floating Volt Coins
        val currentCoins = synchronized(viewModel.floatingCoins) { viewModel.floatingCoins.toList() }
        for (coin in currentCoins) {
            drawFloatingCoin(coin)
        }

        // 3. Draw Power-Ups
        val currentPowerUps = synchronized(viewModel.powerUps) { viewModel.powerUps.toList() }
        for (pu in currentPowerUps) {
            drawPowerUpBadge(pu, textMeasurer)
        }

        // 4. Draw Projectiles (Player Blaster Lasers & Tools)
        val currentProjectiles = synchronized(viewModel.projectiles) { viewModel.projectiles.toList() }
        for (p in currentProjectiles) {
            drawPlayerProjectile(p)
        }

        // 5. Draw Enemies (Apprentice Swarms)
        val currentEnemies = synchronized(viewModel.enemies) { viewModel.enemies.toList() }
        for (enemy in currentEnemies) {
            drawApprenticeShooter(enemy, textMeasurer)
        }

        // 6. Draw Enemy Bullets & Sparks
        val currentSparks = synchronized(viewModel.enemySparks) { viewModel.enemySparks.toList() }
        for (spark in currentSparks) {
            drawEnemyBullet(spark)
        }

        // 7. Draw Super Electrician Player Ship
        val playerX = cw * uiState.playerXRatio + shakeX
        val playerY = ch * uiState.playerYRatio + shakeY
        drawPlayerHero(playerX, playerY, uiState, scrollOffset)

        // 8. Draw Shockwaves
        val currentShockwaves = synchronized(viewModel.shockwaves) { viewModel.shockwaves.toList() }
        for (sw in currentShockwaves) {
            drawCircle(
                color = sw.color.copy(alpha = sw.alpha),
                radius = sw.radius,
                center = Offset(sw.x, sw.y),
                style = Stroke(width = 8f)
            )
        }

        // 9. Draw Particle Sparks
        val currentParticles = synchronized(viewModel.particles) { viewModel.particles.toList() }
        for (particle in currentParticles) {
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }

        // 10. Draw Gnugnu Comic Speech Popups
        val currentPopups = synchronized(viewModel.popups) { viewModel.popups.toList() }
        for (popup in currentPopups) {
            drawGnugnuPopup(popup, textMeasurer)
        }

        // 11. Boss Health Bar (Top of screen)
        if (uiState.bossActive) {
            drawBossHealthBar(cw, uiState.bossHpRatio, textMeasurer)
        }
    }
}

private fun DrawScope.drawShooterBackground(cw: Float, ch: Float, scrollOffset: Float, isOverdrive: Boolean) {
    // Dark deep cyberpunk navy gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = if (isOverdrive) {
                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF0369A1))
            } else {
                listOf(Color(0xFF050B14), Color(0xFF0F172A), Color(0xFF0A0F1D))
            }
        )
    )

    // Vertical Electric Conduits & High-Voltage Rails
    val railLeftX = cw * 0.08f
    val railRightX = cw * 0.92f

    drawLine(
        color = Color(0x3300F0FF),
        start = Offset(railLeftX, 0f),
        end = Offset(railLeftX, ch),
        strokeWidth = 4f
    )
    drawLine(
        color = Color(0x3300F0FF),
        start = Offset(railRightX, 0f),
        end = Offset(railRightX, ch),
        strokeWidth = 4f
    )

    // Scrolling Horizontal Power Grid Lines
    val lineSpacing = 80f
    val startY = scrollOffset % lineSpacing
    var y = startY - lineSpacing
    while (y < ch + lineSpacing) {
        val alpha = if (isOverdrive) 0.35f else 0.15f
        drawLine(
            color = Color(0xFF00E5FF).copy(alpha = alpha),
            start = Offset(railLeftX, y),
            end = Offset(railRightX, y),
            strokeWidth = 1.5f
        )
        y += lineSpacing
    }

    // Glowing Neon Circuit Traces in Center
    val centerBusX = cw * 0.5f
    drawLine(
        color = if (isOverdrive) Color(0x88FFD600) else Color(0x2238BDF8),
        start = Offset(centerBusX, 0f),
        end = Offset(centerBusX, ch),
        strokeWidth = 3f
    )
}

private fun DrawScope.drawPlayerHero(
    px: Float,
    py: Float,
    uiState: GameUiState,
    scrollOffset: Float
) {
    // 1. Thruster Plasma Flame / Sparks
    val flameLength = 22f + (scrollOffset % 12f)
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF00F0FF), Color(0xFFFFCC00), Color.Transparent),
            startY = py + 25f,
            endY = py + 25f + flameLength
        ),
        topLeft = Offset(px - 14f, py + 20f),
        size = Size(28f, flameLength)
    )

    // 2. Shield Bubble (if active)
    if (uiState.shield > 0f || uiState.isOverdriveActive) {
        val shieldAlpha = if (uiState.isOverdriveActive) 0.8f else (uiState.shield / 100f) * 0.5f
        val shieldColor = if (uiState.isOverdriveActive) Color(0xFFFFEE00) else Color(0xFF00F0FF)

        drawCircle(
            color = shieldColor.copy(alpha = shieldAlpha * 0.3f),
            radius = 42f,
            center = Offset(px, py)
        )
        drawCircle(
            color = shieldColor.copy(alpha = shieldAlpha),
            radius = 42f,
            center = Offset(px, py),
            style = Stroke(width = 2.5f)
        )
    }

    // 3. Exosuit Flying Platform / Chassis (High-Tech Electrician Wing)
    val shipPath = Path().apply {
        moveTo(px, py - 32f) // Nose cone
        lineTo(px + 28f, py + 12f) // Right wing tip
        lineTo(px + 18f, py + 26f) // Right engine
        lineTo(px, py + 18f) // Center tail
        lineTo(px - 18f, py + 26f) // Left engine
        lineTo(px - 28f, py + 12f) // Left wing tip
        close()
    }

    drawPath(
        path = shipPath,
        color = Color(0xFF1E293B)
    )
    drawPath(
        path = shipPath,
        color = Color(0xFF00F0FF),
        style = Stroke(width = 2.5f)
    )

    // 4. Yellow Safety Hi-Vis Stripes
    drawLine(
        color = Color(0xFFFFD600),
        start = Offset(px - 14f, py),
        end = Offset(px + 14f, py),
        strokeWidth = 4f
    )

    // 5. Electrician Character Bust & Hardhat
    // Head (Skin tone)
    drawCircle(
        color = Color(0xFFFFD7BA),
        radius = 12f,
        center = Offset(px, py - 6f)
    )

    // Yellow Safety Hardhat
    drawArc(
        color = Color(0xFFFFD600),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(px - 14f, py - 20f),
        size = Size(28f, 22f)
    )
    // Hardhat Brim
    drawLine(
        color = Color(0xFFEAB308),
        start = Offset(px - 16f, py - 8f),
        end = Offset(px + 16f, py - 8f),
        strokeWidth = 3f
    )
    // LED Headlamp
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = 4f,
        center = Offset(px, py - 16f)
    )

    // 6. Dual Tool Blaster Barrels
    val toolColor = uiState.activeTool.accentColor
    drawRect(
        color = toolColor,
        topLeft = Offset(px - 24f, py - 18f),
        size = Size(6f, 16f)
    )
    drawRect(
        color = toolColor,
        topLeft = Offset(px + 18f, py - 18f),
        size = Size(6f, 16f)
    )

    // Muzzle Flash Sparkles
    drawCircle(
        color = Color(0xFFFFEE00),
        radius = 3.5f,
        center = Offset(px - 21f, py - 18f)
    )
    drawCircle(
        color = Color(0xFFFFEE00),
        radius = 3.5f,
        center = Offset(px + 21f, py - 18f)
    )
}

private fun DrawScope.drawApprenticeShooter(
    enemy: Enemy,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val radius = enemy.type.sizeRadius
    val ex = enemy.x
    val ey = enemy.y

    // 1. Cable Spool / Jet Harness for flying apprentice
    drawCircle(
        color = Color(0xFF334155),
        radius = radius + 6f,
        center = Offset(ex, ey)
    )
    drawCircle(
        color = enemy.type.helmetColor,
        radius = radius + 6f,
        center = Offset(ex, ey),
        style = Stroke(width = 2.5f)
    )

    // Cable windings around spool
    for (i in -2..2) {
        drawLine(
            color = if (i % 2 == 0) Color(0xFFEF4444) else Color(0xFF3B82F6),
            start = Offset(ex - radius + 2f, ey + (i * 6f)),
            end = Offset(ex + radius - 2f, ey + (i * 6f)),
            strokeWidth = 2.5f
        )
    }

    // 2. Apprentice Face (Comical / Panicked)
    drawCircle(
        color = Color(0xFFFFDFC4),
        radius = radius * 0.65f,
        center = Offset(ex, ey - 4f)
    )

    // Helmet
    drawArc(
        color = enemy.type.helmetColor,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(ex - radius * 0.7f, ey - radius * 0.95f),
        size = Size(radius * 1.4f, radius * 1.1f)
    )

    // Eyes: Wide shocked cartoon eyes
    val eyeOffset = radius * 0.25f
    drawCircle(color = Color.White, radius = 5f, center = Offset(ex - eyeOffset, ey - 6f))
    drawCircle(color = Color.White, radius = 5f, center = Offset(ex + eyeOffset, ey - 6f))
    drawCircle(color = Color.Black, radius = 2.5f, center = Offset(ex - eyeOffset, ey - 6f))
    drawCircle(color = Color.Black, radius = 2.5f, center = Offset(ex + eyeOffset, ey - 6f))

    // Open mouth shouting "GNUGNU"
    drawOval(
        color = Color(0xFF991B1B),
        topLeft = Offset(ex - 6f, ey + 3f),
        size = Size(12f, 8f)
    )

    // 3. Mini HP Bar (Above Enemy)
    val hpRatio = (enemy.hp.toFloat() / enemy.maxHp).coerceIn(0f, 1f)
    val barWidth = radius * 2f
    drawRoundRect(
        color = Color(0x99000000),
        topLeft = Offset(ex - barWidth / 2f, ey - radius - 14f),
        size = Size(barWidth, 6f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = if (hpRatio > 0.4f) Color(0xFF22C55E) else Color(0xFFEF4444),
        topLeft = Offset(ex - barWidth / 2f, ey - radius - 14f),
        size = Size(barWidth * hpRatio, 6f),
        cornerRadius = CornerRadius(3f, 3f)
    )
}

private fun DrawScope.drawPlayerProjectile(p: Projectile) {
    if (p.isSuperBeam) {
        // High voltage plasma beam
        drawLine(
            color = Color(0xFF00F0FF),
            start = Offset(p.x, p.y + 25f),
            end = Offset(p.x, p.y - 25f),
            strokeWidth = 10f
        )
        drawLine(
            color = Color.White,
            start = Offset(p.x, p.y + 25f),
            end = Offset(p.x, p.y - 25f),
            strokeWidth = 4f
        )
    } else if (p.toolType == ToolType.TAPE) {
        // Rotating vulcanized tape disc
        rotate(p.rotation, Offset(p.x, p.y)) {
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 10f,
                center = Offset(p.x, p.y)
            )
            drawCircle(
                color = Color(0xFF38BDF8),
                radius = 10f,
                center = Offset(p.x, p.y),
                style = Stroke(width = 3f)
            )
        }
    } else if (p.toolType == ToolType.PLIERS) {
        // Dual Pliers Laser Blades
        rotate(p.rotation, Offset(p.x, p.y)) {
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(p.x - 8f, p.y + 12f),
                end = Offset(p.x, p.y - 12f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(p.x + 8f, p.y + 12f),
                end = Offset(p.x, p.y - 12f),
                strokeWidth = 4f
            )
        }
    } else {
        // Screwdriver Dart Laser Bolt
        drawLine(
            color = p.trailColor,
            start = Offset(p.x, p.y + 16f),
            end = Offset(p.x, p.y - 16f),
            strokeWidth = 5f
        )
        drawCircle(
            color = Color(0xFFFFEE00),
            radius = 4f,
            center = Offset(p.x, p.y - 16f)
        )
    }
}

private fun DrawScope.drawEnemyBullet(spark: EnemySpark) {
    when (spark.type) {
        EnemyBulletType.HIGH_VOLTAGE_LASER -> {
            drawCircle(color = Color(0xFFFF0055), radius = spark.radius, center = Offset(spark.x, spark.y))
            drawCircle(color = Color.White, radius = spark.radius * 0.5f, center = Offset(spark.x, spark.y))
        }
        EnemyBulletType.SPARK_BOLT -> {
            drawCircle(color = Color(0xFF8338EC), radius = spark.radius, center = Offset(spark.x, spark.y))
            drawCircle(color = Color(0xFFFFEE00), radius = spark.radius * 0.6f, center = Offset(spark.x, spark.y))
        }
        else -> {
            drawCircle(color = Color(0xFFFFD600), radius = spark.radius, center = Offset(spark.x, spark.y))
            drawCircle(color = Color(0xFFEF4444), radius = spark.radius * 0.5f, center = Offset(spark.x, spark.y))
        }
    }
}

private fun DrawScope.drawFloatingCoin(coin: FloatingCoin) {
    drawCircle(
        color = Color(0xFFFFD600),
        radius = 9f,
        center = Offset(coin.x, coin.y)
    )
    drawCircle(
        color = Color(0xFF00F0FF),
        radius = 9f,
        center = Offset(coin.x, coin.y),
        style = Stroke(width = 2f)
    )
}

private fun DrawScope.drawPowerUpBadge(
    pu: ShooterPowerUp,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    drawCircle(
        color = Color(0xCC0F172A),
        radius = 18f,
        center = Offset(pu.x, pu.y)
    )
    drawCircle(
        color = pu.type.color,
        radius = 18f,
        center = Offset(pu.x, pu.y),
        style = Stroke(width = 2.5f)
    )

    val layoutResult = textMeasurer.measure(
        text = AnnotatedString(pu.type.symbol),
        style = TextStyle(color = pu.type.color, fontSize = 11.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(pu.x - layoutResult.size.width / 2f, pu.y - layoutResult.size.height / 2f)
    )
}

private fun DrawScope.drawGnugnuPopup(
    popup: GnuGnuPopup,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val layoutResult = textMeasurer.measure(
        text = AnnotatedString(popup.text),
        style = TextStyle(
            color = popup.color.copy(alpha = popup.alpha),
            fontSize = (13 * popup.scale).sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    )

    val tw = layoutResult.size.width.toFloat()
    val th = layoutResult.size.height.toFloat()

    // Comic speech bubble background
    drawRoundRect(
        color = Color(0xEE1E293B).copy(alpha = popup.alpha),
        topLeft = Offset(popup.x - tw / 2f - 8f, popup.y - th / 2f - 4f),
        size = Size(tw + 16f, th + 8f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = popup.color.copy(alpha = popup.alpha),
        topLeft = Offset(popup.x - tw / 2f - 8f, popup.y - th / 2f - 4f),
        size = Size(tw + 16f, th + 8f),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    drawText(
        textLayoutResult = layoutResult,
        topLeft = Offset(popup.x - tw / 2f, popup.y - th / 2f)
    )
}

private fun DrawScope.drawBossHealthBar(
    cw: Float,
    hpRatio: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val barWidth = cw * 0.7f
    val startX = (cw - barWidth) / 2f
    val barY = 85f

    drawRoundRect(
        color = Color(0xCC0F172A),
        topLeft = Offset(startX - 4f, barY - 4f),
        size = Size(barWidth + 8f, 18f),
        cornerRadius = CornerRadius(9f, 9f)
    )
    drawRoundRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(startX, barY),
        size = Size(barWidth * hpRatio, 10f),
        cornerRadius = CornerRadius(5f, 5f)
    )

    val text = "MEGA CAPO CANTIERE 380V (BOSS)"
    val layout = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(color = Color(0xFFFFEE00), fontSize = 10.sp, fontWeight = FontWeight.Black)
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset((cw - layout.size.width) / 2f, barY - 14f)
    )
}
