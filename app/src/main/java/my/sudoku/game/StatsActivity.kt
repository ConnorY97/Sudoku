package my.sudoku.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.util.Locale
import java.util.concurrent.TimeUnit

class StatsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val returnButton: ImageButton = findViewById(R.id.backButton)
        returnButton.setOnClickListener {
            // Navigate to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close LoadActivity to prevent back navigation to it
        }

        val tvTotalSavedBoards: TextView = findViewById(R.id.tv_total_saved_boards)
        val tvTotalFinishedBoards: TextView = findViewById(R.id.tv_total_finished_boards)
        val tvTotalPlayTime: TextView = findViewById(R.id.total_time)
        val tvAvgCompletionTime: TextView = findViewById(R.id.tv_avg_completion_time)

        val sharedPreferences = getSharedPreferences("SudokuGame", Context.MODE_PRIVATE)
        val savedBoards = sharedPreferences.getStringSet("SavedBoards", emptySet()) ?: emptySet()
        val finishedBoards = savedBoards.filter {
            sharedPreferences.getBoolean("${it}_isFinished", false)
        }

        // Calculate average completion time for finished boards
        val totalTime = finishedBoards.sumOf {
            sharedPreferences.getLong("${it}_elapsedTime", 0L)
        }
        val avgTime = if (finishedBoards.isNotEmpty()) totalTime / finishedBoards.size else 0L

        // Calculate the total time played
        val totalPlayTime = savedBoards.sumOf {
            sharedPreferences.getLong("${it}_elapsedTime", 0L)
        }

        // Update UI
        tvTotalSavedBoards.text = getString(R.string.total_saved_boards, savedBoards.size)
        tvTotalFinishedBoards.text = getString(R.string.total_finished_boards, finishedBoards.size)
        tvTotalPlayTime.text = getString(R.string.total_time, formatTime(totalPlayTime))
        tvAvgCompletionTime.text = getString(R.string.avg_completion_time, formatTime(avgTime))
    }

    private fun formatTime(timeInMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

}