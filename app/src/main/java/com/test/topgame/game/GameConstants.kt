package com.test.topgame.game

object GameConstants {
    const val GRAVITY = 1800f
    const val JUMP_FORCE = -950f
    const val GROUND_Y = 1200f // Placeholder, will be dynamic based on screen
    const val PLAYER_START_X = 200f
    const val OBSTACLE_SPAWN_INTERVAL = 1500L // ms
    const val BASE_SPEED = 600f
    const val MAX_SPEED = 1200f
    const val SPEED_INCREMENT = 10f // Per second
}

enum class GameStatus {
    IDLE, RUNNING, PAUSED, GAME_OVER
}
