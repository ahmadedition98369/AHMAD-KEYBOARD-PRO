package com.ahmad.keyboard;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Button to enable Keyboard in settings
        Button btnEnable = findViewById(R.id.btn_enable_keyboard);
        btnEnable.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });

        // Social Media Links Setup (Aapki di hui links)
        setupLink(R.id.btn_tiktok, "https://www.tiktok.com/@ahmad_edition98");
        setupLink(R.id.btn_insta, "https://www.instagram.com/ahmad_edition98");
        setupLink(R.id.btn_youtube, "https://youtube.com/@ahmad_edition98");
        setupLink(R.id.btn_facebook, "https://www.facebook.com/share/19jMkDXpC8/");
        setupLink(R.id.btn_pinterest, "https://pin.it/5lsqObWfy");
        setupLink(R.id.btn_twitter, "https://x.com/Ahmad_Edition98");
        setupLink(R.id.btn_whatsapp, "https://wa.me/923115643226"); // Contact
    }

    private void setupLink(int viewId, final String url) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            });
        }
    }
}