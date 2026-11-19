package com.ahmad.keyboardpro

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*

class AhmadIME : InputMethodService() {
    private lateinit var root: LinearLayout
    private var mode = "en"
    private var shift = false
    private var font = 0
    private var theme = 0
    private val handler = Handler(Looper.getMainLooper())

    private class Th(val bg: Int, val key: Int, val txt: Int)
    private val themes = listOf(
        Th(Color.parseColor("#1D1633"), Color.parseColor("#3A2F66"), Color.WHITE),
        Th(Color.parseColor("#FFF0F7"), Color.parseColor("#FFD1E6"), Color.parseColor("#5A1038")),
        Th(Color.parseColor("#EAFFF6"), Color.parseColor("#C9F5E4"), Color.parseColor("#0B4A37")),
        Th(Color.parseColor("#EEF6FF"), Color.parseColor("#CFE6FF"), Color.parseColor("#0B3A6B")),
        Th(Color.parseColor("#101010"), Color.parseColor("#2B2B2B"), Color.WHITE),
        Th(Color.parseColor("#FFF8E1"), Color.parseColor("#FFE9A6"), Color.parseColor("#5A4300"))
    )

    // ---------- Fonts ----------
    private fun cv(up: Int, lo: Int, ex: Map<Char, String> = emptyMap()): (String) -> String = { t ->
        buildString {
            for (c in t) when {
                ex.containsKey(c) -> append(ex[c])
                c in 'A'..'Z' -> appendCodePoint(up + (c - 'A'))
                c in 'a'..'z' -> appendCodePoint(lo + (c - 'a'))
                else -> append(c)
            }
        }
    }
    private fun cps(s: String) = s.codePoints().toArray().map { String(Character.toChars(it)) }
    private fun mp(b: String): (String) -> String {
        val l = cps(b)
        return { t ->
            buildString {
                for (c in t) {
                    val lc = c.lowercaseChar()
                    if (lc in 'a'..'z') append(l.getOrNull(lc - 'a') ?: c.toString()) else append(c)
                }
            }
        }
    }
    private fun comb(m: String): (String) -> String = { t -> if (t == " ") t else t + m }

    private val fonts: List<Pair<String, (String) -> String>> = listOf(
        "Normal" to { t: String -> t },
        "Bold" to cv(0x1D400, 0x1D41A),
        "Italic" to cv(0x1D434, 0x1D44E, mapOf('h' to "ℎ")),
        "Bold Italic" to cv(0x1D468, 0x1D482),
        "Script" to cv(0x1D4D0, 0x1D4EA),
        "Fraktur" to cv(0x1D56C, 0x1D586),
        "Double" to cv(0x1D538, 0x1D552),
        "Sans" to cv(0x1D5A0, 0x1D5BA),
        "Sans Bold" to cv(0x1D5D4, 0x1D5EE),
        "Mono" to cv(0x1D670, 0x1D68A),
        "Circled" to cv(0x24B6, 0x24D0),
        "Wide" to cv(0xFF21, 0xFF41),
        "Squared" to cv(0x1F130, 0x1F130),
        "Black Sq" to cv(0x1F170, 0x1F170),
        "Small Caps" to mp("ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘqʀꜱᴛᴜᴠᴡxʏᴢ"),
        "Tiny" to mp("ᵃᵇᶜᵈᵉᶠᵍʰⁱʲᵏˡᵐⁿᵒᵖqʳˢᵗᵘᵛʷˣʸᶻ"),
        "Upside" to mp("ɐqɔpǝɟƃɥᴉɾʞlɯuodbɹsʇnʌʍxʎz"),
        "Strike" to comb("\u0336"),
        "Underline" to comb("\u0332"),
        "Dotted" to comb("\u0307")
    )

