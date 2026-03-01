package com.test.topgame.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.test.topgame.game.GameConstants
import com.test.topgame.game.GameStatus
import com.test.topgame.game.models.ObstacleType
import com.test.topgame.game.models.PlayerState
import com.test.topgame.game.models.PowerUpType
import com.test.topgame.ui.theme.DarkBackground
import com.test.topgame.ui.theme.NeonCyan
import com.test.topgame.ui.theme.NeonGreen
import com.test.topgame.ui.theme.NeonPink
import com.test.topgame.ui.theme.NeonPurple
import kotlin.math.sin

@Composable
fun GameScreen(
    factory: androidx.lifecycle.ViewModelProvider.Factory,
    viewModel: GameViewModel = viewModel(factory = factory),
    onGameOver: (Int) -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }
    
    val lastFrameTime = remember { mutableStateOf(0L) }
    
    LaunchedEffect(gameState.status) {
        if (gameState.status == GameStatus.RUNNING) {
            lastFrameTime.value = 0L
            while (gameState.status == GameStatus.RUNNING) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTime.value != 0L) {
                        val dt = frameTimeNanos - lastFrameTime.value
                        viewModel.update(dt)
                    }
                    lastFrameTime.value = frameTimeNanos
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { viewModel.jump() })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        if (dragAmount.y > 20) viewModel.slide()
                        else if (dragAmount.y < -20) viewModel.jump()
                        change.consume()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleFactor = size.width / 1080f
            
            scale(scale = scaleFactor, pivot = Offset.Zero) {
                // Background Glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBackground, Color.Black)
                    ),
                    size = size / scaleFactor
                )

                // Power-up indicators
                gameState.powerUps.forEach { pu ->
                    val color = when(pu.type) {
                        PowerUpType.SHIELD -> NeonCyan
                        PowerUpType.SPEED_BOOST -> NeonGreen
                        PowerUpType.SCORE_MULTIPLIER -> NeonPurple
                    }
                    drawCircle(color, 30f, Offset(pu.x + 30f, pu.y + 30f))
                    drawCircle(color.copy(alpha = 0.3f), 50f, Offset(pu.x + 30f, pu.y + 30f))
                }

                // Obstacles
                gameState.obstacles.forEach { obs ->
                    val color = when(obs.type) {
                        ObstacleType.PLATFORM -> Color.Gray
                        ObstacleType.VAULT_BAR -> NeonPurple
                        ObstacleType.FLYING_DRONE -> NeonGreen
                        else -> Color.Red
                    }
                    
                    if (obs.type == ObstacleType.PLATFORM) {
                         drawRoundRect(
                            color = color,
                            topLeft = Offset(obs.x, obs.y),
                            size = Size(obs.width, obs.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        // Neon top edge for platform (safe)
                        drawLine(NeonCyan, Offset(obs.x, obs.y), Offset(obs.x + obs.width, obs.y), strokeWidth = 8f)
                        
                        // Thorns on the FRONT side (lethal)
                        val spikeCount = 5
                        val spikeHeight = 20f
                        for (i in 0 until spikeCount) {
                            val sy = obs.y + (obs.height / spikeCount) * i
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(obs.x, sy)
                                lineTo(obs.x - spikeHeight, sy + (obs.height / spikeCount) / 2)
                                lineTo(obs.x, sy + (obs.height / spikeCount))
                                close()
                            }
                            drawPath(path, Color.Red)
                        }
                    } else {
                        drawRect(color, Offset(obs.x, obs.y), Size(obs.width, obs.height))
                        // Thorns on TOP and FRONT for boxes
                        val spikeCount = 3
                        val spikeSize = 15f
                        // Top spikes
                        for (i in 0 until spikeCount) {
                            val sx = obs.x + (obs.width / spikeCount) * i
                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(sx, obs.y)
                                lineTo(sx + (obs.width / spikeCount) / 2, obs.y - spikeSize)
                                lineTo(sx + (obs.width / spikeCount), obs.y)
                                close()
                            }
                            drawPath(path, Color.Red)
                        }
                    }
                }

                // Player (Stick-man)
                drawStickMan(gameState)

                // Ground
                drawLine(
                    color = NeonCyan,
                    start = Offset(0f, GameConstants.GROUND_Y),
                    end = Offset(2000f, GameConstants.GROUND_Y),
                    strokeWidth = 4f
                )
            }
        }
        
        // HUD
        Column(modifier = Modifier.padding(16.dp)) {
            Text("SCORE: ${gameState.score}", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            if (gameState.player.powerUpActive) {
                Text("POWER UP: ${(gameState.player.powerUpTimeLeft).toInt()}s", color = NeonGreen)
            }
        }

        if (gameState.status == GameStatus.GAME_OVER) {
            GameOverOverlay(
                score = gameState.score,
                onRestart = { viewModel.resetGame(); viewModel.startGame() },
                onMenu = { onGameOver(gameState.score) }
            )
        }
    }
}

