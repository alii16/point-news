package com.example.pointnews.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pointnews.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FAB

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "extra_url";
    private WebView webView;
    private ProgressBar progressBar;
    private FloatingActionButton fabShare; // Deklarasi FAB
    private String currentUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        fabShare = findViewById(R.id.fab_share); // Inisialisasi FAB

        String url = getIntent().getStringExtra(EXTRA_URL);

        if (url == null || url.isEmpty()) {
            Toast.makeText(this, "URL berita tidak valid.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUrl = url;

        // Konfigurasi WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                currentUrl = url;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                currentUrl = url;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(WebViewActivity.this, "Error loading page: " + description, Toast.LENGTH_LONG).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        webView.loadUrl(url);

        // Set OnClickListener untuk FAB
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareNewsArticle();
            }
        });
    }

    private void shareNewsArticle() {
        if (currentUrl == null || currentUrl.isEmpty()) {
            Toast.makeText(this, "Tidak ada URL berita untuk dibagikan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "Ayo baca berita ini: " + currentUrl;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

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