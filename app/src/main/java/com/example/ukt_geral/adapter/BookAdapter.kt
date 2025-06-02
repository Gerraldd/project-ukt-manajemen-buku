package com.example.ukt_geral.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ukt_geral.BookInformationActivity
import com.example.ukt_geral.R
import com.example.ukt_geral.response.DataBook
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation

class BookAdapter(private val listBook: List<DataBook>) :
    RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleBook: TextView = view.findViewById(R.id.tvTitle)
        val authorBook: TextView = view.findViewById(R.id.tvAuthor)
        val lihatBook: Button = view.findViewById(R.id.lihatBuku)
        val publisherBook: TextView = view.findViewById(R.id.tvPublisher)
        val genreBook: TextView = view.findViewById(R.id.tvGenre)
        val ivBook: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_book, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listBook.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = listBook[position]
        val image = book.cover_image.large
        val id = book.id

        Picasso.get()
            .load(image)
            .transform(RoundedCornersTransformation(30, 0))
            .placeholder(R.drawable.loading)
            .error(R.drawable.ic_launcher_background)
            .into(holder.ivBook)

        holder.titleBook.text = book.title
        holder.authorBook.text = "Author      : ${book.author}"
        holder.publisherBook.text = "Publisher : ${book.publisher}"
        holder.genreBook.text = "Genre       : ${book.genre}"
        holder.lihatBook.setOnClickListener {
            val sharedPreferences = holder.itemView.context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putInt("bookId", id)
            editor.putString("image", image)
            editor.apply()

            val intent = Intent(holder.itemView.context, BookInformationActivity::class.java)
            holder.itemView.context.startActivity(intent)
        }
    }
}
