package com.example.ukt_geral.response

data class RegisterResponse(
    val userId: Int,
    val message: String
)

data class LoginResponse(
    val message: String,
    val accessToken: String,
    val refreshToken: String
)

data class RefreshTokenResponse(
    val message: String,
    val accessToken: String
)

data class LogoutResponse(
    val message: String
)

// Book Response
data class BookResponse(
    val status: String,
    val message: String,
    val data: Books,
)

// Get Information Book
data class BookIdResponse(
    val status: String,
    val message: String,
    val data: DataBook
)

data class addBookResponse(
    val status: String,
    val message: String,
    val data: Book
)

data class Book(
    val id: Int,
    val title: String,
    val author: String
)

data class Books(
    val books: List<DataBook>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class DataBook(
    val id: Int,
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val published_date: String,
    val genre: String,
    val language: String,
    val description: String,
    val cover_image: CoverImage,
    val uploaded_by: String
)

data class CoverImage(
    val small: String,
    val medium: String,
    val large: String
)

data class DeleteBookResponse(
    val status: String,
    val message: String
)

data class UpdateBookResponse(
    val status: String,
    val message: String,
    val data: Book
)