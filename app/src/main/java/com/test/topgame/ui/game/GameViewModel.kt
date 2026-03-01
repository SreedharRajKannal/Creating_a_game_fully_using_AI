package com.test.topgame.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.test.topgame.data.ScoreRepository
import com.test.topgame.game.GameEngine
import com.test.topgame.game.GameState
import com.test.topgame.game.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(private val repository: ScoreRepository) : ViewModel() {

    private val engine = GameEngine()
    
    private val _gameState = MutableStateFlow(engine.gameState)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    fun update(deltaTimeNanos: Long) {
        engine.update(deltaTimeNanos)
        _gameState.value = engine.gameState
        
        if (engine.gameState.status == GameStatus.GAME_OVER && _gameState.value.status != GameStatus.GAME_OVER) {
             // Game just ended
        }
    }

    fun jump() {
        engine.jump()
    }

    fun slide() {
        engine.slide()
    }
    
    fun stopSlide() {
        engine.stopSlide()
    }

    fun startGame() {
        engine.startGame()
        _gameState.value = engine.gameState
    }

    fun resetGame() {
        engine.reset()
        _gameState.value = engine.gameState
    }

    fun saveScore(score: Int) {
        viewModelScope.launch {
            repository.saveScore(score)
        }
    }
}

class GameViewModelFactory(private val repository: ScoreRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
