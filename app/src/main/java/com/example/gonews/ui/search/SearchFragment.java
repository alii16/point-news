// app/src/main/java/com/example/gonews/ui/search/SearchFragment.java
package com.example.gonews.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.example.gonews.MainActivity;
import com.example.gonews.R;
import com.example.gonews.ui.NewsAdapter;
import com.example.gonews.viewmodel.SearchViewModel;
import com.google.android.material.chip.Chip; // Import Chip
import com.google.android.material.chip.ChipGroup; // Import ChipGroup

import java.util.List;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError;
    private EditText etSearchQuery;
    private ImageButton btnSearch;
    private ImageButton btnOpenSideNav;
    private SwipeRefreshLayout swipeRefreshLayout;

    // BARU: Variabel untuk ChipGroup dan Chips
    private ChipGroup chipGroupTags;
    private Chip chipTeknologi, chipKesehatan, chipOlahraga, chipPolitik, chipEkonomi, chipHiburan;


    private static final long MIN_LOADING_TIME_MS = 1000;
    private long loadingStartedAt;

    private boolean isLastPage = false;
    private boolean isLoadingMore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        // Inisialisasi View (yang sudah ada)
        recyclerView = root.findViewById(R.id.rv_search_results);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_search);
        tvError = root.findViewById(R.id.tv_error_search);
        etSearchQuery = root.findViewById(R.id.et_search_query);
        btnSearch = root.findViewById(R.id.btn_search);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout_search);

        // BARU: Inisialisasi ChipGroup dan Chips
        chipGroupTags = root.findViewById(R.id.chip_group_tags);
        chipTeknologi = root.findViewById(R.id.chip_teknologi);
        chipKesehatan = root.findViewById(R.id.chip_kesehatan);
        chipOlahraga = root.findViewById(R.id.chip_olahraga);
        chipPolitik = root.findViewById(R.id.chip_politik);
        chipEkonomi = root.findViewById(R.id.chip_ekonomi);
        chipHiburan = root.findViewById(R.id.chip_hiburan);


        // Setup RecyclerView (tetap sama)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());
        }

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Listeners (yang sudah ada)
        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnSearch.setOnClickListener(v -> performSearch());

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // BARU: Set Listener untuk setiap Chip
        Chip.OnClickListener chipClickListener = chipView -> {
            Chip chip = (Chip) chipView;
            String tag = chip.getText().toString();
            etSearchQuery.setText(tag); // Isi EditText dengan tagar
            performSearch(); // Lakukan pencarian
        };

        chipTeknologi.setOnClickListener(chipClickListener);
        chipKesehatan.setOnClickListener(chipClickListener);
        chipOlahraga.setOnClickListener(chipClickListener);
        chipPolitik.setOnClickListener(chipClickListener);
        chipEkonomi.setOnClickListener(chipClickListener);
        chipHiburan.setOnClickListener(chipClickListener);
        // Tambahkan listener untuk chip lain jika ada

        // Observers LiveData (tetap sama)
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), articles -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isLoadingMore) {
                    lottieLoadingView.setVisibility(View.GONE);
                    lottieLoadingView.cancelAnimation();
                }
                swipeRefreshLayout.setRefreshing(false);

                if (articles != null && !articles.isEmpty()) {
                    if (!isLoadingMore) {
                        newsAdapter.setArticles(articles);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        newsAdapter.addArticles(articles);
                    }
                    tvError.setVisibility(View.GONE);
                    isLastPage = false;
                    Log.d("SearchFragment", "Hasil pencarian dimuat. Total artikel: " + newsAdapter.getItemCount());
                } else {
                    if (!isLoadingMore) {
                        newsAdapter.setArticles(null);
                        recyclerView.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Tidak ada berita ditemukan untuk pencarian ini.");
                    } else {
                        isLastPage = true;
                        Toast.makeText(getContext(), "Semua hasil pencarian telah dimuat.", Toast.LENGTH_SHORT).show();
                    }
                }
                isLoadingMore = false;
            }, delayMillis);
        });

        searchViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();
                swipeRefreshLayout.setRefreshing(false);
                isLoadingMore = false;

                if (message != null && !message.isEmpty()) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(message);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error pencarian: " + message, Toast.LENGTH_LONG).show();
                    Log.e("SearchFragment", "Search error: " + message);
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }, delayMillis);
        });

        searchViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (loading) {
                if (!isLoadingMore) {
                    lottieLoadingView.setVisibility(View.VISIBLE);
                    lottieLoadingView.playAnimation();
                    loadingStartedAt = System.currentTimeMillis();
                }
                tvError.setVisibility(View.GONE);
            }
        });

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
                                && totalItemCount > 0) {
                            isLoadingMore = true;
                            searchViewModel.loadMoreSearchResults();
                            Log.d("SearchFragment", "Loading more search results...");
                        }
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (etSearchQuery.getText().toString().trim().isEmpty()) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Masukkan kata kunci untuk refresh pencarian.", Toast.LENGTH_SHORT).show();
                return;
            }
            searchViewModel.resetSearch();
            isLastPage = false;
            isLoadingMore = false;
            performSearch();
        });

        return root;
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Masukkan kata kunci pencarian.", Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
        }

        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        lottieLoadingView.setVisibility(View.VISIBLE);
        lottieLoadingView.playAnimation();
        loadingStartedAt = System.currentTimeMillis();

        searchViewModel.searchNews(query);
    }
}