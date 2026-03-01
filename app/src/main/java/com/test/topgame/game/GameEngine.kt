package com.test.topgame.game

import com.test.topgame.game.models.Obstacle
import com.test.topgame.game.models.ObstacleType
import com.test.topgame.game.models.Player
import com.test.topgame.game.models.PlayerState
import com.test.topgame.game.models.PowerUp
import com.test.topgame.game.models.PowerUpType
import kotlin.random.Random

class GameEngine {

    private var _gameState = GameState()
    val gameState: GameState get() = _gameState

    private var timeSinceLastSpawn = 0L
    private var timeSinceLastPowerUp = 0L

    fun update(deltaTimeNanos: Long) {
        if (_gameState.status != GameStatus.RUNNING) return

        val dt = deltaTimeNanos / 1_000_000_000f

        updateAnimation(dt)
        updatePlayer(dt)
        updateObstacles(dt)
        updatePowerUps(dt)
        checkCollisions()
        updateScore(dt)

        val newSpeed = (_gameState.gameSpeed + (GameConstants.SPEED_INCREMENT * dt)).coerceAtMost(GameConstants.MAX_SPEED)
        _gameState = _gameState.copy(gameSpeed = newSpeed)
    }

    private fun updateAnimation(dt: Float) {
        var nextFrame = _gameState.animationFrame + dt * 10f
        if (nextFrame > 100f) nextFrame = 0f
        _gameState = _gameState.copy(animationFrame = nextFrame)
    }

    private fun updatePlayer(dt: Float) {
        val player = _gameState.player
        
        // Handle Power-Up Timer
        var newPowerUpTime = player.powerUpTimeLeft - dt
        var powerUpActive = player.powerUpActive
        if (newPowerUpTime <= 0) {
            newPowerUpTime = 0f
            powerUpActive = false
        }

        var newVelocityY = player.velocityY + GameConstants.GRAVITY * dt
        var newY = player.y + newVelocityY * dt
        var finalState = player.state

        // Platform and Ground Collision
        var onSurface = false
        val playerBottom = newY + player.height
        
        // Check if landing on top of a PLATFORM
        _gameState.obstacles.filter { it.type == ObstacleType.PLATFORM }.forEach { platform ->
            if (player.x + player.width > platform.x && player.x < platform.x + platform.width) {
                // If player was above platform and is now moving into/below its top surface
                if (player.y + player.height <= platform.y + 20f && playerBottom >= platform.y) {
                    newY = platform.y - player.height
                    newVelocityY = 0f
                    onSurface = true
                }
            }
        }

        // Ground collision
        if (playerBottom >= GameConstants.GROUND_Y) {
            newY = GameConstants.GROUND_Y - player.height
            newVelocityY = 0f
            onSurface = true
        }

        if (onSurface && finalState == PlayerState.JUMPING) {
            finalState = PlayerState.RUNNING
        }

        _gameState = _gameState.copy(
            player = player.copy(
                y = newY,
                velocityY = newVelocityY,
                state = finalState,
                powerUpActive = powerUpActive,
                powerUpTimeLeft = newPowerUpTime
            )
        )
    }

    private fun updateObstacles(dt: Float) {
        val speed = _gameState.gameSpeed
        val obstacles = _gameState.obstacles.map {
            it.copy(x = it.x - speed * dt)
        }.filter { it.x + it.width > -500f }

        _gameState = _gameState.copy(obstacles = obstacles)

        timeSinceLastSpawn += (dt * 1000).toLong()
        val spawnThreshold = (GameConstants.OBSTACLE_SPAWN_INTERVAL * (GameConstants.BASE_SPEED / speed)).toLong()
        
        if (timeSinceLastSpawn > spawnThreshold) {
            spawnObstacle()
            timeSinceLastSpawn = 0
        }
    }

    private fun updatePowerUps(dt: Float) {
        val speed = _gameState.gameSpeed
        val powerUps = _gameState.powerUps.map {
            it.copy(x = it.x - speed * dt)
        }.filter { it.x + it.width > 0 }

        _gameState = _gameState.copy(powerUps = powerUps)

        timeSinceLastPowerUp += (dt * 1000).toLong()
        if (timeSinceLastPowerUp > 5000L) { // Spawn every 5 seconds roughly
            if (Random.nextFloat() > 0.7f) spawnPowerUp()
            timeSinceLastPowerUp = 0
        }
    }

