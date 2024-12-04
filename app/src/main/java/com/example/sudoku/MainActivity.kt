package com.example.sudoku

import android.os.Bundle
import android.widget.EditText
import android.widget.GridLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.sudoku.ui.theme.SudokuTheme

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        val people = listOf("John", "jack")
//
//        setContent {
//            SudokuTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                    LazyColumn {
//                        items(people) {
//                            ListItem(it)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sudokuGrid: GridLayout = findViewById<GridLayout>(R.id.sudokuGrid)

        val gridSize = 9

        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val cell = EditText(this)
                cell.layoutParams = GridLayout.LayoutParams().apply {
                    width = 100 // Adjust cell width
                    height = 100 // Adjust cell height
                    columnSpec = GridLayout.spec(col)
                    rowSpec = GridLayout.spec(row)
                    setMargins(1, 1, 1, 1) // Add some spacing
                }
                cell.textAlignment = EditText.TEXT_ALIGNMENT_CENTER
                cell.setBackgroundResource(android.R.color.white)
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    val people = listOf("John", "jack")
    SudokuTheme {
        Greeting("Android")
        LazyColumn {
            items(people) {
                ListItem(it)
            }
        }
    }
}

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
