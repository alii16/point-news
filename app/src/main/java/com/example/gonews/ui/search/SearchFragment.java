// app/src/main/java/com/example/gonews/ui/search/SearchFragment.java
package com.example.gonews.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout; // Import ini jika belum ada
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    // Tambahkan variabel untuk layout rekomendasi
    private LinearLayout layoutRecommendedCategories; //

    private ChipGroup chipGroupTags;
    private Chip chipTeknologi, chipKesehatan, chipOlahraga, chipPolitik, chipEkonomi, chipHiburan;

    // Variabel untuk Riwayat Pencarian
    private LinearLayout layoutRecentSearches;
    private LinearLayout llRecentSearchChipsContainer;
    private Button btnClearRecentSearches;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "RecentSearchesPrefs";
    private static final String KEY_SEARCH_HISTORY = "searchHistory";
    private static final int MAX_RECENT_SEARCHES = 5;

    private static final long MIN_LOADING_TIME_MS = 1000;
    private long loadingStartedAt;

    private boolean isLastPage = false;
    private boolean isLoadingMore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        // Inisialisasi View
        recyclerView = root.findViewById(R.id.rv_search_results);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_search);
        tvError = root.findViewById(R.id.tv_error_search);
        etSearchQuery = root.findViewById(R.id.et_search_query);
        btnSearch = root.findViewById(R.id.btn_search);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_layout_search);

        // Inisialisasi layoutRecommendedCategories
        layoutRecommendedCategories = root.findViewById(R.id.layout_recommended_categories); //

        chipGroupTags = root.findViewById(R.id.chip_group_tags);
        chipTeknologi = root.findViewById(R.id.chip_teknologi);
        chipKesehatan = root.findViewById(R.id.chip_kesehatan);
        chipOlahraga = root.findViewById(R.id.chip_olahraga);
        chipPolitik = root.findViewById(R.id.chip_politik);
        chipEkonomi = root.findViewById(R.id.chip_ekonomi);
        chipHiburan = root.findViewById(R.id.chip_hiburan);

        // Inisialisasi View untuk Riwayat Pencarian
        layoutRecentSearches = root.findViewById(R.id.layout_recent_searches);
        llRecentSearchChipsContainer = root.findViewById(R.id.ll_recent_search_chips_container);
        btnClearRecentSearches = root.findViewById(R.id.btn_clear_recent_searches);

        // Inisialisasi SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());
        }

        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        // Listeners
        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        // Panggil performSearch() tanpa argumen, akan membaca dari etSearchQuery
        btnSearch.setOnClickListener(v -> performSearch());

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(); // Panggil performSearch() tanpa argumen
                return true;
            }
            return false;
        });

        // Set Listener untuk setiap Chip Kategori
        Chip.OnClickListener chipClickListener = chipView -> {
            Chip chip = (Chip) chipView;
            String tag = chip.getText().toString();
            etSearchQuery.setText(tag); // Isi EditText dengan tagar
            // Panggil performSearch(String query) dengan tagar sebagai argumen
            performSearch(tag);
        };

        chipTeknologi.setOnClickListener(chipClickListener);
        chipKesehatan.setOnClickListener(chipClickListener);
        chipOlahraga.setOnClickListener(chipClickListener);
        chipPolitik.setOnClickListener(chipClickListener);
        chipEkonomi.setOnClickListener(chipClickListener);
        chipHiburan.setOnClickListener(chipClickListener);

        // Setup Listener untuk Tombol Hapus Riwayat
        btnClearRecentSearches.setOnClickListener(v -> {
            clearRecentSearches();
            // Setelah menghapus, tampilkan kembali riwayat yang sudah kosong
            showRecentSearchesAndCategories();
        });

        // Listener untuk perubahan teks di EditText
        etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) { // Jika input kosong, tampilkan riwayat dan chip kategori
                    showRecentSearchesAndCategories();
                    newsAdapter.setArticles(new ArrayList<>()); // Kosongkan hasil pencarian
                    recyclerView.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Observers LiveData
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
                    // Sembunyikan riwayat dan chip kategori saat hasil pencarian tampil
                    hideRecentSearchesAndCategories();
                } else {
                    if (!isLoadingMore) {
                        newsAdapter.setArticles(new ArrayList<>()); // Kosongkan adapter
                        recyclerView.setVisibility(View.GONE);
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("Tidak ada berita ditemukan untuk pencarian ini.");
                        // Jika tidak ada hasil, biarkan riwayat dan chip kategori tetap tersembunyi
                        // User mungkin ingin coba kata kunci lain.
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
                    // Jika ada error, tampilkan kembali riwayat dan chip kategori
                    showRecentSearchesAndCategories();
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
                // Sembunyikan riwayat dan chip kategori saat loading dimulai
                hideRecentSearchesAndCategories();
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
            String currentQuery = etSearchQuery.getText().toString().trim();
            if (currentQuery.isEmpty()) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Masukkan kata kunci untuk refresh pencarian.", Toast.LENGTH_SHORT).show();
                // Tampilkan riwayat dan chip kategori jika tidak ada query saat refresh
                showRecentSearchesAndCategories();
                return;
            }
            searchViewModel.resetSearch();
            isLastPage = false;
            isLoadingMore = false;
            performSearch(currentQuery); // Re-perform search for the current query
        });

        // Tampilkan riwayat dan chip kategori saat fragment pertama kali dimuat
        showRecentSearchesAndCategories();

        return root;
    }

    // Metode performSearch() yang akan dipanggil dari tombol search atau EditText
    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        performSearch(query); // Panggil metode overloaded dengan query
    }

    // Metode performSearch(String query) yang akan dipanggil dari chip atau dari performSearch() di atas
    private void performSearch(String query) {
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Masukkan kata kunci pencarian.", Toast.LENGTH_SHORT).show();
            // Jika query kosong, pastikan riwayat dan chip kategori tampil
            showRecentSearchesAndCategories();
            return;
        }

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
        }

        // Sembunyikan riwayat dan chip kategori saat pencarian dimulai
        hideRecentSearchesAndCategories();

        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        lottieLoadingView.setVisibility(View.VISIBLE);
        lottieLoadingView.playAnimation();
        loadingStartedAt = System.currentTimeMillis();

        searchViewModel.searchNews(query);
        saveSearchQuery(query); // Simpan query ke riwayat setelah pencarian
    }

    // --- LOGIKA RIWAYAT PENCARIAN ---

    private List<String> getRecentSearches() {
        // Menggunakan LinkedHashSet untuk menjaga urutan penambahan dan keunikan
        Set<String> historySet = sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new LinkedHashSet<>());
        // Konversi ke List untuk membalik urutan agar yang terbaru di depan
        List<String> historyList = new ArrayList<>(historySet);
        Collections.reverse(historyList);
        return historyList;
    }

    private void saveSearchQuery(String query) {
        // Menggunakan LinkedHashSet untuk mempertahankan urutan penambahan dan keunikan
        Set<String> historySet = new LinkedHashSet<>(sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new LinkedHashSet<>()));

        // Hapus query lama jika sudah ada (untuk memindahkannya ke paling depan/terbaru)
        historySet.remove(query);
        // Tambahkan query baru ke LinkedHashSet, akan berada di paling akhir (yang terbaru)
        historySet.add(query);

        // Batasi jumlah riwayat dari depan (yang paling lama)
        // Kita perlu cara untuk menghapus dari 'awal' LinkedHashSet
        // Konversi ke List, batasi, lalu konversi kembali ke Set
        List<String> tempHistoryList = new ArrayList<>(historySet);
        while (tempHistoryList.size() > MAX_RECENT_SEARCHES) {
            tempHistoryList.remove(0); // Hapus elemen pertama (yang paling lama)
        }
        // Simpan kembali LinkedHashSet yang sudah dibatasi
        sharedPreferences.edit().putStringSet(KEY_SEARCH_HISTORY, new LinkedHashSet<>(tempHistoryList)).apply();
    }

    private void displayRecentSearchChips(List<String> searches) {
        llRecentSearchChipsContainer.removeAllViews();

        if (searches.isEmpty()) {
            layoutRecentSearches.setVisibility(View.GONE);
            return;
        } else {
            layoutRecentSearches.setVisibility(View.VISIBLE);
        }

        for (String query : searches) {
            Chip chip = new Chip(getContext());
            chip.setText(query);
            chip.setCheckable(false);
            chip.setOnClickListener(v -> {
                etSearchQuery.setText(query);
                etSearchQuery.setSelection(query.length());
                performSearch(query);
            });

            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                removeSearchQuery(query);
                showRecentSearchesAndCategories();
            });

            // --- MENGATUR BACKGROUND, STROKE, DAN CORNER RADIUS ---
            chip.setChipBackgroundColorResource(R.color.chip_background_color); // Mengatur warna latar belakang
            chip.setChipStrokeColorResource(R.color.chip_stroke_color);     // Mengatur warna stroke
            chip.setChipStrokeWidthResource(R.dimen.chip_stroke_width);       // Mengatur lebar stroke
            chip.setChipCornerRadiusResource(R.dimen.chip_corner_radius);     // Mengatur radius sudut

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 0); // Jarak antar chip
            chip.setLayoutParams(params);

            llRecentSearchChipsContainer.addView(chip);
        }
    }
    private void removeSearchQuery(String query) {
        Set<String> historySet = new LinkedHashSet<>(sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new LinkedHashSet<>()));
        historySet.remove(query);
        sharedPreferences.edit().putStringSet(KEY_SEARCH_HISTORY, historySet).apply();
    }

    private void clearRecentSearches() {
        sharedPreferences.edit().remove(KEY_SEARCH_HISTORY).apply();
        Toast.makeText(getContext(), "Riwayat pencarian dihapus", Toast.LENGTH_SHORT).show();
        displayRecentSearchChips(new ArrayList<>()); // Kosongkan tampilan
        // Jika tidak ada riwayat, displayRecentSearchChips akan menyembunyikan layoutRecentSearches
    }

    // Metode untuk menampilkan riwayat dan chip kategori
    private void showRecentSearchesAndCategories() {
        displayRecentSearchChips(getRecentSearches()); // Tampilkan chip riwayat
        // Periksa apakah ada riwayat untuk menampilkan layoutRecentSearches, ini sudah ditangani di displayRecentSearchChips
        layoutRecommendedCategories.setVisibility(View.VISIBLE); // Tampilkan seluruh container rekomendasi
    }

    // Metode untuk menyembunyikan riwayat dan chip kategori
    private void hideRecentSearchesAndCategories() {
        layoutRecentSearches.setVisibility(View.GONE); // Sembunyikan riwayat pencarian
        layoutRecommendedCategories.setVisibility(View.GONE); // Sembunyikan seluruh container rekomendasi
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tampilkan riwayat dan chip kategori jika input pencarian kosong saat kembali ke fragment
        if (etSearchQuery.getText().toString().isEmpty()) {
            showRecentSearchesAndCategories();
        } else {
            // Jika ada teks di input, berarti user sudah dalam proses mencari/melihat hasil
            hideRecentSearchesAndCategories();
        }
    }
}