    private fun spawnObstacle() {
        val type = ObstacleType.values().random()
        val width: Float
        val height: Float
        val y: Float

        when (type) {
            ObstacleType.GROUND_SPIKE -> {
                width = 60f; height = 60f; y = GameConstants.GROUND_Y - height
            }
            ObstacleType.VAULT_BAR -> {
                width = 40f; height = 120f; y = GameConstants.GROUND_Y - height
            }
            ObstacleType.PLATFORM -> {
                width = 400f; height = 200f; y = GameConstants.GROUND_Y - height
            }
            ObstacleType.FLYING_DRONE -> {
                width = 80f; height = 60f; y = GameConstants.GROUND_Y - 240f
            }
        }

        val obstacle = Obstacle(x = 1500f, y = y, width = width, height = height, type = type)
        _gameState = _gameState.copy(obstacles = _gameState.obstacles + obstacle)
    }

    private fun spawnPowerUp() {
        val type = PowerUpType.values().random()
        val y = GameConstants.GROUND_Y - 300f
        val powerUp = PowerUp(x = 1500f, y = y, type = type)
        _gameState = _gameState.copy(powerUps = _gameState.powerUps + powerUp)
    }

    private fun checkCollisions() {
        val player = _gameState.player
        val playerBounds = player.getBounds()
        
        // Obstacles
        for (obstacle in _gameState.obstacles) {
            if (playerBounds.overlaps(obstacle.getBounds())) {
                // If it's a platform, we only die if we hit the SIDE of it
                if (obstacle.type == ObstacleType.PLATFORM) {
                    if (player.x + player.width > obstacle.x + 20f && player.y + player.height > obstacle.y + 20f) {
                        if (!player.powerUpActive) gameOver()
                    }
                } else {
                    if (!player.powerUpActive) gameOver()
                }
            }
        }

        // Power-ups
        val powerUps = _gameState.powerUps.toMutableList()
        val iterator = powerUps.iterator()
        while (iterator.hasNext()) {
            val pu = iterator.next()
            if (playerBounds.overlaps(pu.getBounds())) {
                applyPowerUp(pu.type)
                iterator.remove()
            }
        }
        _gameState = _gameState.copy(powerUps = powerUps)
    }

    private fun applyPowerUp(type: PowerUpType) {
        _gameState = _gameState.copy(
            player = _gameState.player.copy(
                powerUpActive = true,
                powerUpTimeLeft = 5f // 5 seconds
            )
        )
    }

    private fun updateScore(dt: Float) {
        val distance = _gameState.distanceTraveled + (_gameState.gameSpeed * dt)
        val score = (distance / 100).toInt()
        _gameState = _gameState.copy(distanceTraveled = distance, score = score)
    }

    fun jump() {
        if (_gameState.status != GameStatus.RUNNING) return
        val player = _gameState.player
        // Allow double jump or just standard? Let's stay standard for now but check if on ground or platform
        if (player.velocityY == 0f) {
             _gameState = _gameState.copy(
                player = player.copy(state = PlayerState.JUMPING, velocityY = GameConstants.JUMP_FORCE)
            )
        }
    }

    fun slide() {
         if (_gameState.status != GameStatus.RUNNING) return
         if (_gameState.player.state == PlayerState.RUNNING) {
             _gameState = _gameState.copy(player = _gameState.player.copy(state = PlayerState.SLIDING))
         }
    }

    fun stopSlide() {
        if (_gameState.status != GameStatus.RUNNING) return
        if (_gameState.player.state == PlayerState.SLIDING) {
            _gameState = _gameState.copy(player = _gameState.player.copy(state = PlayerState.RUNNING))
        }
    }

    fun startGame() {
        _gameState = GameState(status = GameStatus.RUNNING)
        timeSinceLastSpawn = 0
        timeSinceLastPowerUp = 0
    }

    fun gameOver() {
        _gameState = _gameState.copy(status = GameStatus.GAME_OVER)
    }
    
    fun reset() {
        _gameState = GameState(status = GameStatus.IDLE)
    }
}
