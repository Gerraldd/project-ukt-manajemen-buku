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
import com.example.ukt_geral.request.BookUpdateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpdateBookActivity : AppCompatActivity() {

    private lateinit var isbnEditText: EditText
    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var publisherEditText: EditText
    private lateinit var publishedDateEditText: EditText
    private lateinit var genreEditText: EditText
    private lateinit var languageEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_book)

        isbnEditText = findViewById(R.id.isbnEditText)
        titleEditText = findViewById(R.id.titleEditText)
        authorEditText = findViewById(R.id.authorEditText)
        publisherEditText = findViewById(R.id.publisherEditText)
        publishedDateEditText = findViewById(R.id.publishedDateEditText)
        genreEditText = findViewById(R.id.genreEditText)
        languageEditText = findViewById(R.id.languageEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        val calendar = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val formatter = SimpleDateFormat("yyyy-mm-dd", Locale.getDefault())
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

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val id = sharedPreferences.getInt("bookId", -1)

        saveButton.setOnClickListener {
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
                Toast.makeText(this@UpdateBookActivity, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateBook(id, isbn, title, author, publisher, published_date, genre, language, description)
        }

        getInputBook()
    }

    private fun updateBook(id: Int, isbn: String, title: String, author: String, publisher: String, published_date: String,
                           genre: String, language: String, description: String) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val request = BookUpdateRequest(isbn, title, author, publisher, published_date, genre, language, description)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.updateBook(id, "Bearer $accessToken",request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message

                            Toast.makeText(this@UpdateBookActivity, message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@UpdateBookActivity, BookActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Update Gagal"
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("message")

                        Toast.makeText(this@UpdateBookActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateBookActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UpdateBookActivity, "An error occured: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UpdateBookActivity", "Exception: ${e.message}", e)
                }
            }
        }
    }

    private fun getInputBook() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isbn = sharedPreferences.getString("isbn", "")
        val title = sharedPreferences.getString("title", "")
        val author = sharedPreferences.getString("author", "")
        val publisher = sharedPreferences.getString("publisher", "")
        val published_date = sharedPreferences.getString("published_date", "")
        val genre = sharedPreferences.getString("genre", "")
        val language = sharedPreferences.getString("language", "")
        val description = sharedPreferences.getString("description", "")

        isbnEditText = findViewById(R.id.isbnEditText)
        titleEditText = findViewById(R.id.titleEditText)
        authorEditText = findViewById(R.id.authorEditText)
        publisherEditText = findViewById(R.id.publisherEditText)
        publishedDateEditText = findViewById(R.id.publishedDateEditText)
        genreEditText = findViewById(R.id.genreEditText)
        languageEditText = findViewById(R.id.languageEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)

        isbnEditText.setText(isbn)
        titleEditText.setText(title)
        authorEditText.setText(author)
        publisherEditText.setText(publisher)
        publishedDateEditText.setText(published_date)
        genreEditText.setText(genre)
        languageEditText.setText(language)
        descriptionEditText.setText(description)
    }
}