fun DrawScope.drawStickMan(gameState: com.test.topgame.game.GameState) {
    val player = gameState.player
    val frame = gameState.animationFrame
    val color = if (player.powerUpActive) NeonGreen else NeonPink
    val stroke = 10f
    
    // Calculate dynamic bobbing and leaning
    val bobbing = if (player.state == PlayerState.RUNNING) kotlin.math.abs(sin(frame * 0.2f)) * 15f else 0f
    val lean = if (player.state == PlayerState.RUNNING) 10f else if (player.state == PlayerState.JUMPING) -5f else 0f
    
    val centerX = player.x + player.width / 2
    val headY = player.y + 25f - bobbing
    
    // Rotate the whole stickman slightly for "lean"
    rotate(degrees = lean, pivot = Offset(centerX, player.y + player.height)) {
        // Head
        drawCircle(
            color = color,
            radius = 22f,
            center = Offset(centerX, headY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke)
        )
        
        // Body (Neck to Pelvis)
        val neck = Offset(centerX, headY + 22f)
        val pelvis = Offset(centerX, player.y + 110f - bobbing)
        drawLine(color, neck, pelvis, strokeWidth = stroke, cap = StrokeCap.Round)
        
        if (player.state == PlayerState.SLIDING) {
            // Sliding Pose: Legs tucked, body low
            drawLine(color, pelvis, Offset(centerX + 50f, player.y + player.height - 10f), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, neck, Offset(centerX + 40f, neck.y + 30f), strokeWidth = stroke, cap = StrokeCap.Round)
        } else {
            // Dynamic Legs & Arms
            val legSwing = sin(frame * 0.2f) * 45f
            val armSwing = -sin(frame * 0.2f) * 40f
            
            // Legs
            val leftFoot = Offset(centerX + legSwing, player.y + player.height)
            val rightFoot = Offset(centerX - legSwing, player.y + player.height)
            
            drawLine(color, pelvis, leftFoot, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, pelvis, rightFoot, strokeWidth = stroke, cap = StrokeCap.Round)
            
            // Arms
            val shoulderY = neck.y + 15f
            val leftHand = Offset(centerX + armSwing, shoulderY + 50f)
            val rightHand = Offset(centerX - armSwing, shoulderY + 50f)
            
            drawLine(color, Offset(centerX, shoulderY), leftHand, strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(centerX, shoulderY), rightHand, strokeWidth = stroke, cap = StrokeCap.Round)
        }
    }
    
    // Neon Glow Pulse
    val glowAlpha = 0.2f + kotlin.math.abs(sin(frame * 0.1f)) * 0.2f
    drawCircle(color.copy(alpha = glowAlpha), radius = 120f, center = Offset(centerX, player.y + player.height / 2))
}

@Composable
fun GameOverOverlay(score: Int, onRestart: () -> Unit, onMenu: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("GAME OVER", style = MaterialTheme.typography.displayMedium, color = Color.Red)
            Text("SCORE: $score", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(16.dp))
            Button(onClick = onRestart, modifier = Modifier.padding(8.dp)) { Text("TRY AGAIN") }
            Button(onClick = onMenu, modifier = Modifier.padding(8.dp)) { Text("MAIN MENU") }
        }
    }
}
