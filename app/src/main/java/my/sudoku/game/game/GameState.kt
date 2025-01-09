data class GameState(
    var board: Array<IntArray> = Array(9) { IntArray(9) },
    var editableCells: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf(),
    var elapsedTime: Long = 0L,
    var finished: Boolean = false
) {
    // Deep copy constructor
    fun copy(): GameState {
        return GameState(
            board = Array(9) { row -> board[row].clone() },
            editableCells = editableCells.toMutableMap(),
            elapsedTime = elapsedTime,
            finished = finished
        )
    }

    // Custom equals implementation for deep comparison
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (!board.contentDeepEquals(other.board)) return false
        if (editableCells != other.editableCells) return false
        if (elapsedTime != other.elapsedTime) return false
        if (finished != other.finished) return false

        return true
    }

    // Custom hashCode implementation
    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + editableCells.hashCode()
        result = 31 * result + elapsedTime.hashCode()
        result = 31 * result + finished.hashCode()
        return result
    }

    // Utility functions for board manipulation
    fun getCellValue(row: Int, col: Int): Int {
        return board[row][col]
    }

    fun setCellValue(row: Int, col: Int, value: Int) {
        if (isEditable(row, col)) {
            board[row][col] = value
        }
    }

    fun isEditable(row: Int, col: Int): Boolean {
        return editableCells[Pair(row, col)] ?: false
    }

    fun setEditable(row: Int, col: Int, editable: Boolean) {
        editableCells[Pair(row, col)] = editable
    }

    // Function to check if the board is complete
    fun isBoardComplete(): Boolean {
        // Check if all cells are filled
        for (row in board.indices) {
            for (col in board[row].indices) {
                if (board[row][col] == 0) return false
            }
        }
        return true
    }

    // Reset the game state
    fun reset() {
        board = Array(9) { IntArray(9) }
        editableCells.clear()
        elapsedTime = 0L
        finished = false
    }
}