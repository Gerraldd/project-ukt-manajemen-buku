package com.example.ukt_geral

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukt_geral.api.ApiClient
import com.example.ukt_geral.request.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var arahKeRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        arahKeRegister = findViewById(R.id.arahKeRegister)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginActivity, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        arahKeRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        
        arahKeRegister.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    arahKeRegister.paintFlags = arahKeRegister.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    arahKeRegister.paintFlags = arahKeRegister.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                }
            }
            false
        }
    }

    private fun loginUser(email: String, password: String) {
        val request = LoginRequest(email, password)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.login(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message ?: "Login Berhasil"
                            val accesToken = it.accessToken
                            val refreshToken = it.refreshToken

                            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("name", email)
                            editor.putString("accessToken", accesToken)
                            editor.putString("refreshToken", refreshToken)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        var serverMessage = "Login gagal"

                        if (!errorMessage.isNullOrEmpty()) {
                            try {
                                val jsonError = JSONObject(errorMessage)

                                if (jsonError.has("error")){
                                    serverMessage = jsonError.getString("error")
                                } else if (jsonError.has("errors")) {
                                    val errorArrays = jsonError.getJSONArray("errors")
                                    val firstError = errorArrays.getJSONObject(0)
                                    serverMessage = firstError.getString("msg")
                                }
                            } catch (e: Exception) {
                                Log.e("ErrorLoginActivity", "Exception: ${e.message}", e)
                            }
                        }

                        Toast.makeText(this@LoginActivity, serverMessage, Toast.LENGTH_SHORT).show()
                        Log.e("LoginActivity", "Error: $serverMessage")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Server error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Exception: ${e.message}", e)
                }
            }
        }
    }
}