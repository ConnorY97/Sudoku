package my.sudoku.game

import android.os.Bundle
import androidx.activity.ComponentActivity


class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        val returnButton = findViewById<Button>(R.id.returnButton)
//        returnButton.visibility = View.VISIBLE
//        if (returnButton == null) {
//            Log.e("OnCreate", "Return button is invalid")
//        }
//
//        returnButton.setOnClickListener {
//            Log.i("returnButtonOnClick", "Should error checking be enabled")
//            val checkBox = findViewById<CheckBox>(R.id.checkBox)
//
//
//            // Get the state of the CheckBox
//            val isChecked = checkBox.isChecked
//
//
//            // Create an intent to pass the data
//            val intent = Intent(this, MainActivity::class.java)
//            intent.putExtra("CHECKBOX_STATE", isChecked) // Pass true/false
//            startActivity(intent)
//
//            Log.i("returnButtonOnClick", "Return to the game")
//            val mainActivity = Intent(this, MainActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//            }
//            startActivity(mainActivity)
//        }
    }
}
