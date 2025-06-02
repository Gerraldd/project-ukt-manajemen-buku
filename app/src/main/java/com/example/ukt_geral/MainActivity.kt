package com.example.ukt_geral

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ukt_geral.api.ApiClient
import com.example.ukt_geral.request.RefreshTokenRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var dataBukuButton: Button

    private val handler = Handler(Looper.getMainLooper())
    private val checkTokenInterval: Long = TimeUnit.SECONDS.toMillis(5)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcomeTextView = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutButton)
        dataBukuButton = findViewById(R.id.dataBukuButton)

        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "")

        if (!name.isNullOrEmpty()) {
            welcomeTextView.text = "Selamat datang, $name!"
            welcomeTextView.visibility = View.VISIBLE
        }

        logoutButton.setOnClickListener {
            logoutAndRedirectToLogin()
        }

        dataBukuButton.setOnClickListener {
            val intent = Intent(this@MainActivity, BookActivity::class.java)
            startActivity(intent)
        }

        startTokenChecker()
    }

    private fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size == 3){
                val payload = String(Base64.decode(parts[1], Base64.DEFAULT))
                val jsonObject = JSONObject(payload)
                val expTime = jsonObject.getLong("exp") * 1000
                System.currentTimeMillis() > expTime
            } else true
        } catch (e: Exception) {
            true
        }
    }

    private fun startTokenChecker() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                Log.d("AUTH", "Checking token at: ${System.currentTimeMillis()}")
                checkAndRefreshToken()
                handler.postDelayed(this, checkTokenInterval)
            }
        }, checkTokenInterval)
    }

    private fun checkAndRefreshToken() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", "")
        val refreshToken = sharedPreferences.getString("refreshToken", "")

        if (accessToken != null && isTokenExpired(accessToken)) {
            if (refreshToken != null) {
                if (isTokenExpired(refreshToken)) {
                    logoutAndRedirectToLogin()
                } else {
                    refreshAccessToken(refreshToken)
                }
            } else {
                logoutAndRedirectToLogin()
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String) {
        val request = RefreshTokenRequest(refreshToken)

        CoroutineScope(Dispatchers.IO).launch {
            val response = ApiClient.authService.refreshToken(request)

            runOnUiThread {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val message = it.message
                        val newAccessToken = it.accessToken

                        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("accessToken", newAccessToken)
                        editor.apply()
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    logoutAndRedirectToLogin()
                    Toast.makeText(this@MainActivity, "Refresh token gagal  ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logoutAndRedirectToLogin() {
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("accessToken", null)

        CoroutineScope(Dispatchers.IO).launch {
            var logoutMessage = "Logout gagal"

            if (!accessToken.isNullOrEmpty()) {
                try {
                    val response = ApiClient.authService.logout("Bearer $accessToken")
                    val message = response.body()?.message
                    logoutMessage = if (response.isSuccessful) {
                        message ?: "Logout berhasil"
                    } else {
                        "Logout gagal di server"
                    }
                } catch (e: IOException) {
                    logoutMessage = "Terjadi kesalahan jaringan saat logout"
                    Log.d("AUTH", "IOException saat logout: ${e.message}")
                } catch (e: Exception) {
                    logoutMessage = "An error occurred: ${e.message}"
                    Log.d("AUTH", "Exception saat logout: ${e.message}")
                }
            } else {
                logoutMessage = "Token tidak ditemukan, langsung logout"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, logoutMessage, Toast.LENGTH_SHORT).show()

                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                handler.removeCallbacksAndMessages(null)
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}