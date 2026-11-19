package com.ahmad.keyboardpro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val l = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        fun text(s: String, size: Float) = TextView(this).apply { text = s; textSize = size; setPadding(0, 16, 0, 16) }
        fun btn(s: String, f: () -> Unit) = Button(this).apply { text = s; isAllCaps = false; setOnClickListener { f() } }
        l.addView(text("Ahmad Keyboard Pro", 26f))
        l.addView(text("Step 1: Keyboard ko enable karein (list mein Ahmad Keyboard Pro ON karein).", 15f))
        l.addView(btn("1. Keyboard enable karein") { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) })
        l.addView(text("Step 2: Keyboard select karein.", 15f))
        l.addView(btn("2. Keyboard select karein") {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        })
        l.addView(text("Step 3: Yahan type karke test karein.", 15f))
        l.addView(EditText(this).apply { hint = "Yahan likhein..." })
        setContentView(ScrollView(this).apply { addView(l) })
    }
}
