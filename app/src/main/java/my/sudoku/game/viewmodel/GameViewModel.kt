package my.sudoku.game.viewmodel

import GameState
import androidx.lifecycle.MutableLiveData

class GameViewModel() {
    private val gameState: MutableLiveData<GameState> = MutableLiveData(GameState())
    fun getGameState(
    ): MutableLiveData<GameState> {
        return gameState
    }

    private var finished = false
    fun getFinished(
    ): Boolean {
        return finished
    }
    fun setFinished(
        isFinished: Boolean
    ) {
        finished = isFinished
    }

    private var selectedCell: Pair<Int, Int>? = null
    fun getSelectedCell(
    ): Pair<Int,Int>? {
        return selectedCell
    }
    fun setSelectedCell(
        newCell: Pair<Int, Int>
    ) {
        selectedCell = newCell
    }
}