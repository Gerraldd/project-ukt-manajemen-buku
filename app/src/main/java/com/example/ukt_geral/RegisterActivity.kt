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
import com.example.ukt_geral.request.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var arahKeLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        registerButton = findViewById(R.id.registerButton)
        arahKeLogin = findViewById(R.id.arahKeLogin)

        registerButton.setOnClickListener {
            val name = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val phone = phoneEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this@RegisterActivity, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, password, phone)
        }

        arahKeLogin.setOnClickListener {
            arahKeLogin.paintFlags = arahKeLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        arahKeLogin.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    arahKeLogin.paintFlags = arahKeLogin.paintFlags or Paint.UNDERLINE_TEXT_FLAG
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    arahKeLogin.paintFlags = arahKeLogin.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                }
            }
            false
        }
    }

    private fun registerUser(name: String, email: String, password: String, phone: String) {
        val request = RegisterRequest(name, email, password, phone)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authService.register(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val message = it.message

                            val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("name", name)
                            editor.apply()

                            Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        val jsonError = JSONObject(errorMessage)
                        val serverMessage = jsonError.getString("error")

                        Toast.makeText(this@RegisterActivity, serverMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Terjadi kesalahan pada server", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "Server error: ${e.message}", e)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("RegisterActivity", "Exception: ${e.message}", e)
                }
            }
        }
    }
}