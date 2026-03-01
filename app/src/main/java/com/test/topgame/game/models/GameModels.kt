package com.test.topgame.game.models

import androidx.compose.ui.geometry.Rect
import com.test.topgame.game.GameConstants

enum class PlayerState {
    RUNNING, JUMPING, SLIDING, DEAD
}

data class Player(
    val x: Float = GameConstants.PLAYER_START_X,
    val y: Float = 0f,
    val width: Float = 80f,
    val height: Float = 160f,
    val velocityY: Float = 0f,
    val state: PlayerState = PlayerState.RUNNING,
    val isInvulnerable: Boolean = false,
    val powerUpActive: Boolean = false,
    val powerUpTimeLeft: Float = 0f
) {
    fun getBounds(): Rect {
        val currentHeight = if (state == PlayerState.SLIDING) height / 2 else height
        val currentY = if (state == PlayerState.SLIDING) y + (height / 2) else y
        // Slightly smaller hitbox for better feel
        return Rect(x + 10f, currentY + 10f, x + width - 10f, currentY + currentHeight - 5f)
    }
}

enum class ObstacleType {
    GROUND_SPIKE,   // Small box to jump over
    VAULT_BAR,      // Medium box to vault
    PLATFORM,       // Large box to run on top of
    FLYING_DRONE    // High obstacle to slide under
}

data class Obstacle(
    val id: Long = System.nanoTime(),
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: ObstacleType,
    val passed: Boolean = false
) {
    fun getBounds(): Rect = Rect(x, y, x + width, y + height)
}

enum class PowerUpType {
    SHIELD, SPEED_BOOST, SCORE_MULTIPLIER
}

data class PowerUp(
    val id: Long = System.nanoTime(),
    val x: Float,
    val y: Float,
    val type: PowerUpType,
    val width: Float = 60f,
    val height: Float = 60f
) {
    fun getBounds(): Rect = Rect(x, y, x + width, y + height)
}
