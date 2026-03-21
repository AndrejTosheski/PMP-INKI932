package pmp.inki932.z1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {

    private val dictionary = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureDictionaryFileExists()
        loadUserDictionary()

        setContent {
            MainScreen()
        }
    }

    @Composable
    fun MainScreen() {
        var searchInput by remember { mutableStateOf("") }
        var tagInput by remember { mutableStateOf("") }
        var resultText by remember { mutableStateOf("") }

        var wordList by remember { mutableStateOf(dictionary.toList()) }

        Column(modifier = Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val query = searchInput.trim().lowercase()
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

                    resultText = result ?: "Word not found"
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Search")
            }

            Text(
                text = resultText,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(modifier = Modifier.padding(top = 12.dp)) {

                OutlinedTextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    label = { Text("Add translation") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        val english = searchInput.trim()
                        val macedonian = tagInput.trim()

                        if (english.isNotEmpty() && macedonian.isNotEmpty()) {
                            dictionary[english.lowercase()] = macedonian.lowercase()

                            saveWord(english, macedonian)

                            tagInput = ""
                            wordList = dictionary.toList()
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Save")
                }
            }

            Text(
                text = "Tagged Searches",
                modifier = Modifier.padding(top = 24.dp)
            )

            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(wordList) { (en, mk) ->
                    TagRow(
                        en = en,
                        mk = mk,
                        onDelete = {
                            dictionary.remove(en.lowercase())
                            updateFile()
                            wordList = dictionary.toList()
                        }
                    )
                }
            }

            Button(
                onClick = {
                    dictionary.clear()
                    getDictionaryFile().writeText("")
                    wordList = emptyList()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("Clear All")
            }
        }
    }

    @Composable
    fun TagRow(en: String, mk: String, onDelete: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$en → $mk")

            Button(onClick = onDelete) {
                Text("Delete")
            }
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

        file.forEachLine {
            val parts = it.split(",")
            if (parts.size == 2) {
                val en = parts[0].trim().lowercase()
                val mk = parts[1].trim().lowercase()
                dictionary[en] = mk
            }
        }
    }

    private fun saveWord(en: String, mk: String) {
        val text = "$en, $mk\n"
        openFileOutput("en_mk_recnik.txt", MODE_APPEND).use {
            it.write(text.toByteArray())
        }
    }

    private fun updateFile() {
        val file = getDictionaryFile()
        file.writeText(dictionary.entries.joinToString("\n") {
            "${it.key}, ${it.value}"
        })
    }
}