    // ---------- Layout data ----------
    private val en = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
    private val ur = listOf("قوعرتےءیہپ", "اسدفگھجکل", "زشچطبنم", "ٹڈڑںۓۃ،۔")
    private val num = listOf("1234567890", "@#&-_+=()/", "!?.,;:*%$")
    private val emojis = ("😀 😂 🥹 😍 🥰 😘 😎 🤩 🥺 😭 😡 🤔 🙄 😴 🤗 😇 🥳 😈 😅 😉 😊 🙂 😋 😜 🤪 😏 😬 😱 🤯 😤 " +
        "👍 👎 👏 🙌 🙏 💪 ✌️ 🤞 👌 👋 🤝 ❤️ 🧡 💛 💚 💙 💜 🖤 🤍 💔 💕 💞 💗 💖 💘 💝 " +
        "🔥 ✨ ⭐ 🌟 💫 🎉 🎁 🎂 🎈 🌹 🌷 🌸 🌼 🌻 🍀 🌿 🦋 🐝 🌙 ☀️ 🌈 ☁️ ❄️ 💧 " +
        "🐰 🐻 🐼 🐱 🐶 🦊 🐯 🦁 🐸 🐧 🦢 🐬 🦄 🍓 🍒 🍉 🍑 🍋 🍎 🍇 🍕 🍔 🍟 🍰 🧁 🍩 🍪 🍫 ☕ 🧃 " +
        "🇵🇰 🇸🇦 🇦🇪 🇬🇧 🇺🇸 🇮🇳 🇹🇷 💯 ✅ ❌ ⚠️ 🎵 🎶 📌 📷 💬 🔔 🔒 🕌 📿 ☪️").split(" ")
    private val symbols = cps("★☆✦✧✩✪✫✬✭✮✯✰⚝⛤⊹♡♥❤❥❦❧ღ→←↑↓↔↗↘↙↖➜➤➳➵➸➼↳↬↝↠⇢⇨⟶「」『』【】《》〔〕〖〗⟦⟧꒰꒱༺༻⌜⌝⌞⌟꧁꧂" +
        "ʚɞ୨୧ᡣ૮ა໒ᵔᴗ˶ᵕ⁺˖⋆｡˚𓂃𖦹𓍢─━┄┅┈┉═╌╍⎯︵︶‿⁀﹏⏝⏜⌒〰♩♪♫♬𝄞±×÷≠≈≤≥∞√∑π∆Ω°∫∂∈∪∩©®™€£¥₹₨")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    // ---------- Lifecycle ----------
    override fun onCreateInputView(): View {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        build()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::root.isInitialized) build()
    }

    // ---------- UI ----------
    private fun build() {
        val t = themes[theme]
        root.removeAllViews()
        root.setBackgroundColor(t.bg)
        root.setPadding(dp(3), dp(4), dp(3), dp(6))

        val hs = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val bar = LinearLayout(this)
        bar.addView(chip("🎨", false) { theme = (theme + 1) % themes.size; build() })
        fonts.forEachIndexed { i, f -> bar.addView(chip(f.first, i == font) { font = i; build() }) }
        hs.addView(bar)
        root.addView(hs)

        when (mode) {
            "emo" -> root.addView(scrollPanel(emojis))
            "sym" -> root.addView(scrollPanel(symbols))
            else -> {
                val rows = when (mode) { "ur" -> ur; "num" -> num; else -> en }
                rows.forEach { r -> root.addView(row(cps(r).map { ch -> key(label(ch), 1f) { type(ch) } })) }
            }
        }
        root.addView(bottomRow())
    }

    private fun label(ch: String) = if (mode == "en" && shift) ch.uppercase() else ch

    private fun bottomRow(): LinearLayout {
        val keys = mutableListOf<View>()
        keys += key(if (mode == "num" || mode == "sym" || mode == "emo") "ABC" else "123", 1.4f) {
            mode = if (mode == "en" || mode == "ur") "num" else "en"; build()
        }
        keys += key(if (shift) "⬆" else "⇧", 1.2f) { shift = !shift; if (mode != "en") mode = "en"; build() }
        keys += key(if (mode == "ur") "EN" else "اردو", 1.4f) { mode = if (mode == "ur") "en" else "ur"; build() }
        keys += key("😀", 1.2f) { mode = if (mode == "emo") "en" else "emo"; build() }
        keys += key("space", 4f) { currentInputConnection?.commitText(" ", 1) }
        keys += key("#★", 1.2f) { mode = if (mode == "sym") "en" else "sym"; build() }
        keys += key("⌫", 1.4f, repeat = true) { sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL) }
        keys += key("↵", 1.3f) { enter() }
        return row(keys)
    }

    private fun scrollPanel(items: List<String>): ScrollView {
        val sv = ScrollView(this)
        sv.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(190))
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        items.chunked(8).forEach { chunk ->
            val ks = chunk.map { e -> key(e, 1f) { currentInputConnection?.commitText(e, 1) } }.toMutableList<View>()
            while (ks.size < 8) ks += View(this).apply { layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f) }
            col.addView(row(ks))
        }
        sv.addView(col)
        return sv
    }

    private fun row(views: List<View>) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        views.forEach { addView(it) }
    }

    private fun key(label: String, w: Float, repeat: Boolean = false, action: () -> Unit): Button {
        val t = themes[theme]
        return Button(this).apply {
            text = label
            isAllCaps = false
            textSize = if (label.length > 2) 13f else 18f
            setTextColor(t.txt)
            setPadding(0, 0, 0, 0)
            minWidth = 0; minimumWidth = 0
            background = GradientDrawable().apply { setColor(t.key); cornerRadius = dp(10).toFloat() }
            layoutParams = LinearLayout.LayoutParams(0, dp(46), w).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) }
            if (repeat) {
                val r = object : Runnable { override fun run() { action(); handler.postDelayed(this, 60) } }
                setOnTouchListener { v, e ->
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> { v.isPressed = true; action(); handler.postDelayed(r, 400) }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.isPressed = false; handler.removeCallbacks(r) }
                    }
                    true
                }
            } else setOnClickListener { action() }
        }
    }

    private fun chip(label: String, on: Boolean, action: () -> Unit): TextView {
        val t = themes[theme]
        return TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(if (on) Color.WHITE else t.txt)
            setPadding(dp(12), dp(7), dp(12), dp(7))
            background = GradientDrawable().apply {
                setColor(if (on) Color.parseColor("#7C4DFF") else t.key)
                cornerRadius = dp(18).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(dp(3), dp(2), dp(3), dp(6)) }
            setOnClickListener { action() }
        }
    }

    // ---------- Typing ----------
    private fun type(ch: String) {
        val s = if (mode == "en" && shift) ch.uppercase() else ch
        val out = if (mode == "en") fonts[font].second(s) else s
        currentInputConnection?.commitText(out, 1)
        if (shift) { shift = false; build() }
    }

    private fun enter() {
        val ei = currentInputEditorInfo
        val a = (ei?.imeOptions ?: 0) and EditorInfo.IME_MASK_ACTION
        val noAction = ((ei?.imeOptions ?: 0) and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
        if (!noAction && a != EditorInfo.IME_ACTION_NONE && a != EditorInfo.IME_ACTION_UNSPECIFIED)
            currentInputConnection?.performEditorAction(a)
        else sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }
}
