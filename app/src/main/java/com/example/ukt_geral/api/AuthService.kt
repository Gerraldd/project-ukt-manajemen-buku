package com.example.ukt_geral.api

import com.example.ukt_geral.request.BookInputRequest
import com.example.ukt_geral.request.BookUpdateRequest
import com.example.ukt_geral.request.LoginRequest
import com.example.ukt_geral.request.RefreshTokenRequest
import com.example.ukt_geral.request.RegisterRequest
import com.example.ukt_geral.response.BookIdResponse
import com.example.ukt_geral.response.BookResponse
import com.example.ukt_geral.response.DeleteBookResponse
import com.example.ukt_geral.response.LoginResponse
import com.example.ukt_geral.response.LogoutResponse
import com.example.ukt_geral.response.RefreshTokenResponse
import com.example.ukt_geral.response.RegisterResponse
import com.example.ukt_geral.response.UpdateBookResponse
import com.example.ukt_geral.response.addBookResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("refresh-token")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @POST("logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>

    // Books
    @GET("books")
    suspend fun getAllBooks(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<BookResponse>

    @GET("books/{id}")
    suspend fun getBookById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<BookIdResponse>

    @POST("books")
    suspend fun addBook(
        @Header("Authorization") token: String,
        @Body request: BookInputRequest
    ): Response<addBookResponse>

    @PUT("books/{id}")
    suspend fun updateBook(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body request: BookUpdateRequest
    ): Response<UpdateBookResponse>

    @DELETE("books/{id}")
    suspend fun deleteBook(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<DeleteBookResponse>
}
