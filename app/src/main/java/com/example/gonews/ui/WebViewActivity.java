// app/src/main/java/com/example/gonews/ui/WebViewActivity.java
package com.example.gonews.ui; // Pastikan package ini benar

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gonews.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class WebViewActivity extends AppCompatActivity {

    // BARU: Deklarasikan konstanta untuk kunci Intent extra
    public static final String EXTRA_URL = "NEWS_URL"; // Ini harus sama dengan kunci yang Anda gunakan di MainActivity

    private WebView webView;
    private String currentUrl;
    private FloatingActionButton fabShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                currentUrl = url;
            }
        });

        // Tangkap URL dari Intent menggunakan konstanta EXTRA_URL
        if (getIntent().hasExtra(EXTRA_URL)) { // Ubah di sini
            currentUrl = getIntent().getStringExtra(EXTRA_URL); // Ubah di sini
            if (currentUrl != null && !currentUrl.isEmpty()) {
                webView.loadUrl(currentUrl);
            } else {
                Toast.makeText(this, "URL berita tidak valid.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "Tidak ada URL berita yang diterima.", Toast.LENGTH_SHORT).show();
            finish();
        }

        fabShare = findViewById(R.id.fab_share);
        fabShare.setOnClickListener(view -> {
            if (currentUrl != null && !currentUrl.isEmpty()) {
                shareNewsUrl(currentUrl);
            } else {
                Toast.makeText(this, "Tidak dapat membagikan URL berita.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareNewsUrl(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Baca berita ini: " + url + " - Ditemukan di GoNews App");
        startActivity(Intent.createChooser(shareIntent, "Bagikan berita melalui..."));
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}