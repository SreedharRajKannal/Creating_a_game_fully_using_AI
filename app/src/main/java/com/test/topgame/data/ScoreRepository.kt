package com.test.topgame.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val scoreDao: ScoreDao) {
    val topScores: Flow<List<ScoreEntity>> = scoreDao.getTopScores()

    suspend fun saveScore(score: Int) {
        scoreDao.insertScore(ScoreEntity(score = score))
    }
}
