package com.test.topgame.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.test.topgame.ui.theme.DarkBackground
import com.test.topgame.ui.theme.NeonCyan
import com.test.topgame.ui.theme.NeonPurple

@Composable
fun MenuScreen(
    scores: List<com.test.topgame.data.ScoreEntity> = emptyList(),
    onStartGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color.Black)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEON SHADOW",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    fontSize = 48.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = NeonPurple,
                        blurRadius = 20f
                    )
                )
            )
            Text(
                text = "ECHO RUNNER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = NeonPurple,
                    letterSpacing = 4.sp
                )
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "START RUN",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (scores.isNotEmpty()) {
                Text(
                    text = "TOP SCORES",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan
                )
                scores.forEach { score ->
                    Text(
                        text = "${score.score}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}
