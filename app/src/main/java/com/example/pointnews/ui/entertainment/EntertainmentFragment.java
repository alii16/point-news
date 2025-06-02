package com.example.pointnews.ui.entertainment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.airbnb.lottie.LottieAnimationView;
import com.example.pointnews.R;
import com.example.pointnews.ui.NewsAdapter;
import com.example.pointnews.viewmodel.EntertainmentViewModel;
import com.example.pointnews.MainActivity;

public class EntertainmentFragment extends Fragment {

    private EntertainmentViewModel entertainmentViewModel;
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieLoadingView;
    private TextView tvError;
    private ImageButton btnOpenSideNav;

    private static final long MIN_LOADING_TIME_MS = 1700;
    private long loadingStartedAt;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_entertainment, container, false);

        recyclerView = root.findViewById(R.id.rv_entertainment_news);
        lottieLoadingView = root.findViewById(R.id.lottie_loading_entertainment);
        tvError = root.findViewById(R.id.tv_error_entertainment);
        btnOpenSideNav = root.findViewById(R.id.btn_open_side_nav);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newsAdapter = new NewsAdapter();
        recyclerView.setAdapter(newsAdapter);

        // <--- TAMBAHKAN KODE INI UNTUK MENGATUR ONCLICK LISTENER -->
        // Mengatur MainActivity sebagai listener untuk klik item berita
        if (getActivity() instanceof NewsAdapter.OnItemClickListener) {
            newsAdapter.setOnItemClickListener((NewsAdapter.OnItemClickListener) getActivity());
        }
        // <--- AKHIR KODE BARU -->

        entertainmentViewModel = new ViewModelProvider(this).get(EntertainmentViewModel.class);

        btnOpenSideNav.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openDrawer();
            }
        });

        entertainmentViewModel.getEntertainmentNews().observe(getViewLifecycleOwner(), articles -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();

                if (articles != null && !articles.isEmpty()) {
                    newsAdapter.setArticles(articles);
                    recyclerView.setVisibility(View.VISIBLE);
                    tvError.setVisibility(View.GONE);
                } else {
                    newsAdapter.setArticles(null);
                    recyclerView.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Tidak ada berita hiburan ditemukan.");
                }
            }, delayMillis);
        });

        entertainmentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            long timeElapsed = System.currentTimeMillis() - loadingStartedAt;
            long delayMillis = Math.max(0, MIN_LOADING_TIME_MS - timeElapsed);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lottieLoadingView.setVisibility(View.GONE);
                lottieLoadingView.cancelAnimation();

                if (message != null && !message.isEmpty()) {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText(message);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                } else {
                    tvError.setVisibility(View.GONE);
                }
            }, delayMillis);
        });

        loadEntertainmentHeadlines();

        return root;
    }

    private void loadEntertainmentHeadlines() {
        recyclerView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);
        lottieLoadingView.setVisibility(View.VISIBLE);
        lottieLoadingView.playAnimation();
        loadingStartedAt = System.currentTimeMillis();
        entertainmentViewModel.fetchEntertainmentNews();
    }
}