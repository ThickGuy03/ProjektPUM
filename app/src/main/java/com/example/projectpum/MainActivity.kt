package com.example.projectpum

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectpum.ui.theme.ProjectPumTheme

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectPumTheme {
                // A surface container using the 'background' color from the theme
                Surface() {
                    StartApp()
                }
            }
        }
    }
}

@Composable
fun StartApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainScreen") {
        composable("mainScreen") {
            MainScreen(navController = navController)
        }
        composable("secondScreen") {
            SecondScreen(navController = navController)
        }
        composable("thirdScreen/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            ThirdScreen(navController = navController, itemId = itemId)
        }
    }
}

fun saveDataToFile(context: Context, topic: String, note: String) {
    if (topic.isEmpty() || note.isEmpty()) {
        Toast.makeText(context, "Brak danych", Toast.LENGTH_SHORT).show()
        return
    }

    val fileName = "data3.txt"
    val file = File(context.filesDir, fileName)

    try {
        if (!file.exists()) {
            file.createNewFile()
        }

        val fileOutputStream = FileOutputStream(file, true)
        val outputStreamWriter = OutputStreamWriter(fileOutputStream)

        outputStreamWriter.write("$topic $note\n")

        outputStreamWriter.close()
        fileOutputStream.close()

        Toast.makeText(context, "Zapisano!", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_SHORT).show()
    }

}

fun readDataFromFile(context: Context, index: Int): Pair<String, String> {
    val fileName = "data3.txt"
    val file = File(context.filesDir, fileName)

    try {
        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            var currentLineIndex = 0
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                if (currentLineIndex == index) {
                    val dataParts = line?.split("\\s+".toRegex())
                    if (dataParts != null && dataParts.size == 2) {
                        return Pair(dataParts[0], dataParts[1])
                    }
                }
                currentLineIndex++
            }

            bufferedReader.close()
            inputStreamReader.close()
            fileInputStream.close()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Błąd odczytu", Toast.LENGTH_SHORT).show()
    }

    return Pair("", "")
}

fun modifyDataInFile(context: Context, index: Int, newTopic: String, newNote: String): Boolean {
    val fileName = "data3.txt"
    val file = File(context.filesDir, fileName)

    try {
        if (file.exists()) {
            val lines = file.readLines().toMutableList()

            if (index >= 0 && index < lines.size) {
                val updatedLine = "$newTopic $newNote"
                lines[index] = updatedLine

                val fileWriter = FileWriter(file, false)
                val bufferedWriter = BufferedWriter(fileWriter)

                for (line in lines) {
                    bufferedWriter.write(line)
                    bufferedWriter.newLine()
                }

                bufferedWriter.close()
                fileWriter.close()

                return true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Error modifying data in file", Toast.LENGTH_SHORT).show()
    }

    return false
}

fun deleteDataFromFile(context: Context, index: Int): Boolean {
    val fileName = "data3.txt"
    val file = File(context.filesDir, fileName)

    try {
        if (file.exists()) {
            val lines = file.readLines().toMutableList()

            if (index >= 0 && index < lines.size) {
                lines.removeAt(index)

                val fileWriter = FileWriter(file, false)
                val bufferedWriter = BufferedWriter(fileWriter)

                for (line in lines) {
                    bufferedWriter.write(line)
                    bufferedWriter.newLine()
                }

                bufferedWriter.close()
                fileWriter.close()

                Toast.makeText(context, "Usunięto!", Toast.LENGTH_SHORT).show()
                return true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show()
    }

    Toast.makeText(context, "Błąd podczas usuwania", Toast.LENGTH_SHORT).show()
    return false
}

@Composable
fun MainScreen(navController: NavHostController) {

    var numberOfCards by remember { mutableStateOf(1) }
    var cardTexts by remember { mutableStateOf<List<String>>(emptyList()) }
    val context: Context = LocalContext.current

    // Load text from file
    LaunchedEffect(key1 = numberOfCards) {
        val fileName = "data3.txt"
        try {
            val file = File(context.filesDir, fileName)
            val bufferedReader: BufferedReader = file.bufferedReader()
            cardTexts = bufferedReader.readLines()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .padding(bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            modifier = Modifier
                .padding(10.dp),
            text = "Moje Notatki",
            fontSize = 26.sp
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            items(cardTexts.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clickable {
                            navController.navigate("thirdScreen/$index")
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD4E6F1)) // ARGB
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(15.dp)
                                .align(Alignment.CenterHorizontally),
                            text = cardTexts[index],
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = {
                navController.navigate("secondScreen")
                numberOfCards++
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = "Dodaj Notatke",
                fontSize = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun SecondScreen(navController: NavHostController) {

    val context = LocalContext.current
    var topicText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(20.dp),
            text = "Dodaj Notatke",
            fontSize = 26.sp
        )
        //TEXT FIELDS
        TextField(
            modifier = Modifier
                .padding(10.dp)
                .padding(top = 80.dp),
            value = topicText,
            onValueChange = { newText -> topicText = newText },
            placeholder = { Text("Temat") }
        )
        TextField(
            modifier = Modifier
                .padding(10.dp),
            value = noteText,
            onValueChange = { newText -> noteText = newText },
            placeholder = { Text("Zawartość notatki") }
        )

    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = {
                saveDataToFile(context, topicText, noteText)
                navController.navigate("mainScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = "Dodaj",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = {
                navController.navigate("mainScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = "Wróć",
                fontSize = 20.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun ThirdScreen(navController: NavHostController, itemId: Int) {

    val context = LocalContext.current
    val (tempSubject, tempDegree) = readDataFromFile(context, itemId)

    var topicText by remember { mutableStateOf(tempSubject) }
    var noteText by remember { mutableStateOf(tempDegree) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(20.dp),
            text = "Zmień treść notatki",
            fontSize = 26.sp
        )
        TextField(
            modifier = Modifier
                .padding(10.dp)
                .padding(top = 80.dp),
            value = topicText,
            onValueChange = { newText -> topicText = newText }
        )
        TextField(
            modifier = Modifier
                .padding(10.dp),
            value = noteText,
            onValueChange = { newText -> noteText = newText }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Button(
            onClick = {
                modifyDataInFile(context, itemId, topicText, noteText)
                navController.navigate("mainScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = "Zapisz i wróć",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = {
                deleteDataFromFile(context, itemId)
                navController.navigate("mainScreen")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(
                modifier = Modifier
                    .padding(10.dp),
                text = "Usuń",
                fontSize = 20.sp
            )
        }
    }
}
