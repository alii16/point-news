package com.example.gonews.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView; // Pastikan ini ada
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gonews.MainActivity;
import com.example.gonews.R;
import com.example.gonews.model.Article;
import com.example.gonews.ui.NewsAdapter;
import com.example.gonews.ui.WebViewActivity;
import com.example.gonews.viewmodel.HomeViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements NewsAdapter.OnItemClickListener, SliderAdapter.OnSliderItemClickListener {

    private HomeViewModel homeViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError;
    private TextView tvPageTitle; // <<< Deklarasikan sebagai field
    private ImageButton btnOpenSideNav;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ViewPager2 viewPagerSlider;
    private SliderAdapter sliderAdapter;
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerSlider != null && sliderAdapter != null && sliderAdapter.getItemCount() > 0) {
                int currentItem = viewPagerSlider.getCurrentItem();
                int nextItem = (currentItem + 1) % sliderAdapter.getItemCount();
                viewPagerSlider.setCurrentItem(nextItem, true);
            }
            sliderHandler.postDelayed(this, 3000);
        }
    };

    private static final long MIN_LOADING_TIME_MS = 1700;
    private long loadingStartedAt;

    private boolean isLastPage = false;
    private boolean isLoadingMore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Inisialisasi View Anda di sini ---
        viewPagerSlider = root.findViewById(R.id.view_pager_slider);
        recyclerView = root.findViewById(R.id.rv_home_news);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_home);
        tvError = root.findViewById(R.id.tv_error_home);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout_home);

