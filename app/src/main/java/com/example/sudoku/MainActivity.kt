package com.example.sudoku

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.GridLayout
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sudokuGrid: GridLayout = findViewById<GridLayout>(R.id.sudokuGrid)
        val gridSize = 9

// Define the initial Sudoku board with some numbers
        val sudokuBoard = arrayOf(
            intArrayOf(5, 3, 0, 0, 7, 0, 0, 0, 0),
            intArrayOf(6, 0, 0, 1, 9, 5, 0, 0, 0),
            intArrayOf(0, 9, 8, 0, 0, 0, 0, 6, 0),
            intArrayOf(8, 0, 0, 0, 6, 0, 0, 0, 3),
            intArrayOf(4, 0, 0, 8, 0, 3, 0, 0, 1),
            intArrayOf(7, 0, 0, 0, 2, 0, 0, 0, 6),
            intArrayOf(0, 6, 0, 0, 0, 0, 2, 8, 0),
            intArrayOf(0, 0, 0, 4, 1, 9, 0, 0, 5),
            intArrayOf(0, 0, 0, 0, 8, 0, 0, 7, 9)
        )

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = EditText(this)
                cell.layoutParams = GridLayout.LayoutParams().apply {
                    width = 120
                    height = 120
                    columnSpec = GridLayout.spec(col)
                    rowSpec = GridLayout.spec(row)

                    // Apply thicker margins for 3rd and 6th rows and columns
                    setMargins(
                        if (col % 3 == 0 && col != 0) 4 else 1,  // Left margin
                        if (row % 3 == 0 && row != 0) 4 else 1,  // Top margin
                        1,                                       // Right margin
                        1                                        // Bottom margin
                    )
                }
                cell.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                cell.setBackgroundResource(android.R.color.white)
                cell.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                cell.maxLines = 1  // Ensure only 1 line of input
                cell.gravity = Gravity.CENTER
                // Limit input to a single digit by setting maxLength
                val filter = InputFilter.LengthFilter(1)
                cell.filters = arrayOf(filter)

                // Pre-fill the Sudoku board cells with numbers
                val number = sudokuBoard[row][col]
                if (number != 0) {
                    cell.setText(number.toString()) // Set predefined number
                    cell.isFocusable = false // Make pre-filled cells non-editable
                    cell.isClickable = false // Prevent interaction with pre-filled cells
                } else {
                    cell.isFocusableInTouchMode = true // Allow user input on empty cells
                }

                sudokuGrid.addView(cell)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        //text = "Hello $name!",
        text = "Good evening Beth, I love you",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    val people = listOf("John", "jack")
//    SudokuTheme {
//        Greeting("Android")
//        LazyColumn {
//            items(people) {
//                ListItem(it)
//            }
//        }
//    }
//}

@Composable
fun ListItem(name: String) {
    Card(
        modifier = Modifier.fillMaxSize()
            .padding(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.baseline_person_24),
            contentDescription = "Photo of person"
        )
        Text(
            text = name,
            modifier = Modifier.padding(12.dp)
        )
    }
}
