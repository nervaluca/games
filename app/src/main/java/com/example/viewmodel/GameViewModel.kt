package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.audio.SoundEngine
import com.example.data.AppDatabase
import com.example.data.GameScore
import com.example.data.PlayerProgress
import com.example.model.ApprenticeType
import com.example.model.Enemy
import com.example.model.EnemyBulletType
import com.example.model.EnemySpark
import com.example.model.EnemyState
import com.example.model.FlightPattern
import com.example.model.FloatingCoin
import com.example.model.GameMode
import com.example.model.GameParticle
import com.example.model.GnuGnuPopup
import com.example.model.LevelConfig
import com.example.model.PowerUpType
import com.example.model.Projectile
import com.example.model.Shockwave
import com.example.model.ShooterPowerUp
import com.example.model.ToolType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.MAIN_MENU,
    val gameMode: GameMode = GameMode.CAMPAIGN,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val currentLevelIndex: Int = 0,
    val score: Int = 0,
    val gnugnuCount: Int = 0,
    val voltCoinsEarnedThisRun: Int = 0,
    val comboMultiplier: Int = 1,
    val comboTimer: Float = 0f,
    val maxComboAchieved: Int = 1,
    val health: Float = 100f, // Breaker Stability 0..100
    val shield: Float = 100f, // Shield 0..100
    val overdriveVoltage: Float = 0f, // 0..100
    val isOverdriveActive: Boolean = false,
    val overdriveTimeLeft: Float = 0f,
    val bombCount: Int = 3,
    val activeTool: ToolType = ToolType.SCREWDRIVER,
    val activePowerUp: PowerUpType? = null,
    val powerUpTimeLeft: Float = 0f,
    val timeAttackSecondsLeft: Float = 60f,
    val apprenticesDefeatedInLevel: Int = 0,
    val levelTargetDefeated: Int = 15,
    val bossActive: Boolean = false,
    val bossHpRatio: Float = 1.0f,
    val unlockedTools: Set<ToolType> = setOf(ToolType.SCREWDRIVER, ToolType.PLIERS, ToolType.TAPE),
    val totalVoltCoins: Int = 200,
    val totalGnugnusCareer: Int = 0,
    val screwdriverLevel: Int = 1,
    val pliersLevel: Int = 1,
    val tapeLevel: Int = 1,
    val multimeterLevel: Int = 1,
    val breakerLevel: Int = 1,
    // Player Ship Position (ratio 0..1)
    val playerXRatio: Float = 0.5f,
    val playerYRatio: Float = 0.85f,
    val isAutoFiring: Boolean = true,
    val screenShakeIntensity: Float = 0f,
    val isSoundEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true
)

