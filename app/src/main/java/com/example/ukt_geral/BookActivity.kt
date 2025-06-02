package com.example.ukt_geral

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ukt_geral.adapter.BookAdapter
import com.example.ukt_geral.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class BookActivity : AppCompatActivity() {

    private lateinit var rvBook: RecyclerView
    private lateinit var kembali: Button
    private lateinit var bookAdapter: BookAdapter
    private lateinit var ivServerError: ImageView
    private lateinit var ivLoading: ImageView
    private lateinit var tambah: Button
    private lateinit var tvPaging: TextView
    private lateinit var previous: Button
    private lateinit var next: Button
    private lateinit var paginationLayout: LinearLayout

    private var currentPage = 1
    private var totalPages = 1
    private val limit = 10
    private val maxVisiblePageButtons = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        tvPaging = findViewById(R.id.tvPaging)
        rvBook = findViewById(R.id.rvBook)
        rvBook.layoutManager = LinearLayoutManager(this)

        kembali = findViewById(R.id.kembali)
        tambah = findViewById(R.id.tambah)
        previous = findViewById(R.id.previous)
        next = findViewById(R.id.next)
        paginationLayout = findViewById(R.id.paginationLayout)

        kembali.setOnClickListener {
            val intent = Intent(this@BookActivity, MainActivity::class.java)
            startActivity(intent)
        }

        tambah.setOnClickListener {
            val intent = Intent(this@BookActivity, AddBookActivity::class.java)
            startActivity(intent)
        }


        previous.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                getBooks(currentPage)
            } else {
                Toast.makeText(this@BookActivity, "Anda sudah berada di halaman pertama", Toast.LENGTH_SHORT).show()
            }
        }

        next.setOnClickListener {
            if (currentPage < totalPages) {
                currentPage++
                getBooks(currentPage)
            } else {
                Toast.makeText(this@BookActivity, "Anda sudah berada di akhir halaman", Toast.LENGTH_SHORT).show()
            }
        }

        getBooks(currentPage)
    }

    private fun createPageButtons() {
        paginationLayout.removeAllViews()

        val startPage: Int
        val endPage: Int

        if (totalPages <= maxVisiblePageButtons) {
            startPage = 1
            endPage = totalPages
        } else {
            val halfMaxButtons = maxVisiblePageButtons / 2

            if (currentPage <= halfMaxButtons) {
                startPage = 1
                endPage = maxVisiblePageButtons
            } else if (currentPage + halfMaxButtons >= totalPages) {
                startPage = totalPages - maxVisiblePageButtons + 1
                endPage = totalPages
            } else {
                startPage = currentPage - halfMaxButtons
                endPage = currentPage + halfMaxButtons
            }
        }

        for (i in startPage..endPage) {
            val pageButton = Button(this)
            pageButton.text = i.toString()
            pageButton.textSize = 12f

            val params = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.page_button_width),
                resources.getDimensionPixelSize(R.dimen.page_button_height)
            )
            params.setMargins(8, 0, 8, 0)
            pageButton.layoutParams = params

            if (i == currentPage) {
                pageButton.setBackgroundResource(R.drawable.rounded_button)
                pageButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                pageButton.setBackgroundResource(R.drawable.rounded_normal_page)
                pageButton.setTextColor(ContextCompat.getColor(this, R.color.black))
            }

            pageButton.setOnClickListener {
                currentPage = i
                getBooks(currentPage)
            }

            paginationLayout.addView(pageButton)
        }
    }

    private fun getBooks(page: Int) {
        ivServerError = findViewById(R.id.ivServerError)
        ivLoading = findViewById(R.id.ivLoading)

        runOnUiThread {
            Glide.with(this)
                .asGif()
                .load(R.drawable.loading)
                .into(ivLoading)

            ivLoading.visibility = View.VISIBLE
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.getAllBooks(page, limit)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val listBook = response.body()?.data?.books ?: emptyList()
                        val totalData = response.body()?.data?.total ?: 0
                        totalPages = (totalData + limit - 1) / limit

                        // Adapter untuk menampilkan data buku
                        bookAdapter = BookAdapter(listBook)
                        rvBook.adapter = bookAdapter
                        tvPaging.text = "Page $currentPage of $totalPages"

                        createPageButtons()

                        previous.isEnabled = currentPage > 1
                        next.isEnabled = currentPage < totalPages

                        ivLoading.visibility = View.GONE
                    } else {
                        ivServerError.visibility = View.VISIBLE
                        ivLoading.visibility = View.GONE

                        Toast.makeText(this@BookActivity, "Gagal menampilkan data buku", Toast.LENGTH_SHORT).show()
                        Log.e("BookActivity", "Server error: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    ivServerError.visibility = View.VISIBLE
                    ivLoading.visibility = View.GONE

                    Toast.makeText(this@BookActivity, "Tidak ada koneksi internet", Toast.LENGTH_SHORT).show()
                    Log.e("BookActivity", "Network error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    ivServerError.visibility = View.VISIBLE
                    ivLoading.visibility = View.GONE

                    Toast.makeText(this@BookActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("BookActivity", "Exception: ${e.message}", e)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getBooks(currentPage)
    }
}