package com.example.gonews.ui.entertainment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Tambahkan import ini

import com.airbnb.lottie.LottieAnimationView;
import com.example.gonews.R;
import com.example.gonews.ui.NewsAdapter;
import com.example.gonews.viewmodel.EntertainmentViewModel;
import com.example.gonews.model.Article; // Pastikan ini tetap mengacu pada model Article yang sudah diubah
import com.example.gonews.MainActivity;

import java.util.List; // Tambahkan import ini

public class EntertainmentFragment extends Fragment {

    private EntertainmentViewModel entertainmentViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError;
    private ImageButton btnOpenSideNav;
    private SwipeRefreshLayout swipeRefreshLayout; // Tambahkan ini untuk pull-to-refresh

    private static final long MIN_LOADING_TIME_MS = 1700;
    private long loadingStartedAt;

    private boolean isLastPage = false; // Menandakan apakah sudah di halaman terakhir
    private boolean isLoadingMore = false; // Menandakan apakah sedang memuat lebih banyak data

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_entertainment, container, false);

        recyclerView = root.findViewById(R.id.rv_entertainment_news);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_entertainment);
        tvError = root.findViewById(R.id.tv_error_entertainment);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout_entertainment); // Inisialisasi

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());
        }

        entertainmentViewModel = new ViewModelProvider(this).get(EntertainmentViewModel.class);

        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // --- Observer untuk Data Berita (entertainmentNews) ---
        entertainmentViewModel.getEntertainmentNews().observe(getViewLifecycleOwner(), articles -> {
            // Logika delay loading tetap bisa digunakan
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Sembunyikan loading Lottie setelah data siap
                if (!isLoadingMore) { // Hanya sembunyikan Lottie jika bukan load more (untuk initial/refresh)
                    lottieLoadingView.setVisibility(View.GONE);
                    lottieLoadingView.cancelAnimation();
                }
                swipeRefreshLayout.setRefreshing(false); // Sembunyikan indikator refresh

                if (articles != null && !articles.isEmpty()) {
                    // Jika ini adalah panggilan pertama atau refresh
                    if (!isLoadingMore) {
                        newsAdapter.setArticles(articles); // Reset dan set artikel baru
                        recyclerView.setVisibility(View.VISIBLE);
                    } else { // Jika ini adalah load more
                        newsAdapter.addArticles(articles); // Tambahkan artikel ke daftar yang sudah ada
                    }
                    tvError.setVisibility(View.GONE);
                    Log.d("EntertainmentFragment", "Articles loaded successfully into adapter. Total articles: " + newsAdapter.getItemCount());
                    isLastPage = false; // Ada data, berarti belum halaman terakhir
                } else {
                    // Jika tidak ada artikel yang dikembalikan (respons kosong)
                    // Atau jika ini load more dan tidak ada lagi halaman
                    if (newsAdapter.getItemCount() == 0 && !isLoadingMore) { // Hanya tampilkan error jika tidak ada data sama sekali
                        newsAdapter.setArticles(null);
                        recyclerView.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Tidak ada berita hiburan ditemukan.");
                        Log.w("EntertainmentFragment", "No articles returned from API, or articles list is empty.");
                    } else if (isLoadingMore) { // Jika ini load more dan tidak ada data lagi
                        isLastPage = true; // Menandakan sudah di halaman terakhir
                        Log.d("EntertainmentFragment", "No more articles to load (reached last page).");
                        Toast.makeText(getContext(), "Semua berita hiburan telah dimuat.", Toast.LENGTH_SHORT).show();
                    }
                }
                isLoadingMore = false; // Reset status loading more
            }, delayMillis);
        });

        // --- Observer untuk Pesan Error ---
        entertainmentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();
                swipeRefreshLayout.setRefreshing(false); // Sembunyikan indikator refresh
                isLoadingMore = false; // Reset status loading more

                if (message != null && !message.isEmpty()) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(message);
                    recyclerView.setVisibility(View.GONE); // Sembunyikan RecyclerView saat ada error
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
                    Log.e("EntertainmentFragment", "Displaying error in UI: " + message);
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }, delayMillis);
        });

        // --- Observer untuk Status Loading ---
        entertainmentViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                if (!isLoadingMore) { // Tampilkan Lottie hanya untuk initial load/refresh, bukan load more
                    lottieLoadingView.setVisibility(View.VISIBLE);
                    lottieLoadingView.playAnimation();
                    loadingStartedAt = System.currentTimeMillis();
                }
                tvError.setVisibility(View.GONE); // Sembunyikan error saat loading
            } else {
                // Lottie akan disembunyikan di observer getEntertainmentNews/getErrorMessage dengan delay
            }
        });


        // --- Implementasi Infinite Scrolling ---
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoadingMore && !isLastPage) { // Hanya panggil jika tidak sedang loading dan bukan halaman terakhir
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= newsAdapter.getItemCount()) { // Tambahan: Pastikan adapter tidak kosong
                            isLoadingMore = true; // Set status loading more
                            entertainmentViewModel.loadMoreEntertainmentNews(); // Panggil metode load more
                            // Anda bisa menampilkan progress bar kecil di footer RecyclerView di sini
                            // newsAdapter.addLoadingFooter(); // Contoh, jika NewsAdapter mendukung
                        }
                    }
                }
            }
        });

        // --- Implementasi Pull-to-Refresh ---
        swipeRefreshLayout.setOnRefreshListener(() -> {
            entertainmentViewModel.resetPagination(); // Reset paginasi saat refresh
            isLastPage = false; // Reset status halaman terakhir
            isLoadingMore = false; // Reset status loading more
            loadEntertainmentHeadlines(false); // Muat ulang berita (bukan load more)
        });


        // Panggil untuk memuat berita pertama kali
        loadEntertainmentHeadlines(false); // FALSE berarti ini bukan load more

        return root;
    }

    private void loadEntertainmentHeadlines(boolean isLoadMore) {
        if (!isLoadMore) { // Jika bukan load more, tampilkan loading Lottie
            recyclerView.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            lottieLoadingView.setVisibility(View.VISIBLE);
            lottieLoadingView.playAnimation();
            loadingStartedAt = System.currentTimeMillis();
        }
        entertainmentViewModel.fetchEntertainmentNews(isLoadMore); // Panggil metode ViewModel yang baru
    }
}