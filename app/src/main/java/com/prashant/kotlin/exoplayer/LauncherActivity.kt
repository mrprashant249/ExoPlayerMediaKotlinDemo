package com.prashant.kotlin.exoplayer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Created by BytesBee.
 *
 * @author BytesBee Infotech (Prashant Adesara)
 * @link <a href="https://bytesbee.com">BytesBee</a>
 */
class LauncherActivity : AppCompatActivity() {
    private lateinit var urlInput: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        urlInput = findViewById(R.id.url_input)
        urlInput.setText(Constants.DEFAULT_URL)

        val btnExoPlay: MaterialButton = findViewById(R.id.btnExoPlayer)
        val btnCustomExo: MaterialButton = findViewById(R.id.btnCustomExoPlayer)

        btnExoPlay.setOnClickListener { openNextScreen(MainActivity::class.java) }
        btnCustomExo.setOnClickListener { openNextScreen(CustomExoActivity::class.java) }
    }

    private fun openNextScreen(cls: Class<*>) {
        val videoUrl = urlInput.text?.toString()?.trim() ?: ""
        val intent = Intent(this, cls).apply {
            putExtra(Constants.KEY_URL, if (videoUrl.isEmpty()) Constants.DEFAULT_URL else videoUrl)
        }
        startActivity(intent)
    }
} 