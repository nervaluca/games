package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val gnugnuCount: Int,
    val gameMode: String,
    val maxCombo: Int,
    val levelReached: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "player_progress")
data class PlayerProgress(
    @PrimaryKey
    val id: Int = 1,
    val totalVoltCoins: Int = 100,
    val totalGnugnus: Int = 0,
    val unlockedToolsMask: Int = 7, // Bitmask for unlocked tools
    val screwdriverLevel: Int = 1,
    val pliersLevel: Int = 1,
    val tapeLevel: Int = 1,
    val multimeterLevel: Int = 1,
    val breakerLevel: Int = 1,
    val selectedSkin: String = "CLASSIC"
)
