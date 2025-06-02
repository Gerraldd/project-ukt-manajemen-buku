package com.example.ukt_geral

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukt_geral.api.ApiClient
import com.example.ukt_geral.request.BookInputRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddBookActivity : AppCompatActivity() {

    private lateinit var isbnEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var publisherEditText: EditText
    private lateinit var publishedDateEditText: EditText
    private lateinit var genreEditText: EditText
    private lateinit var languageEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        isbnEditText = findViewById(R.id.isbnEditText)
        titleEditText = findViewById(R.id.titleEditText)
        authorEditText = findViewById(R.id.authorEditText)
        publisherEditText = findViewById(R.id.publisherEditText)
        publishedDateEditText = findViewById(R.id.publishedDateEditText)
        genreEditText = findViewById(R.id.genreEditText)
        languageEditText = findViewById(R.id.languageEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        addButton = findViewById(R.id.addButton)

        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateString = formatter.format(selectedDate.time)

            publishedDateEditText.setText(dateString)
        }

        publishedDateEditText.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        addButton.setOnClickListener {
            val isbn = isbnEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()
            val author = authorEditText.text.toString().trim()
            val publisher = publisherEditText.text.toString().trim()
            val published_date = publishedDateEditText.text.toString().trim()
            val genre = genreEditText.text.toString().trim()
            val language = languageEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || publisher.isEmpty() || published_date.isEmpty()
                || genre.isEmpty() || language.isEmpty() || description.isEmpty() ) {
                Toast.makeText(this@AddBookActivity, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addBook(isbn, title, author, publisher, published_date, genre, language, description)
        }
    }

    private fun addBook(isbn: String, title: String, author: String,publisher: String, published_date: String,
                        genre: String, language: String, description: String) {
        val request = BookInputRequest(isbn, title, author, publisher, published_date, genre, language, description)
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.addBook("Bearer $accessToken", request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message

                            Toast.makeText(this@AddBookActivity, message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@AddBookActivity, BookActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Gagal menambahkan buku"
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("message")

                        Toast.makeText(this@AddBookActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddBookActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    Log.e("AddBookActivity", "Server Error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddBookActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("AddBookActivity.kt", "Exception: ${e.message}", e)
                }
            }
        }
    }
}