enum class AppScreen {
    MAIN_MENU,
    GAMEPLAY,
    WORKSHOP,
    HIGHSCORES,
    INSTRUCTIONS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundEngine = SoundEngine(application)

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "super_electrician.db"
    ).build()

    private val dao = db.gameDao()

    val topScores = dao.getTopScores().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Active Shooter Game Entities
    val enemies = mutableListOf<Enemy>()
    val projectiles = mutableListOf<Projectile>()
    val enemySparks = mutableListOf<EnemySpark>()
    val powerUps = mutableListOf<ShooterPowerUp>()
    val floatingCoins = mutableListOf<FloatingCoin>()
    val shockwaves = mutableListOf<Shockwave>()
    val particles = mutableListOf<GameParticle>()
    val popups = mutableListOf<GnuGnuPopup>()

    // Screen Dimensions in virtual units
    var canvasWidth = 1000f
    var canvasHeight = 1600f

    val levels = listOf(
        LevelConfig(1, "Settore 1: Officina Elettrica", "Addestramento Blaster contro gli Apprendisti Volanti!", 15, 1.0f),
        LevelConfig(2, "Settore 2: Condominio ad Alta Tensione", "Sciami veloci di apprendisti con cesoie e laser!", 25, 1.4f),
        LevelConfig(3, "Settore 3: Centrale Elettrica Industriale", "Raffica pesante, batterie blindate e campi di scintille!", 35, 1.8f),
        LevelConfig(4, "Settore 4: Sottostazione 380V & BOSS", "Scontro finale contro il Mega Capo Cantiere 380V!", 45, 2.2f, allowsBoss = true)
    )

    private var nextEntityId = 1L
    private var fireCooldown = 0f
    private var waveSpawnTimer = 0f
    private var bossSpawned = false
    private var backgroundScrollOffset = 0f
    private val random = Random(System.currentTimeMillis())

    private val funnyGnugnuPhrases = listOf(
        "GNUGNU! ⚡",
        "GNU-GNU!!",
        "Ahi Gnugnu!",
        "GNUUUGNUU!",
        "380V Gnugnu!",
        "Salvavita Gnugnu!"
    )

    init {
        viewModelScope.launch {
            dao.getPlayerProgress().collect { progress ->
                progress?.let { p ->
                    val unlocked = mutableSetOf(ToolType.SCREWDRIVER)
                    if (p.pliersLevel > 0) unlocked.add(ToolType.PLIERS)
                    if (p.tapeLevel > 0) unlocked.add(ToolType.TAPE)
                    if (p.multimeterLevel > 0) unlocked.add(ToolType.MULTIMETER)
                    if (p.breakerLevel > 0) unlocked.add(ToolType.BREAKER_BOMB)

                    _uiState.update { current ->
                        current.copy(
                            totalVoltCoins = p.totalVoltCoins,
                            totalGnugnusCareer = p.totalGnugnus,
                            screwdriverLevel = p.screwdriverLevel.coerceAtLeast(1),
                            pliersLevel = p.pliersLevel.coerceAtLeast(1),
                            tapeLevel = p.tapeLevel.coerceAtLeast(1),
                            multimeterLevel = p.multimeterLevel.coerceAtLeast(1),
                            breakerLevel = p.breakerLevel.coerceAtLeast(1),
                            unlockedTools = unlocked
                        )
                    }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.GAMEPLAY) {
            _uiState.update { it.copy(isPlaying = false, isPaused = false) }
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        soundEngine.isSoundEnabled = enabled
        _uiState.update { it.copy(isSoundEnabled = enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        soundEngine.isHapticsEnabled = enabled
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
    }

    fun selectTool(tool: ToolType) {
        if (_uiState.value.unlockedTools.contains(tool)) {
            _uiState.update { it.copy(activeTool = tool) }
            soundEngine.playZap()
        }
    }

    fun toggleAutoFire() {
        _uiState.update { it.copy(isAutoFiring = !it.isAutoFiring) }
    }

    fun startNewGame(mode: GameMode, levelIndex: Int = 0) {
        synchronized(enemies) { enemies.clear() }
        synchronized(projectiles) { projectiles.clear() }
        synchronized(enemySparks) { enemySparks.clear() }
        synchronized(powerUps) { powerUps.clear() }
        synchronized(floatingCoins) { floatingCoins.clear() }
        synchronized(shockwaves) { shockwaves.clear() }
        synchronized(particles) { particles.clear() }
        synchronized(popups) { popups.clear() }

        bossSpawned = false
        waveSpawnTimer = 0.5f
        fireCooldown = 0f

        val target = if (mode == GameMode.CAMPAIGN) levels[levelIndex.coerceIn(0, levels.size - 1)].targetApprentices else 9999

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAMEPLAY,
                gameMode = mode,
                currentLevelIndex = levelIndex,
                isPlaying = true,
                isPaused = false,
                isGameOver = false,
                isVictory = false,
                score = 0,
                gnugnuCount = 0,
                voltCoinsEarnedThisRun = 0,
                comboMultiplier = 1,
                comboTimer = 0f,
                maxComboAchieved = 1,
                health = 100f,
                shield = 100f,
                overdriveVoltage = 0f,
                isOverdriveActive = false,
                overdriveTimeLeft = 0f,
                bombCount = 3,
                activePowerUp = null,
                powerUpTimeLeft = 0f,
                timeAttackSecondsLeft = 60f,
                apprenticesDefeatedInLevel = 0,
                levelTargetDefeated = target,
                bossActive = false,
                bossHpRatio = 1f,
                playerXRatio = 0.5f,
                playerYRatio = 0.85f,
                screenShakeIntensity = 0f
            )
        }
    }

    fun restartCurrentGame() {
        startNewGame(_uiState.value.gameMode, _uiState.value.currentLevelIndex)
    }

    fun pauseGame() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun nextLevel() {
        val nextIdx = _uiState.value.currentLevelIndex + 1
        if (nextIdx < levels.size) {
            startNewGame(GameMode.CAMPAIGN, nextIdx)
        } else {
            navigateTo(AppScreen.MAIN_MENU)
        }
    }

    /**
     * Move player to position (Drag / Touch)
     */
    fun movePlayer(targetX: Float, targetY: Float) {
        val clampedX = (targetX / canvasWidth).coerceIn(0.08f, 0.92f)
        val clampedY = (targetY / canvasHeight).coerceIn(0.15f, 0.92f)
        _uiState.update { it.copy(playerXRatio = clampedX, playerYRatio = clampedY) }
    }

    /**
     * Trigger Super Bomb (Magnetotermico Shockwave)
     */
    fun triggerMagnetoBomb() {
        val state = _uiState.value
        if (state.bombCount <= 0 || !state.isPlaying || state.isPaused) return

        soundEngine.playBomb()
        _uiState.update {
            it.copy(
                bombCount = it.bombCount - 1,
                screenShakeIntensity = 25f
            )
        }

        val px = canvasWidth * state.playerXRatio
        val py = canvasHeight * state.playerYRatio

        // Spawn massive shockwave
        synchronized(shockwaves) {
            shockwaves.add(
                Shockwave(
                    id = nextEntityId++,
                    x = px,
                    y = py,
                    radius = 20f,
                    maxRadius = canvasWidth * 1.5f,
                    alpha = 1f
                )
            )
        }

        // Clear all enemy bullets
        synchronized(enemySparks) {
            for (bullet in enemySparks) {
                spawnSparkExplosion(bullet.x, bullet.y, 4, bullet.type.name)
            }
            enemySparks.clear()
        }

        // Damage all enemies heavily
        synchronized(enemies) {
            for (enemy in enemies) {
                damageEnemy(enemy, 350, isBomb = true)
            }
        }
    }

    /**
     * Trigger 380V Super Overdrive
     */
    fun activateSuperOverdrive() {
        val state = _uiState.value
        if (state.overdriveVoltage < 99f && !state.isOverdriveActive) return

        soundEngine.playSupercharge()
        _uiState.update {
            it.copy(
                isOverdriveActive = true,
                overdriveTimeLeft = 8.0f,
                overdriveVoltage = 0f,
                shield = 100f,
                screenShakeIntensity = 15f
            )
        }
    }

    /**
     * Shooter Game Tick (60 FPS Update Loop)
     */
    fun updateGameTick(dt: Float) {
        val state = _uiState.value
        if (!state.isPlaying || state.isPaused || state.isGameOver || state.isVictory) return

        // 1. Screen Shake Decay
        if (state.screenShakeIntensity > 0f) {
            _uiState.update { it.copy(screenShakeIntensity = (it.screenShakeIntensity - dt * 30f).coerceAtLeast(0f)) }
        }

        // 2. Power-Up Timers
        if (state.activePowerUp != null) {
            val newTime = state.powerUpTimeLeft - dt
            if (newTime <= 0f) {
                _uiState.update { it.copy(activePowerUp = null, powerUpTimeLeft = 0f) }
            } else {
                _uiState.update { it.copy(powerUpTimeLeft = newTime) }
            }
        }

        // 3. Overdrive Timer
        if (state.isOverdriveActive) {
            val newTime = state.overdriveTimeLeft - dt
            if (newTime <= 0f) {
                _uiState.update { it.copy(isOverdriveActive = false, overdriveTimeLeft = 0f) }
            } else {
                _uiState.update { it.copy(overdriveTimeLeft = newTime) }
            }
        }

        // 4. Combo Multiplier Decay
        if (state.comboTimer > 0f) {
            val newTimer = state.comboTimer - dt
            if (newTimer <= 0f) {
                _uiState.update { it.copy(comboMultiplier = 1, comboTimer = 0f) }
            } else {
                _uiState.update { it.copy(comboTimer = newTimer) }
            }
        }

        // 5. Time Attack Mode countdown
        if (state.gameMode == GameMode.TIME_ATTACK) {
            val newTime = state.timeAttackSecondsLeft - dt
            if (newTime <= 0f) {
                finishGame(victory = true)
                return
            }
            _uiState.update { it.copy(timeAttackSecondsLeft = newTime) }
        }

        // 6. Continuous Rapid Blaster Fire
        fireCooldown -= dt
        if (state.isAutoFiring && fireCooldown <= 0f) {
            firePlayerWeapon()
        }

        // 7. Enemy Wave Spawning
        updateEnemySpawning(dt)

        // 8. Update Projectiles & Collisions
        updateProjectiles(dt)

        // 9. Update Enemies (Movement, Attacks, Gnugnu)
        updateEnemies(dt)

        // 10. Update Enemy Bullets & Player Damage
        updateEnemyBullets(dt)

        // 11. Update PowerUps & Floating Coins
        updateItems(dt)

        // 12. Update Shockwaves & Visual FX
        updateVisualFx(dt)

        // 13. Check Level Target Victory
        if (state.gameMode == GameMode.CAMPAIGN) {
            if (state.apprenticesDefeatedInLevel >= state.levelTargetDefeated && !bossSpawned) {
                val currentLvl = levels[state.currentLevelIndex.coerceIn(0, levels.size - 1)]
                if (currentLvl.allowsBoss) {
                    spawnBoss()
                } else if (enemies.isEmpty()) {
                    finishGame(victory = true)
                }
            } else if (bossSpawned && enemies.isEmpty()) {
                finishGame(victory = true)
            }
        }
    }

    private fun firePlayerWeapon() {
        val state = _uiState.value
        val tool = state.activeTool
        val toolLevel = when (tool) {
            ToolType.SCREWDRIVER -> state.screwdriverLevel
            ToolType.PLIERS -> state.pliersLevel
            ToolType.TAPE -> state.tapeLevel
            ToolType.MULTIMETER -> state.multimeterLevel
            ToolType.BREAKER_BOMB -> state.breakerLevel
        }

        val baseDamage = tool.baseDamage + (toolLevel - 1) * 12
        val firerateFactor = if (state.activePowerUp == PowerUpType.SPEED_FIRE || state.isOverdriveActive) 0.5f else 1.0f
        fireCooldown = tool.fireIntervalSec * firerateFactor

        val px = canvasWidth * state.playerXRatio
        val py = canvasHeight * state.playerYRatio - 35f

        val isTriple = state.activePowerUp == PowerUpType.TRIFASE_SPREAD || state.isOverdriveActive
        val isSuperBeam = state.activePowerUp == PowerUpType.PLASMA_BEAM || state.isOverdriveActive

        soundEngine.playShoot()

        synchronized(projectiles) {
            if (isTriple) {
                // Triple Spread Shot 380V
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px,
                        y = py,
                        vx = 0f,
                        vy = -tool.bulletSpeed,
                        damage = (baseDamage * 1.2f).toInt(),
                        pierceCount = if (tool == ToolType.PLIERS) 3 else 1,
                        isTripleShot = true
                    )
                )
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px - 20f,
                        y = py,
                        vx = -tool.bulletSpeed * 0.25f,
                        vy = -tool.bulletSpeed * 0.95f,
                        damage = (baseDamage * 1.2f).toInt(),
                        pierceCount = if (tool == ToolType.PLIERS) 3 else 1,
                        isTripleShot = true
                    )
                )
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px + 20f,
                        y = py,
                        vx = tool.bulletSpeed * 0.25f,
                        vy = -tool.bulletSpeed * 0.95f,
                        damage = (baseDamage * 1.2f).toInt(),
                        pierceCount = if (tool == ToolType.PLIERS) 3 else 1,
                        isTripleShot = true
                    )
                )
            } else if (tool == ToolType.PLIERS) {
                // Piercing Dual Blades
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px - 15f,
                        y = py,
                        vx = 0f,
                        vy = -tool.bulletSpeed,
                        damage = baseDamage,
                        pierceCount = 3
                    )
                )
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px + 15f,
                        y = py,
                        vx = 0f,
                        vy = -tool.bulletSpeed,
                        damage = baseDamage,
                        pierceCount = 3
                    )
                )
            } else if (tool == ToolType.TAPE) {
                // Bouncing Tape Orb with angle
                val angleSpread = (random.nextFloat() - 0.5f) * 0.4f
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px,
                        y = py,
                        vx = tool.bulletSpeed * angleSpread,
                        vy = -tool.bulletSpeed,
                        damage = baseDamage,
                        bouncesLeft = 2,
                        pierceCount = 2
                    )
                )
            } else {
                // Standard Rapid Laser Blaster / Plasma
                projectiles.add(
                    Projectile(
                        id = nextEntityId++,
                        toolType = tool,
                        x = px,
                        y = py,
                        vx = 0f,
                        vy = -tool.bulletSpeed,
                        damage = if (isSuperBeam) baseDamage * 2 else baseDamage,
                        pierceCount = if (isSuperBeam) 5 else 1,
                        isSuperBeam = isSuperBeam
                    )
                )
            }
        }
    }

    private fun updateEnemySpawning(dt: Float) {
        val state = _uiState.value
        if (bossSpawned) return

        waveSpawnTimer -= dt
        if (waveSpawnTimer <= 0f) {
            val levelMultiplier = if (state.gameMode == GameMode.CAMPAIGN) {
                levels[state.currentLevelIndex.coerceIn(0, levels.size - 1)].spawnRateMultiplier
            } else 1.6f

            waveSpawnTimer = (1.8f / levelMultiplier) + random.nextFloat() * 0.8f

            // Spawn Formations
            val formation = random.nextInt(4)
            val enemyType = when {
                state.currentLevelIndex >= 2 && random.nextFloat() < 0.25f -> ApprenticeType.HEAVY_BATTERY
                state.currentLevelIndex >= 1 && random.nextFloat() < 0.35f -> ApprenticeType.SPARK_SNIPER
                random.nextFloat() < 0.4f -> ApprenticeType.DIVER
                else -> ApprenticeType.SCOUT
            }

            synchronized(enemies) {
                when (formation) {
                    0 -> {
                        // V-Formation
                        val centerX = canvasWidth * (0.3f + random.nextFloat() * 0.4f)
                        enemies.add(Enemy(nextEntityId++, enemyType, centerX, -60f, 0f, enemyType.speed, enemyType.baseHp, enemyType.baseHp, FlightPattern.STRAIGHT_DOWN))
                        enemies.add(Enemy(nextEntityId++, enemyType, centerX - 70f, -130f, 0f, enemyType.speed, enemyType.baseHp, enemyType.baseHp, FlightPattern.STRAIGHT_DOWN))
                        enemies.add(Enemy(nextEntityId++, enemyType, centerX + 70f, -130f, 0f, enemyType.speed, enemyType.baseHp, enemyType.baseHp, FlightPattern.STRAIGHT_DOWN))
                    }
                    1 -> {
                        // Sine-Wave Squad
                        val startX = canvasWidth * 0.15f
                        for (i in 0..3) {
                            enemies.add(
                                Enemy(
                                    id = nextEntityId++,
                                    type = enemyType,
                                    x = startX,
                                    y = -60f - (i * 80f),
                                    vx = 0f,
                                    vy = enemyType.speed * 0.9f,
                                    hp = enemyType.baseHp,
                                    maxHp = enemyType.baseHp,
                                    pattern = FlightPattern.SINE_WAVE_LEFT
                                )
                            )
                        }
                    }
                    2 -> {
                        // ZigZag Diver Attack
                        val startX = canvasWidth * (0.2f + random.nextFloat() * 0.6f)
                        enemies.add(
                            Enemy(
                                id = nextEntityId++,
                                type = ApprenticeType.DIVER,
                                x = startX,
                                y = -60f,
                                vx = 3.5f,
                                vy = 5.0f,
                                hp = ApprenticeType.DIVER.baseHp,
                                maxHp = ApprenticeType.DIVER.baseHp,
                                pattern = FlightPattern.ZIGZAG
                            )
                        )
                    }
                    else -> {
                        // Heavy Squad Line
                        val startX = canvasWidth * (0.25f + random.nextFloat() * 0.5f)
                        enemies.add(
                            Enemy(
                                id = nextEntityId++,
                                type = enemyType,
                                x = startX,
                                y = -70f,
                                vx = 0f,
                                vy = enemyType.speed,
                                hp = enemyType.baseHp,
                                maxHp = enemyType.baseHp,
                                pattern = FlightPattern.STRAIGHT_DOWN
                            )
                        )
                    }
                }
            }
        }
    }

    private fun spawnBoss() {
        bossSpawned = true
        _uiState.update { it.copy(bossActive = true, bossHpRatio = 1.0f) }
        soundEngine.playSupercharge()

        synchronized(enemies) {
            enemies.add(
                Enemy(
                    id = nextEntityId++,
                    type = ApprenticeType.BOSS_FOREMAN,
                    x = canvasWidth * 0.5f,
                    y = -120f,
                    vx = 2.5f,
                    vy = 1.5f,
                    hp = ApprenticeType.BOSS_FOREMAN.baseHp,
                    maxHp = ApprenticeType.BOSS_FOREMAN.baseHp,
                    pattern = FlightPattern.BOSS_ORBIT,
                    gnugnuPhrase = "380V GNUGNU BOSS!"
                )
            )
        }
    }

    private fun updateProjectiles(dt: Float) {
        synchronized(projectiles) {
            val iterator = projectiles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx
                p.y += p.vy
                p.rotation += p.rotationSpeed
                p.lifeTime += dt

                // Bounce off side walls for Tape
                if (p.bouncesLeft > 0) {
                    if (p.x <= 20f || p.x >= canvasWidth - 20f) {
                        p.vx = -p.vx
                        p.bouncesLeft--
                        soundEngine.playZap()
                    }
                }

                // Check out of bounds
                if (p.y < -50f || p.y > canvasHeight + 50f || p.x < -50f || p.x > canvasWidth + 50f) {
                    iterator.remove()
                    continue
                }

                // Check collision with enemies
                var hitEnemy = false
                synchronized(enemies) {
                    for (enemy in enemies) {
                        if (enemy.state == EnemyState.DEFEATED || enemy.state == EnemyState.DYING) continue
                        val radius = enemy.type.sizeRadius
                        val distSq = (p.x - enemy.x) * (p.x - enemy.x) + (p.y - enemy.y) * (p.y - enemy.y)
                        if (distSq <= radius * radius + 400f) {
                            damageEnemy(enemy, p.damage, isBomb = false)
                            p.pierceCount--
                            hitEnemy = true
                            if (p.pierceCount <= 0) break
                        }
                    }
                }

                if (hitEnemy && p.pierceCount <= 0) {
                    spawnSparkExplosion(p.x, p.y, 6, "SPARK")
                    iterator.remove()
                }
            }
        }
    }

    private fun updateEnemies(dt: Float) {
        val state = _uiState.value
        val px = canvasWidth * state.playerXRatio
        val py = canvasHeight * state.playerYRatio

        synchronized(enemies) {
            val iterator = enemies.iterator()
            while (iterator.hasNext()) {
                val enemy = iterator.next()
                enemy.totalTime += dt
                enemy.stateTime += dt
                enemy.shootCooldown -= dt

                // Motion according to flight pattern
                when (enemy.pattern) {
                    FlightPattern.STRAIGHT_DOWN -> {
                        enemy.y += enemy.vy
                    }
                    FlightPattern.SINE_WAVE_LEFT -> {
                        enemy.y += enemy.vy
                        enemy.x = enemy.initialX + sin(enemy.totalTime * 3f) * 140f
                    }
                    FlightPattern.SINE_WAVE_RIGHT -> {
                        enemy.y += enemy.vy
                        enemy.x = enemy.initialX - sin(enemy.totalTime * 3f) * 140f
                    }
                    FlightPattern.ZIGZAG -> {
                        enemy.y += enemy.vy
                        enemy.x += enemy.vx
                        if (enemy.x <= 50f || enemy.x >= canvasWidth - 50f) {
                            enemy.vx = -enemy.vx
                        }
                    }
                    FlightPattern.DIVE_BOMBER -> {
                        val dx = px - enemy.x
                        enemy.x += dx * 0.02f
                        enemy.y += enemy.vy * 1.3f
                    }
                    FlightPattern.BOSS_ORBIT -> {
                        if (enemy.y < 220f) {
                            enemy.y += 1.5f
                        } else {
                            enemy.x += enemy.vx
                            if (enemy.x <= 150f || enemy.x >= canvasWidth - 150f) {
                                enemy.vx = -enemy.vx
                            }
                            enemy.y = 220f + sin(enemy.totalTime * 2f) * 40f
                        }
                    }
                    else -> {
                        enemy.y += enemy.vy
                    }
                }

                // Enemy Shooting at Player
                if (enemy.shootCooldown <= 0f && enemy.y in 50f..(canvasHeight * 0.7f)) {
                    enemy.shootCooldown = enemy.type.bulletIntervalSec + random.nextFloat() * 0.5f
                    shootEnemyBullet(enemy, px, py)
                }

                // Despawn if off bottom screen
                if (enemy.y > canvasHeight + 100f) {
                    // Penalty to breaker stability
                    damageBreaker(8f)
                    iterator.remove()
                    continue
                }

                // Remove if defeated
                if (enemy.state == EnemyState.DEFEATED) {
                    iterator.remove()
                }
            }
        }
    }

    private fun shootEnemyBullet(enemy: Enemy, targetX: Float, targetY: Float) {
        val dx = targetX - enemy.x
        val dy = targetY - enemy.y
        val angle = atan2(dy, dx)
        val bulletSpeed = 6.5f

        synchronized(enemySparks) {
            if (enemy.type.isBoss) {
                // Boss multi-ring arc
                for (offset in listOf(-0.35f, 0f, 0.35f)) {
                    val finalAngle = angle + offset
                    enemySparks.add(
                        EnemySpark(
                            id = nextEntityId++,
                            x = enemy.x,
                            y = enemy.y + 40f,
                            vx = cos(finalAngle) * bulletSpeed,
                            vy = sin(finalAngle) * bulletSpeed,
                            type = EnemyBulletType.HIGH_VOLTAGE_LASER,
                            radius = 9f,
                            damage = 18f
                        )
                    )
                }
            } else if (enemy.type == ApprenticeType.SPARK_SNIPER) {
                enemySparks.add(
                    EnemySpark(
                        id = nextEntityId++,
                        x = enemy.x,
                        y = enemy.y + 20f,
                        vx = cos(angle) * 8.5f,
                        vy = sin(angle) * 8.5f,
                        type = EnemyBulletType.SPARK_BOLT,
                        radius = 7f,
                        damage = 14f
                    )
                )
            } else {
                enemySparks.add(
                    EnemySpark(
                        id = nextEntityId++,
                        x = enemy.x,
                        y = enemy.y + 20f,
                        vx = cos(angle) * bulletSpeed,
                        vy = sin(angle) * bulletSpeed,
                        type = EnemyBulletType.ELECTRIC_ORB,
                        radius = 6f,
                        damage = 10f
                    )
                )
            }
        }
    }

    private fun damageEnemy(enemy: Enemy, damage: Int, isBomb: Boolean) {
        enemy.hp -= damage

        // Play comical "Gnugnu!" and spawn popup
        soundEngine.playGnuGnu()
        soundEngine.playHit()

        // Overdrive & Combo gains
        val combo = (_uiState.value.comboMultiplier + 1).coerceAtMost(10)
        val maxCombo = maxOf(_uiState.value.maxComboAchieved, combo)
        val scoreInc = enemy.type.points * combo

        _uiState.update {
            it.copy(
                score = it.score + scoreInc,
                gnugnuCount = it.gnugnuCount + 1,
                comboMultiplier = combo,
                comboTimer = 3.5f,
                maxComboAchieved = maxCombo,
                overdriveVoltage = (it.overdriveVoltage + 6f).coerceAtMost(100f)
            )
        }

        // Spawn Gnugnu comic popup
        val phrase = funnyGnugnuPhrases[random.nextInt(funnyGnugnuPhrases.size)]
        synchronized(popups) {
            popups.add(
                GnuGnuPopup(
                    id = nextEntityId++,
                    x = enemy.x,
                    y = enemy.y - 20f,
                    text = phrase,
                    scale = if (isBomb) 1.2f else 0.85f
                )
            )
        }

        spawnSparkExplosion(enemy.x, enemy.y, 10, "ELECTRIC")

        if (enemy.type.isBoss) {
            _uiState.update { it.copy(bossHpRatio = (enemy.hp.toFloat() / enemy.maxHp).coerceIn(0f, 1f)) }
        }

        if (enemy.hp <= 0) {
            enemy.state = EnemyState.DEFEATED
            soundEngine.playExplosion()
            spawnSparkExplosion(enemy.x, enemy.y, 25, "SUPER")

            val coinsEarned = if (enemy.type.isBoss) 50 else (random.nextInt(3) + 2)
            _uiState.update {
                it.copy(
                    apprenticesDefeatedInLevel = it.apprenticesDefeatedInLevel + 1,
                    voltCoinsEarnedThisRun = it.voltCoinsEarnedThisRun + coinsEarned,
                    totalVoltCoins = it.totalVoltCoins + coinsEarned,
                    totalGnugnusCareer = it.totalGnugnusCareer + 1
                )
            }

            // Spawn floating Volt Coins
            synchronized(floatingCoins) {
                for (i in 0 until coinsEarned.coerceAtMost(6)) {
                    floatingCoins.add(
                        FloatingCoin(
                            id = nextEntityId++,
                            x = enemy.x + (random.nextFloat() - 0.5f) * 40f,
                            y = enemy.y + (random.nextFloat() - 0.5f) * 40f,
                            vx = (random.nextFloat() - 0.5f) * 3f,
                            vy = 2.5f + random.nextFloat() * 2f,
                            value = 5
                        )
                    )
                }
            }

            // Chance to drop power-up
            if (random.nextFloat() < 0.22f || enemy.type.isBoss) {
                val pType = PowerUpType.values()[random.nextInt(PowerUpType.values().size)]
                synchronized(powerUps) {
                    powerUps.add(
                        ShooterPowerUp(
                            id = nextEntityId++,
                            x = enemy.x,
                            y = enemy.y,
                            type = pType
                        )
                    )
                }
            }
        }
    }

    private fun updateEnemyBullets(dt: Float) {
        val state = _uiState.value
        val px = canvasWidth * state.playerXRatio
        val py = canvasHeight * state.playerYRatio

        synchronized(enemySparks) {
            val iterator = enemySparks.iterator()
            while (iterator.hasNext()) {
                val spark = iterator.next()
                spark.x += spark.vx
                spark.y += spark.vy

                // Out of screen
                if (spark.y > canvasHeight + 40f || spark.y < -40f || spark.x < -40f || spark.x > canvasWidth + 40f) {
                    iterator.remove()
                    continue
                }

                // Check collision with Player Ship
                val distSq = (spark.x - px) * (spark.x - px) + (spark.y - py) * (spark.y - py)
                val hitRadius = 26f
                if (distSq <= hitRadius * hitRadius) {
                    // Hit player
                    damagePlayer(spark.damage)
                    spawnSparkExplosion(spark.x, spark.y, 8, "PLAYER_HIT")
                    iterator.remove()
                }
            }
        }
    }

    private fun damagePlayer(dmg: Float) {
        val state = _uiState.value
        if (state.isOverdriveActive) return // Invulnerable in 380V Overdrive

        soundEngine.playHit()
        _uiState.update { it.copy(screenShakeIntensity = 10f) }

        if (state.shield > 0f) {
            val remainingShield = state.shield - dmg
            if (remainingShield < 0f) {
                _uiState.update { it.copy(shield = 0f, health = (it.health + remainingShield).coerceAtLeast(0f)) }
            } else {
                _uiState.update { it.copy(shield = remainingShield) }
            }
        } else {
            val newHealth = (state.health - dmg).coerceAtLeast(0f)
            _uiState.update { it.copy(health = newHealth) }
            if (newHealth <= 0f) {
                finishGame(victory = false)
            }
        }
    }

    private fun damageBreaker(amount: Float) {
        val newHealth = (_uiState.value.health - amount).coerceAtLeast(0f)
        _uiState.update { it.copy(health = newHealth) }
        if (newHealth <= 0f) {
            finishGame(victory = false)
        }
    }

    private fun updateItems(dt: Float) {
        val state = _uiState.value
        val px = canvasWidth * state.playerXRatio
        val py = canvasHeight * state.playerYRatio

        val hasMagnet = state.activePowerUp == PowerUpType.MAGNET_COINS || state.isOverdriveActive

        // Floating Coins
        synchronized(floatingCoins) {
            val iterator = floatingCoins.iterator()
            while (iterator.hasNext()) {
                val coin = iterator.next()
                coin.lifeTime -= dt

                if (hasMagnet) {
                    val dx = px - coin.x
                    val dy = py - coin.y
                    coin.x += dx * 0.12f
                    coin.y += dy * 0.12f
                } else {
                    coin.x += coin.vx
                    coin.y += coin.vy
                }

                // Check pickup
                val distSq = (coin.x - px) * (coin.x - px) + (coin.y - py) * (coin.y - py)
                if (distSq <= 40f * 40f) {
                    soundEngine.playZap()
                    _uiState.update {
                        it.copy(
                            score = it.score + 50,
                            voltCoinsEarnedThisRun = it.voltCoinsEarnedThisRun + coin.value,
                            totalVoltCoins = it.totalVoltCoins + coin.value
                        )
                    }
                    iterator.remove()
                    continue
                }

                if (coin.lifeTime <= 0f || coin.y > canvasHeight + 50f) {
                    iterator.remove()
                }
            }
        }

        // PowerUps
        synchronized(powerUps) {
            val iterator = powerUps.iterator()
            while (iterator.hasNext()) {
                val pu = iterator.next()
                pu.y += pu.vy
                pu.rotation += 4f
                pu.lifeTime -= dt

                val distSq = (pu.x - px) * (pu.x - px) + (pu.y - py) * (pu.y - py)
                if (distSq <= 45f * 45f) {
                    soundEngine.playPowerUp()
                    if (pu.type == PowerUpType.BOMB_RECHARGE) {
                        _uiState.update { it.copy(bombCount = (it.bombCount + 1).coerceAtMost(5)) }
                    } else if (pu.type == PowerUpType.SHIELD) {
                        _uiState.update { it.copy(shield = 100f, activePowerUp = pu.type, powerUpTimeLeft = pu.type.durationSec) }
                    } else {
                        _uiState.update { it.copy(activePowerUp = pu.type, powerUpTimeLeft = pu.type.durationSec) }
                    }

                    synchronized(popups) {
                        popups.add(
                            GnuGnuPopup(
                                id = nextEntityId++,
                                x = px,
                                y = py - 40f,
                                text = pu.type.title,
                                color = pu.type.color
                            )
                        )
                    }
                    iterator.remove()
                    continue
                }

                if (pu.lifeTime <= 0f || pu.y > canvasHeight + 50f) {
                    iterator.remove()
                }
            }
        }
    }

    private fun updateVisualFx(dt: Float) {
        // Shockwaves
        synchronized(shockwaves) {
            val iterator = shockwaves.iterator()
            while (iterator.hasNext()) {
                val sw = iterator.next()
                sw.radius += 25f
                sw.alpha = (1f - (sw.radius / sw.maxRadius)).coerceIn(0f, 1f)
                if (sw.radius >= sw.maxRadius) {
                    iterator.remove()
                }
            }
        }

        // Particles
        synchronized(particles) {
            val iterator = particles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.x += p.vx
                p.y += p.vy
                p.life -= dt
                p.alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
                if (p.life <= 0f) {
                    iterator.remove()
                }
            }
        }

        // Popups
        synchronized(popups) {
            val iterator = popups.iterator()
            while (iterator.hasNext()) {
                val popup = iterator.next()
                popup.y += popup.vy
                popup.life -= dt
                popup.alpha = (popup.life / 1.2f).coerceIn(0f, 1f)
                popup.scale = (popup.scale + 0.015f).coerceAtMost(1.3f)
                if (popup.life <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    private fun spawnSparkExplosion(x: Float, y: Float, count: Int, type: String) {
        synchronized(particles) {
            for (i in 0 until count) {
                val angle = random.nextFloat() * 2f * PI.toFloat()
                val speed = 2f + random.nextFloat() * 8f
                val color = when (type) {
                    "SUPER" -> listOf(androidx.compose.ui.graphics.Color(0xFF00F0FF), androidx.compose.ui.graphics.Color(0xFFFFEE00), androidx.compose.ui.graphics.Color(0xFFFF0055))[random.nextInt(3)]
                    "PLAYER_HIT" -> androidx.compose.ui.graphics.Color(0xFFEF4444)
                    else -> listOf(androidx.compose.ui.graphics.Color(0xFFFFD600), androidx.compose.ui.graphics.Color(0xFF38BDF8))[random.nextInt(2)]
                }

                particles.add(
                    GameParticle(
                        x = x,
                        y = y,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = color,
                        alpha = 1f,
                        size = 3f + random.nextFloat() * 5f,
                        life = 0.4f + random.nextFloat() * 0.4f,
                        maxLife = 0.8f
                    )
                )
            }
        }
    }

    private fun finishGame(victory: Boolean) {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                isPlaying = false,
                isGameOver = !victory,
                isVictory = victory
            )
        }

        if (victory) {
            soundEngine.playVictory()
        } else {
            soundEngine.playExplosion()
        }

        // Save high score and player progress
        viewModelScope.launch {
            dao.insertScore(
                GameScore(
                    score = state.score,
                    gnugnuCount = state.gnugnuCount,
                    maxCombo = state.maxComboAchieved,
                    gameMode = state.gameMode.name,
                    levelReached = state.currentLevelIndex + 1
                )
            )

            dao.savePlayerProgress(
                PlayerProgress(
                    totalVoltCoins = state.totalVoltCoins,
                    totalGnugnus = state.totalGnugnusCareer,
                    screwdriverLevel = state.screwdriverLevel,
                    pliersLevel = state.pliersLevel,
                    tapeLevel = state.tapeLevel,
                    multimeterLevel = state.multimeterLevel,
                    breakerLevel = state.breakerLevel
                )
            )
        }
    }

    fun buyToolUpgrade(tool: ToolType) {
        val state = _uiState.value
        val currentLevel = when (tool) {
            ToolType.SCREWDRIVER -> state.screwdriverLevel
            ToolType.PLIERS -> state.pliersLevel
            ToolType.TAPE -> state.tapeLevel
            ToolType.MULTIMETER -> state.multimeterLevel
            ToolType.BREAKER_BOMB -> state.breakerLevel
        }

        val cost = when (tool) {
            ToolType.SCREWDRIVER -> currentLevel * 100
            ToolType.PLIERS -> currentLevel * 150
            ToolType.TAPE -> currentLevel * 200
            ToolType.MULTIMETER -> currentLevel * 300
            ToolType.BREAKER_BOMB -> currentLevel * 500
        }

        if (state.totalVoltCoins >= cost) {
            soundEngine.playPowerUp()
            val newCoins = state.totalVoltCoins - cost
            val newLevel = currentLevel + 1
            val newUnlocked = state.unlockedTools + tool

            _uiState.update { current ->
                when (tool) {
                    ToolType.SCREWDRIVER -> current.copy(screwdriverLevel = newLevel, totalVoltCoins = newCoins, unlockedTools = newUnlocked)
                    ToolType.PLIERS -> current.copy(pliersLevel = newLevel, totalVoltCoins = newCoins, unlockedTools = newUnlocked)
                    ToolType.TAPE -> current.copy(tapeLevel = newLevel, totalVoltCoins = newCoins, unlockedTools = newUnlocked)
                    ToolType.MULTIMETER -> current.copy(multimeterLevel = newLevel, totalVoltCoins = newCoins, unlockedTools = newUnlocked)
                    ToolType.BREAKER_BOMB -> current.copy(breakerLevel = newLevel, totalVoltCoins = newCoins, unlockedTools = newUnlocked)
                }
            }

            // Save to database
            viewModelScope.launch {
                val updatedState = _uiState.value
                dao.savePlayerProgress(
                    PlayerProgress(
                        totalVoltCoins = updatedState.totalVoltCoins,
                        totalGnugnus = updatedState.totalGnugnusCareer,
                        screwdriverLevel = updatedState.screwdriverLevel,
                        pliersLevel = updatedState.pliersLevel,
                        tapeLevel = updatedState.tapeLevel,
                        multimeterLevel = updatedState.multimeterLevel,
                        breakerLevel = updatedState.breakerLevel
                    )
                )
            }
        }
    }
}
