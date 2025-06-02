package com.example.ukt_geral

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukt_geral.api.ApiClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class BookInformationActivity : AppCompatActivity() {

    private lateinit var tvIsbn: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var tvPublisherDate: TextView
    private lateinit var tvGenre: TextView
    private lateinit var tvLanguage: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvUploadBy: TextView
    private lateinit var ivBook: ImageView
    private lateinit var kembali: Button
    private lateinit var delete: Button
    private lateinit var update: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_information)

        kembali = findViewById(R.id.kembali)
        delete = findViewById(R.id.delete)
        update = findViewById(R.id.update)
        ivBook = findViewById(R.id.ivBook)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val id = sharedPreferences.getInt("bookId", -1)
        val image = sharedPreferences.getString("image", "")

        Picasso.get()
            .load(image)
            .transform(RoundedCornersTransformation(30, 0))
            .placeholder(R.drawable.loading)
            .error(R.drawable.ic_launcher_background)
            .into(ivBook)

        kembali.setOnClickListener {
            val intent = Intent(this@BookInformationActivity, BookActivity::class.java)
            startActivity(intent)
        }

        delete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Konfirmasi")
                .setMessage("Yakin ingin menghapus?")
                .setPositiveButton("Ya") { dialog, _ ->
                    deleteBookById(id)
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        update.setOnClickListener {
            val intent = Intent(this@BookInformationActivity, UpdateBookActivity::class.java)
            startActivity(intent)
        }

        getBookById(id)
    }

    private fun getBookById(id: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        tvIsbn = findViewById(R.id.tvIsbn)
        tvTitle = findViewById(R.id.tvTitle)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvPublisher = findViewById(R.id.tvPublisher)
        tvPublisherDate = findViewById(R.id.tvPublisherDate)
        tvGenre = findViewById(R.id.tvGenre)
        tvLanguage = findViewById(R.id.tvLanguage)
        tvDescription = findViewById(R.id.tvDescription)
        tvUploadBy = findViewById(R.id.tvUploadBy)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.getBookById(id, "Bearer $accessToken")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val isbn = it.data.isbn
                            val title = it.data.title
                            val author = it.data.author
                            val publisher = it.data.publisher
                            val published_date = it.data.published_date
                            val genre = it.data.genre
                            val language = it.data.language
                            val description = it.data.description
                            val upload_by = it.data.uploaded_by

                            // Batasi response dari published date
                            val published_date_trimmed = if (published_date.length > 10) {
                                published_date.substring(0, 10)
                            } else {
                                published_date
                            }

                            tvIsbn.text = "ISBN : $isbn"
                            tvTitle.text = title
                            tvAuthor.text = "Author : $author"
                            tvPublisher.text = "Publisher : $publisher"
                            tvPublisherDate.text = "Tanggal Publish : $published_date_trimmed"
                            tvGenre.text = "Genre : $genre"
                            tvLanguage.text = "Bahasa : $language"
                            tvDescription.text = "Deskripsi : $description"
                            tvUploadBy.text = "Upload By : $upload_by"

                            val editor = sharedPreferences.edit()
                            editor.putString("isbn", isbn)
                            editor.putString("title", title)
                            editor.putString("author", author)
                            editor.putString("publisher", publisher)
                            editor.putString("published_date", published_date)
                            editor.putString("genre", genre)
                            editor.putString("language", language)
                            editor.putString("description", description)
                            editor.apply()
                        }
                    } else {
                        Toast.makeText(this@BookInformationActivity, "Gagal mengambil informasi buku", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookInformationActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    Log.e("BookInformationActivity", "Server error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookInformationActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("BookInformationActivity", "Exception: ${e.message}", e)
                }
            }
        }
    }

    private fun deleteBookById(id: Int) {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.deleteBook(id, "Bearer $accessToken")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message

                            Toast.makeText(this@BookInformationActivity, message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@BookInformationActivity, BookActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Delete Gagal"
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("message")

                        Toast.makeText(this@BookInformationActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookInformationActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    Log.e("BookInformationActivity", "Server error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookInformationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("BookInformationActivity", "An error occurred: ${e.message}", e)
                }
            }
        }
    }
}