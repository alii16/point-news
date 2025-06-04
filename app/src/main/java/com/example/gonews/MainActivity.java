package com.example.gonews;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.gonews.model.Article;
import com.example.gonews.ui.NewsAdapter;
import com.example.gonews.ui.WebViewActivity;

import com.example.gonews.ui.health.HealthFragment;
import com.example.gonews.ui.home.HomeFragment;
import com.example.gonews.ui.tech.TechFragment;
import com.example.gonews.ui.entertainment.EntertainmentFragment;
import com.example.gonews.ui.search.SearchFragment;

public class MainActivity extends AppCompatActivity implements NewsAdapter.OnItemClickListener {

    private BottomNavigationView bottomNavigationView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Menghilangkan tint pada ikon Bottom Navigation Bar
        bottomNavigationView.setItemIconTintList(null);
        // Menghilangkan tint pada ikon Navigation Drawer
        navigationView.setItemIconTintList(null); // <--- Pastikan baris ini ada untuk drawer

        // Set default fragment (Berita Utama)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Listener untuk Bottom Navigation Bar (tetap sama, karena ini navigasi utama)
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_health) {
                selectedFragment = new HealthFragment();
            } else if (itemId == R.id.nav_tech) {
                selectedFragment = new TechFragment();
            } else if (itemId == R.id.nav_entertainment) {
                selectedFragment = new EntertainmentFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });

        // Listener untuk Navigation Drawer (DIREVISI SESUAI MENU BARU)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId(); // Ambil ID item yang diklik

                // Logika untuk item-item drawer baru (Media Sosial dan Komunikasi)
                if (itemId == R.id.nav_github) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/alii16"));
                    startActivity(browserIntent);
                    Toast.makeText(MainActivity.this, "Membuka GitHub!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_instagram) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/aliiwithahoodie"));
                    startActivity(browserIntent);
                    Toast.makeText(MainActivity.this, "Membuka Instagram!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_linkedin) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/alii-polanunu/"));
                    startActivity(browserIntent);
                    Toast.makeText(MainActivity.this, "Membuka LinkedIn!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_share) {
                    // Logika untuk Bagikan (misalnya, berbagi teks atau link aplikasi)
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Coba GoNews App!");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Dapatkan berita terkini dengan GoNews App: [Link Aplikasi Anda di Play Store/Website]");
                    startActivity(Intent.createChooser(shareIntent, "Bagikan melalui"));
                    Toast.makeText(MainActivity.this, "Membuka opsi Bagikan!", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_send) {
                    // Logika untuk Kirim Feedback (misalnya, membuka aplikasi email)
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:alipolanunu077@gmail.com")); // Ganti dengan email Anda
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback GoNews App");
                    if (emailIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(emailIntent);
                    } else {
                        Toast.makeText(MainActivity.this, "Tidak ada aplikasi email yang terinstal.", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this, "Membuka opsi Kirim Feedback!", Toast.LENGTH_SHORT).show();
                }
                // Hapus semua 'else if' yang merujuk ke nav_home_drawer, nav_health_drawer, dll.
                // karena item-item ini sudah dihapus dari XML menu drawer.

                drawerLayout.closeDrawer(GravityCompat.START); // Tutup drawer setelah item dipilih
                return true;
            }
        });
    }

    // Metode untuk memuat fragment dan mengatur listener
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    // Metode publik untuk membuka DrawerLayout
    public void openDrawer() {
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    // Override onBackPressed untuk menutup drawer jika terbuka
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Implementasi metode onItemClick DARI NewsAdapter.OnItemClickListener
    // Inside your MainActivity, implementing NewsAdapter.OnItemClickListener
    @Override
    public void onItemClick(Article article) {
        if (article != null && article.getLink() != null) { // Use getLink() instead of getUrl()
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, article.getLink()); // Pass the correct URL field
            startActivity(intent);
        } else {
            Toast.makeText(this, "Link berita tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }
}