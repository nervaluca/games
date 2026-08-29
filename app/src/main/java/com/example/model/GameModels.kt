package com.example.model

import androidx.compose.ui.graphics.Color

enum class ToolType(
    val displayName: String,
    val description: String,
    val baseDamage: Int,
    val bulletSpeed: Float,
    val fireIntervalSec: Float,
    val baseColor: Color,
    val accentColor: Color,
    val costCoins: Int
) {
    SCREWDRIVER(
        displayName = "Blaster Cercafase",
        description = "Raffica laser rapida e precisa con lucine cercafase ad alta frequenza!",
        baseDamage = 20,
        bulletSpeed = 26f,
        fireIntervalSec = 0.12f,
        baseColor = Color(0xFFFFB703),
        accentColor = Color(0xFFFB8500),
        costCoins = 0
    ),
    PLIERS(
        displayName = "Laser Pinza 1000V",
        description = "Sparo a ventaglio trifase con lame perforanti che trapassano gli sciami!",
        baseDamage = 45,
        bulletSpeed = 22f,
        fireIntervalSec = 0.22f,
        baseColor = Color(0xFF0077B6),
        accentColor = Color(0xFFE63946),
        costCoins = 150
    ),
    TAPE(
        displayName = "Cannone Nastro Rimbalzante",
        description = "Rotoli vulcanizzati rimbalzanti che creano una barriera impenetrabile di colpi!",
        baseDamage = 35,
        bulletSpeed = 19f,
        fireIntervalSec = 0.18f,
        baseColor = Color(0xFF1D3557),
        accentColor = Color(0xFF457B9D),
        costCoins = 300
    ),
    MULTIMETER(
        displayName = "Raggio Plasma Multimetro 380V",
        description = "Fascio continuo ad arco voltaico continuo che incenerisce i condotti nemici!",
        baseDamage = 70,
        bulletSpeed = 28f,
        fireIntervalSec = 0.25f,
        baseColor = Color(0xFFFF0054),
        accentColor = Color(0xFFFFEE00),
        costCoins = 600
    ),
    BREAKER_BOMB(
        displayName = "Cannone Magnetotermico",
        description = "Scariche pesanti ad onda d'urto che polverizzano le formazioni di apprendisti!",
        baseDamage = 130,
        bulletSpeed = 16f,
        fireIntervalSec = 0.40f,
        baseColor = Color(0xFF38B000),
        accentColor = Color(0xFFCCFF33),
        costCoins = 1000
    )
}

enum class PowerUpType(
    val title: String,
    val color: Color,
    val symbol: String,
    val durationSec: Float = 10f
) {
    TRIFASE_SPREAD("TRIFASE 380V (Sparo Triplo)", Color(0xFFFFD600), "⚡3"),
    PLASMA_BEAM("SUPER RAGGIO VOLTAICO", Color(0xFF00F0FF), "⚡⚡"),
    SHIELD("SCUDO ISOLANTE 1000V", Color(0xFF38BDF8), "🛡️"),
    SPEED_FIRE("OVERCLOCK RAFFICA", Color(0xFFFF0055), "⏩"),
    MAGNET_COINS("MAGNETE VOLT COINS", Color(0xFF4ADE80), "🧲"),
    BOMB_RECHARGE("BOMBA MAGNETO +1", Color(0xFFE11D48), "💣")
}

