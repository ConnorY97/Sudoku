package my.sudoku.game.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import GameState

class GameViewModel : ViewModel() {
    private val gameState: MutableLiveData<GameState> = MutableLiveData(GameState())

    fun getGameState(): MutableLiveData<GameState> {
        return gameState
    }

    private var finished: Boolean = false
    fun getFinished(): Boolean {
        return finished
    }

    fun setFinished(isFinished: Boolean) {
        finished = isFinished
    }

    private var selectedCell: Pair<Int, Int>? = null
    fun getSelectedCell(): Pair<Int, Int>? {
        return selectedCell
    }

    fun setSelectedCell(newCell: Pair<Int, Int>) {
        selectedCell = newCell
    }
}
