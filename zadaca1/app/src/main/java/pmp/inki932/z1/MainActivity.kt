package pmp.inki932.z1

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private val dictionary = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ensureDictionaryFileExists()

        val searchInput = findViewById<TextInputEditText>(R.id.searchInput)
        val resultText = findViewById<TextView>(R.id.resultText)
        val searchButton = findViewById<MaterialButton>(R.id.searchButton)
        val tagInput = findViewById<TextInputEditText>(R.id.tagInput)
        val saveButton = findViewById<MaterialButton>(R.id.saveButton)
        val clearButton = findViewById<MaterialButton>(R.id.clearButton)

        loadUserDictionary()

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim().lowercase()
            var result: String? = null

            if (dictionary.containsKey(query)) {
                result = dictionary[query]
            } else {
                for ((en, mk) in dictionary) {
                    if (mk.lowercase() == query) {
                        result = en
                        break
                    }
                }
            }

            resultText.text = result ?: "Word not found"
        }

        saveButton.setOnClickListener {
            val english = searchInput.text.toString().trim()
            val macedonian = tagInput.text.toString().trim()

            if (english.isNotEmpty() && macedonian.isNotEmpty()) {
                dictionary[english.lowercase()] = macedonian.lowercase()

                saveWord(english, macedonian)

                tagInput.setText("")
            }
        }

        clearButton.setOnClickListener {
            dictionary.clear()
            getDictionaryFile().writeText("")
            val tagContainer = findViewById<LinearLayout>(R.id.tagContainer)
            tagContainer.removeAllViews()
        }
    }

    private fun getDictionaryFile(): File {
        return File(filesDir, "en_mk_recnik.txt")
    }

    private fun ensureDictionaryFileExists() {
        val file = getDictionaryFile()
        if (!file.exists()) {
            assets.open("en_mk_recnik.txt").use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    private fun loadUserDictionary() {
        val file = getDictionaryFile()
        if (!file.exists()) return

        val reader = file.bufferedReader()
        reader.forEachLine {
            val parts = it.split(",")
            if (parts.size == 2) {
                val en = parts[0].trim().lowercase()
                val mk = parts[1].trim().lowercase()
                dictionary[en] = mk
                addTagToUI(en, mk)
            }
        }
        reader.close()
    }

    fun saveWord(en: String, mk: String) {
        val text = "$en, $mk\n"
        openFileOutput("en_mk_recnik.txt", MODE_APPEND).use {
            it.write(text.toByteArray())
        }

        addTagToUI(en, mk)
    }

    private fun addTagToUI(en: String, mk: String) {
        val tagContainer = findViewById<LinearLayout>(R.id.tagContainer)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(4, 4, 4, 4)
        }

        val textView = TextView(this).apply {
            text = "$en → $mk"
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val deleteButton = MaterialButton(this).apply {
            text = "Delete"
            setOnClickListener {
                tagContainer.removeView(row)
                dictionary.remove(en.lowercase())
                val file = getDictionaryFile()
                file.writeText(dictionary.entries.joinToString("\n") { "${it.key}, ${it.value}" })
            }
        }

        row.addView(textView)
        row.addView(deleteButton)
        tagContainer.addView(row)
    }
}