enum class ApprenticeType(
    val title: String,
    val baseHp: Int,
    val points: Int,
    val speed: Float,
    val helmetColor: Color,
    val sizeRadius: Float,
    val bulletIntervalSec: Float = 1.8f,
    val isBoss: Boolean = false
) {
    SCOUT(
        title = "Apprendista Volante su Bobina",
        baseHp = 25,
        points = 100,
        speed = 4.2f,
        helmetColor = Color(0xFFFFD166),
        sizeRadius = 28f,
        bulletIntervalSec = 2.2f
    ),
    DIVER(
        title = "Apprendista Kamikaze a Cesoia",
        baseHp = 30,
        points = 180,
        speed = 5.5f,
        helmetColor = Color(0xFFEF476F),
        sizeRadius = 26f,
        bulletIntervalSec = 1.5f
    ),
    HEAVY_BATTERY(
        title = "Apprendista Blindato Quadro 380V",
        baseHp = 90,
        points = 350,
        speed = 2.0f,
        helmetColor = Color(0xFF118AB2),
        sizeRadius = 38f,
        bulletIntervalSec = 1.2f
    ),
    SPARK_SNIPER(
        title = "Apprendista Sonda Laser",
        baseHp = 50,
        points = 280,
        speed = 3.0f,
        helmetColor = Color(0xFF8338EC),
        sizeRadius = 30f,
        bulletIntervalSec = 1.0f
    ),
    BOSS_FOREMAN(
        title = "MEGA CAPO CANTIERE 380V (BOSS)",
        baseHp = 650,
        points = 3000,
        speed = 2.2f,
        helmetColor = Color(0xFFFF0055),
        sizeRadius = 65f,
        bulletIntervalSec = 0.5f,
        isBoss = true
    )
}

enum class FlightPattern {
    STRAIGHT_DOWN,
    V_FORMATION,
    SINE_WAVE_LEFT,
    SINE_WAVE_RIGHT,
    ZIGZAG,
    DIVE_BOMBER,
    HOVER_SWEEP,
    BOSS_ORBIT
}

enum class EnemyState {
    FLYING_IN,
    ATTACKING,
    HIT_GNUGNU,
    DYING,
    DEFEATED
}

data class Enemy(
    val id: Long,
    val type: ApprenticeType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var hp: Int,
    val maxHp: Int,
    val pattern: FlightPattern = FlightPattern.STRAIGHT_DOWN,
    val initialX: Float = x,
    var state: EnemyState = EnemyState.FLYING_IN,
    var stateTime: Float = 0f,
    var totalTime: Float = 0f,
    var shootCooldown: Float = 1.5f,
    var gnugnuPhrase: String = "GNUGNU!",
    var scale: Float = 1.0f,
    var wobble: Float = 0f,
    var shootAngleOffset: Float = 0f
)

data class Projectile(
    val id: Long,
    val toolType: ToolType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 20f,
    var damage: Int,
    var bouncesLeft: Int = 0,
    var pierceCount: Int = 1,
    var lifeTime: Float = 0f,
    val isSuperBeam: Boolean = false,
    val isTripleShot: Boolean = false,
    val trailColor: Color = toolType.accentColor
)

enum class EnemyBulletType {
    SPARK_BOLT,
    ELECTRIC_ORB,
    SPREAD_ARC,
    HIGH_VOLTAGE_LASER,
    BOUNCING_FUSE
}

data class EnemySpark(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val type: EnemyBulletType = EnemyBulletType.SPARK_BOLT,
    var radius: Float = 6f,
    var life: Float = 1.0f,
    val damage: Float = 12f
)

data class ShooterPowerUp(
    val id: Long,
    var x: Float,
    var y: Float,
    var vy: Float = 2.8f,
    val type: PowerUpType,
    var rotation: Float = 0f,
    var lifeTime: Float = 12f
)

data class FloatingCoin(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val value: Int = 5,
    var lifeTime: Float = 10f
)

data class Shockwave(
    val id: Long,
    var x: Float,
    var y: Float,
    var radius: Float = 10f,
    val maxRadius: Float = 400f,
    var alpha: Float = 1f,
    val color: Color = Color(0xFF00F0FF)
)

data class GameParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var alpha: Float = 1f,
    var size: Float,
    var life: Float,
    val maxLife: Float,
    val isLightning: Boolean = false
)

data class GnuGnuPopup(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    var alpha: Float = 1f,
    var scale: Float = 0.8f,
    var vy: Float = -2.8f,
    var life: Float = 1.2f,
    val color: Color = Color(0xFFFFEE00)
)

enum class GameMode {
    CAMPAIGN,   // Progressive shooter sectors with Bosses
    ENDLESS,    // Endless bullet-hell apprentice swarm
    TIME_ATTACK // 60s frenzy score attack
}

data class LevelConfig(
    val levelNumber: Int,
    val name: String,
    val subtitle: String,
    val targetApprentices: Int,
    val spawnRateMultiplier: Float,
    val allowsBoss: Boolean = false
)
