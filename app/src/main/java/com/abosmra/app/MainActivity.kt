package com.abosmra.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = getString(R.string.welcome)
            textSize = 28f
            setTextColor(Color.parseColor("#1B5E20"))
            gravity = Gravity.CENTER
        }

        val counter = TextView(this).apply {
            textSize = 48f
            gravity = Gravity.CENTER
            text = count.toString()
        }

        val button = Button(this).apply {
            text = getString(R.string.press_me)
            setOnClickListener {
                count++
                counter.text = count.toString()
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            addView(title)
            addView(counter)
            addView(button)
        }

        setContentView(root)
    }
}
