package com.example.datingapp.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize your UI components here
    }
}
