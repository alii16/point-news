package com.example.gonews.ui.tech;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Add this import

import com.airbnb.lottie.LottieAnimationView;
import com.example.gonews.R;
import com.example.gonews.ui.NewsAdapter;
import com.example.gonews.viewmodel.TechViewModel;
import com.example.gonews.model.Article; // Ensure this still points to your modified Article model
import com.example.gonews.MainActivity;

import java.util.List; // Add this import

public class TechFragment extends Fragment {

    private TechViewModel techViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError;
    private ImageButton btnOpenSideNav;
    private SwipeRefreshLayout swipeRefreshLayout; // Add this for pull-to-refresh

    private static final long MIN_LOADING_TIME_MS = 1700;
    private long loadingStartedAt;

    private boolean isLastPage = false; // Indicates if we've reached the last page of data
    private boolean isLoadingMore = false; // Indicates if more data is currently being loaded

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tech, container, false);

        recyclerView = root.findViewById(R.id.rv_tech_news);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_tech);
        tvError = root.findViewById(R.id.tv_error_tech);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout_tech); // Initialize

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());
        }

        techViewModel = new ViewModelProvider(this).get(TechViewModel.class);

        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // --- Observer for News Data (techNews) ---
        techViewModel.getTechNews().observe(getViewLifecycleOwner(), articles -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Hide Lottie loading after data is ready
                if (!isLoadingMore) { // Only hide Lottie if it's not a "load more" (for initial/refresh)
                    lottieLoadingView.setVisibility(View.GONE);
                    lottieLoadingView.cancelAnimation();
                }
                swipeRefreshLayout.setRefreshing(false); // Hide refresh indicator

                if (articles != null && !articles.isEmpty()) {
                    // If this is the first call or a refresh
                    if (!isLoadingMore) {
                        newsAdapter.setArticles(articles); // Reset and set new articles
                        recyclerView.setVisibility(View.VISIBLE);
                    } else { // If this is a "load more"
                        newsAdapter.addArticles(articles); // Add articles to the existing list
                    }
                    tvError.setVisibility(View.GONE);
                    Log.d("TechFragment", "Articles loaded successfully into adapter. Total articles: " + newsAdapter.getItemCount());
                    isLastPage = false; // There's data, so not the last page yet
                } else {
                    // If no articles are returned (empty response)
                    // Or if this is a "load more" and there are no more pages
                    if (newsAdapter.getItemCount() == 0 && !isLoadingMore) { // Only show error if no data at all
                        newsAdapter.setArticles(null);
                        recyclerView.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Tidak ada berita teknologi ditemukan.");
                        Log.w("TechFragment", "No articles returned from API, or articles list is empty.");
                    } else if (isLoadingMore) { // If it's a "load more" and no more data
                        isLastPage = true; // Indicates we've reached the last page
                        Log.d("TechFragment", "No more articles to load (reached last page).");
                        Toast.makeText(getContext(), "Semua berita teknologi telah dimuat.", Toast.LENGTH_SHORT).show();
                    }
                }
                isLoadingMore = false; // Reset loading more status
            }, delayMillis);
        });

        // --- Observer for Error Messages ---
        techViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();
                swipeRefreshLayout.setRefreshing(false); // Hide refresh indicator
                isLoadingMore = false; // Reset loading more status

                if (message != null && !message.isEmpty()) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(message);
                    recyclerView.setVisibility(View.GONE); // Hide RecyclerView on error
                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_LONG).show();
                    Log.e("TechFragment", "Displaying error in UI: " + message);
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }, delayMillis);
        });

        // --- Observer for Loading Status ---
        techViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                if (!isLoadingMore) { // Show Lottie only for initial load/refresh, not load more
                    lottieLoadingView.setVisibility(View.VISIBLE);
                    lottieLoadingView.playAnimation();
                    loadingStartedAt = System.currentTimeMillis();
                }
                tvError.setVisibility(View.GONE); // Hide error while loading
            } else {
                // Lottie will be hidden in the getTechNews/getErrorMessage observers with a delay
            }
        });


        // --- Implement Infinite Scrolling ---
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (linearLayoutManager != null) {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!isLoadingMore && !isLastPage) { // Only call if not currently loading and not on the last page
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= newsAdapter.getItemCount()) { // Additional check: Ensure adapter is not empty
                            isLoadingMore = true; // Set loading more status
                            techViewModel.loadMoreTechNews(); // Call the load more method
                            // You could display a small progress bar in the RecyclerView footer here
                            // newsAdapter.addLoadingFooter(); // Example, if your NewsAdapter supports it
                        }
                    }
                }
            }
        });

        // --- Implement Pull-to-Refresh ---
        swipeRefreshLayout.setOnRefreshListener(() -> {
            techViewModel.resetPagination(); // Reset pagination on refresh
            isLastPage = false; // Reset last page status
            isLoadingMore = false; // Reset loading more status
            loadTechHeadlines(false); // Reload headlines (not a "load more" call)
        });


        // Call to load headlines for the first time
        loadTechHeadlines(false); // FALSE means this is not a "load more"

        return root;
    }

    private void loadTechHeadlines(boolean isLoadMore) {
        if (!isLoadMore) { // If it's not a "load more", show the Lottie loading
            recyclerView.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            lottieLoadingView.setVisibility(View.VISIBLE);
            lottieLoadingView.playAnimation();
            loadingStartedAt = System.currentTimeMillis();
        }
        techViewModel.fetchTechNews(isLoadMore); // Call the updated ViewModel method
    }
}