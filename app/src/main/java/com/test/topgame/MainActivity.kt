package com.test.topgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.test.topgame.data.AppDatabase
import com.test.topgame.data.ScoreRepository
import com.test.topgame.ui.game.GameScreen
import com.test.topgame.ui.game.GameViewModelFactory
import com.test.topgame.ui.menu.MenuScreen
import com.test.topgame.ui.theme.TopGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TopGameTheme {
                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val repository = remember { ScoreRepository(database.scoreDao()) }
                val factory = remember { GameViewModelFactory(repository) }
                
                val topScores = repository.topScores.collectAsState(initial = emptyList())

                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "menu",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("menu") {
                            MenuScreen(
                                scores = topScores.value,
                                onStartGame = { navController.navigate("game") }
                            )
                        }
                        composable("game") {
                            GameScreen(
                                factory = factory,
                                onGameOver = { score ->
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
