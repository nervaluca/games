package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC LIMIT 10")
    fun getTopScores(): Flow<List<GameScore>>

    @Query("SELECT MAX(score) FROM game_scores")
    fun getHighScore(): Flow<Int?>

    @Insert
    suspend fun insertScore(score: GameScore): Long

    @Query("SELECT * FROM player_progress WHERE id = 1")
    fun getPlayerProgress(): Flow<PlayerProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlayerProgress(progress: PlayerProgress)

    @Update
    suspend fun updatePlayerProgress(progress: PlayerProgress)
}

@Database(entities = [GameScore::class, PlayerProgress::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
