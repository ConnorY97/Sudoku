package my.sudoku.game.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import my.sudoku.game.R
import my.sudoku.game.viewmodel.GameViewModel
import java.util.Locale

class UIManager(private val context: Context) {
    fun getSudokuGrid(): GridLayout? {
        if (context is Activity)
        {
            return context.findViewById<GridLayout>(R.id.sudokuGrid)
        }
        else
        {
            Log.e("getSudokuGrid", "Failed to retrieve sudokuGrid")
            return null
        }
    }

    fun initializeSudokuGrid(
        viewModel: GameViewModel
    ) {
        val sudokuGrid = getSudokuGrid()
        val gameState = viewModel.getGameState()
        for (row in 0 until 9) {
            for (col in 0 until 9) {
                val isEditable = Pair(row, col) in (gameState.value?.editableCells ?: return)
                val cell = EditText(context).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 100
                        height = 100
                        columnSpec = GridLayout.spec(col)
                        rowSpec = GridLayout.spec(row)
                        setMargins(
                            if (col % 3 == 0 && col != 0) 4 else 1,
                            if (row % 3 == 0 && row != 0) 4 else 1,
                            1,
                            1
                        )
                    }
                    gravity = Gravity.CENTER
                    textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                    setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
                    setTextColor(Color.BLACK)
                    setPadding(10, 10, 10, 10)

                    if (isEditable) {
                        setOnClickListener {
                            selectCell(row, col, getSudokuGrid()!!, context, viewModel)
                        }
                    }

                    // Make sure the cells cannot be interacted with a keyboard
                    isFocusableInTouchMode = false
                    isFocusable = false
                    inputType = InputType.TYPE_NULL
                }
                val number = gameState.value?.board?.get(row)?.get(col)
                if (number != 0) {
                    cell.setText(String.format(Locale.getDefault(), "%d", number))
                    if (!isEditable) {
                        cell.setTypeface(null, Typeface.BOLD)
                    }
                } else {
                    cell.setText("")
                }

                sudokuGrid!!.addView(cell)
            }
        }
    }

    private fun selectCell(
        row: Int,
        col: Int,
        sudokuGrid: GridLayout,
        context: Context,
        viewModel: GameViewModel
    ) {
        clearCellHighlights(getSudokuGrid()!!, context)
        val cellIndex = row * 9 + col
        val selectedView = sudokuGrid.getChildAt(cellIndex)
        selectedView.setBackgroundColor(Color.YELLOW)
        viewModel.setSelectedCell(Pair(row, col))
    }

    private fun clearCellHighlights(
        sudokuGrid: GridLayout,
        context: Context
    ) {
        for (i in 0 until sudokuGrid.childCount) {
            sudokuGrid.getChildAt(i).setBackgroundColor(ContextCompat.getColor(context, R.color.cell_background))
        }
    }
}