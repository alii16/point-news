package com.example.pointnews.ui.search;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.pointnews.MainActivity;
import com.example.pointnews.R;
import com.example.pointnews.ui.NewsAdapter;
import com.example.pointnews.model.Article;
// Hapus import WebViewActivity jika ada
// import com.example.gonews.ui.WebViewActivity;
import com.example.pointnews.viewmodel.SearchViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class SearchFragment extends Fragment {
    private SearchViewModel searchViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private TextInputEditText etSearchQuery;
    private MaterialButton btnSearch;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError, tvNoResults;
    private ImageButton btnOpenSideNav;
    private LinearLayout recommendationsSection;
    private ChipGroup chipGroupRecommendations;
    private static final long MIN_LOADING_TIME_MS = 1700;
    private long loadingStartedAt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

// Inisialisasi View
        etSearchQuery = root.findViewById(R.id.et_search_query);
        btnSearch = root.findViewById(R.id.btn_search);
        recyclerView = root.findViewById(R.id.rv_search_results);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_search);
        tvError = root.findViewById(R.id.tv_error_search);
        tvNoResults = root.findViewById(R.id.tv_no_results);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);
        recommendationsSection = root.findViewById(R.id.recommendations_section);
        chipGroupRecommendations = root.findViewById(R.id.chip_group_recommendations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);
// <--- KOREKSI PENTING DI SINI: SET LISTENER KE ACTIVITY -->
// Pastikan getActivity() adalah instance dari NewsAdapter.OnItemClickListener (yaitu MainActivity)
        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());

        }


        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        btnSearch.setOnClickListener(v -> performSearch());

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        etSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override

            public void afterTextChanged(android.text.Editable s) {
                if (s.toString().trim().isEmpty() && newsAdapter.getItemCount() == 0) {
                    showRecommendations();
                }
            }
        });

        for (int i = 0; i < chipGroupRecommendations.getChildCount(); i++) {
            View child = chipGroupRecommendations.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                chip.setOnClickListener(v -> {
                    String tag = chip.getText().toString()
                            .replace("#", "")
                            .replaceAll("(?<=[a-z])(?=[A-Z])", " ")
                            .trim() + " ";
                    etSearchQuery.setText(tag);
                    etSearchQuery.setSelection(etSearchQuery.getText().length());
                    performSearch();
                });
            }
        }

        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), articles -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();

                if (articles != null && !articles.isEmpty()) {
                    newsAdapter.setArticles(articles);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.GONE);
                    recommendationsSection.setVisibility(View.GONE);
                } else {
                    newsAdapter.setArticles(null);
                    recyclerView.setVisibility(View.GONE);
                    tvError.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.VISIBLE);
                    recommendationsSection.setVisibility(View.GONE);
                }
            }, delayMillis);
        });

        searchViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message ->{
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();

                if (message != null && !message.isEmpty()) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(message);
                    recyclerView.setVisibility(View.GONE);
                    tvNoResults.setVisibility(View.GONE);
                    recommendationsSection.setVisibility(View.GONE);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }, delayMillis);
        });
        showRecommendations();
        return root;
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Silakan masukkan kata kunci pencarian.", Toast.LENGTH_SHORT).show();
            hideKeyboard();
            showRecommendations();
            newsAdapter.setArticles(null);
            recyclerView.setVisibility(View.GONE);
            tvError.setVisibility(View.GONE);
            tvNoResults.setVisibility(View.GONE);
            return;
        }
        hideKeyboard();
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
        recommendationsSection.setVisibility(View.GONE);
        lottieLoadingView.setVisibility(View.VISIBLE);
        lottieLoadingView.playAnimation();
        loadingStartedAt = System.currentTimeMillis();
        searchViewModel.searchNews(query);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etSearchQuery.getWindowToken(), 0);
        }
    }

    private void showRecommendations() {
        recommendationsSection.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        tvNoResults.setVisibility(View.GONE);
    }
}