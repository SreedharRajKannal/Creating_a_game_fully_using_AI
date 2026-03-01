package com.test.topgame.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM scores ORDER BY score DESC LIMIT 5")
    fun getTopScores(): Flow<List<ScoreEntity>>

    @Insert
    suspend fun insertScore(score: ScoreEntity)
}
