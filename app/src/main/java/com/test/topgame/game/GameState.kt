package com.test.topgame.game

import com.test.topgame.game.models.Obstacle
import com.test.topgame.game.models.Player
import com.test.topgame.game.models.PowerUp

data class GameState(
    val player: Player = Player(),
    val obstacles: List<Obstacle> = emptyList(),
    val powerUps: List<PowerUp> = emptyList(),
    val status: GameStatus = GameStatus.IDLE,
    val score: Int = 0,
    val gameSpeed: Float = GameConstants.BASE_SPEED,
    val distanceTraveled: Float = 0f,
    val animationFrame: Float = 0f
)