//        // Pastikan tvPageTitle diinisialisasi dari ID yang benar
//        tvPageTitle = root.findViewById(R.id.tv_page_title); // <<< Inisialisasi field tvPageTitle
//        if (tvPageTitle != null) { // <<< Tambahkan null check sebagai pencegahan
//            tvPageTitle.setText("Berita Utama"); // Ini baris 82 yang disebutkan di logcat
//        } else {
//            Log.e("HomeFragment", "tvPageTitle is null! Check fragment_home.xml ID.");
//        }


        sliderAdapter = new SliderAdapter(new ArrayList<>(), this);
        viewPagerSlider.setAdapter(sliderAdapter);
        viewPagerSlider.setOffscreenPageLimit(1);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        newsAdapter = new NewsAdapter();
        newsAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(newsAdapter);

        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // --- Observer untuk Data Berita (latestNews) ---
        homeViewModel.getLatestNews().observe(getViewLifecycleOwner(), articles -> {
            if (!isLoadingMore) {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();
            }
            swipeRefreshLayout.setRefreshing(false);

            if (articles != null && !articles.isEmpty()) {
                List<Article> sliderArticles = new ArrayList<>();
                int articlesToProcessForSlider = Math.min(10, articles.size());
                Log.d("HomeFragmentDebug", "Processing first " + articlesToProcessForSlider + " articles for slider.");

                for (int i = 0; i < articlesToProcessForSlider; i++) {
                    Article article = articles.get(i);
                    if (article.getImageUrl() != null && !article.getImageUrl().isEmpty() &&
                            article.getLink() != null && !article.getLink().isEmpty()) {
                        if (sliderArticles.size() < 5) {
                            sliderArticles.add(article);
                            Log.d("HomeFragmentDebug", "Added article '" + article.getTitle() + "' to sliderArticles. Current slider count: " + sliderArticles.size());
                        } else {
                            Log.d("HomeFragmentDebug", "Found 5 valid slider articles, stopping search for slider images.");
                            break;
                        }
                    } else {
                        Log.d("HomeFragmentDebug", "Skipping article '" + article.getTitle() + "' for slider (missing image URL or link).");
                    }
                }

                if (!sliderArticles.isEmpty()) {
                    sliderAdapter.updateData(sliderArticles);
                    viewPagerSlider.setVisibility(View.VISIBLE);
                    sliderHandler.removeCallbacks(sliderRunnable);
                    sliderHandler.postDelayed(sliderRunnable, 3000);
                    Log.d("HomeFragmentDebug", "Slider updated with " + sliderArticles.size() + " articles.");
                } else {
                    viewPagerSlider.setVisibility(View.GONE);
                    sliderHandler.removeCallbacks(sliderRunnable);
                    Log.d("HomeFragmentDebug", "No valid articles for slider. Hiding slider.");
                }

                if (!isLoadingMore) {
                    newsAdapter.setArticles(articles);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    newsAdapter.addArticles(articles);
                }
                tvError.setVisibility(View.GONE);
                Log.d("HomeFragment", "Articles loaded successfully into adapter. Total articles: " + newsAdapter.getItemCount());
                isLastPage = false;
            } else {
                if (newsAdapter.getItemCount() == 0 && !isLoadingMore) {
                    newsAdapter.setArticles(null);
                    recyclerView.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Tidak ada berita utama ditemukan.");
                    Log.w("HomeFragment", "No articles returned from API, or articles list is empty.");
                } else if (isLoadingMore) {
                    isLastPage = true;
                    Log.d("HomeFragment", "No more articles to load (reached last page).");
                    Toast.makeText(getContext(), "Semua berita utama telah dimuat.", Toast.LENGTH_SHORT).show();
                }
                viewPagerSlider.setVisibility(View.GONE);
                sliderHandler.removeCallbacks(sliderRunnable);
            }
            isLoadingMore = false;
        });

        // --- Observer untuk Pesan Error ---
        homeViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            lottieLoadingView.setVisibility(View.GONE);
            lottieLoadingView.cancelAnimation();
            swipeRefreshLayout.setRefreshing(false);
            isLoadingMore = false;

            if (message != null && !message.isEmpty()) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(message);
                Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
                Log.e("HomeFragment", "Displaying error in UI: " + message);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        // --- Observer untuk Status Loading ---
        homeViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                if (!isLoadingMore) {
                    lottieLoadingView.setVisibility(View.VISIBLE);
                    lottieLoadingView.playAnimation();
                    loadingStartedAt = System.currentTimeMillis();
                }
                tvError.setVisibility(View.GONE);
            } else {
                // Lottie akan disembunyikan di observer getLatestNews/getErrorMessage secara langsung tanpa delay
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

                    if (!isLoadingMore && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= newsAdapter.getItemCount()
                                && totalItemCount > 0) {
                            isLoadingMore = true;
                            homeViewModel.loadMoreNews();
                        }
                    }
                }
            }
        });

        // --- Implementasi Pull-to-Refresh ---
        swipeRefreshLayout.setOnRefreshListener(() -> {
            homeViewModel.resetPagination();
            isLastPage = false;
            isLoadingMore = false;
            loadHomeHeadlines(false);
        });

        // Panggil untuk memuat berita pertama kali
        loadHomeHeadlines(false);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sliderAdapter != null && sliderAdapter.getItemCount() > 0) {
            sliderHandler.postDelayed(sliderRunnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    // Metode bantuan untuk memuat berita
    private void loadHomeHeadlines(boolean isLoadMore) {
        if (!isLoadMore) {
            tvError.setVisibility(View.GONE);
            lottieLoadingView.setVisibility(View.VISIBLE);
            lottieLoadingView.playAnimation();
            loadingStartedAt = System.currentTimeMillis();
        }
        homeViewModel.fetchLatestNews(isLoadMore);
    }

    // Implementasi OnItemClickListener dari NewsAdapter (untuk daftar berita RecyclerView)
    @Override
    public void onItemClick(Article article) {
        if (article != null && article.getLink() != null && !article.getLink().isEmpty()) {
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, article.getLink());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Link berita tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    // Implementasi OnSliderItemClickListener dari SliderAdapter (untuk slider ViewPager2)
    @Override
    public void onSliderItemClick(Article article) {
        if (article != null && article.getLink() != null && !article.getLink().isEmpty()) {
            Intent intent = new Intent(getContext(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, article.getLink());
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Link berita tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }
}