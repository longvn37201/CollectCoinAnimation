package com.example.longvn7_lesson4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.longvn7_lesson4.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            btnEarnCoin.setOnClickListener {
                val collectCoinAnimator = CollectCoinAnimator(
                    root.context,
                    it,
                    10000,
                    9999,
                )
                root.addView(collectCoinAnimator)
                collectCoinAnimator.startAnimation()
            }
        }
    }
}